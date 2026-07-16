def deploy(config) {
    config.steps.echo "Deploying ${config.imageRef} with Podman"

    config.steps.sh "podman rm -f ${config.shellQuote(config.containerName)} || true"
    config.steps.sh config.podmanRunCommand()

    config.steps.echo "Application URL: ${config.applicationUrl}"
    config.steps.echo "Health URL: ${config.healthUrl}"
}

