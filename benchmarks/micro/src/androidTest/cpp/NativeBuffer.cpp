#include <jni.h>

int g_buffer_capacity = 0;
const char *g_buffer = nullptr;

extern "C" {

JNIEXPORT void JNICALL Java_com_rejeq_cpcam_microbenchmarks_ByteBufferBenchmark_allocateNativeBuffer(
        JNIEnv* /* env */,  jobject /* obj */,  jint capacity) {
    g_buffer = new char[capacity];
    g_buffer_capacity = capacity;
}

JNIEXPORT void JNICALL Java_com_rejeq_cpcam_microbenchmarks_ByteBufferBenchmark_cleanupNativeBuffer(
        JNIEnv* /* env */, jobject /* obj */) {
    delete[] g_buffer;
}

JNIEXPORT jlong JNICALL Java_com_rejeq_cpcam_microbenchmarks_ByteBufferBenchmark_nativeBufferRead(
        JNIEnv* /* env */, jobject /* obj */) {
    jlong sum = 0;

    const char* data = g_buffer;
    jlong capacity = g_buffer_capacity;

    for (int i = 0; i < capacity; i++) {
        sum += data[i];
    }

    return sum;
}

JNIEXPORT jlong JNICALL Java_com_rejeq_cpcam_microbenchmarks_ByteBufferBenchmark_nativeDirectByteBufferRead(
        JNIEnv *env, jobject /* obj */, jobject buffer) {
    auto* data = (uint8_t*) env->GetDirectBufferAddress(buffer);
    jlong capacity = env->GetDirectBufferCapacity(buffer);
    jlong sum = 0;

    for (int i = 0; i < capacity; i++) {
        sum += data[i];
    }

    return sum;
}

}
