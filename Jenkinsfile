
node {
    properties([[$class: 'ParametersDefinitionProperty', parameterDefinitions: [[$class: 'ChoiceParameterDefinition', choices: "Default\nDev\nStage\nProd", description: 'Environment', name : 'environment']]]])

    if (environment ==~ /(?i)(default)/){
    	environment  = BRANCH_NAME
    }
    
    echo "Environment: ${environment}"
    
    if (environment  ==~ /(?i)(dev|develop|development)/){
    	echo "using develop"
    	env.SWARM_HOST = SWARM_DEVELOP
    } else if (environment  ==~ /(?i)(stage|master)/){
    	echo "using stage"
    	env.SWARM_HOST = SWARM_STAGE
    } else if (environment  ==~ /(?i)prod/){
    	echo "using prod"
    	env.SWARM_HOST = SWARM_PROD
    } else {
    	echo 'using none'
    }
    
    echo 'Swarm Host: ${SWARM_HOST}'
    
    env.DOCKER_REPO = "${DOCKER_REPO_HOST}/${BRANCH_NAME}/"

    stage('Checkout'){
	checkout scm
    } 
    stage('Docker - Build'){
    	sh 'docker build -t="${DOCKER_REPO}jenkins" .'
    }
    stage('Docker - Tag'){
    	sh 'docker tag ${DOCKER_REPO}jenkins ${DOCKER_REPO}jenkins:${BUILD_NUMBER}'
    }
    stage('Docker - Push'){
	sh 'docker push ${DOCKER_REPO}jenkins:${BUILD_NUMBER}'
	sh 'docker push ${DOCKER_REPO}jenkins'
    }
}
