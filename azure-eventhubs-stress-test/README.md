# Azure Event Hubs Stress Test Example

## Run locally

1. Update parameters in file `src/resources/.env`.

2. Add program argument:

    ```shell
    --TEST_CLASS=SendSimpleEvent
    ```

3. Add environment variable:

    ```shell
    ENV_FILE=.env
    ```

4. Start `EventHubsScenarioRunner`.


## Run on cluster

### Build project

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
```

Stop and remove deployed package
```shell
helm uninstall <stress test name> -n <stress test namespace>
```

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


