SRCS       = AggregateMapper.java \
	     AggregateReducer.java \
	     BinomialSamplerMapper.java \
	     CoinFlipSamplerMapper.java \
	     CombineSequenceFileInputFormat.java \
	     FIMPartitioner.java \
	     FIMReducer.java \
	     InputSamplerMapper.java \
	     IntArrayWritable.java \
	     Merge.java \
	     MRDriver.java \
	     PartitionMapper.java \
	     RandIntPartSamplerMapper.java \
	     TextArrayWritable.java \
	     WholeSplitInputFormat.java

CLASSES    = ${SRCS:.java=.class}

# clear out all suffixes
.SUFFIXES:
# # list only those we use
.SUFFIXES: .class .java

.java.class:
	javac -classpath $(CLASSPATH):lib/colt.jar:util/ $<

all: parallel.jar converter.jar distrcount.jar

parallel.jar: $(CLASSES) fpgrowth
	jar cvfm parallel.jar jar-manifest *.class fim/fpgrowth/*.class

fpgrowth: fim/fpgrowth/AssocRuleMining.class fim/fpgrowth/FPgrowth.class fim/fpgrowth/FPgrowthApp.class fim/fpgrowth/FPtree.class fim/fpgrowth/TotalSupportTree.class fim/fpgrowth/TtreeNode.class

converter.jar: util/Txt2SeqConverter.class util/SyntheticDatabaseGenerator.class
	cd util && jar cvfm ../converter.jar jar-manifest Txt2SeqConverter.class && cd ..

distrcount.jar:
	javac -classpath $(CLASSPATH):distrcount/guava.jar distrcount/*java
	cd distrcount && jar cvfm ../distrcount.jar jar-manifest *.class && cd ..
clean: 
	-rm *.class
	-rm fim/fpgrowth/*.class
	-rm distrcount/*.class
	-rm util/*.class
	-rm *.jar

