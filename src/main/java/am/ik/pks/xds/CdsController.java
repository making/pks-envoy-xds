package am.ik.pks.xds;

import am.ik.pks.client.PksApiClient;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.envoyproxy.envoy.api.v2.Cluster;
import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.api.v2.auth.UpstreamTlsContext;
import io.envoyproxy.envoy.api.v2.core.ApiConfigSource;
import io.envoyproxy.envoy.api.v2.core.ConfigSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class CdsController {

    private final PksApiClient pksApiClient;

    private final JsonFormat.Printer printer = JsonFormat.printer()
        .usingTypeRegistry(JsonFormat.TypeRegistry.newBuilder()
            .add(Cluster.getDescriptor())
            .build());

    public CdsController(PksApiClient pksApiClient) {
        this.pksApiClient = pksApiClient;
    }

    @PostMapping(path = "/v2/discovery:clusters")
    public Mono<String> discoveryClusters(@RequestBody String input) {
        ApiConfigSource apiConfigSource = ApiConfigSource.newBuilder()
            .setApiType(ApiConfigSource.ApiType.REST)
            .setRefreshDelay(Duration.newBuilder().setSeconds(10).build())
            .setRequestTimeout(Duration.newBuilder().setSeconds(3).build())
            .addClusterNames("pks-xds")
            .build();

        return this.pksApiClient.getClusters()
            .map(pksCluster -> Cluster.newBuilder()
                .setName(pksCluster.getName())
                .setConnectTimeout(Duration.newBuilder().setSeconds(1).build())
                .setDnsLookupFamily(Cluster.DnsLookupFamily.V4_ONLY)
                .setLbPolicy(Cluster.LbPolicy.ROUND_ROBIN)
                .setType(Cluster.DiscoveryType.EDS)
                .setTlsContext(UpstreamTlsContext.newBuilder().build())
                .setEdsClusterConfig(Cluster.EdsClusterConfig.newBuilder()
                    .setEdsConfig(ConfigSource.newBuilder()
                        .setApiConfigSource(apiConfigSource)
                        .build())
                    .build())
                .build())
            .map(Any::pack)
            .collectList()
            .map(anies -> DiscoveryResponse
                .newBuilder()
                .addAllResources(anies)
                .setTypeUrl(""))
            .flatMap(r -> {
                try {
                    return Mono.just(this.printer.print(r));
                } catch (InvalidProtocolBufferException e) {
                    return Mono.error(e);
                }
            });
    }
}
