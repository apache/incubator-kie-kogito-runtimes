#!/bin/bash

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
		MODULE_RESOURCES_STRING="$MODULE_RESOURCES_STRING $MODULE_RESOURCES_STRING_2"
	done
	popd

	# Add adittional resource.
	if [[ -n $MODULE_RESOURCE ]] ;then 
		    MODULE_RESOURCES_STRING_2=`sed -e "s;%RESOURCE%;$MODULE_RESOURCE;" $TEMPLATE_MODULE_RESOURCE_ROOT`
		MODULE_RESOURCES_STRING="$MODULE_RESOURCES_STRING $MODULE_RESOURCES_STRING_2"
	fi

	# Create the dependency tags for resource definition.
	MODULE_DEPS_STRING=""
	while read module; do
		if [ -n "$module" ]; then
			#Only create the dependence to the module if it exists.
			EXIST_MODULE=$(existModule "$module")
			if [ "$EXIST_MODULE" == "1" ]; then
		       		MODULE_DEPS_STRING_2=`sed -e "s;%DEP%;$module;" $TEMPLATE_MODULE_DEPEDENCY`
				MODULE_DEPS_STRING="$MODULE_DEPS_STRING $MODULE_DEPS_STRING_2"
			else
				echo "Not adding dependency from $MODULE_NAME to $module, due to $module does not exist."
			fi
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

	cd $CDI_EXTENSION_PATH
	export IFS=$ORIGINAL_IFS	
	for extdir in $(ls -d -- *) ; do
		HAS_CDI_EXT=$(hasCDIExtension "$extdir")
		cd $CDI_EXTENSION_PATH/$extdir
		if [ "$HAS_CDI_EXT" == "1" ]; then
			for file in *
			do
				if [ "$file" == "$SPI_EXTENSION_FILENAME" ]; then
					cat $CDI_EXTENSION_PATH/$extdir/$file >> $TARGET_SERVICES_PATH/$SPI_EXTENSION_FILENAME
				else
					cp -r $CDI_EXTENSION_PATH/$extdir/$file $TARGET_SERVICES_PATH
				fi
			done
		fi
	done

	popd
}


