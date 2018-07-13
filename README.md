## Nectar camera server 

First camera server from PapARt project.


## How to use

1. Build the program.

`mvn package`

2. Start a camera

```
## OpenCV camera
java -jar -Xmx64m apps/camera-server.jar --driver OPENCV --device-id 0 --format rgb --output camera0 --resolution 640x480 --stream --stream-set --depth-camera camera0:depth" 

## Depth camera 
java -jar -Xmx64m apps/camera-server.jar --driver OPENNI2 --device-id 0 --format rgb --output camera0 --resolution 640x480 --stream --stream-set --depth-camera camera0:depth" 

## Play a video
java -jar -Xmx64m apps/camera-server.jar --driver FFMPEG --device-id "/home/ditrop/Documents/chat-fr.mp4" --format video --output video0	--stream --stream-set
```