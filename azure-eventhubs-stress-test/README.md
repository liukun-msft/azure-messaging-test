# Azure Event Hubs Stress Test Example

## Development

This project is build on [azure-sdk-chaos](https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md). 

### Project Structure

```
.
├── src/                         # Test code
├── templates/                   # A directory of helm templates that will generate Kubernetes manifest files.
├── Chart.yaml                   # A YAML file containing information about the helm chart and its dependencies
├── Dockerfile                   # A Dockerfile for building the stress test image
├── stress-test-resouce.bicep    # An Azure Bicep for deploying stress test azure resources
├── values.yaml                  # Any default helm template values for this chart, e.g. a `scenarios` list
├── pom.xml
└── README.md
```

### Add Test Scenario

Add a new test class under `\scenarios`.

Extend `EventHubsScenarios` and implement test logic in `run()` method. 

Configure new class as a bean and set the bean name same as the class name. 

Add that class/bean name in values.yaml.

Validate the arguments in job.yaml is correct.

Build project and redeploy to cluster.

### Test existing scenarios

Because currently stress test framework doesn't support customized key/value for specific scenarios, we need copy different job files as a walkaround.

Copy content of job-scenario* file from /jobfiles folder to job.yaml.


## Run locally

Add program argument:

  ```shell
  --TEST_CLASS=<test class name>
  ```

Update required property values in application.properties:

  ```shell
  EVENT_HUB_NAME=<event hub name>
  EVENT_HUBS_CONNECTION_STRING=<event hub connection string>

  STORAGE_CONTAINER_NAME=<storage container name>
  STORAGE_CONNECTION_STRING=<storage connection string>
  ```

Start `EventHubsScenarioRunner`.

## Run on cluster

### Build project

We should build out an artifact for build docker image.

```
cd <current project path>
mvn clean install
```

### Deploy to cluster

Make sure you have installed the following tools:
- [Docker](https://docs.docker.com/get-docker/)
- [Kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
- [Helm](https://helm.sh/docs/intro/install/)
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- [Powershell 7.0+](https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7) 

Update the `namespace` field in Chart.yaml file. To avoid conflict, it is suggested to use **your alias**.
```shell
name: <stress test name>
...
annotations:
  namespace: <stress test namespace>
```

Download project [azure-sdk-tools](https://github.com/Azure/azure-sdk-tools), which is used for the deployment.

Keep in current stress test project, run below deployment commands.

```shell
<root path>\azure-sdk-tools\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -Login -PushImages
```

### Validate jobs status

Get pods and jobs 

```shell
helm list -n <stress test namespace>
kubectl get pods -n <stress test namespace>
kubectl get jobs -n <stress test namespace>
```

List stress test pods
```shell
kubectl get pods -n <stress test namespace> -l release=<stress test name>
```

Get logs from the init-azure-deployer init container, if deploying resources. Omit `-c init-azure-deployer` to get main container logs.

```shell
kubectl logs -n <stress test namespace> <stress test pod name> -c init-azure-deployer
```

If above command output is empty, there may have been startup failures
```shell
kubectl describe pod -n <stress test namespace> <stress test pod name>
```

Get stress test logs
```shell
kubectl logs -n <stress test namespace> <stress test pod name>
# Note that we may define multiple containers (for example, sender and receiver)
kubectl logs -n <stress test namespace> <stress test pod name> -c <container name>
```

Stop and remove deployed package
```shell
helm uninstall <stress test name> -n <stress test namespace>
```
## Configure Faults

[Chaos Mesh](https://chaos-mesh.org/) is used to configure faults against test jobs. There are two ways for the configuration, which are via the UI or via kubernetes manifests.

### Chaos Dashboard

```shell
kubectl port-forward -n stress-infra svc/chaos-dashboard 2333:2333
```
Go to `localhost:2333` in browser and click `New experiment` to submit a fault experiment.

### Chaos Manifest

See [Chaos manifest](https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#chaos-manifest) for details.

Below is an example for network loss. You can create a new yaml file with below content under `template/` folder to deploy on cluster.
```shell
apiVersion: chaos-mesh.org/v1alpha1
kind: NetworkChaos
metadata:
  name: '{{ .Release.Name }}-{{ .Release.Revision }}'
  namespace: {{ .Release.Namespace }}
  annotations:
    experiment.chaos-mesh.org/pause: 'true'
  labels:
    scenario: 'stress'
    release: '{{ .Release.Name }}'
    revision: '{{ .Release.Revision }}'
spec:
  scheduler:
    cron: '@every 30s'
  duration: '10s'
  action: loss
  direction: to
  externalTargets:
    - 'servicebus.windows.net'
  mode: one
  selector:
    labelSelectors:
      testInstance: "eventhub-{{ .Release.Name }}-{{ .Release.Revision }}"
      chaos: 'true'
    namespaces:
      - {{ .Release.Namespace }}
    podPhaseSelectors:
      - 'Running'
  loss:
    loss: '15'
    correlation: '0'
```

## Application Insights

Follow page [Azure Monitor OpenTelemetry-based auto-instrumentation for Java applications](https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-in-process-agent) to download the `applicationinsights-agent-3.2.11.jar`.

Place the jar under `src\main\resources` folder.

Add jvm parameter when start -javaagent:src\main\resources\applicationinsights-agent-3.2.11.jar




