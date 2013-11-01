#!/bin/bash

#rm -fr ~/.m2/repository

echo Building groovy-eclipse-batch and installing to maven local
cd groovy-eclipse-batch-builder
ant extract-create-install

echo Installing groovy-eclipse-compiler to maven local
cd ../groovy-eclipse-compiler
mvn clean install

echo Running integration tests...
cd ../groovy-eclipse-compiler-tests
mvn clean install
