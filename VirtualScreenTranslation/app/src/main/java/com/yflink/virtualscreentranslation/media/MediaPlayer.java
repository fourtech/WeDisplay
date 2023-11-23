package com.yflink.virtualscreentranslation.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;


import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MediaPlayer implements Thread.UncaughtExceptionHandler {

    protected String TAG = getClass().getSimpleName();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG, "uncaughtException:" + Log.getStackTraceString(e));
    }

    protected Thread mDisplayThread = null;
    protected MediaCodec mediaCodec;
    protected volatile boolean isReady = false;
    private Object mLockObj = new Object();

    private void initPlayer() {
        Log.i(TAG, "initPlayer");
    }

    public MediaPlayer() {
        initPlayer();
    }

    public boolean check() {
        return isReady;
    }

    long mDelayTest = 0;
    long mDelayTest1 = -1;
    long inputBufferTime = 0;
    int realFrameRate = 0;
    long lastFrameRateTime = -1;

    public synchronized boolean startDisplay(final Surface surface, int width, int height) {
        Log.i(TAG, "=============startDisplay================");
        if (surface == null || !surface.isValid() || width == 0 || height == 0) {
            Log.i(TAG, "surface is invalid or width == 0 || height == 0!!!");
            return false;
        }
        Log.i(TAG, "startDisplay!!!  mediaCodec:" + mediaCodec);
        if (mediaCodec != null) {
            stop();
        }
        Log.i(TAG, width + "," + height);
        if (mediaCodec == null) {
            try {
                mediaCodec = MediaCodec.createDecoderByType("video/avc");
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                mediaCodec.configure(mediaFormat, surface, null, 0);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "createDecoderByType error222:" + Log.getStackTraceString(e));
                mediaCodec = null;
                return false;
            }
            mediaCodec.start();
            isReady = true;
            Log.i(TAG, "isReady:" + isReady);
            mDisplayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    while (isReady && mediaCodec != null) {
                        try {
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 50 * 1000);
                            if (outputBufferIndex >= 0) {
                                mDelayTest++;
                                if (mDelayTest == 30) {
                                    Log.i(TAG, "mDelayTest draw:");
                                    mDelayTest = 0;
                                }
                                if (SystemClock.elapsedRealtime() - lastFrameRateTime > 1000) {
                                    Log.i(TAG, "realFrameRate:" + realFrameRate);
                                    lastFrameRateTime = SystemClock.elapsedRealtime();
                                    realFrameRate = 0;
                                }
                                realFrameRate++;
                                mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                                if(mDelayTest == 0){
                                    Log.i(TAG, "mDelayTest draw complete:");
                                }
                            }else{
                                //Log.i(TAG, "mDelayTest dequeueOutputBuffer failed:");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "DisplayThread:" + e.toString());
                            break;
                        }
                    }
                    Log.i(TAG, "Exit display video thread!!!");
                }
            });
            mDisplayThread.start();
        }
        return true;
    }

    public void stop() {
        Log.i(TAG, "stop!!!");
        mDelayTest = 0;
        mDelayTest1 = -1;
        inputBufferTime = 0;
        isReady = false;
        mCodecQueue.clear();

        if (mCurMediaCodecRunnable != null) {
            mCurMediaCodecRunnable.stopMediaCodec();
        }
        mCurMediaCodecRunnable = null;

        if (mMediaCodecThread != null) {
            mMediaCodecThread.interrupt();
        }
        mMediaCodecThread = null;
        Log.i(TAG, "++++++++++++++stop!!!");
        try {
            if (mDisplayThread != null) {
                mDisplayThread.join();
            }
            mDisplayThread = null;
            mediaCodec.flush();
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "bbbbbbbbbbbbbb:" + e.toString());
        }
        mediaCodec = null;
        Log.i(TAG, "------------------stop!!!");
    }

    void tempStop() {
        Log.i(TAG, "----tempStop----");
        //RTLinkManager.getInstance().stopPhoneVideo();
        stop();
    }

    private static final int QUEUE_COUNT = 250;
    private BlockingQueue<CodecBuffer> mCodecQueue = new ArrayBlockingQueue<>(QUEUE_COUNT);
    private Thread mMediaCodecThread;

    public void play(final byte[] buf, final int length) {
        if (isReady) {
            if (mMediaCodecThread == null || !mMediaCodecThread.isAlive()) {
                mCurMediaCodecRunnable = new MediaCodecRunnable(mediaCodec);
                mMediaCodecThread = new Thread(mCurMediaCodecRunnable);
                mMediaCodecThread.start();
            }
            if (mCodecQueue.size() < QUEUE_COUNT) {
                //LogUtil.i(TAG, "mCodecQueue.size():"+mCodecQueue.size());
                mCodecQueue.add(new CodecBuffer(buf, length));
                if (mCodecQueue.size() > QUEUE_COUNT / 3) {
                    Log.w(TAG, "-------------codec queue is size() > " + QUEUE_COUNT / 3 + "-------------");
                }
            } else {
                Log.e(TAG, "-------------codec queue is full!-------------");
            }
        }else{
            Log.e(TAG, "-------------codec not ready-------------");
        }
    }

    private MediaCodecRunnable mCurMediaCodecRunnable;

    class CodecBuffer {

        byte[] buffer;
        int bufferLen;

        public CodecBuffer(byte[] buffer, int bufferLen) {
            this.buffer = buffer;
            this.bufferLen = bufferLen;
        }
    }

    class MediaCodecRunnable implements Runnable {

        private MediaCodec codec;
        private int printVideoLog = 0;
        private boolean isStopMediaCodec;

        public MediaCodecRunnable(MediaCodec codec) {
            this.codec = codec;
            isStopMediaCodec = false;
        }

        public void stopMediaCodec() {
            isStopMediaCodec = true;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
            while (!isStopMediaCodec) {
                try {
                    CodecBuffer codecBuffer = mCodecQueue.take();
                    long startTime = SystemClock.elapsedRealtime();
                    if (isReady && codecBuffer.buffer != null && codecBuffer.buffer.length > 0) {
//                        byte[] bs = new byte[codecBuffer.bufferLen-8];
//                        System.arraycopy(codecBuffer.buffer,0,bs,0,codecBuffer.bufferLen-8);
//                        long timeAtEnd = TestManager.getInstance().getTimeAtEnd(codecBuffer.buffer);
//                        inputBuffer(bs, codecBuffer.bufferLen-8);
//                        long l = System.currentTimeMillis() - timeAtEnd;
//                        LogUtil.e(TAG, "play cost time:"+l);
//                        TestManager.getInstance().setDelayShow((int) l);
                        inputBuffer(codecBuffer.buffer, codecBuffer.bufferLen);
                    } else {
                        Log.e(TAG, "play isReady:" + isReady + " buffer len:" + codecBuffer.bufferLen + ", failure!!!");
                    }
                    long costTime = SystemClock.elapsedRealtime() - startTime;
                    if (costTime > 100) {
                        Log.w(TAG, "inputBuffer costTime:" + costTime);
                    }
                } catch (InterruptedException e) {
                    Log.i(TAG, "inputBuffer exception:"+Log.getStackTraceString(e));
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "isStopMediaCodec:" + isStopMediaCodec);
            codec = null;
        }

        private void inputBuffer(byte[] buffer, int length) {
            try {
                if (codec != null) {
                    printVideoLog++;
                    if (printVideoLog > 30) {
                        Log.i(TAG, "queueInputBuffer, len:" + length);
                        printVideoLog = 0;
                    }
                    mDelayTest1++;
                    if (mDelayTest1 == 30) {
                        Log.i(TAG, "mDelayTest queueInputBuffer:");
                        mDelayTest1 = 0;
                    }
                    inputBufferTime++;
//                    if(inputBufferTime > 3){
//                        return;
//                    }
                    ByteBuffer[] inputBuffers = codec.getInputBuffers();
                    int inputBufferIndex = codec.dequeueInputBuffer(-1);
                    if (mDelayTest1 == 0) {
                        Log.i(TAG, "mDelayTest queueInputBuffer complete:");
                    }
                    if (inputBufferIndex >= 0) {
                        int flags = 0;
                        int nalType = buffer[4] & 0x1F;
                        // sps/pps
                        if (nalType == 0x07 || nalType == 0x08) {
                            flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
                            Log.e("Media", "onFrame codec config");
                        }
                        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                        inputBuffer.clear();
                        inputBuffer.rewind();
                        inputBuffer.put(buffer, 0, buffer.length);
                        codec.queueInputBuffer(inputBufferIndex, 0, length, 1, flags);
                    } else {
                        Log.e(TAG, "InputBuffer(invalid) :" + inputBufferIndex + ",isReady:" + isReady);
                        tempStop();
                        Thread.sleep(500);
                        Log.e(TAG, "InputBuffer(invalid) End!!!");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "MediaPlay play error:" + e.toString());
            }

        }
    }

    public void release() {
        if (mediaCodec != null) {
            stop();
        }
    }
}
