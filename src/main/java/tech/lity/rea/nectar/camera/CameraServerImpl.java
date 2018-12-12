package tech.lity.rea.nectar.camera;

import processing.core.*;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CannotCreateCameraException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import redis.clients.jedis.Jedis;
import tech.lity.rea.nectar.camera.CameraFactory;
import tech.lity.rea.nectar.camera.CameraRGBIRDepth;

import processing.data.JSONArray;
import processing.data.JSONObject;
import tech.lity.rea.nectar.camera.CameraOpenNI2;
import tech.lity.rea.nectar.camera.CameraServer;

/**
 *
 * @author Jeremy Laviole, <laviole@rea.lity.tech>
 */
public class CameraServerImpl extends CameraServer {

    Jedis redis, redisSend;
    Camera camera;

    // Arguments
    public static final int REDIS_PORT = 6379;
    public static final String REDIS_HOST = "localhost";
    private long millisOffset = System.currentTimeMillis();

    private String driverName = "";
    private String description = "0";
    private String format = "";
    private int width = 640, height = 480;
    private String markerFileName = "";
    private String input = "marker";
    private String output = "pose";
    private String host = REDIS_HOST;
    private int port = REDIS_PORT;
    private boolean isUnique = false;
    private boolean isStreamSet = false;
    private boolean isStreamPublish = true;
    private Camera.Type type;

    boolean running = true;

    boolean isVerbose = false;
    boolean isSilent = false;
    private boolean useDepth = false;

    private long colorImageCount = 0;
    private long depthImageCount = 0;

    private CameraRGBIRDepth dcamera;

    public CameraServerImpl(String[] args) {
        checkArguments(args);
        connectRedis();

        try {
            // application only using a camera
            // screen rendering
//            camera = CameraFactory.createCamera(Camera.Type.OPENCV, "0", "");
            camera = CameraFactory.createCamera(type, description, format);
            camera.setSize(width, height);

            if (useDepth) {
                if (camera instanceof CameraRGBIRDepth) {
                    dcamera = (CameraRGBIRDepth) camera;
                    dcamera.setUseColor(true);
                    dcamera.setUseDepth(true);
                    sendDepthParams(dcamera.getDepthCamera());
                    initDepthMemory(
                            dcamera.getDepthCamera().width(),
                            dcamera.getDepthCamera().height(),
                            2, 1);

                    dcamera.actAsColorCamera();

                } else {
                    die("Camera not recognized as a depth camera.");
                }
            }

            if (camera instanceof CameraOpenNI2) {
                CameraOpenNI2 cameraNI = (CameraOpenNI2) camera;
                cameraNI.sendToRedis(this, host, port);
            }

            camera.start();
            sendParams(camera);
            initMemory(width, height, 3, 1);

//            camera.setParent(applet);
//            camera.setCalibration(cameraCalib);
        } catch (CannotCreateCameraException ex) {
            Logger.getLogger(CameraServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            die(ex.toString());
        }

//        papart.startTracking();

//        papart.startTracking();
    }

    private Options options;

    private String buildDriverNames() {
        Camera.Type[] values = Camera.Type.values();
        StringBuilder driversText = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            driversText.append(values[i]);
            if (i != values.length - 1) {
                driversText.append(", ");
            }
        }
        return driversText.toString();
    }

