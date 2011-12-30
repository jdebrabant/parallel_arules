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

public class MRAprioriDriver extends Configured implements Tool
{
		
	public static void main(String args[]) throws Exception
	{
		if (args.length < 2)
		{
			System.out.println("usage: <path to input database> <path to output global FIs>");
			System.exit(1); 
		}

		int res = ToolRunner.run(new MRAprioriDriver(), args);

		System.exit(res);
	}

	public int run(String args[]) throws Exception
	{
		long job_start_time, job_end_time; 
		long job_runtime; 
		
		JobConf conf = new JobConf(getConf()); 
			
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
		conf.setInt("mapred.task.timeout", 60000000); 

		conf.setJarByClass(MRAprioriDriver.class);
			
		conf.setMapOutputKeyClass(Text.class); 
		conf.setMapOutputValueClass(IntWritable.class); 
			
		conf.setOutputKeyClass(Text.class); 
		conf.setOutputValueClass(DoubleWritable.class); 
			
		conf.setMapperClass(MRAprioriMapper.class);
		conf.setCombinerClass(MRAprioriCombiner.class);
		conf.setReducerClass(MRAprioriReducer.class);
			
		FileInputFormat.addInputPath(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
			
		job_start_time = System.currentTimeMillis(); 
		JobClient.runJob(conf);
		job_end_time = System.currentTimeMillis(); 
			
		job_runtime = (job_end_time-job_start_time) / 1000; 
			
		System.out.println("total job runtime (seconds): " + job_runtime); 

		return 0;
	}
	
}
