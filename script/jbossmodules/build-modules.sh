# ------------------------------------------------------------------------------------------
# Script to build EAP 6.1.0 deployment structure for BPMS
# ------------------------------------------------------------------------------------------
createModuleXML() {
	MODULE_NAME=$1
	MODULE_PATH=$2
	MODULE_DEPS_FILE=$3
	MODULE_RESOURCE=$4

	echo '<?xml version="1.0" encoding="UTF-8"?>' > $MODULE_PATH/main/module.xml
	echo "<module xmlns=\"urn:jboss:module:1.0\" name=\"$MODULE_NAME\">" >> $MODULE_PATH/main/module.xml
	echo "  <resources>" >> $MODULE_PATH/main/module.xml
# Add jar resources	
find $MODULE_PATH/main/*.jar -type f -printf '    <resource-root path="%f"/>\n' >> $MODULE_PATH/main/module.xml

# Add adittional resource.
if [[ -n $MODULE_RESOURCE ]] ;then 
	    echo "    <resource-root path=\"$MODULE_RESOURCE\"/>" >> $MODULE_PATH/main/module.xml
fi
	echo "  </resources>">> $MODULE_PATH/main/module.xml
	echo "  <dependencies>">> $MODULE_PATH/main/module.xml
	while read module; do
		if [ -n "$module" ]; then
		    echo "    <module name=\"$module\" export=\"true\"/>" >> $MODULE_PATH/main/module.xml   
		fi
	done < $MODULE_DEPS_FILE
	echo "  </dependencies>">> $MODULE_PATH/main/module.xml
	echo "</module>">> $MODULE_PATH/main/module.xml
}


# Program arguments
if [ $# -ne 2 ];
then
  echo "Missing arguments"
  echo "Usage: ./deploy.sh <path to EAP6.1 war file> <path to EAP 6.1 dashbuilder WAR file>"
  exit 65
fi


BASE_DIR=`pwd`
DIST_DIR=$BASE_DIR/dist
TMP_DIR=$BASE_DIR/tmp

echo 'BASE_DIR' $BASE_DIR

#DASHBUILDER_WAR=$BASE_DIR/jbpm-dashbuilder-jboss-as7.war
DASHBUILDER_WAR=$2
#KIE_WB_WAR=$BASE_DIR/kie-wb.war
KIE_WB_WAR=$1

if [ ! -f $DASHBUILDER_WAR ];
then
   echo "File $DASHBUILDER_WAR does not exist."
   exit 1
fi

if [ ! -f $KIE_WB_WAR ];
then
   echo "File $KIE_WB_WAR does not exist."
   exit 1
fi

echo "Base directory is: $BASE_DIR"
echo "Creating distribution in: $DIST_DIR"

# Setup modules locations
MODULE_LIB=$DIST_DIR/modules/system/layers/bpms/org/kie/lib
MODULE_KIE=$DIST_DIR/modules/system/layers/bpms/org/kie
MODULE_JBPM=$DIST_DIR/modules/system/layers/bpms/org/jbpm
MODULE_DROOLS=$DIST_DIR/modules/system/layers/bpms/org/drools
# create modules 3rd party * kie-commons
MODULE_CAMEL=$DIST_DIR/modules/system/layers/bpms/org/apache/camel
MODULE_COMMONS_MATH=$DIST_DIR/modules/system/layers/bpms/org/apache/commons/math
MODULE_HELIX=$DIST_DIR/modules/system/layers/bpms/org/apache/helix
MODULE_LUCENE=$DIST_DIR/modules/system/layers/bpms/org/apache/lucene
MODULE_JGIT=$DIST_DIR/modules/system/layers/bpms/org/eclipse/jgit
MODULE_ZOOKEEPER=$DIST_DIR/modules/system/layers/bpms/org/apache/zookeeper
# create modules 3rd party * drools
MODULE_AETHER=$DIST_DIR/modules/system/layers/bpms/org/sonatype/aether
MODULE_BATIK=$DIST_DIR/modules/system/layers/bpms/org/apache/batik 
MODULE_ANT=$DIST_DIR/modules/system/layers/bpms/org/apache/ant 
MODULE_MAVEN=$DIST_DIR/modules/system/layers/bpms/org/apache/maven
MODULE_WAGON=$DIST_DIR/modules/system/layers/bpms/org/apache/maven/wagon
MODULE_MVEL=$DIST_DIR/modules/system/layers/bpms/org/mvel 
MODULE_sonPLEXUS=$DIST_DIR/modules/system/layers/bpms/org/sonatype/plexus 
MODULE_codePLEXUS=$DIST_DIR/modules/system/layers/bpms/org/codehouse/plexus 
MODULE_POI=$DIST_DIR/modules/system/layers/bpms/org/apache/poi 
MODULE_PROTOBUF=$DIST_DIR/modules/system/layers/bpms/com/google/protobuf 
MODULE_SISU=$DIST_DIR/modules/system/layers/bpms/org/sonatype/sisu
MODULE_sonaMAVEN=$DIST_DIR/modules/system/layers/bpms/org/sonatype/maven
# create modules 3rd party * jbpm
MODULE_COMPRESS=$DIST_DIR/modules/system/layers/bpms/org/apache/commons/compress
MODULE_EXEC=$DIST_DIR/modules/system/layers/bpms/org/apache/commons/exec
MODULE_NET=$DIST_DIR/modules/system/layers/bpms/org/apache/commons/net
MODULE_VFS=$DIST_DIR/modules/system/layers/bpms/org/apache/commons/vfs
MODULE_SOLDER=$DIST_DIR/modules/system/layers/bpms/org/jboss/solder

echo '**** Cleaning output dirs ****'
rm -rf $DIST_DIR
rm -rf $TMP_DIR

mkdir -p $DIST_DIR
mkdir -p $TMP_DIR

# Create structure
mkdir  -p $MODULE_LIB/main
mkdir  -p $MODULE_KIE/main
mkdir  -p $MODULE_JBPM/main
mkdir  -p $MODULE_DROOLS/main
mkdir  -p $MODULE_CAMEL/main
mkdir  -p $MODULE_COMMONS_MATH/main
mkdir  -p $MODULE_HELIX/main
mkdir  -p $MODULE_LUCENE/main
mkdir  -p $MODULE_JGIT/main
mkdir  -p $MODULE_ZOOKEEPER/main
mkdir  -p $MODULE_AETHER/main
mkdir  -p $MODULE_BATIK/main
mkdir  -p $MODULE_ANT/main
mkdir  -p $MODULE_MAVEN/main
mkdir  -p $MODULE_MVEL/main
mkdir  -p $MODULE_sonPLEXUS/main
mkdir  -p $MODULE_codePLEXUS/main
mkdir  -p $MODULE_POI/main
mkdir  -p $MODULE_PROTOBUF/main 
mkdir  -p $MODULE_SISU/main
mkdir  -p $MODULE_sonaMAVEN/main
mkdir  -p $MODULE_WAGON/main
mkdir  -p $MODULE_COMPRESS/main
mkdir  -p $MODULE_EXEC/main
mkdir  -p $MODULE_NET/main
mkdir  -p $MODULE_VFS/main
mkdir  -p $MODULE_SOLDER/main

mkdir -p $DIST_DIR/standalone/deployments

# Unzip original kie-wb
#
rm -rf $TMP_DIR
mkdir -p $TMP_DIR/kie-wb
cd $TMP_DIR/kie-wb
jar xf $KIE_WB_WAR
cd $BASE_DIR


#
# Clean unrequired libs
#

rm $TMP_DIR/kie-wb/WEB-INF/lib/jsp-api*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/jsp-api*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/commons-bean*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/commons-bean*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/commons-logging*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/commons-logging*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/jaxb*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/jaxb*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/jaxrs-api-*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/jaxrs-api-*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/jboss-intercepto*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/jboss-intercepto*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/jta*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/jta*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/log4j*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/log4j*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/xmlschema-core*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/xmlschema-core*.jar deleted"
rm $TMP_DIR/kie-wb/WEB-INF/lib/stax-api*.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/stax-api*.jar deleted"
## Duplicated!!!
rm $TMP_DIR/kie-wb/WEB-INF/lib/freemarker-2.3.8.jar
echo $TMP_DIR"/kie-wb/WEB-INF/lib/freemarker-2.3.8.jar deleted"


# ------------------------------------------------------------------------------------------
# Move jars around
# ------------------------------------------------------------------------------------------
echo '***** Creating layer *****'

mv $TMP_DIR/kie-wb/WEB-INF/lib/*.jar $MODULE_LIB/main

# KIE WORKBENCH (WEB-INF/lib)
mv $MODULE_LIB/main/kie-wb*.jar            $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/drools-wb*.jar         $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/drools-workbench*.jar  $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/jbpm-console*.jar      $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/jbpm-designer*.jar     $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/jbpm-form-modeler*.jar $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/uberfire-*.jar         $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/guvnor-*.jar           $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/errai-*.jar            $TMP_DIR/kie-wb/WEB-INF/lib
mv $MODULE_LIB/main/taglib*.jar            $TMP_DIR/kie-wb/WEB-INF/lib


# KIE / JBPM / DROOLS
mv $MODULE_LIB/main/kie-*.jar              $MODULE_KIE/main
mv $MODULE_LIB/main/kieora*.jar            $MODULE_KIE/main
mv $MODULE_LIB/main/jbpm-*.jar             $MODULE_JBPM/main
mv $MODULE_LIB/main/drools-*.jar           $MODULE_DROOLS/main
# JBPM PATCH
mkdir $MODULE_JBPM/main/META-INF
cp $BASE_DIR/patches/modules/jbpm/META-INF/* $MODULE_JBPM/main/META-INF

# new modules KIE dependencies

mv $MODULE_LIB/main/camel-core-*.jar               $MODULE_CAMEL/main
mv $MODULE_LIB/main/camel-josql-*.jar              $MODULE_CAMEL/main
mv $MODULE_LIB/main/commons-math-*.jar             $MODULE_COMMONS_MATH/main
mv $MODULE_LIB/main/helix-core-*-incubating.jar    $MODULE_HELIX/main
mv $MODULE_LIB/main/lucene-analyzers-common-*.jar  $MODULE_LUCENE/main
mv $MODULE_LIB/main/lucene-codecs-*.jar            $MODULE_LUCENE/main
mv $MODULE_LIB/main/lucene-core-*.jar              $MODULE_LUCENE/main
mv $MODULE_LIB/main/lucene-queries-*.jar           $MODULE_LUCENE/main
mv $MODULE_LIB/main/lucene-queryparser-*.jar       $MODULE_LUCENE/main
mv $MODULE_LIB/main/lucene-sandbox-*.jar           $MODULE_LUCENE/main
mv $MODULE_LIB/main/org.eclipse.jgit-*.jar         $MODULE_JGIT/main
mv $MODULE_LIB/main/zookeeper-*.jar                $MODULE_ZOOKEEPER/main

# new modules DROOLS dependencies

mv $MODULE_LIB/main/aether-api-*.jar                   $MODULE_AETHER/main
mv $MODULE_LIB/main/aether-connector-file-*.jar        $MODULE_AETHER/main
mv $MODULE_LIB/main/aether-connector-wagon-*.jar       $MODULE_AETHER/main
mv $MODULE_LIB/main/aether-impl-*.jar                  $MODULE_AETHER/main
mv $MODULE_LIB/main/aether-spi-*.jar                   $MODULE_AETHER/main
mv $MODULE_LIB/main/aether-util-*.jar                  $MODULE_AETHER/main
mv $MODULE_LIB/main/batik-*.jar                        $MODULE_BATIK/main
mv $MODULE_LIB/main/ant-*.jar                          $MODULE_ANT/main
mv $MODULE_LIB/main/ant-launcher-*.jar                 $MODULE_ANT/main
mv $MODULE_LIB/main/maven-aether-provider-*.jar        $MODULE_MAVEN/main
mv $MODULE_LIB/main/maven-artifact-*.jar               $MODULE_MAVEN/main
mv $MODULE_LIB/main/maven-compat-*.jar                 $MODULE_MAVEN/main
mv $MODULE_LIB/main/maven-core-*.jar                   $MODULE_MAVEN/main  
mv $MODULE_LIB/main/maven-model-*.jar                  $MODULE_MAVEN/main   
mv $MODULE_LIB/main/maven-model-builder-*.jar          $MODULE_MAVEN/main
mv $MODULE_LIB/main/maven-plugin-api-*.jar             $MODULE_MAVEN/main 
mv $MODULE_LIB/main/maven-repository-metadata-*.jar    $MODULE_MAVEN/main
mv $MODULE_LIB/main/maven-settings-*.jar               $MODULE_MAVEN/main
mv $MODULE_LIB/main/maven-settings-builder-*.jar       $MODULE_MAVEN/main
mv $MODULE_LIB/main/mvel2-*.Final.jar                  $MODULE_MVEL/main
mv $MODULE_LIB/main/plexus-cipher-*.jar                $MODULE_sonPLEXUS/main
mv $MODULE_LIB/main/plexus-classworlds-*.jar           $MODULE_codePLEXUS/main
mv $MODULE_LIB/main/plexus-component-annotations-*.jar $MODULE_codePLEXUS/main
mv $MODULE_LIB/main/plexus-interpolation-*.jar         $MODULE_codePLEXUS/main
mv $MODULE_LIB/main/plexus-utils-*.jar                 $MODULE_codePLEXUS/main
mv $MODULE_LIB/main/poi-*.jar                          $MODULE_POI/main 
mv $MODULE_LIB/main/poi-ooxml-*.jar                    $MODULE_POI/main
mv $MODULE_LIB/main/poi-ooxml-schemas-*.jar            $MODULE_POI/main 
mv $MODULE_LIB/main/protobuf-java-*.jar                $MODULE_PROTOBUF/main
mv $MODULE_LIB/main/sisu-guice-*-no_aop.jar            $MODULE_SISU/main
mv $MODULE_LIB/main/sisu-inject-bean-*.jar             $MODULE_SISU/main
mv $MODULE_LIB/main/wagon-provider-api-*.jar           $MODULE_WAGON/main
mv $MODULE_LIB/main/wagon-ahc-*.jar                    $MODULE_sonaMAVEN/main

# new modules JBPM dependencies

mv $MODULE_LIB/main/commons-compress-*.jar            $MODULE_COMPRESS/main
mv $MODULE_LIB/main/commons-exec-*.jar                $MODULE_EXEC/main
mv $MODULE_LIB/main/commons-net-*.jar                 $MODULE_NET/main
mv $MODULE_LIB/main/commons-vfs-*.jar                 $MODULE_VFS/main
mv $MODULE_LIB/main/sisu-inject-plexus-*.jar          $MODULE_SISU/main
mv $MODULE_LIB/main/solder-api-*.Final.jar            $MODULE_SOLDER/main
mv $MODULE_LIB/main/solder-impl-*.Final.jar           $MODULE_SOLDER/main 
mv $MODULE_LIB/main/solder-logging-*.Final.jar        $MODULE_SOLDER/main
#mkdir $MODULE_SOLDER/main/META-INF
#cp $BASE_DIR/patches/modules/solder/META-INF/* $MODULE_SOLDER/main/META-INF


# ------------------------------------------------------------------------------------------
# Generate modules.xml
# ------------------------------------------------------------------------------------------


#
# Generate library module for drools
#
MODULE_DROOLS_DEPS=./dependencies/drools.dependencies
createModuleXML "org.drools" $MODULE_DROOLS $MODULE_DROOLS_DEPS

#
# Generate library module for jbpm
#
MODULE_JBPM_DEPS=./dependencies/jbpm.dependencies
createModuleXML "org.jbpm" $MODULE_JBPM $MODULE_JBPM_DEPS "META-INF"

#
# Generate library module for kie
#
MODULE_KIE_DEPS=./dependencies/kie.dependencies
createModuleXML "org.kie" $MODULE_KIE $MODULE_KIE_DEPS

# 
# Generate library module for lib
#
MODULE_LIB_DEPS=./dependencies/kie-lib.dependencies
createModuleXML "org.kie.lib" $MODULE_LIB $MODULE_LIB_DEPS


#
# Generate library modules for other dependiencies
#


#
# Generate library module for aether
#
MODULE_AETHER_DEPS=./dependencies/aether.dependencies
createModuleXML "org.sonatype.aether" $MODULE_AETHER $MODULE_AETHER_DEPS

#
# Generate library module for aether
#
MODULE_AETHER_DEPS=./dependencies/batik.dependencies
createModuleXML "org.apache.batik" $MODULE_BATIK $MODULE_BATIK_DEPS

#
# Generate library module for ant
#
MODULE_ANT_DEPS=./dependencies/ant.dependencies
createModuleXML "org.apache.ant" $MODULE_ANT $MODULE_ANT_DEPS


#
# Generate library module for camel
#
MODULE_CAMEL_DEPS=./dependencies/camel.dependencies
createModuleXML "org.apache.camel" $MODULE_CAMEL $MODULE_CAMEL_DEPS

#
# Generate library module for commons.compress
#
MODULE_COMPRESS_DEPS=./dependencies/compress.dependencies
createModuleXML "org.apache.commons.compress" $MODULE_COMPRESS $MODULE_COMPRESS_DEPS


#
# Generate library module for commons.exec
#
MODULE_exec_DEPS=./dependencies/exec.dependencies
createModuleXML "org.apache.commons.exec" $MODULE_EXEC $MODULE_exec_DEPS

#
# Generate library module for commons.math
#
MODULE_CMATH_DEPS=./dependencies/commons_math.dependencies
createModuleXML "org.apache.commons.math" $MODULE_COMMONS_MATH $MODULE_CMATH_DEPS


#
# Generate library module for commons.net
#
MODULE_net_DEPS=./dependencies/net.dependencies
createModuleXML "org.apache.commons.net" $MODULE_NET $MODULE_net_DEPS

#
# Generate library module for commons.vfs
#
MODULE_vfs_DEPS=./dependencies/vfs.dependencies
createModuleXML "org.apache.commons.vfs" $MODULE_VFS $MODULE_vfs_DEPS


#
# Generate library module for helix
#
MODULE_HELIX_DEPS=./dependencies/helix.dependencies
createModuleXML "org.apache.helix" $MODULE_HELIX $MODULE_HELIX_DEPS

#
# Generate library module for jgit
#
MODULE_JGIT_DEPS=./dependencies/jgit.dependencies
createModuleXML "org.eclipse.jgit" $MODULE_JGIT $MODULE_JGIT_DEPS

#
# Generate library module for lucene
#
MODULE_LUCENE_DEPS=./dependencies/lucene.dependencies
createModuleXML "org.apache.lucene" $MODULE_LUCENE $MODULE_LUCENE_DEPS

#
# Generate library module for maven
#
MODULE_MAVEN_DEPS=./dependencies/maven.dependencies
createModuleXML "org.apache.maven" $MODULE_MAVEN $MODULE_MAVEN_DEPS

#
# Generate library module for sonatype.maven
#
MODULE_SMAVEN_DEPS=./dependencies/sonatype_maven.dependencies
createModuleXML "org.sonatype.maven" $MODULE_sonaMAVEN $MODULE_SMAVEN_DEPS

#
# Generate library module for mvel
#
MODULE_MVEL_DEPS=./dependencies/mvel.dependencies
createModuleXML "org.mvel" $MODULE_MVEL $MODULE_MVEL_DEPS

#
# Generate library module for sonatype.plexus
#
MODULE_SPLEXUS_DEPS=./dependencies/sonatype_plexus.dependencies
createModuleXML "org.sonatype.plexus" $MODULE_sonPLEXUS $MODULE_SPLEXUS_DEPS

#
# Generate library module for codehouse.plexus
#
MODULE_CPLEXUS_DEPS=./dependencies/codehouse_plexus.dependencies
createModuleXML "org.codehouse.plexus" $MODULE_codePLEXUS $MODULE_CPLEXUS_DEPS

#
# Generate library module for poi
#
MODULE_POI_DEPS=./dependencies/poi.dependencies
createModuleXML "org.apache.poi" $MODULE_POI $MODULE_POI_DEPS

#
# Generate library module for protobuf
#
MODULE_PBUF_DEPS=./dependencies/protobuf.dependencies
createModuleXML "com.google.protobuf" $MODULE_PROTOBUF $MODULE_PBUF_DEPS

#
# Generate library module for sisu
#
MODULE_SISU_DEPS=./dependencies/sisu.dependencies
createModuleXML "org.sonatype.sisu" $MODULE_SISU $MODULE_SISU_DEPS

#
# Generate library module for solder
#
MODULE_solder_DEPS=./dependencies/solder.dependencies
#createModuleXML "org.jboss.solder" $MODULE_SOLDER $MODULE_solder_DEPS "META-INF"
createModuleXML "org.jboss.solder" $MODULE_SOLDER $MODULE_solder_DEPS

#
# Generate library module for wagon
#
MODULE_WAGON_DEPS=./dependencies/wagon.dependencies
createModuleXML "org.apache.maven.wagon" $MODULE_WAGON $MODULE_WAGON_DEPS

#
# Generate library module for zookeeper
#
MODULE_ZOOKEEPER_DEPS=./dependencies/zookeeper.dependencies
createModuleXML "org.apache.zookeeper" $MODULE_ZOOKEEPER $MODULE_ZOOKEEPER_DEPS

cp $BASE_DIR/layers.conf $DIST_DIR/modules

#
# Create new WAR with dependencies to created modules
echo '**** Generating new KIE-WB WAR ****'
cd $TMP_DIR/kie-wb

cp $BASE_DIR/jboss-deployment-structure.xml $TMP_DIR/kie-wb/WEB-INF

echo '   Applying temporary fixes....'

#
# Workaround until solder problem is solved
#
mkdir $TMP_DIR/kie-wb/META-INF/services

cp $BASE_DIR/patches/cdi-extensions/solder/* $TMP_DIR/kie-wb/META-INF/services
# Workaround Lucene
cp $BASE_DIR/patches/cdi-extensions/lucene/* $TMP_DIR/kie-wb/META-INF/services

# Workaround Solder filter
#cp $BASE_DIR/patches/web.xml $TMP_DIR/kie-wb/WEB-INF

# Other META-INF fixes
#cp -rf $BASE_DIR/patches/META-INF/* $TMP_DIR/kie-wb/META-INF

# Generate the resulting WAR file.
jar cf $DIST_DIR/standalone/deployments/kie-wb.war *

echo '**** Copying dashbuilder WAR ****'
cp $DASHBUILDER_WAR $DIST_DIR/standalone/deployments/jbpm-dashbuilder.war


echo '**** ZIPPING DISTRIBUTION ****'
cd $DIST_DIR
zip -r $DIST_DIR/bpms-modules.zip *
cd $BASE_DIR

echo '**** DISTRIBUTION GENERATED AT dist/bpms-modules.zip ****'

