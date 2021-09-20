void execute(def pipelinesCommon) {
    maven.mvnVersionsSet(pipelinesCommon.getDefaultMavenCommand(), pipelinesCommon.getKogitoVersion(), !pipelinesCommon.isRelease())
    maven.mvnSetVersionProperty(pipelinesCommon.getDefaultMavenCommand(), 'version.org.kie', pipelinesCommon.getDroolsVersion())
}

return this
