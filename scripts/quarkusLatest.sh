# This script allows to replicate the changes necessary to run Kogito using Quarkus latest snapshot
# Ensure to build Quarkus locally first
mvn -f ../pom.xml versions:set-property -pl :kogito-dependencies-bom -Dproperty=version.io.quarkus -DnewVersion=999-SNAPSHOT -DgenerateBackupPoms=false
mvn -f ../pom.xml versions:set-property -pl :kogito-dependencies-bom -Dproperty=version.io.quarkus.quarkus-test-maven -DnewVersion=999-SNAPSHOT -DgenerateBackupPoms=false
mvn -f ../pom.xml versions:compare-dependencies -pl :kogito-dependencies-bom -pl :kogito-build-parent -pl :kogito-quarkus-bom -pl :kogito-build-no-bom-parent -DremotePom=io.quarkus:quarkus-bom:999-SNAPSHOT -DupdatePropertyVersions=true -DupdateDependencies=true -DgenerateBackupPoms=false