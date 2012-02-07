all: 
	javac -classpath $(CLASSPATH):lib/colt.jar *.java
	javac -classpath $(CLASSPATH):lib/colt.jar fim/fpgrowth/*.java
	jar cvfm parallel.jar jar-manifest *.class fim/fpgrowth/*.class

clean: 
	rm *.class
	rm parallel.jar
