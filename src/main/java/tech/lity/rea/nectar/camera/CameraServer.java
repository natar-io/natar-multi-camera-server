package tech.lity.rea.nectar.camera;

import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole, <laviole@rea.lity.tech>
 */
public abstract interface CameraServer {

    abstract public Jedis createRedisConnection();
    abstract public void sendImage();
    abstract public String getOutput();
    abstract public long time();
}
