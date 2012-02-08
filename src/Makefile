all: 
	javac -classpath $(CLASSPATH):lib/colt.jar *.java
	javac -classpath $(CLASSPATH):lib/colt.jar fim/fpgrowth/*.java
	javac -classpath $(CLASSPATH):mrapriori/guava.jar mrapriori/*.java
	jar cvfm parallel.jar jar-manifest *.class fim/fpgrowth/*.class
	jar cvfm converter.jar util/jar-manifest util/Txt2SeqConverter.class
	jar cvfm mrapriori.jar mrapriori/jar-manifest mrapriori/*.class

clean: 
	rm *.class
	rm parallel.jar
