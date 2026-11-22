@description('Environment name') @allowed(['dev','test','prod']) param environment string
@description('Resource name prefix') param prefix string = 'meridian-pay'
@description('Azure region') param location string = resourceGroup().location
@description('Countries handled by this deployment') param countries array = ['MX','CO','CL']
@description('Container image, pinned by digest in production') param intakeImage string
@description('Tags') param tags object = {}

var suffix = uniqueString(resourceGroup().id, environment)
var commonTags = union(tags, { environment: environment, workload: 'premium-payments' })

resource log 'Microsoft.OperationalInsights/workspaces@2022-10-01' = {
  name: '${prefix}-logs-${environment}-${suffix}'
  location: location
  tags: commonTags
  properties: { sku: { name: 'PerGB2018' }, retentionInDays: 30, features: { enableLogAccessUsingOnlyResourcePermissions: true } }
}
resource appi 'Microsoft.Insights/components@2020-02-02' = {
  name: '${prefix}-appi-${environment}-${suffix}'
  location: location
  kind: 'web'
  tags: commonTags
  properties: { Application_Type: 'web', WorkspaceResourceId: log.id, RetentionInDays: 30 }
}
resource vault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: take(replace('${prefix}-kv-${environment}-${suffix}', '-', ''), 24)
  location: location
  tags: commonTags
  properties: {
    tenantId: subscription().tenantId
    enableRbacAuthorization: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 90
    publicNetworkAccess: 'Disabled'
    sku: { family: 'A', name: 'standard' }
    networkAcls: { bypass: 'AzureServices', defaultAction: 'Deny', ipRules: [], virtualNetworkRules: [] }
  }
}resource cosmos 'Microsoft.DocumentDB/databaseAccounts@2024-05-15' = {
  name: '${prefix}-db-${environment}-${suffix}'
  location: location
  kind: 'GlobalDocumentDB'
  tags: commonTags
  properties: {
    databaseAccountOfferType: 'Standard'
    locations: [{ locationName: location, failoverPriority: 0, isZoneRedundant: environment == 'prod' }]
    consistencyPolicy: { defaultConsistencyLevel: 'Session' }
    enableAutomaticFailover: environment == 'prod'
    publicNetworkAccess: 'Disabled'
    disableLocalAuth: true
    minimalTlsVersion: 'Tls12'
    capabilities: [{ name: 'EnableServerless' }]
  }
}
resource db 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases@2024-05-15' = {
  parent: cosmos
  name: 'payments'
  properties: { resource: { id: 'payments' }, options: { throughput: null } }
}
resource events 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers@2024-05-15' = {
  parent: db
  name: 'payment-events'
  properties: {
    resource: { id: 'payment-events', partitionKey: { paths: ['/paymentKey'], kind: 'Hash', version: 2 }, defaultTtl: 1123200, indexingPolicy: { indexingMode: 'consistent', includedPaths: [{ path: '/provider/*' }, { path: '/correlationId/?' }, { path: '/processingStatus/?' }], excludedPaths: [{ path: '/*' }] } }
  }
}
resource sb 'Microsoft.ServiceBus/namespaces@2022-10-01-preview' = {
  name: '${prefix}-sb-${environment}-${suffix}'
  location: location
  sku: { name: 'Premium', tier: 'Premium', capacity: environment == 'prod' ? 2 : 1 }
  tags: commonTags
  properties: { minimumTlsVersion: '1.2', publicNetworkAccess: 'Disabled', disableLocalAuth: true }
}
resource workQueue 'Microsoft.ServiceBus/namespaces/queues@2022-10-01-preview' = {
  parent: sb
  name: 'payments-work'
  properties: { lockDuration: 'PT1M', maxDeliveryCount: 10, requiresDuplicateDetection: true, duplicateDetectionHistoryTimeWindow: 'PT10M', requiresSession: true, deadLetteringOnMessageExpiration: true, defaultMessageTimeToLive: 'P14D', maxSizeInMegabytes: 5120 }
}
resource topic 'Microsoft.ServiceBus/namespaces/topics@2022-10-01-preview' = {
  parent: sb
  name: 'premium-payment-events'
  properties: { requiresDuplicateDetection: true, duplicateDetectionHistoryTimeWindow: 'PT10M', defaultMessageTimeToLive: 'P14D', maxSizeInMegabytes: 5120 }
}
resource dlqAlert 'Microsoft.Insights/metricAlerts@2018-03-01' = {
  name: '${prefix}-dlq-depth-${environment}'
  location: 'global'
  properties: {
    description: 'Dead-letter queue depth must be investigated'
    severity: 1
    enabled: true
    scopes: [sb.id]
    evaluationFrequency: 'PT1M'
    windowSize: 'PT5M'
    criteria: { 'odata.type': 'Microsoft.Azure.Monitor.SingleResourceMultipleMetricCriteria', allOf: [{ name: 'DeadLetterMessages', metricName: 'DeadletteredMessages', metricNamespace: 'Microsoft.ServiceBus/namespaces', operator: 'GreaterThan', threshold: 0, timeAggregation: 'Maximum', criterionType: 'Static' }] }
    actions: []
  }
}
resource appEnv 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: '${prefix}-aca-env-${environment}-${suffix}'
  location: location
  tags: commonTags
  properties: { appLogsConfiguration: { destination: 'log-analytics', logAnalyticsConfiguration: { customerId: log.properties.customerId, sharedKey: log.listKeys().primarySharedKey } } }
}
resource intake 'Microsoft.App/containerApps@2024-03-01' = {
  name: '${prefix}-intake-${environment}-${suffix}'
  location: location
  tags: commonTags
  identity: { type: 'SystemAssigned' }
  properties: {
    managedEnvironmentId: appEnv.id
    configuration: { ingress: { external: false, targetPort: 8080, transport: 'http' }, secrets: [] }
    template: {
      scale: { minReplicas: environment == 'prod' ? 2 : 1, maxReplicas: environment == 'prod' ? 20 : 3, rules: [{ name: 'http-concurrency', http: { metadata: { concurrentRequests: '40' } } }] }
      containers: [{ name: 'intake', image: intakeImage, resources: { cpu: 0.5, memory: '1Gi' }, env: [{ name: 'SPRING_PROFILES_ACTIVE', value: environment }, { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appi.properties.ConnectionString }, { name: 'COSMOS_ENDPOINT', value: cosmos.properties.documentEndpoint }, { name: 'SERVICEBUS_NAMESPACE', value: sb.properties.serviceBusEndpoint }] }]
    }
  }
}
var cosmosDataRole = '00000000-0000-0000-0000-000000000002'
var sbSenderRole = '69a216fc-b8fb-44d8-bc22-1f3c2cd27a39'
resource cosmosAccess 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(cosmos.id, intake.id, cosmosDataRole)
  scope: cosmos
  properties: { roleDefinitionId: resourceId('Microsoft.DocumentDB/databaseAccounts/sqlRoleDefinitions', cosmos.name, cosmosDataRole), principalId: intake.identity.principalId, principalType: 'ServicePrincipal' }
}
resource sbAccess 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(sb.id, intake.id, sbSenderRole)
  scope: sb
  properties: { roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', sbSenderRole), principalId: intake.identity.principalId, principalType: 'ServicePrincipal' }
}
resource vaultAccess 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(vault.id, intake.id, '4633458b-17de-408a-b874-0445c86b69e6')
  scope: vault
  properties: { roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '4633458b-17de-408a-b874-0445c86b69e6'), principalId: intake.identity.principalId, principalType: 'ServicePrincipal' }
}output serviceBusNamespace string = sb.name
output cosmosEndpoint string = cosmos.properties.documentEndpoint
output keyVaultName string = vault.name
output deployedCountries array = countries
