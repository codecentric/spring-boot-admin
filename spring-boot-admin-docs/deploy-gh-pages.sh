#!/bin/bash
##
# Makes a shallow clone for gh-pages branch, copies the new docs, adds, commits and pushes 'em.
#
# Requires the environment variable GH_TOKEN to be set to a valid GitHub-api-token.
#
# Usage:
# ./deploy-gh-pages.sh <project-version>
#
# project-version    The version folder to use in gh-pages
##
set -o errexit -o nounset

PROJECT_VERSION=$1

GH_URL="https://${GH_TOKEN}@github.com/codecentric/spring-boot-admin.git"
TEMPDIR="$(mktemp -d /tmp/gh-pages.XXX)"

echo "Cloning gh-pages branch..."
git clone --branch gh-pages --single-branch --depth 1 --config user.name="Johannes Edmeier" --config user.email="johannes.edmeier@gmail.com" "$GH_URL" "$TEMPDIR"

if [[ -d "$TEMPDIR"/"${PROJECT_VERSION}-SNAPSHOT" ]]; then
   echo "Removing ${PROJECT_VERSION}-SNAPSHOT..."
   rm -rf "$TEMPDIR"/"${PROJECT_VERSION}-SNAPSHOT"
fi

echo "Copying new docs..."
mkdir -p "$TEMPDIR"/"${PROJECT_VERSION}"
cp -r target/generated-docs/* "$TEMPDIR"/"${PROJECT_VERSION}"/

pushd "$TEMPDIR" >/dev/null
git add --all .

if git diff-index --quiet HEAD; then
  echo "No changes detected."
else
  echo "Commit changes..."
  git commit --message "Docs for ${PROJECT_VERSION}"
  echo "Pushing gh-pages..."
  git push origin gh-pages
fi

popd >/dev/null

rm -rf "$TEMPDIR"
exit 0 
