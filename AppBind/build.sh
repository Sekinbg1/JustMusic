#!/bin/bash
export ANDROID_HOME="/home/bill/Android/Sdk"
export ANDROID_PLATFORM=21
export NDK="${ANDROID_HOME}/ndk/26.1.10909125"
#ANDROID_NDK=/home/bill/sdks/android/ndk/21.4.7075529
export AL_PATH="/home/bill/IdeaProjects/AudioSystem/AppBind/soundMixer/openal-soft-1.23.1"
export CGO_CFLAGS="-O2 -g -I ${AL_PATH}/include"
export CGO_CPPFLAGS="-I ${AL_PATH}/include"
export CGO_CXXFLAGS="-O2 -g -I ${AL_PATH}/include"
export ANDROID_NDK_HOME=${NDK}
export CGO_LDFLAGS='-s -O2 -g'
export QUIC_GO_LOG_LEVEL=DEBUG

function mkso() {
    mkdir -p ${AL_PATH}/build/arm32
    mkdir -p ${AL_PATH}/build/aarch64
    mkdir -p ${AL_PATH}/build/x86
    mkdir -p ${AL_PATH}/build/x86_64

    make -C ${AL_PATH}/build/arm32 clean
    make -C ${AL_PATH}/build/aarch64 clean
    make -C ${AL_PATH}/build/x86 clean
    make -C ${AL_PATH}/build/x86_64 clean

    cmake -D CMAKE_TOOLCHAIN_FILE=/home/bill/Android/Sdk/ndk/26.1.10909125/build/cmake/android.toolchain.cmake\
          -D ANDROID_ABI="armeabi-v7a"\
          -D ANROID_PLATFORM=android-$ANDROID_PLATFORM\
          -S ${AL_PATH}\
          -B ${AL_PATH}/build/arm32
    cmake -D CMAKE_TOOLCHAIN_FILE=/home/bill/Android/Sdk/ndk/26.1.10909125/build/cmake/android.toolchain.cmake\
          -D ANDROID_ABI="arm64-v8a"\
          -D ANROID_PLATFORM=android-$ANDROID_PLATFORM\
          -S ${AL_PATH}\
          -B ${AL_PATH}/build/aarch64
    cmake -D CMAKE_TOOLCHAIN_FILE=/home/bill/Android/Sdk/ndk/26.1.10909125/build/cmake/android.toolchain.cmake\
          -D ANDROID_ABI="x86"\
          -D ANROID_PLATFORM=android-$ANDROID_PLATFORM\
          -S ${AL_PATH}\
          -B ${AL_PATH}/build/x86
    cmake -D CMAKE_TOOLCHAIN_FILE=/home/bill/Android/Sdk/ndk/26.1.10909125/build/cmake/android.toolchain.cmake\
          -D ANDROID_ABI="x86_64"\
          -D ANROID_PLATFORM=android-$ANDROID_PLATFORM\
          -S ${AL_PATH}\
          -B ${AL_PATH}/build/x86_64


    make -C ${AL_PATH}/build/arm32 -j8
    make -C ${AL_PATH}/build/aarch64 -j8
    make -C ${AL_PATH}/build/x86 -j8
    make -C ${AL_PATH}/build/x86_64 -j8
    rm -rf libs/*
    mkdir -p libs/armeabi-v7a
    mkdir -p libs/arm64-v8a
    mkdir -p libs/x86
    mkdir -p libs/x86_64
    cp ${AL_PATH}/build/arm32/libopenal.so libs/armeabi-v7a
    cp ${AL_PATH}/build/aarch64/libopenal.so libs/arm64-v8a
    cp ${AL_PATH}/build/x86/libopenal.so libs/x86
    cp ${AL_PATH}/build/x86_64/libopenal.so libs/x86_64
    rm -r ../newjustpiano/app/libs/*
    cp -av libs/* ../newjustpiano/app/libs
}
if [ $# -eq 1 ]; then
    if [ "$1" == "-c" ]; then
      mkso
    fi
elif [ $# -gt 1 ]; then
    echo "只能接收一个参数"
fi

gomobile bind -target android -androidapi=$ANDROID_PLATFORM -o ../newjustpiano/app/aars/AppBind.aar  AppBind/soundMixer AppBind/Client

