cmake_minimum_required(VERSION 3.16)

if(POLICY CMP0135)
    cmake_policy(SET CMP0135 NEW)
endif()

project(jni_deps)

set(DOWNLOAD_DIR "${CMAKE_CURRENT_BINARY_DIR}" CACHE PATH "Directory where all sources would be downloaded")
set(TOTAL_JOBS "4" CACHE STRING "Total jobs that would be used to build deps")


if (${CMAKE_BUILD_TYPE} STREQUAL "Release" OR ${CMAKE_BUILD_TYPE} STREQUAL "RelWithDebInfo")
    message(STATUS "Using build: release")
    set(IS_RELEASE 1)
else()
    message(STATUS "Using build: debug")
    set(IS_RELEASE 0)
endif()


list(APPEND CMAKE_MODULE_PATH "${PROJECT_SOURCE_DIR}/cmake")
set(EXTRA_PROJECT_ARGS DOWNLOAD_NO_PROGRESS true)

message(STATUS "Using download directory: '${DOWNLOAD_DIR}'")
list(APPEND EXTRA_PROJECT_ARGS DOWNLOAD_DIR ${DOWNLOAD_DIR})

message(STATUS "Using jobs: '${TOTAL_JOBS}'")

include(ExternalProject)
include(BuildMbedTLS)
include(BuildFFmpeg)
