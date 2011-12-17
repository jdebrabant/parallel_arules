
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

public class MapClass extends Mapper<LongWritable, Text, IntWritable, Text>
{
	public static final int REDUCER_NUM = 64; 
	
	@Override
	public void map(LongWritable lineNum, Text value, Context context) throws IOException, InterruptedException
	{
		Random rand = new Random();
		
		try
		{
			int key = (int)(rand.nextInt() % REDUCER_NUM);
			context.write(new IntWritable(key), value);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
}
