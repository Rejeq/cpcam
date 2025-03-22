set(FFMPEG_PREFIX_DIR "${DEPS_DIR}/ffmpeg")

function(MakeFFmpegLibStatic target)
    add_library(ffmpeg_${target} STATIC IMPORTED)
    add_library(FFmpeg::${target} ALIAS ffmpeg_${target})
    set_property(TARGET ffmpeg_${target} PROPERTY IMPORTED_LOCATION "${FFMPEG_PREFIX_DIR}/lib/${target}.a")
    target_include_directories(ffmpeg_${target} INTERFACE ${FFMPEG_PREFIX_DIR}/include)
endfunction()

function(MakeFFmpegLibShared target)
    add_library(ffmpeg_${target} SHARED IMPORTED)
    add_library(FFmpeg::${target} ALIAS ffmpeg_${target})
    set_property(TARGET ffmpeg_${target} PROPERTY IMPORTED_LOCATION "${FFMPEG_PREFIX_DIR}/lib/${target}.so")
    target_include_directories(ffmpeg_${target} INTERFACE ${FFMPEG_PREFIX_DIR}/include)
endfunction()

function(MakeFFmpegLib target)
    if (${IS_RELEASE})
        MakeFFmpegLibStatic(${target})
    else()
        MakeFFmpegLibShared(${target})
    endif()
endfunction()

MakeFFmpegLib(libavcodec)
MakeFFmpegLib(libavdevice)
MakeFFmpegLib(libavfilter)
MakeFFmpegLib(libavformat)
MakeFFmpegLib(libavutil)
MakeFFmpegLib(libswresample)
MakeFFmpegLib(libswscale)
MakeFFmpegLib(libpostproc)


# zlib - part of NDK api
target_link_libraries(ffmpeg_libavformat INTERFACE z)
