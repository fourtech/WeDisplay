package com.yflink.virtualscreentranslation.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoEncoder implements Runnable{

    private final String TAG = getClass().getSimpleName();
    private MediaCodec mCodec;
    private Surface mInputSurface;
    private Thread mEncoderThread;
    private volatile boolean mHasStart = false;

    private OnEncoderEventListener mOnEncoderEventListener;

    @Override
    public void run() {
        byte[] outData = new byte[30 * 1024];
        MediaCodec.BufferInfo outBufferInfo = new MediaCodec.BufferInfo();
        int outDataLength = 0;
        mCodec.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (mHasStart) {
            int outputBufferIndex = 0;
//            try {
                outputBufferIndex = mCodec.dequeueOutputBuffer(outBufferInfo, 10000);
                if (outputBufferIndex >= 0 && mCodec != null) {
                    ByteBuffer outputBuffer = mCodec.getOutputBuffer(outputBufferIndex);
                    Log.i(TAG, "dequeueOutputBuffer outputBuffer:"+outputBuffer);
                    if (outputBuffer == null) continue;
                    if (outData.length < outBufferInfo.size) {
                        outData = new byte[outBufferInfo.size];
                    }
                    outputBuffer.get(outData, 0, outBufferInfo.size);
                    outDataLength = outBufferInfo.size;
                    outputBuffer.clear();
                    mCodec.releaseOutputBuffer(outputBufferIndex, false);

                    if (mOnEncoderEventListener != null) {
                        mOnEncoderEventListener.onEncoderBufferAvailable(outData, outDataLength);
                    }
                }
//            } catch (Exception e) {
//                Log.e(TAG, "dequeueOutputBuffer failed");
//                break;
//            }
        }
        mInputSurface.release();

//        mCodec.flush();
//        mCodec.stop();
//        mCodec.release();
        mCodec = null;
    }

    public interface OnEncoderEventListener{
        void onInputSurfaceAvailable(Surface inputSurface);
        void onEncoderBufferAvailable(byte[] data, int len);
    }
    public void setOnEncoderEventListener(OnEncoderEventListener listener){
        mOnEncoderEventListener = listener;
    }

    private MediaCodec createCodec(int dstWidth, int dstHeight) {
        Log.d(TAG, "createCodec---dstWidth: " + dstWidth + ", dstHeight: " + dstHeight);
        try {
            MediaCodec codec = MediaCodec.createEncoderByType("video/avc");
            int bitRate = 1024 * 1024 * 2;

            int iFrameInterval = 1;
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", dstWidth, dstHeight);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
            //mediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, 30);
  //          mediaFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 100000);
//            mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
//            mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,  0x7F000789);
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            return codec;
        } catch (IOException e) {
            Log.d(TAG, "createCodec---IOException");
        }
        return null;
    }

    public static File createFileFromBytes(byte[] b,int len,  String outputFile, boolean needDelold) {
        File ret = null;
        BufferedOutputStream stream = null;
        try {
            ret = new File(outputFile);
            if (needDelold) {
                ret.delete();
            }
            FileOutputStream fstream = new FileOutputStream(ret, true);
            stream = new BufferedOutputStream(fstream);

            if (b != null)
                stream.write(b, 0, len);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("createFileFromBytes", e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("createFileFromBytes", e.toString());
                }
            }
        }
        return ret;
    }


    public void start(){
        mCodec = createCodec(850, 610);
        if(mCodec != null){
            mInputSurface = mCodec.createInputSurface();
            if (mOnEncoderEventListener != null) {
                mOnEncoderEventListener.onInputSurfaceAvailable(mInputSurface);
            }
            mHasStart = true;
            mEncoderThread = new Thread(this, "videoEncoder");
            mEncoderThread.start();
        }
    }

    public void stop(){
        mHasStart = false;
    }
}
