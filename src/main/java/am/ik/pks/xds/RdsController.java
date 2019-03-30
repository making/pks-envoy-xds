package am.ik.pks.xds;

import am.ik.pks.client.PksApiClient;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.api.v2.RouteConfiguration;
import io.envoyproxy.envoy.api.v2.route.Route;
import io.envoyproxy.envoy.api.v2.route.RouteAction;
import io.envoyproxy.envoy.api.v2.route.RouteMatch;
import io.envoyproxy.envoy.api.v2.route.VirtualHost;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class RdsController {

    private final PksApiClient pksApiClient;

    private final JsonFormat.Printer printer = JsonFormat.printer()
        .usingTypeRegistry(JsonFormat.TypeRegistry.newBuilder()
            .add(RouteConfiguration.getDescriptor())
            .build());

    public RdsController(PksApiClient pksApiClient) {
        this.pksApiClient = pksApiClient;
    }

    @PostMapping(path = "/v2/discovery:routes")
    public Mono<String> discoveryRoutes(@RequestBody String input) {
        return this.pksApiClient.getClusters()
            .map(pksCluster -> VirtualHost.newBuilder()
                .setName(pksCluster.getName())
                .addDomains(pksCluster.getParameters().getKubernetesMasterHost())
                .addRoutes(Route.newBuilder()
                    .setMatch(RouteMatch.newBuilder()
                        .setPrefix("/")
                        .build())
                    .setRoute(RouteAction.newBuilder()
                        .setCluster(pksCluster.getName())
                        .setHostRewrite(pksCluster.getParameters().getKubernetesMasterHost())
                        .build())
                    .build())
                .build())
            .collectList()
            .map(virtualHosts -> DiscoveryResponse.newBuilder()
                .setTypeUrl("")
                .addResources(Any.pack(RouteConfiguration.newBuilder()
                    .setName("pks-route")
                    .addAllVirtualHosts(virtualHosts)
                    .build())))
            .flatMap(r -> {
                try {
                    return Mono.just(this.printer.print(r));
                } catch (InvalidProtocolBufferException e) {
                    return Mono.error(e);
                }
            }).log("routes");
    }
}
