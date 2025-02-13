M2_SETTINGS_INPUT='./docker-deploy-mvn-settings.xml'
M2_SETTINGS_OUTPUT="${M2_SETTINGS_OUTPUT:=~/.m2}"


GITHUB_REPOSITORY="${GITHUB_REPOSITORY:=yoga1290/spring-core-system}" #TODO
GITHUB_OWNER="${GITHUB_OWNER:=TODO}" #TODO
GITHUB_TOKEN="${GITHUB_TOKEN:=TODO}" #TODO

cat $M2_SETTINGS_INPUT | \
    sed "s!\$GITHUB_REPOSITORY!$GITHUB_REPOSITORY!g" | \
    sed "s!\$GITHUB_OWNER!$GITHUB_OWNER!g" | \
    sed "s!\$GITHUB_TOKEN!$GITHUB_TOKEN!g" >"${M2_SETTINGS_OUTPUT}/settings.xml"

chmod 744 "${M2_SETTINGS_OUTPUT}/settings.xml";

mvn clean install -U -DskipTests
mvn deploy -DskipTests
