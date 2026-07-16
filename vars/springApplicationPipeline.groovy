def call(Map config = [:]) {
    def pipelineConfig = new org.tagore.cicd.SpringApplicationConfig(this, config)

    pipeline {
        agent any

        options {
            timestamps()
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: pipelineConfig.buildsToKeep))
        }

        environment {
            APP_NAME = "${pipelineConfig.appName}"
            IMAGE_NAME = "${pipelineConfig.imageName}"
            IMAGE_TAG = "${pipelineConfig.imageTag}"
            CONTAINER_NAME = "${pipelineConfig.containerName}"
            DEPLOY_PORT = "${pipelineConfig.deployPort}"
            CONTAINER_PORT = "${pipelineConfig.containerPort}"
        }

        stages {
            stage('Init') {
                steps {
                    script {
                        pipelineConfig.refresh(this)
                        springTooling.check(pipelineConfig)
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        pipelineConfig.refresh(this)
                        springBuilder.build(pipelineConfig)
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        pipelineConfig.refresh(this)
                        springPodman.deploy(pipelineConfig)
                    }
                }
            }
        }

        post {
            success {
                script {
                    pipelineConfig.refresh(this)
                    echo "Application URL: ${pipelineConfig.applicationUrl}"
                    echo "Health URL: ${pipelineConfig.healthUrl}"
                }
            }
        }
    }
}

