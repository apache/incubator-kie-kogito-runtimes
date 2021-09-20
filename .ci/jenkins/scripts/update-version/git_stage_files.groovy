void execute(def pipelinesCommon) {
    echo 'Hello from Git Stage Files script'
    githubscm.findAndStageNotIgnoredFiles('pom.xml') 
}

return this
