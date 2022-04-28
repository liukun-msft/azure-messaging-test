@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

var eventHubsNamespaceName = '${baseName}-eventhubs'
var eventHubName = 'stress-test-event-hub'

resource eventHubsNamespace 'Microsoft.EventHub/namespaces@2021-11-01' = {
  name: eventHubsNamespaceName
  location: location
  sku: {
    name: 'Standard'
    tier: 'Standard'
  }
  properties: {}
}

resource eventHub 'Microsoft.EventHub/namespaces/eventhubs@2021-11-01' = {
  name: eventHubName
  properties: {
    messageRetentionInDays: 1
    partitionCount: 32
  }
  dependsOn: [
    eventHubsNamespace
  ]
}

output EVENT_HUB_NAMESPACE string = eventHubsNamespaceName
output EVENT_HUB_HOSTNAME string = '${eventHubsNamespaceName}.servicebus.windows.net'
output EVENT_HUB_NAME string = eventHubName
