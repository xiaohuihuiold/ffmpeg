//
// Created by xhh on 7/20/19.
//

#include <jni.h>
#include <android/log.h>
#include <cstdio>

extern "C" {
#include <libswresample/swresample.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>


JNIEXPORT void
JNICALL Java_com_xhhold_tool_ffmpeg01_MainActivity_testFFmpeg(JNIEnv *env, jobject obj) {
    // TODO: 测试代码
    av_register_all();
    AVFormatContext *pFormatCtx = avformat_alloc_context();
    //open
    if (avformat_open_input(&pFormatCtx, "http://192.168.249.167/music.flac", nullptr, nullptr) != 0) {
        return;
    }
    //获取视频信息
    if (avformat_find_stream_info(pFormatCtx, nullptr) < 0) {
        return;
    }
    int audio_stream_idx = -1;
    for (int i = 0; i < pFormatCtx->nb_streams; ++i) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_idx = i;
            break;
        }
    }
    // 获取解码器上下文
    AVCodecContext *pCodecCtx = pFormatCtx->streams[audio_stream_idx]->codec;
    // 获取解码器
    AVCodec *pCodex = avcodec_find_decoder(pCodecCtx->codec_id);
    // 打开解码器
    if (avcodec_open2(pCodecCtx, pCodex, nullptr) < 0) {
    }
    // 申请avpakcet，装解码前的数据
    auto *packet = (AVPacket *) av_malloc(sizeof(AVPacket));
    // 申请avframe，装解码后的数据
    AVFrame *frame = av_frame_alloc();//得到SwrContext ，进行重采样，具体参考http://blog.csdn.net/jammg/article/details/52688506
    SwrContext *swrContext = swr_alloc();
    // 缓存区
    auto *out_buffer = (uint8_t *) av_malloc(44100 * 2);
    // 输出的声道布局（立体声）
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;
    // 输出采样位数  16位
    enum AVSampleFormat out_formart = AV_SAMPLE_FMT_S16;
    // 输出的采样率必须与输入相同
    int out_sample_rate = pCodecCtx->sample_rate;

    // swr_alloc_set_opts将PCM源文件的采样格式转换为自己希望的采样格式
    swr_alloc_set_opts(swrContext, out_ch_layout, out_formart, out_sample_rate, pCodecCtx->channel_layout,
                       pCodecCtx->sample_fmt, pCodecCtx->sample_rate, 0, nullptr);
    swr_init(swrContext);
    // 获取通道数  2
    int out_channer_nb = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
    // 反射得到Class类型
    jclass david_player = env->GetObjectClass(obj);
    //    反射得到createAudio方法
    jmethodID createAudio = env->GetMethodID(david_player, "createTrack", "(II)V");
    // 反射调用createAudio
    env->CallVoidMethod(obj, createAudio, 44100, out_channer_nb);
    jmethodID audio_write = env->GetMethodID(david_player, "playTrack", "([BI)V");
    int got_frame;
    while (av_read_frame(pFormatCtx, packet) >= 0) {
        if (packet->stream_index == audio_stream_idx) {
            // 解码  mp3   编码格式frame----pcm   frame
            avcodec_decode_audio4(pCodecCtx, frame, &got_frame, packet);
            if (got_frame) {
                // 解码
                swr_convert(swrContext, &out_buffer, 44100 * 2, (const uint8_t **) frame->data, frame->nb_samples);
                // 缓冲区的大小
                int size = av_samples_get_buffer_size(nullptr, out_channer_nb, frame->nb_samples, AV_SAMPLE_FMT_S16, 1);
                jbyteArray audio_sample_array = env->NewByteArray(size);
                env->SetByteArrayRegion(audio_sample_array, 0, size, (const jbyte *) out_buffer);
                env->CallVoidMethod(obj, audio_write, audio_sample_array, size);
                env->DeleteLocalRef(audio_sample_array);
            }
        }
    }
}
}