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
                .setKubernetesMasterIps(Arrays.asList("54.64.203.141"))
                .setParameters(new PksCluster.Parameters() {

                    {
                        setKubernetesMasterHost("demo1.pks.bosh.tokyo");
                        setKubernetesMasterPort(443);
                    }
                })
                .createPksCluster(),
            new PksClusterBuilder()
                .setUuid("539c7ba6-9e25-494f-99ad-4094a33ab633")
                .setName("demo2")
                .setLastAction("SUCCEEDED")
                .setKubernetesMasterIps(Arrays.asList("13.113.212.19"))
                .setParameters(new PksCluster.Parameters() {

                    {
                        setKubernetesMasterHost("demo2.pks.bosh.tokyo");
                        setKubernetesMasterPort(443);
                    }
                })
                .createPksCluster());
    }

}
