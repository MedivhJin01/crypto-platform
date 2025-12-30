pipeline {
  agent any

  tools {
    maven 'maven3'
  }

  environment {
    EC2_HOST = "98.84.97.49"
    EC2_DIR  = "/opt/crypto-platform"
    JAR_NAME = "crypto-platform-0.0.1-SNAPSHOT.jar"
  }

  stages {
    stage('Check tools') {
      steps {
        sh 'which mvn || true'
        sh 'mvn -v || true'
        sh 'java -version || true'
      }
    }

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build JAR') {
      steps {
        sh 'mvn -B -DskipTests clean package'
        sh "ls -lh target/${JAR_NAME}"
      }
    }

    stage('Upload artifacts to EC2') {
      steps {
        sshagent(credentials: ['ec2-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} 'mkdir -p ${EC2_DIR}/target'
            scp -o StrictHostKeyChecking=no target/${JAR_NAME} ec2-user@${EC2_HOST}:${EC2_DIR}/target/${JAR_NAME}
            scp -o StrictHostKeyChecking=no Dockerfile docker-compose.yml ec2-user@${EC2_HOST}:${EC2_DIR}/
          """
        }
      }
    }

    stage('Deploy on EC2') {
      steps {
        sshagent(credentials: ['ec2-ssh-key']) {
          sh """
            ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} '
              cd ${EC2_DIR} &&
              test -f .env || (echo "ERROR: ${EC2_DIR}/.env missing" && exit 1) &&
              test -f target/${JAR_NAME} || (echo "ERROR: jar missing" && exit 1) &&
              docker compose down &&
              docker compose up -d --build &&
              docker compose ps
            '
          """
        }
      }
    }
  }
}