package am.ik.pks;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pks.envoy")
public class AppProps {

    private String tlsPrivateKey = "/etc/envoy/cert.pem";

    private String tlsCertificate = "/etc/envoy/private.pem";

    public String getTlsPrivateKey() {
        return tlsPrivateKey;
    }

    public void setTlsPrivateKey(String tlsPrivateKey) {
        this.tlsPrivateKey = tlsPrivateKey;
    }

    public String getTlsCertificate() {
        return tlsCertificate;
    }

    public void setTlsCertificate(String tlsCertificate) {
        this.tlsCertificate = tlsCertificate;
    }
}
