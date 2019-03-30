package am.ik.pks.client;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Component
public class PksApiClient {

    public Flux<PksCluster> getClusters() {
        return Flux.just(new PksClusterBuilder()
            .setUuid("d2adf0c1-9401-44eb-b479-8c42abc05c6b")
            .setName("demo1")
            .setLastAction("SUCCEEDED")
            .setKubernetesMasterIps(Arrays.asList("54.64.203.141", "13.113.212.19"))
            .setParameters(new PksCluster.Parameters() {

                {
                    setKubernetesMasterHost("demo1.pks.example.com");
                    setKubernetesMasterPort(443);
                }
            })
            .createPksCluster());
    }

}
