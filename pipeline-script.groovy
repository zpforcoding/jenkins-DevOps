node('jenkins-slave') { 
  stage('Clone') {
    echo "1.Clone stage"
    git url: "https://github.com/zpforcoding/jenkins-DevOps.git"
    script {
      build_tag = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    }
    echo "build_tag:${build_tag}"
  }
  stage('Test') { 
    echo "2.Test Stage"
  }
//stage("Deploy to Dev") {
//     echo "5.Deploy DEV"
//     sh "ls -la /usr/bin/docker"
//     sh "ls -la /home/jenkins/agent"
//     sh "pwd"
//     sh "kubectl get pod -A"
//     sh "docker info"
//     //sh "sed -i 's/<BUILD_TAG>/${build_tag}/' k8s-dev.yaml"
//     //sh "sed -i 's/<BRANCH_NAME>/${env.BRANCH_NAME}/' k8s-dev.yaml"
//     // sh "bath running-development.sh"
//     //sh "kubectl apply -f k8s-dev.yaml --validate=false"
//   }
  stage('Build') { 
    echo "3.Build Docker Image Stage"
    sh "docker build -t beck/jenkins-demo:${build_tag} ."
    sh "docker tag beck/jenkins-demo:${build_tag} harbor:8085/beck/jenkins-demo:${build_tag}"
  }
  stage("Push") { 
    echo "4.Push Docker Image Stage"
    withCredentials([usernamePassword(credentialsId: '6e2498bb-e5a5-4608-b870-5e9bc1528ae0', passwordVariable: 'HarborPassword', usernameVariable: 'HarborUser')]) {
      echo "${HarborPassword}"
      sh "docker login -u ${HarborUser} -p ${HarborPassword} harbor:8085"
      sh "docker push harbor:8085/beck/jenkins-demo:${build_tag}"
    }
  }
  stage("Deploy to Dev") {
    echo "5.Deploy to DEV"
    sh "ls -la /usr/app"
    sh "echo ${env.BRANCH_NAME}"
    sh "sed -i 's/<BUILD_TAG>/${build_tag}/' k8s-dev.yaml"
    sh "sed -i 's/<BUILD_BRANCH>/${env.BRANCH_NAME}/' k8s-dev.yaml"
    sh "kubectl apply -f k8s-dev.yaml --validate=false"
  }
}