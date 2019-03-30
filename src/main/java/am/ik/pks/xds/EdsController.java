package am.ik.pks.xds;

import am.ik.pks.client.PksApiClient;
import am.ik.pks.client.PksCluster;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.envoyproxy.envoy.api.v2.ClusterLoadAssignment;
import io.envoyproxy.envoy.api.v2.DiscoveryRequest;
import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.api.v2.core.Address;
import io.envoyproxy.envoy.api.v2.core.SocketAddress;
import io.envoyproxy.envoy.api.v2.endpoint.Endpoint;
import io.envoyproxy.envoy.api.v2.endpoint.LbEndpoint;
import io.envoyproxy.envoy.api.v2.endpoint.LocalityLbEndpoints;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class EdsController {

    private final PksApiClient pksApiClient;

    private final JsonFormat.Printer printer = JsonFormat.printer()
        .usingTypeRegistry(JsonFormat.TypeRegistry.newBuilder()
            .add(ClusterLoadAssignment.getDescriptor())
            .build());

    public EdsController(PksApiClient pksApiClient) {
        this.pksApiClient = pksApiClient;
    }

    @PostMapping(path = "/v2/discovery:endpoints")
    public Mono<String> discoveryEndpoints(@RequestBody String input) throws InvalidProtocolBufferException {
        DiscoveryRequest.Builder requestBuilder = DiscoveryRequest.newBuilder();
        JsonFormat.parser().merge(input, requestBuilder);
        DiscoveryRequest request = requestBuilder.build();
        List<String> resourceNames = new ArrayList<>(request.getResourceNamesList());
        return this.pksApiClient.getClusters()
            .collectMap(PksCluster::getName, Function.identity())
            .map(map -> {
                DiscoveryResponse.Builder responseBuilder = DiscoveryResponse.newBuilder();
                resourceNames.forEach(resourceName -> {
                    PksCluster pksCluster = map.get(resourceName);
                    if (pksCluster != null) {
                        List<LbEndpoint> lbEndpoints = pksCluster.getKubernetesMasterIps().stream()
                            .map(address -> LbEndpoint.newBuilder()
                                .setEndpoint(Endpoint.newBuilder()
                                    .setAddress(Address.newBuilder()
                                        .setSocketAddress(SocketAddress.newBuilder()
                                            .setAddress(address)
                                            .setPortValue(443)
                                            .build())
                                        .build())
                                    .build())
                                .build())
                            .collect(Collectors.toList());
                        ClusterLoadAssignment clusterLoadAssignment = ClusterLoadAssignment.newBuilder()
                            .setClusterName(resourceName)
                            .addEndpoints(LocalityLbEndpoints.newBuilder()
                                .addAllLbEndpoints(lbEndpoints)
                                .build())
                            .build();
                        responseBuilder.setTypeUrl("")
                            .addResources(Any.pack(clusterLoadAssignment));
                    }
                });
                return responseBuilder;
            })
            .flatMap(r -> {
                try {
                    return Mono.just(this.printer.print(r));
                } catch (InvalidProtocolBufferException e) {
                    return Mono.error(e);
                }
            });
    }
}
