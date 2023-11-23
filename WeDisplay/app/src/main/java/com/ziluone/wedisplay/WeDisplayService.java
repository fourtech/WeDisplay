package com.ziluone.wedisplay;

import static android.graphics.PixelFormat.RGBA_8888;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

import android.app.ActivityOptions;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjectionManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class WeDisplayService extends Service implements ImageReader.OnImageAvailableListener {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 960;
//    private static final int WIDTH = 960;
//    private static final int HEIGHT = 320;
    private static final int DPI = 160;
    private static final int FORMAT = RGBA_8888;
    // private static final int FORMAT = PixelFormat.RGB_565;
    private static final int FLAG = VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY;

    private MediaProjectionManager mMPM;

    private VirtualDisplay mVD;
    private DisplayManager mDm;
    private ImageReader mImgReader;
    private NsdManager mNsdManager;
    private Handler mH;
    private HandlerThread mT;
    private static int sVirtualDisplayId = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        (mT = new HandlerThread("WeDisplay-IMG")).start();
        mH = new Handler(mT.getLooper());
        mDm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        mImgReader = ImageReader.newInstance(WIDTH, HEIGHT, FORMAT, 2);
        mVD = mDm.createVirtualDisplay("WeDisplay", WIDTH, HEIGHT, DPI, mImgReader.getSurface(), FLAG);
        mImgReader.setOnImageAvailableListener(this, mH);
        sVirtualDisplayId = mVD.getDisplay().getDisplayId();
        mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);
        mNsdManager.discoverServices("inspirations-display", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

        NsdServiceInfo info = new NsdServiceInfo();
        info.setServiceName("inspirations-display");
        info.setServiceType("_http._tcp.");
        mNsdManager.resolveService(info, mResolveListener);

        new Thread(mDisplayJob, "we-display").start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    long sendTime = 0;

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image != null) {
            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            int rowPadding = rowStride - pixelStride * WIDTH;
            Log.i("@@@@@@@@", "onImageAvailable() pixelStride=" + pixelStride
                    + ", rowStride=" + rowStride + ", rowPadding=" + rowPadding);
            Bitmap bmp = Bitmap.createBitmap(WIDTH + rowPadding / pixelStride, HEIGHT, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buffer);
            long now = SystemClock.uptimeMillis();
            Log.i("@@@@@@@@", "onImageAvailable() mOut=" + mOut + ", time=" + (now - sendTime));
            if (mOut != null && (now - sendTime) > 10000) {
                try {
                    byte[] head = new byte[] {
                            (byte) 0xAA, (byte) 0x55, (byte) 0xAA, (byte) 0x55, (byte) 0x88,
                            (byte) 0x66, (byte) 0x99, (byte) 0x20, (byte) 0x20, (byte) 0x12
                    };

                    byte[] data = new byte[bmp.getByteCount() / 2];
                    int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
                    bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
                    for (int i = 0, j = 0; i < pixels.length; i++) {
                        int c = pixels[i];
                        int r = Color.red(c);
                        int g = Color.green(c);
                        int b = Color.blue(c);

                        /*
                        int s = ((r << 8) & 0xF800) | ((g << 3) & 0x7E0) | ((b >> 3));
                        data[j++] = (byte) ((s >> 8) & 0xFF);
                        data[j++] = (byte) (s & 0xFF);
                         */

                        int v = 0;
                        v |= ((r >> 3) << 11);
                        v |= ((g >> 2) << 5);
                        v |= (b >> 3);
                        data[j++] = (byte) (v & 0xFF);
                        data[j++] = (byte) ((v >> 8) & 0xFF);

                        /*
                        int x = i % bmp.getWidth();
                        if (x < 10 || x > bmp.getWidth() - 10) {
                            data[j++] = (byte) (0xE0);
                            data[j++] = (byte) (0x07);
                        } else {
                            if (i < 100 * bmp.getWidth()) {
                                data[j++] = (byte) (0xE0);
                                data[j++] = (byte) (0xFF);
                            } else {
                                data[j++] = (byte) (0x00);
                                data[j++] = (byte) (0xF8);
                            }
                        }
                         */
                    }
                    Log.e("@@@@@@@@", "onImageAvailable() send=" + data.length);
                    mOut.write(head);
                    mOut.write(data);
                    mOut.flush();
                    sendTime = now;
                } catch (Throwable t) {
                    Log.e("@@@@@@@@", "onImageAvailable() send error", t);
                }
            }
            image.close();
        }
    }

    @Override
    public void onDestroy() {
        sVirtualDisplayId = -1;
        super.onDestroy();
    }

    public static int getVirtualDisplayId() {
        return sVirtualDisplayId;
    }

    private NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String s, int i) {
            Log.i("@@@@@@@@", "onStartDiscoveryFailed() s=" + s + ", i=" + i);
        }

        @Override
        public void onStopDiscoveryFailed(String s, int i) {
            Log.i("@@@@@@@@", "onStopDiscoveryFailed() s=" + s + ", i=" + i);
        }

        @Override
        public void onDiscoveryStarted(String s) {
            Log.i("@@@@@@@@", "onDiscoveryStarted() s=" + s);
        }

        @Override
        public void onDiscoveryStopped(String s) {
            Log.i("@@@@@@@@", "onDiscoveryStopped() s=" + s);
        }

        @Override
        public void onServiceFound(NsdServiceInfo info) {
            Log.i("@@@@@@@@", "onServiceFound() info=" + info);
        }

        @Override
        public void onServiceLost(NsdServiceInfo info) {
            Log.i("@@@@@@@@", "onServiceFound() info=" + info);
        }
    };

    private NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo info, int i) {
            Log.i("@@@@@@@@", "onResolveFailed() info=" + info + ", i=" + i);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo info) {
            Log.i("@@@@@@@@", "onServiceResolved() info=" + info);
        }
    };

    private OutputStream mOut;
    private Runnable mDisplayJob = new Runnable() {
        @Override
        public void run() {
            Log.i("@@@@@@@@", "mDisplayJob: [start]");
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("192.168.4.2", 8765), 5000);
                mOut = socket.getOutputStream();
                while (true) {
                    int r = 0;
                    byte[] data = new byte[1024];
                    InputStream in = socket.getInputStream();
                    while ((r = in.read(data)) > 0) {
                        Log.i("@@@@@@@@", "mDisplayJob: [in="+r+"]");
                    }
                }
            } catch (Throwable t) {
                Log.e("@@@@@@@@", "mDisplayJob: error", t);
            }
            Log.i("@@@@@@@@", "mDisplayJob: [end]");
        }
    };
}
