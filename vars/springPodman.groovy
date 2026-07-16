def deploy(config) {
    config.steps.echo "Deploying ${config.imageRef} with Podman"

    config.run(
        "podman rm -f ${config.shellQuote(config.containerName)} || true",
        "podman rm -f ${config.windowsQuote(config.containerName)} || exit /b 0"
    )
    config.run(config.podmanRunCommand(), config.windowsPodmanRunCommand())

    config.steps.echo "Application URL: ${config.applicationUrl}"
    config.steps.echo "Health URL: ${config.healthUrl}"
}
