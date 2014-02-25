#!/bin/bash

# Simple script to quickly check for changes between groovy versions, limited to just those changes that are
# in files patched by us.

# To use this script... define the following variables according to your environment

#The tag of the previous new groovy version
TAG_OLD=GROOVY_2_2_1
TAG_NEW=GRECLIPS_2_2_2

#Path to the root of groovy-core git clone
GROOVY_CORE_REPO=/home/kdvolder/git/groovy-core

# Run the script from the dir you found it in with
# bash <script-name>

### Shouldn't need to change anything below this line
cd src
INTERESTING_FILES=`find -print | sort`

cd $GROOVY_CORE_REPO/src/main

for f in $INTERESTING_FILES
do
    # Filter further: Only files that exist in both trees are actually interesting.
    if [ -f $f ]
    then
        echo "================" $f
        git diff $TAG_OLD $TAG_NEW -- $f
    fi
done
