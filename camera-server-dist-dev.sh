#!/bin/bash

CP=$(<./classpath.txt)
java  -Xmx128m -cp $CP:/usr/share/java/natar-apps.jar:./target/natar-multi-camera-server-0.1.2a.jar tech.lity.rea.nectar.camera.CameraServerImpl $@
