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
    sh "pwd"
    sh "ls -la"
    sh "ls -la app"
    sh "echo ${env.BRANCH_NAME}"
    sh "sed -i 's/<BUILD_TAG>/${build_tag}/' k8s-dev.yaml"
    sh "sed -i 's/<BUILD_BRANCH>/${env.BRANCH_NAME}/' k8s-dev.yaml"
    sh "bash running-development.sh" 
    sh "kubectl apply -f k8s-dev.yaml --validate=false"
  }
  stage("Promote to qa") {
    def userInput = input(
      id: 'userInput',
      message: 'Promote to qa?'
      parameters: [
        [
          $class: 'ChoiceParameterDefinition',
          choices: "YES\nNO",
          name: 'Env'
        ]
      ]
    )
    echo "This is a deployment step to ${userInput}"
    if (userInput == "YES") {
      sh "sed -i 's/<BUILD_TAG>/${build_tag}/' k8s-qa.yaml"
      sh "sed -i 's/<BRANCH_NAME>/${env.BRANCH_NAME}/' k8s-qa.yaml"
      // "sh bash running-qa.sh"
      sh "kubectl apply -f k8s-qa.yaml --validate=false"
      sh "sleep 6"
      sh "kubectl get pods -n qatest"
    } else {
      //exit
    }
  }
}