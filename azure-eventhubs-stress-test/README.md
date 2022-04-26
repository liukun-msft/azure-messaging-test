# Azure Event Hubs Test on AKS

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

## Deploy by azure-sdk-tools deployment script


```shell
cd <current project path>
<root path>\azure-sdk-tools\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -Login  -PushImages
```

```
Error: 
C:\Users\liuku\Documents\project\azure-sdk-tools\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 : A
positional parameter cannot be found that accepts argument 'PSModule-Helpers.ps1'.
At line:1 char:1
+ C:\Users\liuku\Documents\project\azure-sdk-tools\eng\common\scripts\s ...
+ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : InvalidArgument: (:) [deploy-stress-tests.ps1], ParameterBindingException
    + FullyQualifiedErrorId : PositionalParameterNotFound,deploy-stress-tests.ps1
```


## Deploy and Run by Helm

```shell
helm dependency build
helm install event-hubs-example -n <your namespace> . --set stress-test-addons.env=test
```
