cmake_minimum_required(VERSION 3.4.1)

project(cpcam_jni)

message("Deps target directory: ${DEPS_TARGET_DIR}")
set(DEPS_DIR "${DEPS_TARGET_DIR}/${CMAKE_BUILD_TYPE}/${CMAKE_SYSTEM_PROCESSOR}")

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/cmake")

if (${CMAKE_BUILD_TYPE} STREQUAL "Release" OR ${CMAKE_BUILD_TYPE} STREQUAL "RelWithDebInfo")
    message(STATUS "Using build: release")
    set(IS_RELEASE 1)
else()
    message(STATUS "Using build: debug")
    set(IS_RELEASE 0)
endif()

if (${IS_RELEASE})
    set(CMAKE_CXX_VISIBILITY_PRESET hidden)
    set(CMAKE_C_VISIBILITY_PRESET hidden)
    add_compile_options("-flto")
    add_link_options("-flto")
endif()

find_library(log-lib log)
find_package(FFmpeg)

add_library(cpcam_jni SHARED
    FFmpegUtils.cpp
    FFmpegUtils.h
    JniUtils.cpp
    JniUtils.h
    Log.h
    PixFmt.h
    VideoConfig.cpp
    VideoConfig.h

    ./output/FFmpegOutput.cpp
    ./output/FFmpegOutput.h
    ./output/FFmpegOutput_jni.cpp

    ./stream/FFmpegVideoStream.cpp
    ./stream/FFmpegVideoStream.h
    ./stream/FFmpegVideoStream_jni.cpp
    ./stream/FrameData.h
)

target_include_directories(cpcam_jni PRIVATE .)

target_compile_features(cpcam_jni PRIVATE cxx_std_20)

target_compile_options(cpcam_jni PRIVATE
    $<$<CXX_COMPILER_ID:MSVC>:/W4 /wd4244 /wd4267 /wd4127>
    $<$<NOT:$<CXX_COMPILER_ID:MSVC>>:
        -Wall -Wextra -Wpedantic
        -Werror=switch>
)

target_link_libraries(cpcam_jni PUBLIC
    ${log-lib}
    android
    mediandk

    FFmpeg::libavcodec
    FFmpeg::libavformat
    FFmpeg::libavutil
    FFmpeg::libswscale
)
