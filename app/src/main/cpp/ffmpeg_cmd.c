#include <jni.h>
#include "ffmpeg.h"
#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "FFMPEG_JNI"
#endif

JNIEXPORT jint JNICALL
Java_com_chasonc_ndkffmpeg_FFmpeg_run(JNIEnv *env, jobject obj, jobjectArray commands) {

    int argc = (*env)->GetArrayLength(env, commands);
    char *argv[argc];

    char info[1000] = {0};
    sprintf(info,"%s\n",avcodec_configuration());

    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env, js, 0);
    }
    return run(argc,argv);
}

JNIEXPORT jint JNICALL
Java_com_chasonc_ndkffmpeg_FFmpeg_getVideoAngle
        (JNIEnv *env, jclass jcls, jstring j_videoPath){
    const char *c_videoPath = (*env)->GetStringUTFChars(env,j_videoPath,NULL);
    //1. 注册所有组件
    av_register_all();
    //2. 打开视频、获取视频信息，
    //  其中，fmtCtx为封装格式上下文
    AVFormatContext *fmtCtx = avformat_alloc_context();
    avformat_open_input(&fmtCtx,c_videoPath,NULL,NULL);
    //3. 获取视频流的索引位置
    int i;
    int v_stream_idx = -1;
    for(i=0 ; i<fmtCtx->nb_streams ; i++){
        if(fmtCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO){
            v_stream_idx = i;
            break;
        }
    }
    // 4. 获取旋转角度，元数据
    AVDictionaryEntry *tag = NULL;
    tag = av_dict_get(fmtCtx->streams[v_stream_idx]->metadata,"rotate",tag,NULL);
    int angle = -1;
    if(tag != NULL){
        // 将char *强制转换为into类型
        angle = atoi(tag->value);
    }
    // 5.释放封装格式上下文
    avformat_free_context(fmtCtx);
    (*env)->ReleaseStringUTFChars(env,j_videoPath,c_videoPath);
    return angle;
}