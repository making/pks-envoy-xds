package am.ik.pks.client;

import java.util.List;

public class PksClusterBuilder {

    private List<String> kubernetesMasterIps;

    private String lastAction;

    private String lastActionDescription;

    private String lastActionState;

    private String name;

    private PksCluster.Parameters parameters;

    private String planName;

    private String uuid;

    public PksCluster createPksCluster() {
        return new PksCluster(name, planName, lastAction, lastActionState, lastActionDescription, uuid, kubernetesMasterIps, parameters);
    }

    public PksClusterBuilder setKubernetesMasterIps(List<String> kubernetesMasterIps) {
        this.kubernetesMasterIps = kubernetesMasterIps;
        return this;
    }

    public PksClusterBuilder setLastAction(String lastAction) {
        this.lastAction = lastAction;
        return this;
    }

    public PksClusterBuilder setLastActionDescription(String lastActionDescription) {
        this.lastActionDescription = lastActionDescription;
        return this;
    }

    public PksClusterBuilder setLastActionState(String lastActionState) {
        this.lastActionState = lastActionState;
        return this;
    }

    public PksClusterBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public PksClusterBuilder setParameters(PksCluster.Parameters parameters) {
        this.parameters = parameters;
        return this;
    }

    public PksClusterBuilder setPlanName(String planName) {
        this.planName = planName;
        return this;
    }

    public PksClusterBuilder setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
}