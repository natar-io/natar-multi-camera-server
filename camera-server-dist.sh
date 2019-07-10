#!/bin/bash

CP=$(</usr/share/natar/java-natar-multi-camera-server/classpath.txt)
java  -Xmx128m -cp $CP:/usr/share/java/natar-multi-camera-server.jar tech.lity.rea.nectar.camera.CameraServerImpl $@
