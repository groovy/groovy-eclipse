#!/bin/bash

rm -fr ~/.m2/repository/org/codehaus/groovy

echo Building groovy-eclipse-batch artifacts
cd groovy-eclipse-batch-builder
ant clean install

echo Building groovy-eclipse-compiler artifacts
cd ../groovy-eclipse-compiler
mvn clean install

echo Running integration tests...
cd ../groovy-eclipse-compiler-tests
mvn clean integration-test
