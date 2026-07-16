def build(config) {
    config.steps.echo "Building ${config.appName}"

    if (!config.skipTests) {
        config.run(config.testCommand, config.windowsTestCommand)
    }

    config.run(config.buildCommand, config.windowsBuildCommand)

    def containerFile = config.containerFile
    if (!containerFile) {
        containerFile = '.jenkins.Containerfile'
        prepareGeneratedContainerJar(config)
        config.steps.writeFile file: containerFile, text: generatedContainerFile(config)
    }

    config.run(
        "podman build -f ${config.shellQuote(containerFile)} -t ${config.shellQuote(config.imageRef)} .",
        "podman build -f ${config.windowsQuote(containerFile)} -t ${config.windowsQuote(config.imageRef)} ."
    )
}

private void prepareGeneratedContainerJar(config) {
    config.run("""mkdir -p .jenkins
jar_file=""
for dir in target build/libs; do
  if [ -d "\$dir" ]; then
    jar_file=\$(find "\$dir" -maxdepth 1 -type f -name '*.jar' ! -name '*plain.jar' ! -name 'original-*.jar' | head -n 1)
  fi
  if [ -n "\$jar_file" ]; then
    break
  fi
done
if [ -z "\$jar_file" ]; then
  jar_file=\$(ls -1 ${config.jarFilePattern} 2>/dev/null | head -n 1)
fi
if [ -z "\$jar_file" ]; then
  echo "No jar artifact found for generated container image"
  exit 1
fi
cp "\$jar_file" .jenkins/app.jar
""", """if not exist .jenkins mkdir .jenkins
set "JAR_FILE="
if exist target (
  for %%F in (target\\*.jar) do (
    echo %%~nxF | findstr /I /R "plain\\.jar original-.*\\.jar" >nul || if not defined JAR_FILE set "JAR_FILE=%%F"
  )
)
if not defined JAR_FILE if exist build\\libs (
  for %%F in (build\\libs\\*.jar) do (
    echo %%~nxF | findstr /I /R "plain\\.jar original-.*\\.jar" >nul || if not defined JAR_FILE set "JAR_FILE=%%F"
  )
)
if not defined JAR_FILE (
  echo No jar artifact found for generated container image
  exit /b 1
)
copy /Y "%JAR_FILE%" ".jenkins\\app.jar"
"""
    )
}

private String generatedContainerFile(config) {
    return """FROM eclipse-temurin:${config.javaVersion}-jre
WORKDIR /app
COPY .jenkins/app.jar /app/app.jar
EXPOSE ${config.containerPort}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
"""
}
