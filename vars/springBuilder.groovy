def build(config) {
    config.steps.echo "Building ${config.appName}"

    if (!config.skipTests) {
        config.steps.sh config.testCommand
    }

    config.steps.sh config.buildCommand

    def containerFile = config.containerFile
    if (!containerFile) {
        containerFile = '.jenkins.Containerfile'
        prepareGeneratedContainerJar(config)
        config.steps.writeFile file: containerFile, text: generatedContainerFile(config)
    }

    config.steps.sh "podman build -f ${config.shellQuote(containerFile)} -t ${config.shellQuote(config.imageRef)} ."
}

private void prepareGeneratedContainerJar(config) {
    config.steps.sh """mkdir -p .jenkins
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
"""
}

private String generatedContainerFile(config) {
    return """FROM eclipse-temurin:${config.javaVersion}-jre
WORKDIR /app
COPY .jenkins/app.jar /app/app.jar
EXPOSE ${config.containerPort}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
"""
}
