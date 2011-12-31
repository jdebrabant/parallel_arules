#!/bin/bash 

make 
jar cvfm parallel.jar jar-manifest *.class fim/fpgrowth/*.class
