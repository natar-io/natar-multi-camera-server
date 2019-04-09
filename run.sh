
CP=$(<classpath.txt)
java -cp $CP:target/*:../nectar-apps/target/* tech.lity.rea.nectar.camera.CameraServerImpl --driver OPENNI2 --device-id 0 --format rgb --output camera0 --stream --depth-camera -v


