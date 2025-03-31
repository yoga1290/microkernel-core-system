CI_DIR_PATH="${CI_DIR_PATH:=ci/push-mvn-registry}"
M2_SETTINGS_OUTPUT="${M2_SETTINGS_OUTPUT:=./}"

sh +x "${CI_DIR_PATH}/mvn-install.sh"

mvn deploy -X -s "${M2_SETTINGS_OUTPUT}/settings.xml" -DskipTests