    private void checkArguments(String[] passedArgs) {
        options = new Options();

//        public static Camera createCamera(Camera.Type type, String description, String format)
//        options.addRequiredOption("i", "input", true, "Input key of marker locations.");
        options.addRequiredOption("d", "driver", true, "Driver to use amongst: " + buildDriverNames());
        options.addRequiredOption("id", "device-id", true, "Device id, path or name (driver dependant).");
        options.addOption("f", "format", true, "Format, e.g.: for depth cameras rgb, ir, depth.");
        options.addOption("r", "resolution", true, "Image size, can be used instead of width and height, default 640x480.");
        // Generic options

        options.addOption("s", "stream", false, " stream mode (PUBLISH).");
        options.addOption("sg", "stream-set", false, " stream mode (SET).");
        options.addOption("u", "unique", false, "Unique mode, run only once and use get/set instead of pub/sub");
        options.addOption("dc", "depth-camera", false, "Load the depth video when available.");

        options.addOption("h", "help", false, "print this help.");
        options.addOption("v", "verbose", false, "Verbose activated.");
        options.addOption("si", "silent", false, "Silent activated.");
        options.addOption("u", "unique", false, "Unique mode, run only once and use get/set instead of pub/sub");
        options.addRequiredOption("o", "output", true, "Output key.");
        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
        options.addOption("rh", "redishost", true, "Redis host, default is: " + REDIS_HOST);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        // -u -i markers -cc data/calibration-AstraS-rgb.yaml -mc data/A4-default.svg -o pose
        try {
            cmd = parser.parse(options, passedArgs);

            driverName = cmd.getOptionValue("d");
            type = Camera.Type.valueOf(driverName);
            description = cmd.getOptionValue("id");
            output = cmd.getOptionValue("o");
            format = cmd.hasOption("f") ? cmd.getOptionValue("f") : format;

            if (cmd.hasOption("r")) {
                String resolution = cmd.getOptionValue("r");
                String[] split = resolution.split("x");
                width = Integer.parseInt(split[0]);
                height = Integer.parseInt(split[1]);
            }

            if (cmd.hasOption("h")) {
                die("", true);
            }

            if (cmd.hasOption("sg")) {
                isStreamSet = true;
                isStreamPublish = false;
            }

            useDepth = cmd.hasOption("dc");
            isUnique = cmd.hasOption("u");
            isVerbose = cmd.hasOption("v");
            isSilent = cmd.hasOption("si");
            host = cmd.hasOption("rh") ? cmd.getOptionValue("rh") : host;
            port = cmd.hasOption("rp") ? Integer.parseInt(cmd.getOptionValue("rp")) : port;

        } catch (ParseException ex) {
            die(ex.toString(), true);
//            Logger.getLogger(PoseEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Jedis connectRedis() {
        try {
            redis = new Jedis(host, port);
            if (redis == null) {
                throw new Exception("Cannot connect to server. ");
            }
            return redis;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
    }

    private void sendParams(Camera cam) {
        redis.set(output + ":width", Integer.toString(cam.width()));
        redis.set(output + ":height", Integer.toString(cam.height()));
        redis.set(output + ":channels", Integer.toString(3));
        redis.set(output + ":pixelformat", cam.getPixelFormat().toString());
    }

    private void sendDepthParams(Camera cam) {
        redis.set(output + ":depth:width", Integer.toString(cam.width()));
        redis.set(output + ":depth:height", Integer.toString(cam.height()));
        redis.set(output + ":depth:channels", Integer.toString(2));
        redis.set(output + ":depth:pixelformat", cam.getPixelFormat().toString());
    }

    @Override
    public void run() {
        while (running) {
            sendImage();
        }

    }

    PImage copy = null;
    byte[] imageData;
    byte[] depthImageData;

    private void initMemory(int width, int height, int nChannels, int bytePerChannel) {
        imageData = new byte[nChannels * width * height * bytePerChannel];
    }

    private void initDepthMemory(int width, int height, int nChannels, int bytePerChannel) {
        depthImageData = new byte[nChannels * width * height * bytePerChannel];
    }

    int lastTime = 0;

    public void sendImage() {
        if (camera != null) {

            // OpenNI2 camera is not grabbed here
            if(camera instanceof CameraOpenNI2){
                try {
                    Thread.sleep(1000);
                    return;
                } catch (InterruptedException ex) {
                    Logger.getLogger(CameraServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            // warning, some cameras do not grab ?
            camera.grab();
            sendColorImage();
            if (useDepth) {
                sendDepthImage();
            }
        }
    }

    private void sendColorImage() {
        ByteBuffer byteBuffer;
        if (useDepth) {
            if (dcamera.getColorCamera().getIplImage() == null) {
                log("null color Image -d", "");
                return;
            }
            byteBuffer = dcamera.getColorCamera().getIplImage().getByteBuffer();
        } else {

            // get the pixels from native memory
            if (camera.getIplImage() == null) {
                log("null color Image", "");
                return;
            }
            byteBuffer = camera.getIplImage().getByteBuffer();
        }
        colorImageCount++;
        byteBuffer.get(imageData);
        String name = output;
        byte[] id = name.getBytes();
        String time = Long.toString(time());
        if (isUnique) {
            redis.set(id, imageData);
            running = false;
            log("Sending (SET) image to: " + output, "");
            redis.set((name + ":timestamp"), time);
        } else {
            if (isStreamSet) {
                redis.set(id, imageData);
                redis.set((name + ":timestamp"), time);
                log("Sending (SET) image to: " + output, "");
            }
            if (isStreamPublish) {
                JSONObject imageInfo = new JSONObject();
                imageInfo.setLong("timestamp", time());
                imageInfo.setLong("imageCount", colorImageCount);
                redis.set(id, imageData);
                redis.publish(id, imageInfo.toString().getBytes());
                log("Sending (PUBLISH) image to: " + output, "");
            }
        }

    }

    private void sendDepthImage() {
        if (dcamera.getDepthCamera().getIplImage() == null) {
            log("null depth Image", "");
            return;
        }
        depthImageCount++;
        ByteBuffer byteBuffer = dcamera.getDepthCamera().getIplImage().getByteBuffer();
        byteBuffer.get(depthImageData);
        String name = output + ":depth:raw";
        byte[] id = (name).getBytes();
        String time = Long.toString(time());

        if (isUnique) {
            redis.set(id, depthImageData);
            redis.set((name + ":timestamp"), time);

            running = false;
            log("Sending (SET) image to: " + name, "");
        } else {
            if (isStreamSet) {

                redis.set(id, depthImageData);
                redis.set((name + ":timestamp"), time);

                log("Sending (SET) image to: " + name, "");
            }
            if (isStreamPublish) {
                JSONObject imageInfo = new JSONObject();
                imageInfo.setLong("timestamp", time());
                imageInfo.setLong("imageCount", depthImageCount);
                redis.set(id, depthImageData);
                redis.publish(id, imageInfo.toString().getBytes());
                log("Sending (PUBLISH) image to: " + name, "");
            }

        }

    }

    public long time() {
        return System.currentTimeMillis() - millisOffset;
    }

    public void die(String why) {
        die(why, false);
    }

    public void die(String why, boolean usage) {
        if (usage) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CameraServer", options);
        }
        System.out.println(why);
        System.exit(-1);
    }

    public void log(String normal, String verbose) {
        if (isSilent) {
            return;
        }
        if (normal != null) {
            System.out.println(normal);
        }
        if (isVerbose && verbose != null) {
            System.out.println(verbose);
        }
    }

    /**
     * @param passedArgs the command line arguments
     */
    static public void main(String[] passedArgs) {

        CameraServerImpl cameraServer = new CameraServerImpl(passedArgs);
        cameraServer.start();
    }

    public String getOutput() {
        return this.output;
    }

}
