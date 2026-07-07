// Shared library step: helm upgrade --install with --atomic so a bad
// release automatically rolls back instead of leaving the namespace
// half-upgraded.
def call(Map config) {
    def releaseName = config.releaseName
    def chartPath = config.chartPath
    def namespace = config.namespace ?: 'default'
    def imageTag = config.imageTag ?: 'latest'

    sh """
        helm upgrade --install ${releaseName} ${chartPath} \
          --namespace ${namespace} --create-namespace \
          --set image.tag=${imageTag} \
          --wait --timeout 5m --atomic
        kubectl rollout status deployment/${releaseName} -n ${namespace} --timeout=120s
    """
}
