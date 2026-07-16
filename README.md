# common-cicd

Jenkins shared library for common CI/CD pipelines.

## Jenkins Setup

Configure this repository as a Global Pipeline Library in Jenkins:

1. Go to **Manage Jenkins > System > Global Trusted Pipeline Libraries**.
2. Add a library named `common-cicd`.
3. Point the SCM configuration to this repository.
4. Select the branch Jenkins should load.

## Spring Application Pipeline

Use the shared step from an application repository:

```groovy
@Library('common-cicd') _

springApplicationPipeline(
  appName: 'orders-api',
  deployPort: 8080
)
```

See [examples/spring-app.Jenkinsfile](examples/spring-app.Jenkinsfile) for a complete job file.

The pipeline has three stages:

- `Init`: checks required tooling.
- `Build`: builds the Spring application and container image.
- `Deploy`: runs the application with Podman and prints the URL to hit.

The library supports Linux and Windows Jenkins agents. On Windows it uses Jenkins `bat`
steps instead of `sh`.

## Expected Application Repository

The application repository should include one of:

- `mvnw` or `pom.xml` for Maven.
- `gradlew` or `build.gradle` / `build.gradle.kts` for Gradle.

If no `Containerfile` or `Dockerfile` exists, the library creates a simple temporary Spring Boot container file that runs the built jar.

## Configuration

```groovy
springApplicationPipeline(
  appName: 'orders-api',
  deployPort: 8080,
  containerPort: 8080,
  javaVersion: '17',
  imageName: 'localhost/orders-api',
  imageTag: env.BUILD_NUMBER,
  hostName: 'jenkins-agent.example.com',
  healthPath: '/actuator/health',
  jarFilePattern: 'target/*.jar',
  buildCommand: './mvnw -B clean package -DskipTests',
  testCommand: './mvnw -B test',
  skipTests: false
)
```

### Options

| Option | Default | Description |
| --- | --- | --- |
| `appName` | Jenkins job name | Container name and default image name source. |
| `deployPort` | `8080` | Host port exposed by Podman. |
| `containerPort` | `8080` | Container port the Spring app listens on. |
| `javaVersion` | `17` | Java runtime used by the generated container file. |
| `imageName` | `localhost/<appName>` | Podman image name. |
| `imageTag` | Jenkins build number | Podman image tag. |
| `hostName` | `localhost` | Host used in the printed application URL. Use this for remote Jenkins agents. |
| `healthPath` | `/actuator/health` | URL path printed for health checks. |
| `jarFilePattern` | auto-detected | Jar path copied by the generated container file. |
| `buildCommand` | auto-detected | Custom build command. |
| `testCommand` | auto-detected | Custom test command. |
| `skipTests` | `false` | Skip the test command when true. |
| `podmanNetwork` | empty | Optional Podman network name. |
| `extraRunArgs` | empty list | Extra arguments passed to `podman run`. |
| `environment` | empty map | Environment variables passed to the container. |
