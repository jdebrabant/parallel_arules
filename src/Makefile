all: 
	javac -classpath $(CLASSPATH):lib/colt.jar *.java
	javac -classpath $(CLASSPATH):lib/colt.jar fim/fpgrowth/*.java
	javac -classpath $(CLASSPATH):distrcount/guava.jar distrcount/*.java
	javac -classpath $(CLASSPATH):util/ util/*.java
	jar cvfm parallel.jar jar-manifest *.class fim/fpgrowth/*.class
	cd util && jar cvfm ../converter.jar jar-manifest Txt2SeqConverter.class && cd ..
	cd distrcount && jar cvfm ../distrcount.jar jar-manifest *.class && cd ..

clean: 
	rm *.class
	rm fim/fpgrowth/*.class
	rm distrcount/*.class
	rm util/*.class
	rm *.jar
