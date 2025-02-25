#M2_SETTINGS_INPUT='./mmvn-settings.xml'
CI_DIR_PATH='ci/push-mvn-registry'
M2_SETTINGS_INPUT="${CI_DIR_PATH}/mvn-settings-${MVN_PROFILE:=github}.xml"
#M2_SETTINGS_OUTPUT="${M2_SETTINGS_OUTPUT:=~/.m2}"
M2_SETTINGS_OUTPUT="${M2_SETTINGS_OUTPUT:=./}"

POM_INPUT="${CI_DIR_PATH}/mvn-pom-${MVN_PROFILE:=github}.xml"
POM_OUTPUT_TMP="${CI_DIR_PATH}/mvn-pom-tmp.xml"
POM_OUTPUT="./pom.xml"

MVN_PROFILE="${MVN_PROFILE:GITHUB_OR_GITLAB}"
USERNAME="${USERNAME:=yoga1290}"
GIT_REPOSITORY_NAME="${GIT_REPOSITORY_NAME:=spring-core-system}" #TODO
ACCESS_TOKEN="${ACCESS_TOKEN:=TODO}"

############# GENERATE SETTINGS.XML ################
cat $M2_SETTINGS_INPUT | \
    sed "s!\$MVN_PROFILE!$MVN_PROFILE!g" | \
    sed "s!\$GIT_REPOSITORY_NAME!$GIT_REPOSITORY_NAME!g" | \
    sed "s!\$USERNAME!$USERNAME!g" | \
    sed "s!\$GITLAB_PROJECT_ID!$GITLAB_PROJECT_ID!g" | \
    sed "s!\$ACCESS_TOKEN!$ACCESS_TOKEN!g" >"${M2_SETTINGS_OUTPUT}/settings.xml"
chmod 744 "${M2_SETTINGS_OUTPUT}/settings.xml";
#############################################

############# GENERATE POM.XML ################
cat $POM_INPUT | \
    sed "s!\$MVN_PROFILE!$MVN_PROFILE!g" | \
    sed "s!\$GIT_REPOSITORY_NAME!$GIT_REPOSITORY_NAME!g" | \
    sed "s!\$USERNAME!$USERNAME!g" | \
    sed "s!\$GITLAB_PROJECT_ID!$GITLAB_PROJECT_ID!g" | \
    sed "s!\$ACCESS_TOKEN!$ACCESS_TOKEN!g" >"${POM_OUTPUT_TMP}"

POM_OUTPUT_TMP_TEXT_WITH_ESCAPED_LINES=$(cat "$POM_OUTPUT_TMP" | tr '\n' '\f')
sed "s!<distributionManagement>!$POM_OUTPUT_TMP_TEXT_WITH_ESCAPED_LINES!" "$POM_OUTPUT" >"${POM_OUTPUT_TMP}"
cat $POM_OUTPUT_TMP | tr '\f' '\n' > $POM_OUTPUT
#############################################

mvn clean install -s "${M2_SETTINGS_OUTPUT}/settings.xml" -U -DskipTests
