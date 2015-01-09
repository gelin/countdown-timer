#!/bin/sh

PROJECT="android-countdown"
DROPBOX="/home/gelin/Dropbox/android@gelin"
BITBUCKET="/gelin/android-countdown/downloads"
. ./upload.credentials

eval $( xgrep -n "android=http://schemas.android.com/apk/res/android" -x "/manifest/@android:versionName|/manifest/@android:versionCode" AndroidManifest.xml -t | \
	sed 's/android:/ANDROID_/' )

#echo $ANDROID_versionName
#echo $ANDROID_versionCode


DEBUGAPK="$PROJECT-debug.apk"

if [ -f "bin/$DEBUGAPK" ]
then

	echo "Uploading $DEBUGAPK"

	cp "bin/$DEBUGAPK" "$DROPBOX"

	UPLOADNAME="$PROJECT-$ANDROID_versionName-$ANDROID_versionCode-debug.apk"
	echo "$UPLOADNAME"
	cp "bin/$DEBUGAPK" "bin/$UPLOADNAME"

    #upload-to-bitbucket.sh "$BITBUCKET_USER" "$BITBUCKET_PASSWORD" "$BITBUCKET" "bin/$UPLOADNAME"

fi


RELEASEAPK="$PROJECT-release.apk"

if [ -f "bin/$RELEASEAPK" ]
then

	echo "Uploading $RELEASEAPK"

	cp "bin/$RELEASEAPK" "bin/$PROJECT.apk"
	cp "bin/$PROJECT.apk" "$DROPBOX"

	UPLOADNAME="$PROJECT-$ANDROID_versionName-$ANDROID_versionCode.apk"
	echo "$UPLOADNAME"
	cp "bin/$RELEASEAPK" "bin/$UPLOADNAME"

    #upload-to-bitbucket.sh "$BITBUCKET_USER" "$BITBUCKET_PASSWORD" "$BITBUCKET" "bin/$UPLOADNAME"

fi
