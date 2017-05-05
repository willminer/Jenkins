def repos = [
    "core-server",
    "core-worker",
    "discovery-client",
    "discovery-health-worker",
    "discovery-middleware",
    "discovery-model",
    "discovery-proxy",
    "discovery-service",
    "discovery-test-tools",
    "generator-service-gen",
    "generator-worker-gen",
    "location-service",
    "mdn-email-worker",
    "mdn-push-worker",
    "mdn-service",
    "mdn-sms-worker",
    "mdn-web-worker",
    "multi-tenancy-db",
    "payment-service",
    "replica-service",
    "security-middleware",
    "security-model",
    "security-service",
    "stash",
    "tenant-middleware",
    "tenant-model",
    "tenant-service"
];


node {
    properties([parameters([
        string(defaultValue: 'development', description: 'Branch to create  release off', name: 'BASE_BRANCH'), 
        string(defaultValue: '', description: 'Release branch name', name: 'RELEASE_BRANCH'), 
        booleanParam(defaultValue: false, description: 'Does RELEASE_BRANCH exist', name: 'EXISTING_BRANCH')
        ]), pipelineTriggers([])])

    stage('Clean'){
        deleteDir()
    }

    stage('Checkout'){
        for (int i = 0; i < repos.size(); i++) {
            checkout(repos.get(i), BASE_BRANCH)
        }
    }


    stage('Create release branch'){
        for(int i=0; i < repos.size(); i++){
            createBranch(repos.get(i), RELEASE_BRANCH, EXISTING_BRANCH)
        }
    }

    stage('Configure package.jsons'){
        // replace development branch name with target branch
        sh ("""find . -name "package.json" -exec sed -i "s/#${BASE_BRANCH}/#${RELEASE_BRANCH}/" {} \\;""")

        //Add Oauth token into seach github reference
        sh ("""find . -name "package.json" -exec sed -i "s/https:\\/\\/(\\w.*)@github/https:\\/\\/${GITHUB_OAUTH_TOKEN}\\@github/g" {} \\;""")
    }

    stage('Commit package.jsons'){
        for(int i=0; i < repos.size(); i++){
            commitFiles(repos.get(i), RELEASE_BRANCH)
        }
    }

    stage('Push release branch'){
        for(int i=0; i < repos.size(); i++){
            pushBranch(repos.get(i), RELEASE_BRANCH)
        }
    }
}

def createBranch(REPO, TARGET_BRANCH, EXISTING_BRANCH){
    dir(REPO){
        echo "Creating Target Branch ${TARGET_BRANCH} for ${REPO}"
        if(EXISTING_BRANCH == true){
            sh "git checkout ${TARGET_BRANCH}"
        }else{
            sh "git checkout -b ${TARGET_BRANCH}"  
        }
    }
}

def commitFiles(REPO, RELEASE){
    dir(REPO){

        //Set the git configs for this temp repo
        sh 'git config user.name "Jenkins"'
        sh 'git config user.email "jenkins-ch@cchs.com"'

        echo "Commiting package.jsons for ${REPO}"
        sh """ find . -name "package.json" -exec git add {} \\; """
        try{
            sh " git commit -m'Configuring release ${RELEASE}' "
        }catch(error){
            echo "error: ${error}"
        }
    }
}

def pushBranch(REPO, TARGET_BRANCH){
    dir(REPO){
        //Set the git configs for this temp repo
        sh 'git config push.default matching'
        echo "Pushing ${TARGET_BRANCH} for ${REPO}"
        sh "git push origin ${TARGET_BRANCH}"
    }
}

def checkout(REPO, TARGET_BRANCH){
    dir(REPO){
        echo "Checking out ${TARGET_BRANCH} in ${REPO}"
        git url: "https://${GITHUB_OAUTH_TOKEN}@github.com/cdspteam/" + REPO, branch: TARGET_BRANCH
    }
}
