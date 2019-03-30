package am.ik.pks.xds;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.api.v2.Listener;
import io.envoyproxy.envoy.api.v2.core.Address;
import io.envoyproxy.envoy.api.v2.core.ApiConfigSource;
import io.envoyproxy.envoy.api.v2.core.ConfigSource;
import io.envoyproxy.envoy.api.v2.core.SocketAddress;
import io.envoyproxy.envoy.api.v2.listener.Filter;
import io.envoyproxy.envoy.api.v2.listener.FilterChain;
import io.envoyproxy.envoy.config.filter.network.http_connection_manager.v2.HttpConnectionManager;
import io.envoyproxy.envoy.config.filter.network.http_connection_manager.v2.HttpFilter;
import io.envoyproxy.envoy.config.filter.network.http_connection_manager.v2.Rds;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LdsController {

    private final JsonFormat.Printer printer = JsonFormat.printer()
        .usingTypeRegistry(JsonFormat.TypeRegistry.newBuilder()
            .add(Listener.getDescriptor())
            .add(HttpConnectionManager.getDescriptor())
            .build());

    @PostMapping(path = "/v2/discovery:listeners")
    public String discoveryListeners(@RequestBody String input) throws InvalidProtocolBufferException {
        ApiConfigSource apiConfigSource = ApiConfigSource.newBuilder()
            .setApiType(ApiConfigSource.ApiType.REST)
            .setRefreshDelay(Duration.newBuilder().setSeconds(10).build())
            .setRequestTimeout(Duration.newBuilder().setSeconds(3).build())
            .addClusterNames("pks-xds")
            .build();

        HttpConnectionManager httpConnectionManager = HttpConnectionManager.newBuilder()
            .setStatPrefix("ingress_http")
            .setCodecType(HttpConnectionManager.CodecType.HTTP1)
            .setRds(Rds.newBuilder()
                .setRouteConfigName("pks-route")
                .setConfigSource(ConfigSource.newBuilder()
                    .setApiConfigSource(apiConfigSource)
                    .build())
                .build())
            .addHttpFilters(HttpFilter.newBuilder().setName("envoy.router").build())
            .build();

        Listener listener = Listener.newBuilder()
            .setName("pks")
            .setAddress(Address.newBuilder()
                .setSocketAddress(SocketAddress.newBuilder()
                    .setAddress("0.0.0.0")
                    .setPortValue(10000)
                    .build())
                .build())
            .addFilterChains(FilterChain.newBuilder()
                .addFilters(Filter.newBuilder()
                    .setName("envoy.http_connection_manager")
                    .setTypedConfig(Any.pack(httpConnectionManager))
                    .build()))
            .build();
        String response = this.printer.print(DiscoveryResponse
            .newBuilder()
            .setTypeUrl("")
            .addResources(Any.pack(listener)));
        System.out.println("listeners: " + response);
        return response;
    }
}
