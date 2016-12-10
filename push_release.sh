BRANCH="master"

# Are we on the right branch?
if [ "$TRAVIS_BRANCH" = "$BRANCH" ]; then
  
  # Is this not a Pull Request?
  if [ "$TRAVIS_PULL_REQUEST" = false ]; then
    
    # Is this not a build which was triggered by setting a new tag?
    if [ -z "$TRAVIS_TAG" ]; then
      echo -e "Starting to push release.\n"

      git config --global user.email "travis@travis-ci.org"
      git config --global user.name "Travis"

      # Add tag and push to master.
      git tag -a v3.0.29-${TRAVIS_BUILD_NUMBER} -m "Travis build v3.0.29-${TRAVIS_BUILD_NUMBER} pushed a tag."
      git push origin --tags
      git fetch origin

      echo -e "Done pushing the release.\n"
  fi
  fi
fi
