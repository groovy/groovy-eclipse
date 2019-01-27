#!/bin/bash

WD=`pwd`

echo Building groovy-eclipse-batch and deploying or staging to codehaus
cd ${WD}/groovy-eclipse-batch-builder
ant clean publish

echo Deploying/staging groovy-eclipse-compiler to codehaus
cd ${WD}/groovy-eclipse-compiler
mvn clean deploy

#clear maven local to make sure we use the deployed artifacts
# for testing
rm -fr ~/.m2/repository/org/codehaus/groovy/groovy-eclipse-*

echo Running integration tests...
cd ${WD}/groovy-eclipse-compiler-tests
mvn clean integration-test
