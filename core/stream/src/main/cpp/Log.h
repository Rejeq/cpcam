#pragma once

#include <android/log.h>

#define LOG_PREFIX "jni_"
#define _LOG_TAG LOG_PREFIX LOG_TAG

#if 1

#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, _LOG_TAG, __VA_ARGS__)
#define LOG_WARN(...) __android_log_print(ANDROID_LOG_WARN, _LOG_TAG, __VA_ARGS__)
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, _LOG_TAG, __VA_ARGS__)

#ifdef NDEBUG
#define LOG_DEBUG(...) (void) 0
#define LOG_TRACE(...) (void) 0
#else
#define LOG_DEBUG(...) __android_log_print(ANDROID_LOG_DEBUG, _LOG_TAG, __VA_ARGS__)
#define LOG_TRACE(...) __android_log_print(ANDROID_LOG_VERBOSE, _LOG_TAG, __VA_ARGS__)
#endif

#else

#define LOG_ERROR(...) (void) 0
#define LOG_TRACE(...) (void) 0
#define LOG_DEBUG(...) (void) 0
#define LOG_INFO(...) (void) 0
#define LOG_WARN(...) (void) 0

#endif
