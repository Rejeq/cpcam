#!/usr/bin/env bash

set -e

if [ -z "$ANDROID_NDK" ]; then
    echo "Please set ANDROID_NDK to the Android NDK folder"
    exit 1
fi

cd "$(dirname "$0")"

ANDROID_VERSION=21
BUILD_ABIS="armeabi-v7a,x86,x86_64,arm64-v8a"
BUILD_DEBUG=true
BUILD_DIR=./build
BUILD_RELEASE=true
CMAKE=cmake
DOWNLOAD_DIR=./downloads
FLEXIBLE_PAGE_SIZE=true
PREFIX_DIR=./targets
TOTAL_JOBS=$(nproc)

function print_help {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --abi          Comma separated list of target ABIs [default: $BUILD_ABIS]"
    echo "  --api          Minimum android api version [default: $ANDROID_VERSION]"
    echo "  --no-debug     Disable debug building"
    echo "  --no-release   Disable release building"
    echo "  -b, --build    Build directory [default: $BUILD_DIR]"
    echo "  -d, --download Download directory [default: $DOWNLOAD_DIR]"
    echo "  -f, --flexible Use flexible page size [default: $FLEXIBLE_PAGE_SIZE]"
    echo "  -h, --help     Print this help message"
    echo "  -j, --jobs     Allow N jobs at once [default: $TOTAL_JOBS]"
    echo "  -p, --prefix   Prefix directory [default: $PREFIX_DIR]"
}

function run_cmake {
    ABI=$1
    BUILD_TYPE=$2

    TARGET_BUILD_DIR=${BUILD_DIR}/${BUILD_TYPE}/${ABI}
    TARGET_PREFIX_DIR=${PREFIX_DIR}/${BUILD_TYPE}/${ABI}

    # For details see:
    # https://cmake.org/cmake/help/latest/manual/cmake-toolchains.7.html#cross-compiling-for-android-with-the-ndk
    $CMAKE -H. \
        -B"${TARGET_BUILD_DIR}" \
        -G 'Unix Makefiles' \
        -DCMAKE_INSTALL_PREFIX="${TARGET_PREFIX_DIR}" \
        -DCMAKE_BUILD_TYPE="${BUILD_TYPE}" \
        -DCMAKE_SYSTEM_NAME=Android \
        -DCMAKE_SYSTEM_VERSION="${ANDROID_VERSION}" \
        -DANDROID_PLATFORM=android-"${ANDROID_VERSION}" \
        -DCMAKE_ANDROID_ARCH_ABI="${ABI}" \
        -DANDROID_ABI="${ABI}" \
        -DANDROID_NDK="${ANDROID_NDK}" \
        -DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES="${FLEXIBLE_PAGE_SIZE}" \
        -DCMAKE_ANDROID_NDK="${ANDROID_NDK}" \
        -DCMAKE_TOOLCHAIN_FILE="${ANDROID_NDK}"/build/cmake/android.toolchain.cmake \
        -DDOWNLOAD_DIR="${DOWNLOAD_DIR}" \
        -DTOTAL_JOBS="${TOTAL_JOBS}"

    $CMAKE --build "${TARGET_BUILD_DIR}" -j"${TOTAL_JOBS}"
}

while [[ "$#" -gt 0 ]]; do
    case $1 in
    -h | --help)
        print_help
        exit 0
        ;;
    --abi)
        BUILD_ABIS="$2"
        shift 2
        ;;
    --no-debug)
        BUILD_DEBUG=false
        shift 1
        ;;
    --no-release)
        BUILD_RELEASE=false
        shift 1
        ;;
    --api)
        ANDROID_VERSION="$2"
        shift 2
        ;;
    -b | --build)
        BUILD_DIR="$2"
        shift 2
        ;;
    -p | --prefix)
        PREFIX_DIR="$2"
        shift 2
        ;;
    -d | --download)
        DOWNLOAD_DIR="$2"
        shift 2
        ;;
    -j | --jobs)
        TOTAL_JOBS="$2"
        shift 2
        ;;
    -f | --flexible)
        FLEXIBLE_PAGE_SIZE="$2"
        shift 2
        ;;
    *) # Handle unknown options
        echo "Unknown option: $1"
        print_help
        exit 1
        ;;
    esac
done

BUILD_DIR=$(readlink -f "$BUILD_DIR")
PREFIX_DIR=$(readlink -f "$PREFIX_DIR")
DOWNLOAD_DIR=$(readlink -f "$DOWNLOAD_DIR")

IFS=',' read -ra abi_array <<<"$BUILD_ABIS"
for abi in "${abi_array[@]}"; do
    if $BUILD_DEBUG; then
        run_cmake "$abi" Debug
    fi
    if $BUILD_RELEASE; then
        run_cmake "$abi" Release
    fi
done

ln -fs "${PREFIX_DIR}/Release" "${PREFIX_DIR}/RelWithDebInfo"
