/**************************************************************************************************************************

 * File: MRDriver.java
 * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
			Matteo Riondato (matteo@cs.brown.edu)
 * Last Modified: 12/27/2011
 
 * Description:
	Driver for Hadoop implementation of parallel association rule mining.
 
 * Usage: java MRDriver <mapper id> <path to input database> <path to output local FIs> <path to output global FIs>
	* mapper id - specifies which Map method should be used
		1 for partition mapper, 2 for binomial mapper, 3 for weighted coin flip sampler 
	* path to input database - path to file containing transactions in .dat format (1 transaction per line)
	* local FI output - path to directory to write local (per-reducer) FIs
	* global FI output - path to directory to write global FIs (combined from all local FIs)
 
***************************************************************************************************************************/

import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.lib.InputSampler;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MRDriver extends Configured implements Tool
{
	public final int MR_TIMEOUT_MILLI = 60000000;
	
	public static void main(String args[]) throws Exception
	{
		if (args.length != 10)
		{
			System.out.println("usage: java MRDriver <epsilon> <delta> <minFreqPercent> <d> <datasetSize> <nodes> <mapper id> <path to input database> " + 
							   "<path to output local FIs> <path to output global FIs>");
			System.exit(1); 
		}

		int res = ToolRunner.run(new MRDriver(), args);

		System.exit(res);
	}

	public int run(String args[]) throws Exception
	{
		long job_start_time, job_end_time; 
		long job_runtime; 
		float epsilon = Float.parseFloat(args[0]);
		double delta = Double.parseDouble(args[1]);
		int minFreqPercent = Integer.parseInt(args[2]);
		int d = Integer.parseInt(args[3]);
		int datasetSize = Integer.parseInt(args[4]);
		int nodes = Integer.parseInt(args[5]);

		
		/************************ Job 1 (local FIM) Configuration ************************/
		
		JobConf conf = new JobConf(getConf()); 

		int numSamples = (int) Math.floor(0.95 * nodes * conf.getInt("mapred.tasktracker.tasks.maximum", 10));
		double phi = 2 + 4 * Math.log(delta) / numSamples - Math.sqrt(16 * Math.pow(Math.log(delta), 2) / numSamples + 8 * Math.log(delta) / numSamples + 3);
		assert phi > 0.0 && phi < 1.0;
		int sampleSize = (int) Math.ceil((2 / Math.pow(epsilon, 2))*(d + Math.log(1/ phi)));
		double freq = datasetSize / ((double) numSamples * sampleSize);

		conf.setInt("PARMM.reducersNum", numSamples);
		conf.setInt("PARMM.datasetSize", datasetSize);
		conf.setInt("PARMM.minFreqPercent", minFreqPercent);
		conf.setFloat("PARMM.epsilon", epsilon);
			
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		conf.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 

		conf.setJarByClass(MRDriver.class);
			
		conf.setMapOutputKeyClass(IntWritable.class); 
		conf.setMapOutputValueClass(Text.class); 
			
		conf.setOutputKeyClass(Text.class); 
		conf.setOutputValueClass(DoubleWritable.class); 

		FileInputFormat.addInputPath(conf, new Path(args[7]));
		FileOutputFormat.setOutputPath(conf, new Path(args[8]));
		
		// set the mapper classs based on command line option
		if(args[6].equals("1"))
		{
			System.out.println("running partition mapper..."); 
			conf.setMapperClass(PartitionMapper.class);
		}
		else if(args[6].equals("2"))
		{
			System.out.println("running binomial mapper..."); 
			conf.setMapperClass(BinomialSamplerMapper.class);
		}
		else if(args[6].equals("3"))
		{
			System.out.println("running coin mapper..."); 
			conf.setMapperClass(CoinFlipSamplerMapper.class);
		}
		else if(args[6].equals("4"))
		{
			System.out.println("running sampler mapper..."); 
			conf.setMapperClass(InputSamplerMapper.class);
			
			// XXX call to getSample is causing NullPointerException, didn't debug since we need to rewrite it anyways. JD
			// create a random sample of size T*m
			InputSampler.Sampler<LongWritable,Text> sampler = new
			InputSampler.RandomSampler<LongWritable,Text>(freq, numSamples);  
			LongWritable[] samples = sampler.getSample(new TextInputFormat(), conf);

			Collections.shuffle(Arrays.asList(samples));

			// for each key in the sample, create a list of all T samples to which this key belongs
			Hashtable<LongWritable, ArrayList<IntWritable>> hashTable = new Hashtable<LongWritable, ArrayList<IntWritable>>();
			for (int i=0; i < samples.length; i++) 
			{
				ArrayList<IntWritable> sampleIDs = null;
				if (hashTable.contains(samples[i]))  
					sampleIDs = hashTable.get(samples[i]);
				else
					sampleIDs = new ArrayList<IntWritable>();
				sampleIDs.add(new IntWritable(i % sampleSize));
				hashTable.put(samples[i], sampleIDs);
			}
			
			MapFile.Writer writer = null;
			FileSystem fs = null;
			try 
			{
				//writer = MapFile.createWriter(fs, conf, path, LongWritable.class, IntArrayWritable.class);
				
				// MapFile.Writer will create 2 files, samplesMap/data and samplesMap/index
				fs = FileSystem.get(conf);
				//fs = FileSystem.getLocal(conf); // get the driver's local filesystem
				writer = new MapFile.Writer(conf, fs,
						"samplesMap",
						LongWritable.class,
						IntArrayWritable.class);
				
				// create sorted list of keys (need to append keys to MapFile in sorted order)
				ArrayList<LongWritable> sorted_keys = new ArrayList<LongWritable>(hashTable.keySet()); 
				Collections.sort(sorted_keys); 
				
				for(LongWritable key : sorted_keys)
				//for (LongWritable key : hashTable.keySet())
				{
					ArrayList<IntWritable> sampleIDs = hashTable.get(key);
					IntArrayWritable sampleIDsIAW = new IntArrayWritable();

					sampleIDsIAW.set(sampleIDs.toArray(new IntWritable[1]));

					writer.append(key, sampleIDsIAW);
				}

			} 
			finally 
			{
				IOUtils.closeStream(writer);
			}

			DistributedCache.addCacheFile(new URI(fs.getUri().toString() + "samplesMap/data"), conf);
			DistributedCache.addCacheFile(new URI(fs.getUri().toString() + "samplesMap/index"), conf);
			DistributedCache.createSymlink(conf); 
		}
		else
		{
			// NOT REACHED
		}
		
		// XXX Why is it necessary to change the default hash partitioner? JD
		conf.setPartitionerClass(FIMPartitioner.class);

		conf.setReducerClass(FIMReducer.class);
			
		job_start_time = System.currentTimeMillis(); 
		JobClient.runJob(conf);
		job_end_time = System.currentTimeMillis(); 
			
		job_runtime = (job_end_time-job_start_time) / 1000; 
			
		System.out.println("local FIM runtime (seconds): " + job_runtime);	
	
		/************************ Job 2 (aggregation) Configuration ************************/
		
		JobConf confAggr = new JobConf(getConf());

		confAggr.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		confAggr.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 

		confAggr.setJarByClass(MRDriver.class);
			
		confAggr.setMapOutputKeyClass(Text.class); 
		confAggr.setMapOutputValueClass(Text.class); 
			
		confAggr.setOutputKeyClass(Text.class); 
		confAggr.setOutputValueClass(DoubleWritable.class); 
			
		confAggr.setMapperClass(IdentityMapper.class);
		confAggr.setReducerClass(AggregateReducer.class);
			
		confAggr.setInputFormat(KeyValueTextInputFormat.class);

		KeyValueTextInputFormat.addInputPath(confAggr, new Path(args[8]));
		FileOutputFormat.setOutputPath(confAggr, new Path(args[9]));

		job_start_time = System.currentTimeMillis(); 
		JobClient.runJob(confAggr);
		job_end_time = System.currentTimeMillis(); 
			
		job_runtime = (job_end_time-job_start_time) / 1000; 
			
		System.out.println("aggregation runtime (seconds): " +
				job_runtime); 
		 
		return 0;
	}
}

