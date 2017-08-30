package com.futureworkshops.android.autocapture.camera.view;

import android.graphics.PointF;

import com.futureworkshops.datacap.common.camera.view.BaseCameraView;
import com.ibm.datacap.sdk.ui.camera.AbstractCaptureView;
import com.ibm.datacap.sdk.ui.camera.DatacapCameraView;

public interface MainCameraView extends BaseCameraView {

    void setDocumentListener(DatacapCameraView.DatacapDetectionListener datacapDetectionListener);

    void setPhotoListener(AbstractCaptureView.PictureListener pictureListener);

    void edit(String path, PointF[] corners);
}
