package tech.lity.rea.nectar;

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

/**
 *
 * @author Jeremy Laviole, <laviole@rea.lity.tech>
 */
public class CameraServer extends Thread {

    Jedis redis, redisSend;
    Camera camera;

    // Arguments
    public static final int REDIS_PORT = 6379;
    public static final String REDIS_HOST = "localhost";

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
    private Camera.Type type;

    boolean running = true;

    boolean isVerbose = false;
    boolean isSilent = false;

    public CameraServer(String[] args) {
        checkArguments(args);
        connectRedist();

        try {
            // application only using a camera
            // screen rendering
//            camera = CameraFactory.createCamera(Camera.Type.OPENCV, "0", "");
            camera = CameraFactory.createCamera(type, description, format);
            camera.setSize(width, height);
            camera.start();
            sendParams(camera);
            initMemory(width, height, 3, 1);

//            camera.setParent(applet);
//            camera.setCalibration(cameraCalib);
        } catch (CannotCreateCameraException ex) {
            Logger.getLogger(CameraServer.class.getName()).log(Level.SEVERE, null, ex);
            die(ex.toString());
        }

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
        options.addOption("w", "width", true, "Image width, default 640.");
        options.addOption("h", "height", true, "Image height, default 480.");
        options.addOption("r", "resolution", true, "Image size, can be used instead of width and height, default 640x480.");
        // Generic options
        options.addOption("h", "help", false, "print this help.");
        options.addOption("v", "verbose", false, "Verbose activated.");
        options.addOption("s", "silent", false, "Silent activated.");
        options.addOption("u", "unique", false, "Unique mode, run only once and use get/set instead of pub/sub");
        options.addRequiredOption("o", "output", true, "Output key.");
        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
        options.addOption("rh", "redishost", true, "Redis host, default is: " + REDIS_HOST);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        // -u -i markers -cc data/calibration-AstraS-rgb.yaml -mc data/A4-default.svg -o pose
        try {
            cmd = parser.parse(options, passedArgs);

            if (cmd.hasOption("d")) {
                driverName = cmd.getOptionValue("d");
                type = Camera.Type.valueOf(driverName);
                // TODO: CHECK TYPE PROBLEM
            }
            if (cmd.hasOption("id")) {
                description = cmd.getOptionValue("id");
            }

            if (cmd.hasOption("f")) {
                format = cmd.getOptionValue("f");
            }
            if (cmd.hasOption("r")) {
                String resolution = cmd.getOptionValue("r");
                String[] split = resolution.split("x");
                width = Integer.parseInt(split[0]);
                height = Integer.parseInt(split[1]);
            }

            if (cmd.hasOption("w")) {
                width = Integer.parseInt(cmd.getOptionValue("w"));
            }
            if (cmd.hasOption("h")) {
                height = Integer.parseInt(cmd.getOptionValue("h"));
            }

            if (cmd.hasOption("h")) {
                die("", true);
            }

            if (cmd.hasOption("o")) {
                output = cmd.getOptionValue("o");
            } else {
                die("Please set an output key with -o or --output ", true);
            }

            if (cmd.hasOption("u")) {
                isUnique = true;
            }
            if (cmd.hasOption("v")) {
                isVerbose = true;
            }
            if (cmd.hasOption("s")) {
                isSilent = true;
            }
            if (cmd.hasOption("rh")) {
                host = cmd.getOptionValue("rh");
            }
            if (cmd.hasOption("rp")) {
                port = Integer.parseInt(cmd.getOptionValue("rp"));
            }
        } catch (ParseException ex) {
            die(ex.toString(), true);
//            Logger.getLogger(PoseEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void connectRedist() {
        try {
            redis = new Jedis(host, port);
            if (redis == null) {
                throw new Exception("Cannot connect to server. ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
    }

    private void sendParams(Camera cam) {
        redis.set(output + ":width",  Integer.toString(cam.width()));
        redis.set(output + ":height",  Integer.toString(cam.height()));
        redis.set(output + ":channels",  Integer.toString(3));
    }

    @Override
    public void run() {
        while (running) {
            sendImage();
        }

    }

    PImage copy = null;
    byte[] imageData;

    private void initMemory(int width, int height, int nChannels, int bytePerChannel) {
        imageData = new byte[nChannels * width * height * bytePerChannel];
    }

    int lastTime = 0;

    public void sendImage() {
        if (camera != null) {
            // warning, some cameras do not grab ?
            camera.grab();

            // get the pixels from native memory
            if (camera.getIplImage() == null) {
                log("null Image", "");
                return;
            }
            ByteBuffer byteBuffer = camera.getIplImage().getByteBuffer();
            byteBuffer.get(imageData);
            String name = output;
            byte[] id = name.getBytes();
            if (isUnique) {
                redis.set(id, imageData);
                running = false;
                log("Sending (SET) image to: " + output, "");
            } else {
                redis.publish(id, imageData);
                log("Sending (PUBLISH) image to: " + output, "");
            }
        }
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

        CameraServer cameraServer = new CameraServer(passedArgs);
        cameraServer.start();
    }

}
