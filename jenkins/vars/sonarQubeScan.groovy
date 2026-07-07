// Shared library step: run a SonarQube analysis and enforce the quality
// gate before letting the pipeline continue.
def call(Map config) {
    def projectKey = config.projectKey

    withSonarQubeEnv('sonarqube-main') {
        sh "sonar-scanner -Dsonar.projectKey=${projectKey}"
    }

    timeout(time: 5, unit: 'MINUTES') {
        def qg = waitForQualityGate()
        if (qg.status != 'OK') {
            error "Pipeline aborted: SonarQube quality gate failed (${qg.status})"
        }
    }
}
