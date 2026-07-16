package org.tagore.cicd

class SpringApplicationConfig implements Serializable {
    private static final long serialVersionUID = 1L

    transient def steps
    Map raw

    SpringApplicationConfig(def steps, Map raw = [:]) {
        this.steps = steps
        this.raw = raw ?: [:]
    }

    void refresh(def steps) {
        this.steps = steps
    }

    String getAppName() {
        return sanitize(raw.appName ?: envValue('JOB_BASE_NAME') ?: 'spring-app')
    }

    String getContainerName() {
        return sanitize(raw.containerName ?: appName)
    }

    String getImageName() {
        return raw.imageName ?: "localhost/${appName}"
    }

    String getImageTag() {
        return "${raw.imageTag ?: envValue('BUILD_NUMBER') ?: 'local'}"
    }

    String getImageRef() {
        return "${imageName}:${imageTag}"
    }

    int getDeployPort() {
        return (raw.deployPort ?: 8080) as int
    }

    int getContainerPort() {
        return (raw.containerPort ?: 8080) as int
    }

    String getJavaVersion() {
        return "${raw.javaVersion ?: '17'}"
    }

    String getHealthPath() {
        return normalizePath(raw.healthPath ?: '/actuator/health')
    }

    boolean getSkipTests() {
        return (raw.skipTests ?: false) as boolean
    }

    String getBuildsToKeep() {
        return "${raw.buildsToKeep ?: '20'}"
    }

    String getContainerFile() {
        if (raw.containerFile) {
            return "${raw.containerFile}"
        }
        if (fileExists('Containerfile')) {
            return 'Containerfile'
        }
        if (fileExists('Dockerfile')) {
            return 'Dockerfile'
        }
        return null
    }

    String getBuildCommand() {
        if (raw.buildCommand) {
            return "${raw.buildCommand}"
        }
        if (fileExists('mvnw')) {
            return './mvnw -B clean package -DskipTests'
        }
        if (fileExists('pom.xml')) {
            return 'mvn -B clean package -DskipTests'
        }
        if (fileExists('gradlew')) {
            return './gradlew clean build -x test'
        }
        return 'gradle clean build -x test'
    }

    String getTestCommand() {
        if (raw.testCommand) {
            return "${raw.testCommand}"
        }
        if (fileExists('mvnw')) {
            return './mvnw -B test'
        }
        if (fileExists('pom.xml')) {
            return 'mvn -B test'
        }
        if (fileExists('gradlew')) {
            return './gradlew test'
        }
        return 'gradle test'
    }

    String getJarFilePattern() {
        if (raw.jarFilePattern) {
            return "${raw.jarFilePattern}"
        }
        if (fileExists('mvnw') || fileExists('pom.xml')) {
            return 'target/*.jar'
        }
        return 'build/libs/*.jar'
    }

    String getApplicationUrl() {
        return "http://${hostName}:${deployPort}"
    }

    String getHealthUrl() {
        return "${applicationUrl}${healthPath}"
    }

    String getHostName() {
        return "${raw.hostName ?: envValue('JENKINS_AGENT_HOST') ?: 'localhost'}"
    }

    boolean hasSupportedBuildFile() {
        return fileExists('mvnw') ||
            fileExists('pom.xml') ||
            fileExists('gradlew') ||
            fileExists('build.gradle') ||
            fileExists('build.gradle.kts')
    }

    String podmanRunCommand() {
        List<String> args = ['podman', 'run', '-d', '--name', containerName, '-p', "${deployPort}:${containerPort}"]

        if (raw.podmanNetwork) {
            args.addAll(['--network', "${raw.podmanNetwork}"])
        }

        Map envVars = (raw.environment ?: [:]) as Map
        envVars.each { key, value ->
            args.addAll(['-e', "${key}=${value}"])
        }

        List extraRunArgs = (raw.extraRunArgs ?: []) as List
        extraRunArgs.each { arg ->
            args.add("${arg}")
        }

        args.add(imageRef)
        return args.collect { shellQuote("${it}") }.join(' ')
    }

    String shellQuote(String value) {
        return "'${value.replace("'", "'\"'\"'")}'"
    }

    boolean fileExists(String path) {
        return steps.fileExists(path)
    }

    private String envValue(String key) {
        try {
            return steps.env[key]
        } catch (ignored) {
            return null
        }
    }

    private String sanitize(String value) {
        return value.toLowerCase().replaceAll(/[^a-z0-9_.-]+/, '-').replaceAll(/^-+|-+$/, '')
    }

    private String normalizePath(String value) {
        return value.startsWith('/') ? value : "/${value}"
    }
}
