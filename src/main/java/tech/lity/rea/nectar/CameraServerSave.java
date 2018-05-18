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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import redis.clients.jedis.Jedis;
import tech.lity.rea.nectar.camera.CameraFactory;

/**
 *
 * @author Jeremy Laviole, <laviole@rea.lity.tech>
 */
@SuppressWarnings("serial")
public class CameraServerSave extends PApplet {

    Jedis redis;
    Camera camera;

    @Override
    public void settings() {
        // the application will be rendered in full screen, and using a 3Dengine.
        size(200, 200, P3D);
    }

    @Override
    public void setup() {

        connectRedist();

        try {
            // application only using a camera
            // screen rendering
            
            camera = CameraFactory.createCamera(Camera.Type.OPENCV, "0", "");
//            camera.setParent(applet);
//            camera.setCalibration(cameraCalib);
        } catch (CannotCreateCameraException ex) {
            Logger.getLogger(CameraServerSave.class.getName()).log(Level.SEVERE, null, ex);
        }

//        papart.startTracking();

//        papart.startTracking();
    }

    void connectRedist() {
        redis = new Jedis("127.0.0.1", 6379);
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
    }


    int k = 0;
    PImage copy = null;

    @Override
    public void draw() {

        if (copy == null) {
            copy = createImage(640, 480, RGB);
        }

        if (camera != null && camera.getPImage() != null) {
            // Send the pixels 
            PImage img = camera.getPImageCopyTo(copy);
            copy.loadPixels();

//            redis.set(key, value)
//            image(.getPImage(), 0, 0, width, height);
            image(copy, 0, 0, width, height);

            String name = defaultName;
//            byte[] id = {(byte) 0};
            byte[] id = name.getBytes();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(copy.pixels);
                out.flush();
                byte[] yourBytes = bos.toByteArray();

                System.out.println("Redis set image. " + name + " img: " + k++);
//                redis.set(id, yourBytes);
                redis.publish(id, yourBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
        return int_bytes;
    }

    public int bytesToInt(byte[] int_bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(int_bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        int my_int = ois.readInt();
        ois.close();
        return my_int;
    }

    // TODO: add hostname ?
    public static final String OUTPUT_PREFIX = "nectar:";
    public static final String OUTPUT_PREFIX2 = ":camera-server:camera";
    public static final String REDIS_PORT = "6379";

    static String defaultHost = "jiii-mi";
    static String defaultName = OUTPUT_PREFIX + defaultHost + OUTPUT_PREFIX2 + "#0";

    /**
     * @param passedArgs the command line arguments
     */
    static public void main(String[] passedArgs) {

        Options options = new Options();
//         options.addOption("i", "input", true, "Input line in Redis if any.");
        options.addOption("o", "output", true, "Output line in Redis if any, default is:" + defaultName);
        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
        options.addOption("rh", "redishost", true, "Redis host, default is: 127.0.0.1");
        options.addOption("h", "host", true, "this computer's name.");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, passedArgs);

            if (cmd.hasOption("o")) {
                String output = cmd.getOptionValue("o");

                System.out.println("Output: " + output);
            } else {
                System.out.println("No output value"); // print the date
                System.out.println("Default output: " + defaultName); // print the date
            }

        } catch (ParseException ex) {
            Logger.getLogger(CameraServerSave.class.getName()).log(Level.SEVERE, null, ex);
        }

//        if (passedArgs != null) {
//            PApplet.main(concat(appletArgs, passedArgs));
//        } else {
        String[] appletArgs = new String[]{tech.lity.rea.nectar.CameraServerSave.class.getName()};
        PApplet.main(appletArgs);
//        }
    }
}
