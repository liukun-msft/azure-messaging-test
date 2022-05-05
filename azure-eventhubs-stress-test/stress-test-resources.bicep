@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

var eventHubsVersion = '2021-11-01'
var eventHubsNamespaceName = '${baseName}-eventhubs'
var eventHubName = 'test-event-hub'
var eventHubAuthRulesName = '${baseName}-eventhub-rules'

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
  parent: eventHubsNamespace
  name: eventHubName
  properties: {
    messageRetentionInDays: 1
    partitionCount: 32
  }
}

resource eventHubAuthRules 'Microsoft.EventHub/namespaces/eventhubs/authorizationRules@2021-11-01' = {
  parent: eventHub
  name: eventHubAuthRulesName
  properties:  {
    rights: [
      'Manage'
      'Send'
      'Listen'
    ]
  }
}

output RESOURCE_GROUP string = resourceGroup().name
output EVENT_HUB_NAMESPACE string = eventHubsNamespaceName
output EVENT_HUB_HOSTNAME string = '${eventHubsNamespaceName}.servicebus.windows.net'
output EVENT_HUB_NAME string = eventHubName
output EVENT_HUBS_CONNECTION_STRING string = listkeys(eventHubAuthRulesName, eventHubsVersion).primaryConnectionString
