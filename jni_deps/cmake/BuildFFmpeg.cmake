set(FFMPEG_SOURCE_DIR "${DOWNLOAD_DIR}/ffmpeg")

set(FFMPEG_C_FLAGS "--target=${ANDROID_LLVM_TRIPLE} -I${CMAKE_INSTALL_PREFIX}/mbedtls/include")
set(FFMPEG_LD_FLAGS "--target=${ANDROID_LLVM_TRIPLE} -L${CMAKE_INSTALL_PREFIX}/mbedtls/lib")
set(FFMPEG_EXTRA_CONFIG "")

if(${CMAKE_ANDROID_ARCH_ABI} STREQUAL x86)
    list(APPEND FFMPEG_EXTRA_CONFIG --disable-asm)
elseif(${CMAKE_ANDROID_ARCH_ABI} STREQUAL x86_64)
    list(APPEND FFMPEG_EXTRA_CONFIG
        --disable-asm
        --x86asmexe="\"${ANDROID_TOOLCHAIN_ROOT}/bin/yasm\""
    )
endif()

if (${IS_RELEASE})
    # Build static libraries in release mode
    list(APPEND FFMPEG_EXTRA_CONFIG
            --enable-lto
            --enable-small
            --disable-debug
    )

    string(APPEND FFMPEG_C_FLAGS " -fvisibility=hidden")
else()
    # Build shared libraries for faster incremental builds
    list(APPEND FFMPEG_EXTRA_CONFIG
        --enable-shared
        --enable-pic
        --disable-static
        --enable-debug
        --disable-stripping
        --disable-optimizations
    )

    string(APPEND FFMPEG_C_FLAGS " -Og")
endif()

set(CONFIGURE_COMMAND ${FFMPEG_SOURCE_DIR}/src/ffmpeg/configure
    --arch=${CMAKE_SYSTEM_PROCESSOR}
    --sysroot=${CMAKE_SYSROOT}
    --prefix=${CMAKE_INSTALL_PREFIX}/ffmpeg
    --target-os=android

    --cc=${CMAKE_C_COMPILER}
    --ar=${ANDROID_AR}
    --ranlib=${CMAKE_RANLIB}
    --as=${CMAKE_C_COMPILER}
    --strip=${ANDROID_TOOLCHAIN_ROOT}/bin/llvm-strip${ANDROID_TOOLCHAIN_SUFFIX}
    --nm=${ANDROID_TOOLCHAIN_ROOT}/bin/llvm-nm${ANDROID_TOOLCHAIN_SUFFIX}

    --extra-cflags="'${FFMPEG_C_FLAGS}'"
    --extra-ldflags="'${FFMPEG_LD_FLAGS}'"

    --enable-cross-compile

    --disable-programs
    --disable-doc
    --disable-autodetect
    --disable-everything
    --disable-swscale-alpha
    --enable-jni
    --enable-version3

    --enable-mediacodec

    --enable-encoder=h264_mediacodec
    --enable-bsf=h264_metadata

    --enable-encoder=vp8_mediacodec

    --enable-encoder=vp9_mediacodec
    --enable-bsf=vp9_metadata

    --enable-encoder=av1_mediacodec
    --enable-bsf=av1_metadata

    --enable-encoder=mpeg4_mediacodec

    --enable-encoder=hevc_mediacodec
    --enable-bsf=hevc_metadata

    --enable-encoder=mjpeg

    --enable-muxer=mpegts
    --enable-muxer=mjpeg
    --enable-muxer=smjpeg
    --enable-muxer=rtsp
    --enable-muxer=rtp
    --enable-muxer=rtp_mpegts
    --enable-muxer=hls

    --enable-mbedtls

    --enable-protocol=tcp
    --enable-protocol=udp
    --enable-protocol=tls
    --enable-protocol=http
    --enable-protocol=https
    --enable-protocol=rtmp
    --enable-protocol=rtp
    --enable-protocol=srtp
    --enable-protocol=hls
    --enable-protocol=file
    --enable-protocol=ftp
    --enable-protocol=gopher
    --enable-protocol=gophers

    ${FFMPEG_EXTRA_CONFIG}
)

message(STATUS "FFmpeg configure command:")
string(REPLACE ";" " " CONFIGURE_COMMAND_WITH_SPACES "${CONFIGURE_COMMAND}")
message("${CONFIGURE_COMMAND_WITH_SPACES}")

message(STATUS "Generator: ${CMAKE_GENERATOR}")

if (NOT "${CMAKE_GENERATOR}" STREQUAL "Unix Makefiles")
    message(FATAL_ERROR "FFmpeg can be builded only with make generator")
endif()

ExternalProject_Add(ffmpeg
    URL "https://ffmpeg.org/releases/ffmpeg-7.1.1.tar.gz"
    URL_HASH SHA256=9a6e57a446b671012612aaeb9df5126794d5ac8f2015ca220934f99a6a4e0601

    DEPENDS mbedtls

    CONFIGURE_COMMAND ${CONFIGURE_COMMAND}
    BUILD_COMMAND ${CMAKE_MAKE_PROGRAM} clean && ${CMAKE_MAKE_PROGRAM} -j${TOTAL_JOBS}
    INSTALL_COMMAND ${CMAKE_MAKE_PROGRAM} install

    PREFIX ${FFMPEG_SOURCE_DIR}
    ${EXTRA_PROJECT_ARGS}
)
