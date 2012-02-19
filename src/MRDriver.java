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
import java.util.Random;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DefaultStringifier;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MRDriver extends Configured implements Tool
{
	public final int MR_TIMEOUT_MILLI = 60000000;
	
	public static void main(String args[]) throws Exception
	{
		if (args.length != 11)
		{
			System.out.println("usage: java MRDriver <epsilon> <delta> <minFreqPercent> <d> <datasetSize> <numSamples> <phi> <mapper id> <path to input database> " + 
							   "<path to output local FIs> <path to output global FIs>");
			System.exit(1); 
		}

		int res = ToolRunner.run(new MRDriver(), args);

		System.exit(res);
	}

	public int run(String args[]) throws Exception
	{
		FileSystem fs = null;
		Path samplesMapPath = null;

		long job_start_time, job_end_time; 
		long job_runtime; 
		float epsilon = Float.parseFloat(args[0]);
		double delta = Double.parseDouble(args[1]);
		int minFreqPercent = Integer.parseInt(args[2]);
		int d = Integer.parseInt(args[3]);
		int datasetSize = Integer.parseInt(args[4]);
		int numSamples = Integer.parseInt(args[5]);
		double phi = Double.parseDouble(args[6]);
		Random rand;


		/************************ Job 1 (local FIM) Configuration ************************/
		
		JobConf conf = new JobConf(getConf()); 

		/*
		 * Compute the number of required "votes" for an itemsets to be
		 * declared frequent 	
		 */
		// The +1 at the end is needed to ensure reqApproxNum > numsamples / 2.
		int reqApproxNum = (int) Math.floor((numSamples*(1-phi))-Math.sqrt(numSamples*(1-phi)*2*Math.log(1/delta))) + 1;
		int sampleSize = (int) Math.ceil((2 / Math.pow(epsilon, 2))*(d + Math.log(1/ phi)));
		//System.out.println("reducersNum: " + numSamples + " reqApproxNum: " + reqApproxNum);

		conf.setInt("PARMM.reducersNum", numSamples);
		conf.setInt("PARMM.datasetSize", datasetSize);
		conf.setInt("PARMM.minFreqPercent", minFreqPercent);
		conf.setInt("PARMM.sampleSize", sampleSize);
		conf.setFloat("PARMM.epsilon", epsilon);

		// Set the number of reducers equal to the number of samples, to
		// maximize parallelism. Required by our Partitioner.
		conf.setNumReduceTasks(numSamples);

		// XXX: why do we disable the speculative execution? MR
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		conf.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 

		/* 
		 * Enable compression of map output.
		 *
		 * We do it for this job and not for the aggregation one because
		 * each mapper there only print out one record for each itemset,
		 * so there isn't much to compress, I'd say. MR
		 *
		 * In Amazon MapReduce compression of the map output seems to be
		 * happen by default and the Snappy codec is used, which is
		 * extremely fast.
		 */
		conf.setBoolean("mapred.compress.map.output", true); 
		//conf.setMapOutputCompressorClass(com.hadoop.compression.lzo.LzoCodec.class);

		conf.setJarByClass(MRDriver.class);
			
		conf.setMapOutputKeyClass(IntWritable.class); 
		conf.setMapOutputValueClass(Text.class); 
			
		conf.setOutputKeyClass(Text.class); 
		conf.setOutputValueClass(DoubleWritable.class); 

		conf.setInputFormat(SequenceFileInputFormat.class);
		// We write the collections found in a reducers as a SequenceFile 
		conf.setOutputFormat(SequenceFileOutputFormat.class);
		SequenceFileOutputFormat.setOutputPath(conf, new Path(args[9]));

		
		// set the mapper class based on command line option
		switch(Integer.parseInt(args[7]))
		{
			case 1:
				System.out.println("running partition mapper..."); 
				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
				conf.setMapperClass(PartitionMapper.class);
				break;
			case 2:
				System.out.println("running binomial mapper..."); 
				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
				conf.setMapperClass(BinomialSamplerMapper.class);
				break;
			case 3:
				System.out.println("running coin mapper..."); 
				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
				conf.setMapperClass(CoinFlipSamplerMapper.class);
			case 4:
				System.out.println("running sampler mapper..."); 
				SequenceFileInputFormat.addInputPath(conf, new Path(args[8]));
				conf.setMapperClass(InputSamplerMapper.class);

				// create a random sample of size T*m
				rand = new Random();
				job_start_time = System.nanoTime(); 
				int[] samples = new int[numSamples * sampleSize];
				for (int i = 0; i < numSamples * sampleSize; i++)
				{
					samples[i] = rand.nextInt(datasetSize);
				}

				// for each key in the sample, create a list of all T samples to which this key belongs
				Hashtable<LongWritable, ArrayList<IntWritable>> hashTable = new Hashtable<LongWritable, ArrayList<IntWritable>>();
				for (int i=0; i < numSamples * sampleSize; i++) 
				{
					ArrayList<IntWritable> sampleIDs = null;
					LongWritable key = new LongWritable(samples[i]);
					if (hashTable.containsKey(key))  
						sampleIDs = hashTable.get(key);
					else
						sampleIDs = new ArrayList<IntWritable>();
					sampleIDs.add(new IntWritable(i % numSamples));
					hashTable.put(key, sampleIDs);
				}

				/*
				 * Convert the Hastable to a MapWritable which we will
				 * write to HDFS and distribute to all Mappers using
				 * DistributedCache
				 */
				MapWritable map = new MapWritable();
				for (LongWritable key : hashTable.keySet())
				{
					ArrayList<IntWritable> sampleIDs = hashTable.get(key);
					IntArrayWritable sampleIDsIAW = new IntArrayWritable();
					sampleIDsIAW.set(sampleIDs.toArray(new IntWritable[sampleIDs.size()]));
					map.put(key, sampleIDsIAW);
				}

				fs = FileSystem.get(URI.create("samplesMap.ser"), conf);
				samplesMapPath = new Path("samplesMap.ser");
				FSDataOutputStream out = fs.create(samplesMapPath, true);
				map.write(out);
				out.sync();
				out.close();
				DistributedCache.addCacheFile(new URI(fs.getWorkingDirectory() + "/samplesMap.ser#samplesMap.ser"), conf);
				// stop the sampling timer	
				job_end_time = System.nanoTime(); 
				job_runtime = (job_end_time-job_start_time) / 1000000; 
				System.out.println("sampling runtime (milliseconds): " + job_runtime);	
				break; // end switch case
			case 5:	
				System.out.println("running random integer partition mapper..."); 
				conf.setInputFormat(WholeSplitInputFormat.class);
				Path inputFilePath = new Path(args[8]);
				WholeSplitInputFormat.addInputPath(conf, inputFilePath);
				conf.setMapperClass(RandIntPartSamplerMapper.class);
				// Compute number of map tasks.
				fs = inputFilePath.getFileSystem(conf);
				FileStatus inputFileStatus = fs.getFileStatus(inputFilePath);
				long len = inputFileStatus.getLen();
				long blockSize = inputFileStatus.getBlockSize();
				conf.setLong("mapred.min.split.size", blockSize);
				conf.setLong("mapred.max.split.size", blockSize);
				int mapTasksNum = ((int) (len / blockSize)) + 1;
				conf.setNumMapTasks(mapTasksNum);
				//System.out.println("len: " + len + " blockSize: " 
				//		+ blockSize + " mapTasksNum: " + mapTasksNum);
				// Extract random integer partition of total sample
				// size into up to mapTasksNum partitions.
				// XXX I'm not sure this is a correct way to do
				// it.
				rand = new Random();
				IntWritable[][] toSampleArr = new IntWritable[mapTasksNum][numSamples];
				for (int j = 0; j < numSamples; j++)
				{
					IntWritable[] tempToSampleArr = new IntWritable[mapTasksNum];
					int sum = 0;
					int i;
					for (i = 0; i < mapTasksNum -1; i++)
					{
						int size = rand.nextInt(sampleSize - sum);
						tempToSampleArr[i] = new IntWritable(size);
						sum += size;
						if (sum > numSamples * sampleSize)
						{
							System.out.println("Something went wrong generating the sample Sizes");
							System.exit(1);
						}
						if (sum == sampleSize)
						{
							break;
						}
					}
					if (i == mapTasksNum -1) 
					{
						tempToSampleArr[i] = new IntWritable(sampleSize - sum);
					}
					else 
					{
						for (; i < mapTasksNum; i++)
						{
							tempToSampleArr[i] = new IntWritable(0); 
						}
					}
					Collections.shuffle(Arrays.asList(tempToSampleArr));
					for (i = 0; i < mapTasksNum; i++)
					{
						toSampleArr[i][j] = tempToSampleArr[i];
					}
				}

				for (int i = 0; i < mapTasksNum; i++)
				{
					DefaultStringifier.storeArray(conf, toSampleArr[i], "PARMM.toSampleArr_" + i);
				}
				break;
			default:
				System.err.println("Wrong Mapper ID. Can only be in [1,5]");
				System.exit(1);
				break;
		}
		
		/*
		 * We don't use the default hash partitioner because we want to
		 * maximize the parallelism. That's why we also fix the number
		 * of reducers.
		 */
		conf.setPartitionerClass(FIMPartitioner.class);

		conf.setReducerClass(FIMReducer.class);
			

	
		/************************ Job 2 (aggregation) Configuration ************************/
		
		JobConf confAggr = new JobConf(getConf());

		confAggr.setInt("PARMM.reducersNum", numSamples);
		confAggr.setInt("PARMM.reqApproxNum", reqApproxNum);
		confAggr.setInt("PARMM.sampleSize", sampleSize);
		confAggr.setFloat("PARMM.epsilon", epsilon);

		// XXX: Why do we disable speculative execution? MR
		confAggr.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		confAggr.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 

		confAggr.setJarByClass(MRDriver.class);
			
		confAggr.setMapOutputKeyClass(Text.class); 
		confAggr.setMapOutputValueClass(DoubleWritable.class); 
			
		confAggr.setOutputKeyClass(Text.class); 
		confAggr.setOutputValueClass(Text.class); 
			
		confAggr.setMapperClass(AggregateMapper.class);
		confAggr.setReducerClass(AggregateReducer.class);
			
		confAggr.setInputFormat(SequenceFileInputFormat.class);
		SequenceFileInputFormat.addInputPath(confAggr, new Path(args[9]));

		FileOutputFormat.setOutputPath(confAggr, new Path(args[10]));


		job_start_time = System.nanoTime(); 
		RunningJob FIMjob = JobClient.runJob(conf);
		job_end_time = System.nanoTime(); 

		RunningJob aggregateJob = JobClient.runJob(confAggr);
		long job2_end_time = System.nanoTime(); 
			
		job_runtime = (job_end_time-job_start_time) / 1000000; 
			
		System.out.println("local FIM runtime (milliseconds): " + job_runtime);	

		long job2_runtime = (job2_end_time-job_end_time) / 1000000; 
			
		System.out.println("aggregation runtime (milliseconds): " +
				job2_runtime); 

		if (args[7].equals("4")) {
			// Remove samplesMap file 
			fs.delete(samplesMapPath, false);
		}

		Counters counters = FIMjob.getCounters();
		Counters.Group FIMMapperStartTimesCounters = counters.getGroup("FIMMapperStart");
		long[] FIMMapperStartTimes = new long[FIMMapperStartTimesCounters.size()];
		int i = 0;
		for (Counters.Counter counter : FIMMapperStartTimesCounters)
		{
			FIMMapperStartTimes[i++] = counter.getCounter();
		}

		Counters.Group FIMMapperEndTimesCounters = counters.getGroup("FIMMapperEnd");
		long[] FIMMapperEndTimes = new long[FIMMapperEndTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : FIMMapperEndTimesCounters)
		{
			FIMMapperEndTimes[i++] = counter.getCounter();
		}

		Counters.Group FIMReducerStartTimesCounters = counters.getGroup("FIMReducerStart");
		long[] FIMReducerStartTimes = new long[FIMReducerStartTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : FIMReducerStartTimesCounters)
		{
			FIMReducerStartTimes[i++] = counter.getCounter();
		}

		Counters.Group FIMReducerEndTimesCounters = counters.getGroup("FIMReducerEnd");
		long[] FIMReducerEndTimes = new long[FIMReducerEndTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : FIMReducerEndTimesCounters)
		{
			FIMReducerEndTimes[i++] = counter.getCounter();
		}

		counters = aggregateJob.getCounters();
		Counters.Group AggregateMapperStartTimesCounters = counters.getGroup("AggregateMapperStart");
		long[] AggregateMapperStartTimes = new long[AggregateMapperStartTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : AggregateMapperStartTimesCounters)
		{
			AggregateMapperStartTimes[i++] = counter.getCounter();
		}

		Counters.Group AggregateMapperEndTimesCounters = counters.getGroup("AggregateMapperEnd");
		long[] AggregateMapperEndTimes = new long[AggregateMapperEndTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : AggregateMapperEndTimesCounters)
		{
			AggregateMapperEndTimes[i++] = counter.getCounter();
		}

		Counters.Group AggregateReducerStartTimesCounters = counters.getGroup("AggregateReducerStart");
		long[] AggregateReducerStartTimes = new long[AggregateReducerStartTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : AggregateReducerStartTimesCounters)
		{
			AggregateReducerStartTimes[i++] = counter.getCounter();
		}

		Counters.Group AggregateReducerEndTimesCounters = counters.getGroup("AggregateReducerEnd");
		long[] AggregateReducerEndTimes = new long[AggregateReducerEndTimesCounters.size()];
		i = 0;
		for (Counters.Counter counter : AggregateReducerEndTimesCounters)
		{
			AggregateReducerEndTimes[i++] = counter.getCounter();
		}

		long FIMMapperStartMin = FIMMapperStartTimes[0];
		for (long l : FIMMapperStartTimes)
		{
			if (l < FIMMapperStartMin)
			{
				FIMMapperStartMin = l;
			}
		}
		long FIMMapperEndMax= FIMMapperEndTimes[0];
		for (long l : FIMMapperEndTimes)
		{
			if (l > FIMMapperEndMax)
			{
				FIMMapperEndMax = l;
			}
		}
		System.out.println("FIMMapper total runtime (milliseconds): " + (FIMMapperEndMax - FIMMapperStartMin));
		long[] FIMMapperRunTimes = new long[FIMMapperStartTimes.length];
		long FIMMapperRunTimesSum = 0;
		for (int l = 0; l < FIMMapperStartTimes.length; l++)
		{
			FIMMapperRunTimes[l] = FIMMapperEndTimes[l] - FIMMapperStartTimes[l];
			FIMMapperRunTimesSum += FIMMapperRunTimes[l];
		}
		System.out.println("FIMMapper average task runtime (milliseconds): " + FIMMapperRunTimesSum / FIMMapperStartTimes.length );
		long FIMMapperRunTimesMin = FIMMapperRunTimes[0];
		long FIMMapperRunTimesMax = FIMMapperRunTimes[0];
		for (long l : FIMMapperRunTimes)
		{
			if (l < FIMMapperRunTimesMin)
			{
				FIMMapperRunTimesMin = l;
			}
			if (l > FIMMapperRunTimesMax)
			{
				FIMMapperRunTimesMax = l;
			}
		}
		System.out.println("FIMMapper minimum task runtime (milliseconds): " + FIMMapperRunTimesMin);
		System.out.println("FIMMapper maximum task runtime (milliseconds): " + FIMMapperRunTimesMax);

		long FIMReducerStartMin = FIMReducerStartTimes[0];
		for (long l : FIMReducerStartTimes)
		{
			if (l < FIMReducerStartMin)
			{
				FIMReducerStartMin = l;
			}
		}
		long FIMReducerEndMax= FIMReducerEndTimes[0];
		for (long l : FIMReducerEndTimes)
		{
			if (l > FIMReducerEndMax)
			{
				FIMReducerEndMax = l;
			}
		}
		System.out.println("1st round shuffle phase runtime (milliseconds): " + (FIMReducerStartMin - FIMMapperEndMax));
		System.out.println("FIMReducer total runtime (milliseconds): " + (FIMReducerEndMax - FIMReducerStartMin));
		long[] FIMReducerRunTimes = new long[FIMReducerStartTimes.length];
		long FIMReducerRunTimesSum = 0;
		for (int l = 0; l < FIMReducerStartTimes.length; l++)
		{
			FIMReducerRunTimes[l] = FIMReducerEndTimes[l] - FIMReducerStartTimes[l];
			FIMReducerRunTimesSum += FIMReducerRunTimes[l];
		}
		System.out.println("FIMReducer average task runtime (milliseconds): " + FIMReducerRunTimesSum / FIMReducerStartTimes.length);
		long FIMReducerRunTimesMin = FIMReducerRunTimes[0];
		long FIMReducerRunTimesMax = FIMReducerRunTimes[0];
		for (long l : FIMReducerRunTimes)
		{
			if (l < FIMReducerRunTimesMin)
			{
				FIMReducerRunTimesMin = l;
			}
			if (l > FIMReducerRunTimesMax)
			{
				FIMReducerRunTimesMax = l;
			}
		}
		System.out.println("FIMReducer minimum task runtime (milliseconds): " + FIMReducerRunTimesMin);
		System.out.println("FIMReducer maximum task runtime (milliseconds): " + FIMReducerRunTimesMax);

		long AggregateMapperStartMin = AggregateMapperStartTimes[0];
		for (long l : AggregateMapperStartTimes)
		{
			if (l < AggregateMapperStartMin)
			{
				AggregateMapperStartMin = l;
			}
		}
		long AggregateMapperEndMax = AggregateMapperEndTimes[0];
		for (long l : AggregateMapperEndTimes)
		{
			if (l > AggregateMapperEndMax)
			{
				AggregateMapperEndMax = l;
			}
		}
		System.out.println("1st-2nd round phase runtime (milliseconds): " + (AggregateMapperStartMin - FIMReducerEndMax));
		System.out.println("AggregateMapper total runtime (milliseconds): " + (AggregateMapperEndMax - AggregateMapperStartMin));
		long[] AggregateMapperRunTimes = new long[AggregateMapperStartTimes.length];
		long AggregateMapperRunTimesSum = 0;
		for (int l = 0; l < AggregateMapperStartTimes.length; l++)
		{
			AggregateMapperRunTimes[l] = AggregateMapperEndTimes[l] - AggregateMapperStartTimes[l];
			AggregateMapperRunTimesSum += AggregateMapperRunTimes[l];
		}
		System.out.println("AggregateMapper average task runtime (milliseconds): " + AggregateMapperRunTimesSum / AggregateMapperStartTimes.length);
		long AggregateMapperRunTimesMin = AggregateMapperRunTimes[0];
		long AggregateMapperRunTimesMax = AggregateMapperRunTimes[0];
		for (long l : AggregateMapperRunTimes)
		{
			if (l < AggregateMapperRunTimesMin)
			{
				AggregateMapperRunTimesMin = l;
			}
			if (l > AggregateMapperRunTimesMax)
			{
				AggregateMapperRunTimesMax = l;
			}
		}
		System.out.println("AggregateMapper minimum task runtime (milliseconds): " + AggregateMapperRunTimesMin);
		System.out.println("AggregateMapper maximum task runtime (milliseconds): " + AggregateMapperRunTimesMax);

		long AggregateReducerStartMin = AggregateReducerStartTimes[0];
		for (long l : AggregateReducerStartTimes)
		{
			if (l < AggregateReducerStartMin)
			{
				AggregateReducerStartMin = l;
			}
		}
		long AggregateReducerEndMax= AggregateReducerEndTimes[0];
		for (long l : AggregateReducerEndTimes)
		{
			if (l > AggregateReducerEndMax)
			{
				AggregateReducerEndMax = l;
			}
		}
		System.out.println("2nd round shuffle phase runtime (milliseconds): " + (AggregateReducerStartMin - AggregateMapperEndMax));
		System.out.println("AggregateReducer total runtime (milliseconds): " + (AggregateReducerEndMax - AggregateReducerStartMin));
		long[] AggregateReducerRunTimes = new long[AggregateReducerStartTimes.length];
		long AggregateReducerRunTimesSum = 0;
		for (int l = 0; l < AggregateReducerStartTimes.length; l++)
		{
			AggregateReducerRunTimes[l] = AggregateReducerEndTimes[l] - AggregateReducerStartTimes[l];
			AggregateReducerRunTimesSum += AggregateReducerRunTimes[l];
		}
		System.out.println("AggregateReducer average task runtime (milliseconds): " + AggregateReducerRunTimesSum / AggregateReducerStartTimes.length);
		long AggregateReducerRunTimesMin = AggregateReducerRunTimes[0];
		long AggregateReducerRunTimesMax = AggregateReducerRunTimes[0];
		for (long l : AggregateReducerRunTimes)
		{
			if (l < AggregateReducerRunTimesMin)
			{
				AggregateReducerRunTimesMin = l;
			}
			if (l > AggregateReducerRunTimesMax)
			{
				AggregateReducerRunTimesMax = l;
			}
		}
		System.out.println("AggregateReducer minimum task runtime (milliseconds): " + AggregateReducerRunTimesMin);
		System.out.println("AggregateReducer maximum task runtime (milliseconds): " + AggregateReducerRunTimesMax);

		return 0;
	}
}

