#!/bin/bash
if [ -e "$HOME/.keytool/springsource.jks" ]
then
echo Signing $1
#jarsigner -tsa https://timestamp.geotrust.com/tsa -keystore ~/.keytool/springsource.jks -keypass $KEY_PASSWORD -storepass $STORE_PASSWORD $1 vmware
jarsigner -keystore ~/.keytool/springsource.jks -keypass $bamboo_signing_key_password -storepass $bamboo_signing_store_password $1 vmware
fi
