# Required env var:
##########################
# MVN_PROFILE
# GIT_REPOSITORY_NAME
# USERNAME
# ACCESS_TOKEN
# DOCKER_FILE
##########################
IMAGE_NAME=$( echo "${GIT_REPOSITORY_NAME}" | sed -e "s/.*\///g" )
TAG=$(date +%y.%m).$(git tag -l | wc -l)
echo "IMAGE: $IMAGE_NAME:$TAG"

# https://www.cyberciti.biz/faq/linux-unix-shell-programming-converting-lowercase-uppercase/
REPO=$(echo "${GIT_REPOSITORY_NAME}" | tr '[:upper:]' '[:lower:]')

# fixes deplocated; see https://github.blog/changelog/2022-10-11-github-actions-deprecating-save-state-and-set-output-commands/#patching-your-actions-and-workflows
### echo "::set-output name=REPO::$REPO"
# echo "REPO=${REPO}" >> $GITHUB_OUTPUT
# echo "TAG=${TAG}" >> $GITHUB_OUTPUT
# echo "IMAGE_NAME=${IMAGE_NAME}" >> $GITHUB_OUTPUT

# see https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#pushing-container-images
#DOCKER_IMAGE="ghcr.io/${REPO}:${TAG}"
#echo "BUILDING ${DOCKER_IMAGE}"

sh +x ./ci/push-mvn-registry/mvn-deploy.sh

# NOTE: TOKEN_PUBLISH_PACKAGE is a custom token in "Repo Secrets" 
#echo "${TOKEN_PUBLISH_PACKAGE}" | docker login ghcr.io -u "${GITHUB_REPOSITORY_OWNER}" --password-stdin

#docker build -t "${DOCKER_IMAGE}" -f ${DOCKER_FILE} .
## docker push "${DOCKER_IMAGE}"
#
#echo "RUNNING ${DOCKER_IMAGE}..."
#docker run \
#    --env "MVN_PROFILE=${MVN_PROFILE}" \
#    --env "ACCESS_TOKEN=${ACCESS_TOKEN}" \
#    --env "GIT_REPOSITORY_NAME=${GIT_REPOSITORY_NAME}" \
#    --env "USERNAME=${USERNAME}" \
#    --env "GITLAB_PROJECT_ID=${GITLAB_PROJECT_ID}" \
#    "${DOCKER_IMAGE}"
