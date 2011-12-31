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

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MRDriver extends Configured implements Tool
{
	public final int MR_TIMEOUT_MILLI = 60000000;
	
	public static void main(String args[]) throws Exception
	{
		if (args.length != 4)
		{
			System.out.println("usage: java MRDriver <mapper id> <path to input database> " + 
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
		
		/************************ Job 1 (local FIM) Configuration ************************/
		
		JobConf conf = new JobConf(getConf()); 
			
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		conf.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 

		conf.setJarByClass(MRDriver.class);
			
		conf.setMapOutputKeyClass(IntWritable.class); 
		conf.setMapOutputValueClass(Text.class); 
			
		conf.setOutputKeyClass(Text.class); 
		conf.setOutputValueClass(DoubleWritable.class); 
		
		// set the mapper classs based on command line option
		if(args[0].equals("1"))
		{
			conf.setMapperClass(PartitionMapper.class);
		}
		else if(args[0].equals("2"))
		{
			conf.setMapperClass(BinomialSamplerMapper.class);
		}
		else if(args[0].equals("3"))
		{
			conf.setMapperClass(CoinFlipSamplerMapper.class);
		}
		
		conf.setReducerClass(FIMReducer.class);
			
		FileInputFormat.addInputPath(conf, new Path(args[1]));
		FileOutputFormat.setOutputPath(conf, new Path(args[2]));
			
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

		KeyValueTextInputFormat.addInputPath(confAggr, new Path(args[2]));
		FileOutputFormat.setOutputPath(confAggr, new Path(args[3]));

		job_start_time = System.currentTimeMillis(); 
		JobClient.runJob(confAggr);
		job_end_time = System.currentTimeMillis(); 
			
		job_runtime = (job_end_time-job_start_time) / 1000; 
			
		System.out.println("aggregation runtime (seconds): " +
				job_runtime); 

		return 0;
	}
}

