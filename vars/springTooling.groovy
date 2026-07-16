def check(config) {
    config.steps.echo 'Checking required CI tooling'

    config.run('java -version')

    if (config.hasMavenWrapper()) {
        if (config.isUnix()) {
            config.run('chmod +x mvnw')
        }
        config.run('./mvnw -v', 'mvnw.cmd -v')
    } else if (config.fileExists('pom.xml')) {
        config.run('mvn -v')
    }

    if (config.hasGradleWrapper()) {
        if (config.isUnix()) {
            config.run('chmod +x gradlew')
        }
        config.run('./gradlew -v', 'gradlew.bat -v')
    } else if (config.fileExists('build.gradle') || config.fileExists('build.gradle.kts')) {
        config.run('gradle -v')
    }

    config.run('podman --version')

    if (!config.hasSupportedBuildFile()) {
        config.steps.error 'No supported Spring build file found. Expected Maven or Gradle.'
    }
}
