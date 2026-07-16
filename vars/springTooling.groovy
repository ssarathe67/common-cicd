def check(config) {
    config.steps.echo 'Checking required CI tooling'

    config.steps.sh 'java -version'

    if (config.fileExists('mvnw')) {
        config.steps.sh 'chmod +x mvnw'
        config.steps.sh './mvnw -v'
    } else if (config.fileExists('pom.xml')) {
        config.steps.sh 'mvn -v'
    }

    if (config.fileExists('gradlew')) {
        config.steps.sh 'chmod +x gradlew'
        config.steps.sh './gradlew -v'
    } else if (config.fileExists('build.gradle') || config.fileExists('build.gradle.kts')) {
        config.steps.sh 'gradle -v'
    }

    config.steps.sh 'podman --version'

    if (!config.hasSupportedBuildFile()) {
        config.steps.error 'No supported Spring build file found. Expected Maven or Gradle.'
    }
}

