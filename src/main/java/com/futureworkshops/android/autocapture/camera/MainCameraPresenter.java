package com.futureworkshops.android.autocapture.camera;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.futureworkshops.android.autocapture.camera.view.MainCameraView;
import com.futureworkshops.datacap.common.camera.BaseCameraPresenter;
import com.ibm.capture.sdk.ui.camera.CameraView;
import com.ibm.datacap.sdk.ui.camera.AbstractCaptureView;
import com.ibm.datacap.sdk.ui.camera.DatacapCameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

public class MainCameraPresenter extends BaseCameraPresenter<MainCameraView> implements
        DatacapCameraView.DatacapDetectionListener, AbstractCaptureView.PictureListener {

    public static final String IMAGE = "image.jpg";

    private final Context context;

    @Inject
    public MainCameraPresenter(@NonNull MainCameraView mainCameraView, Context context) {
        super(mainCameraView);
        this.context = context;
    }

    @Override
    public void configureCameraView() {
        super.configureCameraView();
        getCameraView().setDocumentListener(this);
        getCameraView().setPhotoListener(this);
    }

    @Override
    public void onDocumentProcessingStarted() {

    }

    @Override
    public void onDocumentDetected(DatacapCameraView.DatacapDetectionResult datacapDetectionResult) {
        getCameraView().edit(pathOf(datacapDetectionResult.getOriginalImage()), datacapDetectionResult.getCorners());
    }

    @Override
    public void onPictureTaken(CameraView cameraView, byte[] bytes, int i) {
        Log.e("Error", "On Picture taken: " + bytes);
        getCameraView().edit(pathOf(fromByteArray(bytes)), null);
    }

    private String pathOf(Bitmap bitmap) {
        return saveToInternalStorage(bitmap, context, IMAGE);
    }

    private Bitmap fromByteArray(byte[] itsBytes) {
        return BitmapFactory.decodeByteArray(itsBytes, 0, itsBytes.length);
    }

    public static String saveToInternalStorage(Bitmap bitmapImage, Context context, String imageName) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, imageName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
