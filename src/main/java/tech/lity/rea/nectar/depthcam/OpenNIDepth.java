/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.lity.rea.nectar.depthcam;

import java.nio.ShortBuffer;
import org.bytedeco.javacpp.opencv_core;

/**
 *
 * @author realitytech
 */
public class OpenNIDepth  {

    private ShortBuffer frameData;
    float[] histogram;

    public OpenNIDepth() {
    }

//    @Override
//    public float findDepth(int offset) {
//        int depth = (int) (frameData.get(offset) & 0xFFFF);
//        if (depth == 0) {
//            return INVALID_DEPTH;
//        }
//        return depth;
//    }
//
//    @Override
//    public void updateDepth(opencv_core.IplImage depthImage) {
//        frameData = depthImage.getShortBuffer();
//    }
}
