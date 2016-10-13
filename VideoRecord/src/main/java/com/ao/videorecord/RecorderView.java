package com.ao.videorecord;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by xiaao on 12/10/2016.
 */

public class RecorderView extends SurfaceView implements SurfaceHolder.Callback {

    public static String TAG = "RecorderView";

    private boolean recording;
    private MediaRecorder mediaRecorder;
    private SurfaceHolder surfaceHolder;
    private String outputFileLocation;

    private Camera camera;
    private int orientation = 0;

    public RecorderView(Context context) {
        super(context, null);
        Log.d(TAG, "super(context, null)");
    }

    public RecorderView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        Log.d(TAG, "super(context, attrs, 0)");

        outputFileLocation = context.getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4";

        recording = false;

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        prepareMediaRecorder();
    }

    public boolean prepareMediaRecorder() {
        camera = getCameraInstance();

        if (camera == null) {
            return false;
        }

        // set the orientation here to enable portrait recording.
        setCameraDisplayOrientation(0);
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

//        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        //set video format
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //set audio format
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //set bit rate
        mediaRecorder.setVideoEncodingBitRate(10000000);
        //set video encoder
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //set video size
        mediaRecorder.setVideoSize(1280, 720);
        //set video frame rate
        mediaRecorder.setVideoFrameRate(30);
        //set max duration
        mediaRecorder.setMaxDuration(10000);

        //set output location
        mediaRecorder.setOutputFile(outputFileLocation);
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setOrientationHint(orientation);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private Camera getCameraInstance() {
        Camera c;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            setCameraDisplayOrientation(0);
            previewCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //Call when activity pause
    public void onActivityDestroy() {
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    public void setOutputFile(String filename) {
        outputFileLocation = filename;
        mediaRecorder.setOutputFile(filename);
    }

    public String getOutputFileLocation() {
        return outputFileLocation;
    }

    public void startRecording() {
        releaseCamera();
        if (!prepareMediaRecorder()) {
            Toast.makeText(getContext(), "Fail in prepareMediaRecorder()!", Toast.LENGTH_LONG).show();
            return;
        }
        mediaRecorder.start();
        recording = true;
    }

    public void stopRecording() {
        mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        Toast.makeText(getContext(), "Video saved:" + outputFileLocation, Toast.LENGTH_SHORT).show();
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = new MediaRecorder();
        }
        if (null != camera) {
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    public void previewCamera() {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Cannot start preview", e);
        }
    }

    public void setCameraDisplayOrientation(int cameraId) {

        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        orientation = result;
        camera.setDisplayOrientation(result);
    }

}
