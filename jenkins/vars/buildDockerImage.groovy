// Shared library step: build + push a multi-stage Docker image, tagging
// with both the given tag and "latest", and pushing build metadata to
// JFrog Artifactory for lifecycle tracking.
def call(Map config) {
    def imageName = config.imageName
    def tag = config.tag ?: 'latest'
    def dockerfile = config.dockerfile ?: 'Dockerfile'

    sh """
        docker build -f ${dockerfile} -t ${imageName}:${tag} -t ${imageName}:latest .
        docker push ${imageName}:${tag}
        docker push ${imageName}:latest
    """

    // Record build info in Artifactory so image -> commit -> pipeline run is traceable
    rtBuildInfo(captureEnv: true)
    rtDockerPush(serverId: 'artifactory-main', image: "${imageName}:${tag}", targetRepo: 'docker-local')
    rtPublishBuildInfo(serverId: 'artifactory-main')
}
