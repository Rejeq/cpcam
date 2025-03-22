#include "VideoConfig.h"

#include "JniUtils.h"

// obj must be PixFmt class
PixFmt to_pix_fmt(JNIEnv *env, jobject obj) {
    jclass clazz = env->GetObjectClass(obj);
    jmethodID pixFmt_id = env->GetMethodID(clazz, "ordinal", "()I");
    // FIXME: Make sure that ordinal is in range of [0, PixFmt::MaxValue]
    int ordinal = env->CallIntMethod(obj, pixFmt_id);
    return (PixFmt)ordinal;
}

VideoConfig VideoConfig::build(JNIEnv *env, jobject obj) {
    jclass clazz = env->GetObjectClass(obj);

    jfieldID codecName_field =
        env->GetFieldID(clazz, "codecName", "Ljava/lang/String;");
    jobject codecName_str = env->GetObjectField(obj, codecName_field);

    jfieldID pixFmt_field = env->GetFieldID(
        clazz, "pixFmt", "Lcom/rejeq/cpcam/core/stream/jni/FFmpegPixFmt;");
    jobject pixFmt_obj = env->GetObjectField(obj, pixFmt_field);

    jfieldID bitrate_field = env->GetFieldID(clazz, "bitrate", "J");
    jlong bitrate = env->GetLongField(obj, bitrate_field);

    jfieldID framerate_field = env->GetFieldID(clazz, "framerate", "I");
    jint framerate = env->GetIntField(obj, framerate_field);

    jfieldID width_field = env->GetFieldID(clazz, "width", "I");
    jint width = env->GetIntField(obj, width_field);

    jfieldID height_field = env->GetFieldID(clazz, "height", "I");
    jint height = env->GetIntField(obj, height_field);

    return VideoConfig{
        .codec_name = to_string(env, (jstring)codecName_str),
        .pix_fmt = to_pix_fmt(env, pixFmt_obj),
        .bitrate = bitrate,
        .framerate = framerate,
        .width = width,
        .height = height,
    };
}
