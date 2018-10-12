#!/bin/bash

PATCH_URL="https://github.com/mimers/AndroidStudio-XCC-Patch/archive/android-studio-cc-patch.jar"
CONTENT_JAR="Contents/plugins/android/lib/sdk-common.jar"
PATCHED_JAR="Contents/plugins/android/lib/sdk-common-patched.jar"

if [[ $1 == '-u' ]]; then
	for i in /Applications/Android\ Studio*.app; do
		CONTENT_DIR=$(dirname "$i/$CONTENT_JAR")
		echo "undo patch for $i"
		if [[ ! -f "$i/$PATCHED_JAR" ]]; then
			echo "not patched."
			continue
		fi
		if [[ ! -f "$CONTENT_DIR/sdk-common.jar.bak" ]]; then
			echo "backup file doesn't exist"
			continue
		fi
		rm "$i/$PATCHED_JAR" && echo "removed $i/$PATCHED_JAR"
		mv -v "$CONTENT_DIR/sdk-common.jar.bak" "$i/$CONTENT_JAR" && echo ""
	done
	exit 0
fi
patch_count=0
for i in /Applications/Android\ Studio*.app; do
	if [[ ! -f android-studio-cc-patch.jar ]]; then
		echo "download patch"
		if [[ ! `which wget` ]]; then
			curl -L $PATCH_URL -o android-studio-cc-patch.jar
		else
			wget $PATCH_URL -O android-studio-cc-patch.jar
		fi
	fi
	if [[ -f "$i/$PATCHED_JAR" ]]; then
		echo $i already patched.
		continue
	fi
	if [[ ! -f "$i/$CONTENT_JAR" ]]; then
		echo $i/$CONTENT_JAR doesn\'t exist.
		continue
	fi
	CONTENT_DIR=$(dirname "$i/$CONTENT_JAR")
	echo "apply patch for $i"
	cp "$i/$CONTENT_JAR" "$CONTENT_DIR/sdk-common.jar.bak"
	mv "$i/$CONTENT_JAR" "$i/$PATCHED_JAR"
	java -jar android-studio-cc-patch.jar "$i/$PATCHED_JAR" && printf "\e[1;33mdone\e[0m\n\n"
	patch_count=$((patch_count+1))
done

test $patch_count -gt 0 && printf "you can exec \e[1;33m'$(basename $0) -u'\e[0m to undo patch :)\n"
