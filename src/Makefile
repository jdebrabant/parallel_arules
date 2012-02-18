all: 
	javac -classpath $(CLASSPATH):lib/colt.jar *.java
	javac -classpath $(CLASSPATH):lib/colt.jar fim/fpgrowth/*.java
	javac -classpath $(CLASSPATH):distrcount/guava.jar distrcount/*.java
	javac -classpath $(CLASSPATH):util/ util/*.java
	jar cvfm parallel.jar jar-manifest *.class fim/fpgrowth/*.class
	jar cvfm converter.jar util/jar-manifest util/Txt2SeqConverter.class
	jar cvfm distrcount.jar distrcount/jar-manifest distrcount/*.class

clean: 
	rm *.class
	rm fim/fpgrowth/*.class
	rm distrcount/*.class
	rm util/*.class
	rm *.jar
