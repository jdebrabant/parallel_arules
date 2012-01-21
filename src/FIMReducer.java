import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import fim.fpgrowth.*; 

public class FIMReducer extends MapReduceBase implements Reducer<IntWritable, Text, Text, DoubleWritable>
{
	int minFreqPercent;
	int sampleSize;

	@Override
	public void configure(JobConf conf) 
	{
		minFreqPercent = conf.getInt("PARMM.minFreqPercent", 20); 
		sampleSize = conf.getInt("PARMM.sampleSize", 1000);
	}

	@Override
	public void reduce(IntWritable key, Iterator<Text> values, 
			OutputCollector<Text,DoubleWritable> output, 
			Reporter reporter) throws IOException
	{			
	  	FPgrowth.mineFrequentItemsets(values, sampleSize, minFreqPercent, output);
	}
}



