//package tech.lity.rea.nectar;
//
//import processing.core.*;
//import tech.lity.rea.nectar.camera.Camera;
//import tech.lity.rea.nectar.camera.CannotCreateCameraException;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutput;
//import java.io.ObjectOutputStream;
//import java.net.ConnectException;
//import java.nio.ByteBuffer;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.Option;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.Option.Builder;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.ParseException;
//import redis.clients.jedis.Jedis;
//import tech.lity.rea.nectar.camera.CameraFactory;
//import org.redisson.Redisson;
//import org.redisson.api.RBinaryStream;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.redisson.client.codec.*;
//import org.redisson.config.TransportMode;
//
///**
// *
// * @author Jeremy Laviole, <laviole@rea.lity.tech>
// */
//public class CameraServerRedisson extends Thread {
//
//    Camera camera;
//
//    boolean running = true;
//
//    public CameraServerRedisson(String[] args) {
//        connectRedist();
//
//        try {
//            // application only using a camera
//            // screen rendering
////            camera = CameraFactory.createCamera(Camera.Type.OPENCV, "0", "");
//            camera = CameraFactory.createCamera(Camera.Type.OPENNI2, "0", "rgb");
//            camera.setSize(640, 480);
//            camera.start();
//            initMemory(640, 480, 3, 1);
////            camera.setParent(applet);
////            camera.setCalibration(cameraCalib);
//        } catch (CannotCreateCameraException ex) {
//            Logger.getLogger(CameraServerRedisson.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
////        papart.startTracking();
////        papart.startTracking();
//    }
//
//    protected void checkArguments(String[] passedArgs) {
//        Options options = new Options();
////         options.addOption("i", "input", true, "Input line in Redis if any.");
//        options.addOption("o", "output", true, "Output line in Redis if any, default is:" + defaultName);
//        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
//        options.addOption("rh", "redishost", true, "Redis host, default is: 127.0.0.1");
//        options.addOption("h", "host", true, "this computer's name.");
//
//        CommandLineParser parser = new DefaultParser();
//        try {
//            CommandLine cmd = parser.parse(options, passedArgs);
//
//            if (cmd.hasOption("o")) {
//                String output = cmd.getOptionValue("o");
//
//                System.out.println("Output: " + output);
//            } else {
//                System.out.println("No output value"); // print the date
//                System.out.println("Default output: " + defaultName); // print the date
//            }
//
//        } catch (ParseException ex) {
//            Logger.getLogger(CameraServerRedisson.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    RedissonClient redisson;
//
//    private void connectRedist() {
//        try {
//            Config config = new Config();
//
//            ByteArrayCodec codec = org.redisson.client.codec.ByteArrayCodec.INSTANCE;
//            config.setTransportMode(TransportMode.EPOLL);
//            config.useSingleServer().setAddress("redis://127.0.0.1:6379");
////        config.useClusterServers()
////                // use "rediss://" for SSL connection
////                .addNodeAddress("redis://127.0.0.1:6379");
//            config.setCodec(codec);
////        
//            redisson = Redisson.create(config);
//
//            if (redisson == null) {
//                throw new Exception("Cannot connect to server. ");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(0);
//        }
//        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
//    }
//
//    @Override
//    public void run() {
//        while (running) {
//            sendImage();
//        }
//
//    }
//
//    PImage copy = null;
//    byte[] imageData;
//
//    private void initMemory(int width, int height, int nChannels, int bytePerChannel) {
//        imageData = new byte[nChannels * width * height * bytePerChannel];
//    }
//
//    public void sendImage() {
//
//        if (camera != null) {
//            // warning, some cameras do not grab ?
//            camera.grab();
//
//            // get the pixels from native memory
//            if (camera.getIplImage() == null) {
//                System.out.println("null Image");
//                return;
//            }
//            ByteBuffer byteBuffer = camera.getIplImage().getByteBuffer();
//            byteBuffer.get(imageData);
//
////            redis.set(key, value)
////            image(.getPImage(), 0, 0, width, height);
//            String name = defaultName;
//            byte[] id = name.getBytes();
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ObjectOutput out = null;
//            try {
//                out = new ObjectOutputStream(bos);
//                out.writeObject(imageData);
//                out.flush();
//                byte[] yourBytes = bos.toByteArray();
//
//                System.out.println("Redis set image. " + name);
////                redis.set(id, yourBytes);
////                redis.publish(id, yourBytes);
//                
//                RBinaryStream binaryStream = redisson.getBinaryStream(name);
//                binaryStream.set(imageData);
//                
//            } catch (ConnectException e) {
//                e.printStackTrace();
//                System.exit(-1);
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.exit(-1);
//            }
//
//        }
//    }
//
//    // TODO: add hostname ?
//    public static final String OUTPUT_PREFIX = "nectar:";
//    public static final String OUTPUT_PREFIX2 = ":camera-server:camera";
//    public static final String REDIS_PORT = "6379";
//
//    static String defaultHost = "jiii-mi";
//    static String defaultName = OUTPUT_PREFIX + defaultHost + OUTPUT_PREFIX2 + "#0";
//
//    /**
//     * @param passedArgs the command line arguments
//     */
//    static public void main(String[] passedArgs) {
//
//        CameraServerRedisson cameraServer = new CameraServerRedisson(passedArgs);
//        cameraServer.start();
//    }
//
//    public byte[] intToBytes(int my_int) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutput out = new ObjectOutputStream(bos);
//        out.writeInt(my_int);
//        out.close();
//        byte[] int_bytes = bos.toByteArray();
//        bos.close();
//        return int_bytes;
//    }
//
//    public int bytesToInt(byte[] int_bytes) throws IOException {
//        ByteArrayInputStream bis = new ByteArrayInputStream(int_bytes);
//        ObjectInputStream ois = new ObjectInputStream(bis);
//        int my_int = ois.readInt();
//        ois.close();
//        return my_int;
//    }
//}
