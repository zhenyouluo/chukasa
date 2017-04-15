#!/bin/sh

TEMPORARY_DIRECTORY="/tmp/mokyu"
if [ ! -d "$TEMPORARY_DIRECTORY" ]; then
  mkdir $TEMPORARY_DIRECTORY
fi
cd $TEMPORARY_DIRECTORY
wget https://github.com/video-dev/hls.js/archive/v$1.tar.gz -O v$1.tar.gz
tar zxvf v$1.tar.gz
cd hls.js-$1/dist
cp hls.js $2
cp hls.min.js $2
cp hls.light.js $2
cp hls.light.min.js $2