#!/bin/bash

CP=$(<./classpath-dist.txt)
java  -Xmx128m -cp $CP:./target/natar-multi-camera-server-0.1.2c.jar tech.lity.rea.nectar.camera.CameraServerImpl  $@
