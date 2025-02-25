# Required env var:
##########################
# MVN_PROFILE
# GIT_REPOSITORY_NAME
# USERNAME
# ACCESS_TOKEN
# DOCKER_FILE
# PWD_PATH=.
##########################
IMAGE_NAME=$( echo "${GIT_REPOSITORY_NAME}" | sed -e "s/.*\///g" )
TAG=$(date +%y.%m).$(git tag -l | wc -l)
echo "IMAGE: $IMAGE_NAME:$TAG"

# https://www.cyberciti.biz/faq/linux-unix-shell-programming-converting-lowercase-uppercase/
REPO=$(echo "${GIT_REPOSITORY_NAME}" | tr '[:upper:]' '[:lower:]')

# fixes deplocated; see https://github.blog/changelog/2022-10-11-github-actions-deprecating-save-state-and-set-output-commands/#patching-your-actions-and-workflows
### echo "::set-output name=REPO::$REPO"

# see https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#pushing-container-images
DOCKER_IMAGE="ghcr.io/${REPO}:${TAG}"
echo "BUILDING ${DOCKER_IMAGE}"

docker build -t "${DOCKER_IMAGE}" -f ${DOCKER_FILE} .

echo "RUNNING ${DOCKER_IMAGE}..."
docker run \
    --env "PWD_PATH=." \
    --env "MVN_PROFILE=${MVN_PROFILE}" \
    --env "ACCESS_TOKEN=${ACCESS_TOKEN}" \
    --env "GIT_REPOSITORY_NAME=${GIT_REPOSITORY_NAME}" \
    --env "USERNAME=${USERNAME}" \
    --env "GITLAB_PROJECT_ID=${GITLAB_PROJECT_ID}" \
    "${DOCKER_IMAGE}"

# NOTE: ACCESS_TOKEN is a custom token in "Repo Secrets"
#echo "${ACCESS_TOKEN}" | docker login ghcr.io -u "${GITHUB_REPOSITORY_OWNER}" --password-stdin
# docker push "${DOCKER_IMAGE}"