hasCDIExtension() {
	CDI_EXTENSION=$1
	EXISTS="0"

	MODULE_PATCHES_CDI_EXTENSIONS=`sed '/^\#/d' $KIE_WEBAPP_MODULE_FILE | grep "module.patches.cdi-extensions"  | tail -n 1 | sed 's/^.*=//'`

	
	export IFS=","
	for cdiext in $MODULE_PATCHES_CDI_EXTENSIONS; do
		if [ "$cdiext" == "$CDI_EXTENSION" ]; then
			EXISTS="1"
		fi	
	done

	echo "$EXISTS"
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
			MODULES_DEF_STRING="$MODULES_DEF_STRING $RESOURCE_DEF"
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

	echo "Creating static module: $MODULE_NAME"

	# Setup modules location
	MODULE_DIST_PATH=$DIST_DIR/modules/system/layers/$MODULE_LOCATION

	# Create structure
	mkdir  -p $MODULE_DIST_PATH/main

	# Add jars
	export IFS=","
	for res in $MODULE_RESOURCES; do
		# First find the resource in the kie-wb/kie-drools-wb jars
		RESOURCE_FILE=$JARS_DIR/wb-war/$res
		RESOURCES_ARRAY=(`find $JARS_DIR/wb-war/ -name "$res"`)
		if [ "$RESOURCES_ARRAY" == "" ]; then
			# If not found, find the resource in jbpm-dashbuilder jars
			RESOURCE_FILE=$JARS_DIR/jbpm-dashbuilder/$res
			RESOURCES_ARRAY=(`find $JARS_DIR/jbpm-dashbuilder/ -name "$res"`)
		fi
		if [ "$RESOURCES_ARRAY" != "" ]; then
			mv $RESOURCE_FILE $MODULE_DIST_PATH/main
			echo "$res *** $MODULE_NAME" >> $TMP_DIR/module-resources-mapping.out
		else
			echo "No resource matched for pattern $res"
		fi
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

existModule() {
	MODULE_NAME_TO_CHECK=$1
	EXISTS="0"

	while read module; do
		if [ -n "$module" ]; then
			if [ "$module" == "$MODULE_NAME_TO_CHECK" ]; then
				EXISTS="1"
			fi
		fi
	done < $MODULE_LIST_FILE

	while read module; do
		if [ -n "$module" ]; then
			if [ "$module" == "$MODULE_NAME_TO_CHECK" ]; then
				EXISTS="1"
			fi
		fi
	done < $EAP_MODULE_LIST_FILE

	echo "$EXISTS"
}

# Program arguments.
if [ $# -ne 5 ];
then
  echo "Missing arguments"
  echo "Usage: ./deploy.sh <modules.list file> <eap modules.list file> <webapp module name> <path to EAP6.1 war file> <path to EAP 6.1 dashbuilder WAR file> "
  exit 65
fi

# Initialize program variables.
BASE_DIR=`pwd`
DIST_DIR=$BASE_DIR/dist
TMP_DIR=$BASE_DIR/tmp
WAR_DIR=$TMP_DIR/war
JARS_DIR=$TMP_DIR/jars
TEMPLATES_DIR=$BASE_DIR/templates
JBPM_DASH_WEBAPP_MODULE_FILE=$BASE_DIR/modules/jbpm-dashbuilder.module
JBPM_DASH_WEBAPP_MODULE_DEPS_FILE=$BASE_DIR/modules/jbpm-dashbuilder.dependencies
TEMPLATE_JBOSS_DEPLOYMENT_STRUCTURE=$TEMPLATES_DIR/jboss-deployment-structure.template
TEMPLATE_JBOSS_DEPLOYMENT_STRUCTURE_MODULE=$TEMPLATES_DIR/jboss-deployment-structure-module.template
TEMPLATE_MODULE=$TEMPLATES_DIR/module.template
TEMPLATE_MODULE_RESOURCE_ROOT=$TEMPLATES_DIR/module-resource-root.template
TEMPLATE_MODULE_DEPEDENCY=$TEMPLATES_DIR/module-dependency.template
ORIGINAL_IFS=$IFS

# Initialize program arguments.
DASHBUILDER_WAR=$5
KIE_WB_WAR=$4
MODULE_LIST_FILE=$1
EAP_MODULE_LIST_FILE=$2
KIE_WEBAPP_MODULE_FILE=$BASE_DIR/modules/$3.module
KIE_WEBAPP_MODULE_DEPS_FILE=$BASE_DIR/modules/$3.dependencies

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
mkdir -p $JARS_DIR/wb-war
mkdir -p $JARS_DIR/jbpm-dashbuilder
mkdir -p $WAR_DIR

# Create dist deployments strcuture.
mkdir -p $DIST_DIR/standalone/deployments

mkdir -p $DIST_DIR/modules
cp $BASE_DIR/layers.conf $DIST_DIR/modules

# Unzip original kie-wb/kie-drools-wb and jbpm-dashbuilder
#
rm -rf $WAR_DIR
mkdir -p $WAR_DIR/wb-war
mkdir -p $WAR_DIR/jdpm-dashbuilder
cd $WAR_DIR/wb-war
jar xf $KIE_WB_WAR
cd $WAR_DIR/jdpm-dashbuilder
jar xf $DASHBUILDER_WAR
cd $BASE_DIR

#
# Clean unrequired libs
#
rm $WAR_DIR/wb-war/WEB-INF/lib/jaxb*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/jaxb*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/jaxrs-api-*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/jaxrs-api-*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/jboss-intercepto*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/jboss-intercepto*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/jta*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/jta*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/log4j*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/log4j*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/xmlschema-core*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/xmlschema-core*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/stax-api*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/stax-api*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/jboss-jsp-api*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/jboss-jsp-api*.jar deleted"
rm $WAR_DIR/wb-war/WEB-INF/lib/jms*.jar
echo $WAR_DIR"/wb-war/WEB-INF/lib/jms*.jar deleted"

# Extract the jars to a temp directory.
mv $WAR_DIR/wb-war/WEB-INF/lib/*.jar $JARS_DIR/wb-war
mv $WAR_DIR/jdpm-dashbuilder/WEB-INF/lib/*.jar $JARS_DIR/jbpm-dashbuilder

# Create webapp dynamic module.
echo "Creating webapp dynamic module for kie-wb"
createWebappModule $KIE_WEBAPP_MODULE_FILE $JARS_DIR/wb-war $WAR_DIR/wb-war/WEB-INF/lib

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

		createModule "$MODULE_NAME" "$MODULE_LOCATION" $MODULE_DEPS "$MODULE_RESOURCES" "$MODULE_PATCHES_METAINF"

	fi
done < $MODULE_LIST_FILE


#
# Create new WAR with dependencies to created modules
echo '**** Generating new KIE-WB/KIE-DROOLS-WB WAR ****'
cd $WAR_DIR/wb-war

# Create and add the jboss-deployment-structure.xml to the generated WAR artifact.
createJbossDeploymentStructureFile $KIE_WEBAPP_MODULE_DEPS_FILE $TMP_DIR/wb-war-jboss-deployment-structure.xml
mv $TMP_DIR/wb-war-jboss-deployment-structure.xml $WAR_DIR/wb-war/WEB-INF/jboss-deployment-structure.xml

echo 'Applying temporary fixes....'
#
# Workaround until solder problem is solved
#
mkdir $WAR_DIR/wb-war/META-INF/services
fixCDIExtensions  $BASE_DIR/patches/cdi-extensions $WAR_DIR/wb-war/META-INF/services

# Workaround Solder filter
cp $BASE_DIR/patches/web.xml $WAR_DIR/wb-war/WEB-INF

# Generate the resulting WAR file.
jar cf $DIST_DIR/standalone/deployments/wb-war.war *

echo '**** Generating new JBPM-DASHBUILDER WAR ****'
cd $WAR_DIR/jdpm-dashbuilder

createJbossDeploymentStructureFile $JBPM_DASH_WEBAPP_MODULE_DEPS_FILE $TMP_DIR/jbpm-dashbuilder-jboss-deployment-structure.xml
mv $TMP_DIR/jbpm-dashbuilder-jboss-deployment-structure.xml $WAR_DIR/jdpm-dashbuilder/WEB-INF/jboss-deployment-structure.xml

jar cf $DIST_DIR/standalone/deployments/jbpm-dashbuilder.war *

echo '**** ZIPPING DISTRIBUTION ****'
cd $DIST_DIR
zip -rq $DIST_DIR/bpms-modules.zip *
cd $BASE_DIR

echo '**** DISTRIBUTION GENERATED AT dist/bpms-modules.zip ****'

