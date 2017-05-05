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
        string(defaultValue: 'development', description: 'Base Branch', name: 'BASE_BRANCH'), 
        string(defaultValue: '', description: 'Branch to merge', name: 'BRANCH_TO_MERGE')
        ]), pipelineTriggers([])])

    stage('Clean'){
        deleteDir()
    }

    stage('Checkout'){
        for (int i = 0; i < repos.size(); i++) {
            checkout(repos.get(i), BASE_BRANCH)
        }
    }

    stage('Merge branch'){
        for(int i=0; i < repos.size(); i++){
            mergeBranch(repos.get(i), BRANCH_TO_MERGE)
        }
    }

    stage('Configure package.jsons'){
        // replace development branch name with target branch
        sh ("""find . -name "package.json" -exec sed -i "s/#${BRANCH_TO_MERGE}/#${BASE_BRANCH}/" {} \\;""")
    }

    stage('Commit package.jsons'){
        for(int i=0; i < repos.size(); i++){
            commitFiles(repos.get(i), BASE_BRANCH)
        }
    }

    stage('Push release branch'){
        for(int i=0; i < repos.size(); i++){
            pushBranch(repos.get(i), BASE_BRANCH)
        }
    }
}

def mergeBranch(REPO, TARGET_BRANCH){
    dir(REPO){
        //Set the git configs for this temp repo
        sh 'git config user.name "Jenkins"'
        sh 'git config user.email "jenkins-ch@cchs.com"'
        // Merge remote repository
        sh "git merge --no-ff origin/${TARGET_BRANCH}"
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

def commitFiles(REPO, BRANCH){
    dir(REPO){

        //Set the git configs for this temp repo
        sh 'git config user.name "Jenkins"'
        sh 'git config user.email "jenkins-ch@cchs.com"'

        echo "Commiting package.jsons for ${REPO}"
        sh """ find . -name "package.json" -exec git add {} \\; """
        try{
            sh " git commit -m'Setting package.json branch to ${BRANCH}' "
        }catch(error){
            echo "error: ${error}"
        }
    }
}

