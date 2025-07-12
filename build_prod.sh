#!/bin/bash
rm -rf target
pwd
rm -rf src/main/frontend/generated
rm -rf src/main/bundles/prod.bundle
ls -al src/main/bundles
rm -rf package-lock.json
mvn clean package -Pproduction -DskipTests
