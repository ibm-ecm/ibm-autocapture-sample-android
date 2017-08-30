package com.futureworkshops.android.autocapture.camera.view;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.futureworkshops.android.autocapture.R;
import com.futureworkshops.android.autocapture.camera.MainCameraPresenter;
import com.futureworkshops.datacap.common.camera.configuration.ConfigurationService;
import com.futureworkshops.datacap.common.camera.view.BaseCameraActivity;
import com.ibm.datacap.sdk.api.DatacapApi;
import com.ibm.datacap.sdk.ui.camera.AbstractCaptureView;
import com.ibm.datacap.sdk.ui.camera.DatacapCameraView;

import javax.inject.Inject;


public class MainActivity extends BaseCameraActivity implements MainCameraView {

    public static final String ARGS_BITMAP_PATH = "bitmap_path";
    public static final String ARGS_CORNERS = "corners";

    @Inject
    MainCameraPresenter mainCameraPresenter;
    @Inject
    DatacapApi datacapApi;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, ConfigurationService.class));
        mainCameraPresenter.configureCameraView();

        Log.e("Error", "activity " + datacapApi);

        View takePhotoButton = findViewById(R.id.take_photo);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.takePhoto();
            }
        });
    }

    @Override
    protected void onCameraStarted() {
        cameraView.startDetection();
    }

    @Override
    protected void onCameraStopped() {
        if (cameraView.isDetecting()) {
            cameraView.stopDetection();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void setDocumentListener(DatacapCameraView.DatacapDetectionListener datacapDetectionListener) {
        cameraView.setDatacapDetectionListener(datacapDetectionListener);
    }

    @Override
    public void setPhotoListener(AbstractCaptureView.PictureListener pictureListener) {
        cameraView.setPictureListener(pictureListener);
    }

    @Override
    public void edit(String path, PointF[] corners) {
        onCameraStarted();
        cameraView.stop();
        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra(ARGS_BITMAP_PATH, path);
        intent.putExtra(ARGS_CORNERS, corners);
        startActivity(intent);

    }
}
