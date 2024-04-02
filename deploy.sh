M2_SETTINGS_INPUT='./deploy-mvn-settings.xml'
M2_SETTINGS_OUTPUT="${M2_SETTINGS_OUTPUT:=~/.m2}"


GITHUB_REPOSITORY="${GITHUB_REPOSITORY:=spring-core-system}" #TODO
GITHUB_OWNER="${GITHUB_OWNER:=TODO}" #TODO
GITHUB_TOKEN="${GITHUB_TOKEN:=TODO}" #TODO

mkdir -p "${M2_SETTINGS_OUTPUT}";
chmod -R 744 "${M2_SETTINGS_OUTPUT}";
touch "${M2_SETTINGS_OUTPUT}/settings.xml";
chmod 744 "${M2_SETTINGS_OUTPUT}/settings.xml";

ls -l $M2_SETTINGS_OUTPUT;

cat $M2_SETTINGS_INPUT | \
    sed "s!\$GITHUB_REPOSITORY!$GITHUB_REPOSITORY!g" | \
    sed "s!\$GITHUB_OWNER!$GITHUB_OWNER!g";

cat $M2_SETTINGS_INPUT | \
    sed "s!\$GITHUB_REPOSITORY!$GITHUB_REPOSITORY!g" | \
    sed "s!\$GITHUB_OWNER!$GITHUB_OWNER!g" | \
    sed "s!\$GITHUB_TOKEN!$GITHUB_TOKEN!g" >"${M2_SETTINGS_OUTPUT}/settings.xml"

chmod 744 "${M2_SETTINGS_OUTPUT}/settings.xml";
#mvn clean install ### moved to Dockerfile's RUN
mvn clean install -U -DskipTests
mvn deploy -DskipTests
