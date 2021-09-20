void execute(def pipelinesCommon) {
    echo 'Hello from init script'
    if (pipelinesCommon.isRelease() || pipelinesCommon.isCreatePR()) {
        // Verify version is set
        assert pipelinesCommon.getKogitoVersion()

        if (pipelinesCommon.isRelease()) {
            // Verify if on right release branch
            assert pipelinesCommon.getGitBranch() == util.getReleaseBranchFromVersion(pipelinesCommon.getKogitoVersion())
        }
    }
}

return this
