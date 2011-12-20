import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

public class ParallelFrequentItemsetMiner extends Configured implements Tool
{
		
	public static void main(String args[]) throws Exception
	{
		if (args.length != 2)
		{
			System.out.println("usage: <path to input database> <path to output files>");
			System.exit(1); 
		}

		int res = ToolRunner.run(new Configuration(), new ParallelFrequentItemsetMiner(), args);

		System.exit(res);
	}

	public int run(String args[]) throws Exception
	{
		long job_start_time, job_end_time; 
		long job_runtime; 
		int ret;
		
		Configuration conf = getConf(); 
			
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		conf.setInt("mapred.task.timeout", 60000000); 

		Job job = new Job(conf);

		job.setJarByClass(ParallelFrequentItemsetMiner.class);
			
		job.setMapOutputKeyClass(IntWritable.class); 
		job.setMapOutputValueClass(Text.class); 
			
		job.setOutputKeyClass(Text.class); 
		job.setOutputValueClass(IntWritable.class); 
			
		//job.setMapperClass(MapClass.class);
		//job.setMapperClass(MapClassBinomialSampler.class);
		job.setMapperClass(MapClassCoinFlipSampler.class);
		job.setReducerClass(ReduceClass.class);
			
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
		job_start_time = System.currentTimeMillis(); 
		job.waitForCompletion(true);
		job_end_time = System.currentTimeMillis(); 
			
		job_runtime = (job_end_time-job_start_time) / 1000; 
			
		System.out.println("total job runtime (seconds): " + job_runtime); 

		if (job.isSuccessful())
		{
			ret = 0;
		} 
		else
		{
			ret = 1;
		}
		return ret;
	}
	
}

