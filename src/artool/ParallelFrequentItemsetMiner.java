import java.util.*; 
import java.io.*; 
import java.lang.Math;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

import laur.dm.ar.*;
import laur.tools.Timer;


public class ParallelFrequentItemsetMiner
{
		
	public static void main(String args[])
	{
		long job_start_time, job_end_time; 
		long job_runtime; 
		
		if (args.length != 2)
		{
			System.out.println("usage: <path to input database> <path to output files>");
			System.exit(1); 
		}
		
		try 
		{
			
			
			Configuration conf = new Configuration(); 
			conf.setInt("mapreduce.task.timeout", 6000000); 
			
			Job job = new Job(conf);
			
			job.setJarByClass(ParallelFrequentItemsetMiner.class);
			
			job.setMapOutputKeyClass(IntWritable.class); 
			job.setMapOutputValueClass(Text.class); 
			
			job.setOutputKeyClass(Text.class); 
			job.setOutputValueClass(IntWritable.class); 
			
			job.setMapperClass(MapClass.class);
			job.setReducerClass(ReduceClass.class);
			
			FileInputFormat.setInputPaths(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
			job_start_time = System.currentTimeMillis(); 
			job.waitForCompletion(true);
			job_end_time = System.currentTimeMillis(); 
			
			job_runtime = (job_end_time-job_start_time) / 1000; 
			
			System.out.println("total job runtime (seconds): " + job_runtime); 
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
}





