# Azure Event Hubs Stress Test Example

## Run locally

Configure and start `EventHubsScenarioRunner`.

Add program arguments:

```sh
--TEST_CLASS=SendSimpleEvent
```

Add environment variables:

```sh
EVENT_HUBS_CONNECTION_STRING="your event hubs connect string" 
EVENT_HUB_NAME="your event hub name"
```

## Build docker image

Install docker desktop

Build local image
```sh
docker build . -t "<registry server host name>/<your alias>/<test job image name>:<version>"
```

Push to registry
```shell
docker push "<registry server host name>/<your username>/<test job image name>:<version>"
```

## Deploy to stress test cluster
Make sure the powershell version is 7.0 or above. (Win10 default version is 5.1.)

```shell
cd <current project path>
<root path>\azure-sdk-tools\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -Login -PushImages
```

Validate jobs and pods

```shell
helm list -n <stress test namespace>
kubectl get jobs -n <stress test namespace>
kubectl get pods -n <stress test namespace>
```

```shell
# List stress test pods
kubectl get pods -n <stress test namespace> -l release=<stress test name>

# Get logs from the init-azure-deployer init container, if deploying resources. Omit `-c init-azure-deployer` to get main container logs.
kubectl logs -n <stress test namespace> <stress test pod name> -c init-azure-deployer

# If empty, there may have been startup failures
kubectl describe pod -n <stress test namespace> <stress test pod name>
```

Get test logs
```shell
kubectl logs -n <stress test namespace> <stress test pod name>
```


Stop and remove
```shell
helm uninstall <stress test name> -n <stress test namespace>
```



