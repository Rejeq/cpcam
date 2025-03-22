#pragma once

#include <jni.h>
#include <string>

JavaVM *get_jvm();
std::string to_string(JNIEnv *env, jstring str);
