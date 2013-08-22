# ------------------------------------------------------------------------------------------
# Script to build EAP 6.1.0 deployment structure for BPMS
# ------------------------------------------------------------------------------------------
createModuleXML() {
	MODULE_NAME=$1
	MODULE_PATH=$2
	MODULE_DEPS_FILE=$3
	MODULE_RESOURCE=$4
	
	# Create the resource-root tags for resource definition.
	MODULE_RESOURCES_STRING=""
	pushd .
	cd $MODULE_PATH/main
	for i in *.jar
	do 
		MODULE_RESOURCES_STRING_2=`sed -e "s;%RESOURCE%;$i;" $TEMPLATE_MODULE_RESOURCE_ROOT`
		MODULE_RESOURCES_STRING="$MODULE_RESOURCES_STRING\n$MODULE_RESOURCES_STRING_2"
	done
	popd

	# Add adittional resource.
	if [[ -n $MODULE_RESOURCE ]] ;then 
		    MODULE_RESOURCES_STRING_2=`sed -e "s;%RESOURCE%;$MODULE_RESOURCE;" $TEMPLATE_MODULE_RESOURCE_ROOT`
		MODULE_RESOURCES_STRING="$MODULE_RESOURCES_STRING\n$MODULE_RESOURCES_STRING_2"
	fi

	# Create the dependency tags for resource definition.
	MODULE_DEPS_STRING=""
	while read module; do
		if [ -n "$module" ]; then
	       		MODULE_DEPS_STRING_2=`sed -e "s;%DEP%;$module;" $TEMPLATE_MODULE_DEPEDENCY`
			MODULE_DEPS_STRING="$MODULE_DEPS_STRING\n$MODULE_DEPS_STRING_2"
		fi
	done < $MODULE_DEPS_FILE

	# Generate the final module.xml from template.
	sed -e "s;%NAME%;$MODULE_NAME;" -e "s;%RESOURCES%;$MODULE_RESOURCES_STRING;" -e "s;%DEPS%;$MODULE_DEPS_STRING;" $TEMPLATE_MODULE > $MODULE_PATH/main/module.xml
}

