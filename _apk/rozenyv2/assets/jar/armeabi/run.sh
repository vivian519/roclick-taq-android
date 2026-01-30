#!/bin/sh

chmod 777 /data/data/com.iceflow.rozenyv2/files/jar/armeabi/touchSpriteCore

#点量云手机目录
default_dl_path="/mnt/user_data/rozenyv2"
#默认的目录
default_path="/sdcard/rozenyv2"

if [ -d $default_dl_path ]; then

    /data/data/com.iceflow.rozenyv2/files/jar/armeabi/touchSpriteCore   com.iceflow.rozenyv2   $default_dl_path
else

    /data/data/com.iceflow.rozenyv2/files/jar/armeabi/touchSpriteCore   com.iceflow.rozenyv2   $default_path
fi
