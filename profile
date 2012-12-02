# To create new project
# android create project -t android-7 -p project -k com.jeffboody.SPPMirror -a SPPMirror
#
# To update project for new SDK
# android update project -p project -t android-7

# App
export APP=SPPMirror

# Update SDK/NDK to point to the Android SDK
SDK=/home/jeffboody/android/android-sdk

#-- DON'T CHANGE BELOW LINE --

export PATH=$SDK/tools:$SDK/platform-tools:$PATH
echo "sdk.dir=$SDK" > project/local.properties

export TOP=`pwd`
alias croot='cd $TOP'
