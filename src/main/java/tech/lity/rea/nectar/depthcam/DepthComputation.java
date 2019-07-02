/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017-2018 RealityTech
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
package tech.lity.rea.nectar.depthcam;

import org.bytedeco.javacpp.opencv_core;

/**
 * Abstraction for finding depth for each device.
 *
 * @author Jeremy Laviole
 */
public interface DepthComputation {

    public static final float INVALID_DEPTH = -1;
//    public float findDepth(int offset, Object buffer);

    public float findDepth(int offset);

    public void updateDepth(opencv_core.IplImage buffer);
}
