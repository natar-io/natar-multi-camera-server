/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package tech.lity.rea.nectar.camera;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.ARToolKitPlus;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_imgcodecs;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class CameraThread extends Thread {

    private final Camera camera;
    Camera cameraForMarkerboard;
    private boolean compute;
    private IplImage image, grayImage;
//    private DetectedMarker[] detectedMarkers;

    public boolean stop;

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;

        cameraForMarkerboard = camera;

        // Thread version... No bonus whatsoever for now.
        initThreadPool();
    }

    private final int nbThreads = 4;
    private ExecutorService threadPool = null;

    private void tryInitThreadPool() {
        if (threadPool == null) {
            this.initThreadPool();
        }
    }

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(nbThreads);
//        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {

        while (!stop) {
            checkSubCamera();
            camera.grab();
            // If there is no camera for tracking...
//            if (cameraForMarkerboard == null || !compute || camera.getTrackedSheets().isEmpty()) {
//                continue;
//            }
            image = camera.getIplImage();
            if (image != null) {
//                this.compute();
            }
        }
    }

    /**
     * Set an image, used without starting the thread...
     * @param image 
     */
    public void setImage(IplImage image) {
        this.image = image;
    }

    private void checkSubCamera() {
        if (!(camera instanceof CameraRGBIRDepth)) {
            return;
        }
        Camera actAsCam = ((CameraRGBIRDepth) camera).getActingCamera();
        if (actAsCam != null) {
            cameraForMarkerboard = actAsCam;
        }
    }

    /**
     * Find the Markers, or features. 
     * Can be used without a running thread with setImage. 
     */
    public void compute() {
        try {
            camera.getSheetSemaphore().acquire();

            camera.getSheetSemaphore().release();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
