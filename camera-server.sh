#!/bin/bash

CP=$(<camera-server.txt)
java  -Xmx128m -cp $CP:apps/camera-server.jar:apps/apps.jar tech.lity.rea.nectar.camera.CameraServerImpl $@
