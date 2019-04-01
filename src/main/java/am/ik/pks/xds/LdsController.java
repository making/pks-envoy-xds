package am.ik.pks.xds;

import am.ik.pks.AppProps;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.api.v2.Listener;
import io.envoyproxy.envoy.api.v2.auth.CommonTlsContext;
import io.envoyproxy.envoy.api.v2.auth.DownstreamTlsContext;
import io.envoyproxy.envoy.api.v2.auth.TlsCertificate;
import io.envoyproxy.envoy.api.v2.core.Address;
import io.envoyproxy.envoy.api.v2.core.ApiConfigSource;
import io.envoyproxy.envoy.api.v2.core.ConfigSource;
import io.envoyproxy.envoy.api.v2.core.DataSource;
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

    private final AppProps props;

    public LdsController(AppProps props) {
        this.props = props;
    }

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
            .setCodecType(HttpConnectionManager.CodecType.AUTO)
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
                    .setPortValue(this.props.getListenerPort())
                    .build())
                .build())
            .addFilterChains(FilterChain.newBuilder()
                .addFilters(Filter.newBuilder()
                    .setName("envoy.http_connection_manager")
                    .setTypedConfig(Any.pack(httpConnectionManager))
                    .build())
                .setTlsContext(DownstreamTlsContext.newBuilder()
                    .setCommonTlsContext(CommonTlsContext.newBuilder()
                        .addTlsCertificates(TlsCertificate.newBuilder()
                            .setCertificateChain(DataSource.newBuilder()
                                .setFilename(this.props.getTlsCertificate())
                                .build())
                            .setPrivateKey(DataSource.newBuilder()
                                .setFilename(this.props.getTlsPrivateKey())
                                .build())
                            .build())
                        .build())
                    .build())
            )
            .build();
        return this.printer.print(DiscoveryResponse
            .newBuilder()
            .setTypeUrl("")
            .addResources(Any.pack(listener)));
    }
}