fixCDIExtensions() {
	SPI_EXTENSION_FILENAME="javax.enterprise.inject.spi.Extension"
	CDI_EXTENSION_PATH=$1
	TARGET_SERVICES_PATH=$2

	pushd .

	#Create the spi extension file to append contents.
	cat /dev/null > $TARGET_SERVICES_PATH/$SPI_EXTENSION_FILENAME

	for dir in $CDI_EXTENSION_PATH/*/
	do
		dir=${dir%*/}
		cd $CDI_EXTENSION_PATH/${dir##*/}
			for file in *
			do
				if [ "$file" == "$SPI_EXTENSION_FILENAME" ]; then
					cat $CDI_EXTENSION_PATH/${dir##*/}/$file >> $TARGET_SERVICES_PATH/$SPI_EXTENSION_FILENAME
				else
					cp -r $CDI_EXTENSION_PATH/${dir##*/}/$file $TARGET_SERVICES_PATH
				fi
			done
		cd ..
	done
	popd
}

createWebappModule() {
	MODULE_FILE=$1
	SRC_DIR=$2
	DST_DIR=$3
	
	MODULE_RESOURCES=`sed '/^\#/d' $MODULE_FILE | grep "module.resources"  | tail -n 1 | sed 's/^.*=//'`

	if [ -n "$MODULE_RESOURCES" ]; then
		export IFS=","
		for res in $MODULE_RESOURCES; do
		  mv $SRC_DIR/$res $DST_DIR
		done
	fi
	
}

createJbossDeploymentStructureFile() {
	MODULE_DEPS=$1
	RESULT_FILE=$2

	MODULES_DEF_STRING=""
	while read resource; do
		if [ -n "$resource" ]; then
			RESOURCE_DEF=`sed -e "s;%MODULE_NAME%;$resource;" $TEMPLATE_JBOSS_DEPLOYMENT_STRUCTURE_MODULE`
			MODULES_DEF_STRING="$MODULES_DEF_STRING\n$RESOURCE_DEF"
		fi
	done < $MODULE_DEPS

	# Generate the jboss-deployment-structure.xml from template.
	sed -e "s;%DEPS%;$MODULES_DEF_STRING;" $TEMPLATE_JBOSS_DEPLOYMENT_STRUCTURE > $RESULT_FILE
}

	
createModule() {
	MODULE_NAME=$1
	MODULE_LOCATION=$2
	MODULE_DEPS_FILE=$3
	MODULE_RESOURCES=$4
	MODULE_PATCHES=$5

	# Setup modules location
	MODULE_DIST_PATH=$DIST_DIR/modules/system/layers/$MODULE_LOCATION

	# Create structure
	mkdir  -p $MODULE_DIST_PATH/main

	# Add jars
	export IFS=","
	for res in $MODULE_RESOURCES; do
	  mv $JARS_DIR/kie-wb/$res $MODULE_DIST_PATH/main
	done

	# Module META-INF patches.
	METAINF=""
	if [ -n "$MODULE_PATCHES" ]; then
		mkdir $MODULE_DIST_PATH/main/META-INF
		cp -r $BASE_DIR/patches/$MODULE_PATCHES $MODULE_DIST_PATH/main/META-INF
		METAINF="META-INF"
	fi

	# Create the module descriptor.
	createModuleXML "$MODULE_NAME" $MODULE_DIST_PATH $BASE_DIR/modules/$MODULE_DEPS_FILE $METAINF	
}

# Program arguments.
if [ $# -ne 2 ];
then
  echo "Missing arguments"
  echo "Usage: ./deploy.sh <path to EAP6.1 war file> <path to EAP 6.1 dashbuilder WAR file>"
  exit 65
fi

# Initialize program variables.
BASE_DIR=`pwd`
DIST_DIR=$BASE_DIR/dist
TMP_DIR=$BASE_DIR/tmp
WAR_DIR=$TMP_DIR/war
JARS_DIR=$TMP_DIR/jars
TEMPLATES_DIR=$BASE_DIR/templates
KIE_WEBAPP_MODULE_FILE=$BASE_DIR/modules/kie-wb-webapp.module
KIE_WEBAPP_MODULE_DEPS_FILE=$BASE_DIR/modules/kie-wb-webapp.dependencies
JBPM_DASH_WEBAPP_MODULE_FILE=$BASE_DIR/modules/jbpm-dashbuilder.module
JBPM_DASH_WEBAPP_MODULE_DEPS_FILE=$BASE_DIR/modules/jbpm-dashbuilder.dependencies
MODULE_LIST_FILE=$BASE_DIR/modules/modules.list
TEMPLATE_JBOSS_DEPLOYMENT_STRUCTURE=$TEMPLATES_DIR/jboss-deployment-structure.template
TEMPLATE_JBOSS_DEPLOYMENT_STRUCTURE_MODULE=$TEMPLATES_DIR/jboss-deployment-structure-module.template
TEMPLATE_MODULE=$TEMPLATES_DIR/module.template
TEMPLATE_MODULE_RESOURCE_ROOT=$TEMPLATES_DIR/module-resource-root.template
TEMPLATE_MODULE_DEPEDENCY=$TEMPLATES_DIR/module-dependency.template

# Initialize program arguments.
DASHBUILDER_WAR=$2
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

echo '**** Cleaning output dirs ****'
rm -rf $DIST_DIR
rm -rf $TMP_DIR

mkdir -p $DIST_DIR
mkdir -p $TMP_DIR
mkdir -p $JARS_DIR/kie-wb
mkdir -p $JARS_DIR/jbpm-dashbuilder
mkdir -p $WAR_DIR

# Create dist deployments strcuture.
mkdir -p $DIST_DIR/standalone/deployments

mkdir -p $DIST_DIR/modules
cp $BASE_DIR/layers.conf $DIST_DIR/modules

# Unzip original kie-wb and jbpm-dashbuilder
#
rm -rf $WAR_DIR
mkdir -p $WAR_DIR/kie-wb
mkdir -p $WAR_DIR/jdpm-dashbuilder
cd $WAR_DIR/kie-wb
jar xf $KIE_WB_WAR
cd $WAR_DIR/jdpm-dashbuilder
jar xf $DASHBUILDER_WAR
cd $BASE_DIR

#
# Clean unrequired libs
#
rm $WAR_DIR/kie-wb/WEB-INF/lib/jsp-api*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/jsp-api*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/commons-bean*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/commons-bean*.jar deleted"
#rm $WAR_DIR/kie-wb/WEB-INF/lib/commons-logging*.jar
#echo $WAR_DIR"/kie-wb/WEB-INF/lib/commons-logging*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/jaxb*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/jaxb*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/jaxrs-api-*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/jaxrs-api-*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/jboss-intercepto*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/jboss-intercepto*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/jta*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/jta*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/log4j*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/log4j*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/xmlschema-core*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/xmlschema-core*.jar deleted"
rm $WAR_DIR/kie-wb/WEB-INF/lib/stax-api*.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/stax-api*.jar deleted"
## Duplicated!!!
rm $WAR_DIR/kie-wb/WEB-INF/lib/freemarker-2.3.8.jar
echo $WAR_DIR"/kie-wb/WEB-INF/lib/freemarker-2.3.8.jar deleted"

# Extract the jars to a temp directory.
mv $WAR_DIR/kie-wb/WEB-INF/lib/*.jar $JARS_DIR/kie-wb
mv $WAR_DIR/jdpm-dashbuilder/WEB-INF/lib/*.jar $JARS_DIR/jbpm-dashbuilder

# Create webapp dynamic module.
echo "Creating webapp dynamic module for kie-wb"
createWebappModule $KIE_WEBAPP_MODULE_FILE $JARS_DIR/kie-wb $WAR_DIR/kie-wb/WEB-INF/lib

echo "Creating webapp dynamic module for jbpm-dashbuilder"
createWebappModule $JBPM_DASH_WEBAPP_MODULE_FILE $JARS_DIR/jbpm-dashbuilder $WAR_DIR/jdpm-dashbuilder/WEB-INF/lib

# Create static modules.
echo "Creating static modules..."
while read module; do
	if [ -n "$module" ]; then

		MODULE_FILE="$BASE_DIR/modules/$module.module"
		MODULE_NAME=`sed '/^\#/d' $MODULE_FILE | grep "module.name"  | tail -n 1 | sed 's/^.*=//'`
		MODULE_RESOURCES=`sed '/^\#/d' $MODULE_FILE | grep "module.resources"  | tail -n 1 | sed 's/^.*=//'`
		MODULE_LOCATION=`sed '/^\#/d' $MODULE_FILE | grep "module.location"  | tail -n 1 | sed 's/^.*=//'`
		MODULE_DEPS="$MODULE_NAME.dependencies"
		MODULE_PATCHES_METAINF=`sed '/^\#/d' $MODULE_FILE | grep "module.patches.metainf"  | tail -n 1 | sed 's/^.*=//'`


		echo "Creating static module: $MODULE_NAME"
		createModule "$MODULE_NAME" "$MODULE_LOCATION" $MODULE_DEPS "$MODULE_RESOURCES" "$MODULE_PATCHES_METAINF"

	fi
done < $MODULE_LIST_FILE


#
# Create new WAR with dependencies to created modules
echo '**** Generating new KIE-WB WAR ****'
cd $WAR_DIR/kie-wb

# Create and add the jboss-deployment-structure.xml to the generated WAR artifact.
createJbossDeploymentStructureFile $KIE_WEBAPP_MODULE_DEPS_FILE $TMP_DIR/kie-wb-jboss-deployment-structure.xml
mv $TMP_DIR/kie-wb-jboss-deployment-structure.xml $WAR_DIR/kie-wb/WEB-INF/jboss-deployment-structure.xml

echo 'Applying temporary fixes....'
#
# Workaround until solder problem is solved
#
mkdir $WAR_DIR/kie-wb/META-INF/services
fixCDIExtensions  $BASE_DIR/patches/cdi-extensions $WAR_DIR/kie-wb/META-INF/services

# Workaround Solder filter
cp $BASE_DIR/patches/web.xml $WAR_DIR/kie-wb/WEB-INF

# Generate the resulting WAR file.
jar cf $DIST_DIR/standalone/deployments/kie-wb.war *

echo '**** Generating new JBPM-DASHBUILDER WAR ****'
cd $WAR_DIR/jdpm-dashbuilder

createJbossDeploymentStructureFile $JBPM_DASH_WEBAPP_MODULE_DEPS_FILE $TMP_DIR/jbpm-dashbuilder-jboss-deployment-structure.xml
mv $TMP_DIR/jbpm-dashbuilder-jboss-deployment-structure.xml $WAR_DIR/jdpm-dashbuilder/WEB-INF/jboss-deployment-structure.xml

jar cf $DIST_DIR/standalone/deployments/jbpm-dashbuilder.war *

echo '**** ZIPPING DISTRIBUTION ****'
cd $DIST_DIR
zip -r $DIST_DIR/bpms-modules.zip *
cd $BASE_DIR

echo '**** DISTRIBUTION GENERATED AT dist/bpms-modules.zip ****'

