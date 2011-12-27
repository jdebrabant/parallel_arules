#!/bin/bash 

javac -cp $CLASSPATH:lib/colt.jar *.java
jar cvfm parallel.jar jar-manifest *.class