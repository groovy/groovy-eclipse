#!/bin/bash

echo Building groovy-eclipse-batch and deploying or staging to codehaus
cd groovy-eclipse-batch-builder
#ant extract-create-install
#ant extract-create-publish

echo Deploying/staging groovy-eclipse-compiler to codehaus
cd ../groovy-eclipse-compiler
mvn clean deploy

#clear maven local to make sure we use the deployed artifacts
# for testing
rm -fr ~/.m2/repository

echo Running integration tests...
cd ../groovy-eclipse-compiler-tests
mvn clean install
