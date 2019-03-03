package com.zh.xplan.ui.camera.record;

import android.hardware.Camera;

import java.util.ArrayList;

public class CameraInfo {

    protected int cameraId;
    protected Camera camera;
    protected ArrayList<Size> pictureSizes = new ArrayList<>();
    protected ArrayList<Size> previewSizes = new ArrayList<>();
    protected final int frontCamera;

    public CameraInfo(int id, Camera.CameraInfo info) {
        cameraId = id;
        frontCamera = info.facing;
    }

    public int getCameraId() {
        return cameraId;
    }

    private Camera getCamera() {
        return camera;
    }

    public ArrayList<Size> getPreviewSizes() {
        return previewSizes;
    }

    public ArrayList<Size> getPictureSizes() {
        return pictureSizes;
    }

    /*private int getScore(CameraSelectionCriteria criteria) {
        int score = 10;
        if (criteria != null) {
            if ((criteria.getFacing().isFront() && frontCamera != Camera.CameraInfo.CAMERA_FACING_FRONT) || (!criteria.getFacing().isFront() && frontCamera != Camera.CameraInfo.CAMERA_FACING_BACK)) {
                score = 0;
            }
        }
        return (score);
    }*/
}
