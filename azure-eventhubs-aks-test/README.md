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

## Run on AKS
Install Kubectl

Create a file `secret.yaml` and include below content.

```shell
apiVersion: v1
kind: Secret
metadata:
  name: event-hubs-secret
  namespace: liuku
type: Opaque
data:
  EVENT_HUBS_CONNECTION_STRING: <replace-event-hubs-connection-string-base64>
```

Get encoded connection string, run below command in wsl/linux bash.
```shell
echo <eventhubs-connection-string> | base64
```


Submit secret 

```shell
kubectl apply -f secret.yaml
```

Create a file `job.yaml` and including below content.

```shell
apiVersion: batch/v1
kind: Job
metadata:
  name: <your job name>
  namespace: <your namespace name>
spec:
  template:
    spec:
      containers:
      - name: <container name, pick anything>
        image: <container image name>
        imagePullPolicy: Always
        command: ["test entrypoint command/binary"]
        args: [<args string array for your test command>]
      restartPolicy: Never
  backoffLimit: 1
```

Submit job

```shell
kubectl create -f testjob.yaml
```