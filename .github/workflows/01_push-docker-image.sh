# Required env var:
##########################
# GITHUB_REPOSITORY
# GITHUB_REPOSITORY_OWNER
# TOKEN_PUBLISH_PACKAGE
# DOCKER_FILE
##########################
IMAGE_NAME=$( echo "${GITHUB_REPOSITORY}" | sed -e "s/.*\///g" )
TAG=$(date +%y.%m).$(git tag -l | wc -l)
echo "IMAGE: $IMAGE_NAME:$TAG"

# https://www.cyberciti.biz/faq/linux-unix-shell-programming-converting-lowercase-uppercase/
REPO=$(echo "${GITHUB_REPOSITORY}" | tr '[:upper:]' '[:lower:]')

# fixes deplocated; see https://github.blog/changelog/2022-10-11-github-actions-deprecating-save-state-and-set-output-commands/#patching-your-actions-and-workflows
### echo "::set-output name=REPO::$REPO"
# echo "REPO=${REPO}" >> $GITHUB_OUTPUT
# echo "TAG=${TAG}" >> $GITHUB_OUTPUT
# echo "IMAGE_NAME=${IMAGE_NAME}" >> $GITHUB_OUTPUT

# see https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#pushing-container-images
DOCKER_IMAGE="ghcr.io/${REPO}:${TAG}"
echo "BUILDING ${DOCKER_IMAGE}"

# NOTE: TOKEN_PUBLISH_PACKAGE is a custom token in "Repo Secrets" 
echo "${TOKEN_PUBLISH_PACKAGE}" | docker login ghcr.io -u "${GITHUB_REPOSITORY_OWNER}" --password-stdin

docker build -t "${DOCKER_IMAGE}" -f ${DOCKER_FILE} .
docker push "${DOCKER_IMAGE}"