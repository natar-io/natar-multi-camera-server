/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016 Jérémy Laviole
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

import processing.core.PApplet;
import tech.lity.rea.nectar.calibration.HomographyCalibration;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CameraFactory;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.CameraRGBIRDepth;
import tech.lity.rea.nectar.camera.CannotCreateCameraException;
import tech.lity.rea.nectar.depthcam.DepthCameraDevice;
import tech.lity.rea.nectar.depthcam.DepthComputation;
import tech.lity.rea.nectar.depthcam.OpenNIDepth;

/**
 *
 * @author Jeremy Laviole
 */
public final class NectarOpenNI extends DepthCameraDevice {

    private final CameraNectar cameraNectar;

    public NectarOpenNI(PApplet parent, Camera anotherCam) throws CannotCreateCameraException {
        super(parent);

        if (anotherCam instanceof CameraNectar) {
            this.camera = (CameraNectar) anotherCam;
            this.camera.setUseDepth(true);
        } else {
            this.anotherCamera = anotherCam;
        }

        if (this.anotherCamera == null) {
            this.anotherCamera = getColorCamera();
        }

        cameraNectar = (CameraNectar) camera;
    }

    @Override
    public CameraNectar getMainCamera() {
        return cameraNectar;
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.NECTAR;
    }

    @Override
    public void loadDataFromDevice() {
//        setStereoCalibration(cameraNI.getHardwareExtrinsics());
    }

    @Override
    public DepthComputation createDepthComputation() {
        return new OpenNIDepth();
    }
}
