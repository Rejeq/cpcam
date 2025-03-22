cmake_minimum_required(VERSION 3.4.1)

project(cpcam_bench_jni)

add_library(cpcam_bench_jni SHARED
        ./NativeBuffer.cpp
)
