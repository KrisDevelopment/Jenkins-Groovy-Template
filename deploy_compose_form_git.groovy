
properties([
	[
		$class: 'BuildBlockerProperty', 
		blockLevel: 'NODE',
		blockingJobs: "${JOB_NAME}",
		useBuildBlocker: true
	], 
	parameters([
		string(name: 'SCM_REPO', defaultValue: '', description: 'The repo URL of the project you want to build'),
		string(name: 'SCM_CREDENTAILS_ID', defaultValue: '', description: ""),
		string(name: 'SCM_BRANCH', defaultValue: 'master', description: "Name of the branch (main, master, trunk, development, etc.)"),
    string(name: 'NODE_LABEL', defaultValue: 'app_docs', description: "The label of the node you want to deploy to"),
	]),
])

scmTimeout = 600
scmCredentails = params.SCM_CREDENTAILS_ID
scmBranch = params.SCM_BRANCH
gitRepo = params.SCM_REPO

def scmCheckout(){
  checkout([$class: 'GitSCM', branches: [[name: scmBranch]], extensions: [[$class: 'CheckoutOption', timeout: scmTimeout], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: true, recursiveSubmodules: true, reference: '', timeout: scmTimeout, trackingSubmodules: true], [$class: 'CleanBeforeCheckout', deleteUntrackedNestedRepositories: true]], userRemoteConfigs: [[credentialsId: scmCredentails, url: gitRepo]]])
}

pipeline {
  agent {
    label params.NODE_LABEL
  }

  stages {
    stage('Checkout'){
      steps {
        script {
          scmCheckout()
        }
      }
    }

    stage('Run'){
      steps {
        script {
          sh 'docker-compose up --build --detach --force-recreate --remove-orphans'
        }
      }
    }
  }
}
