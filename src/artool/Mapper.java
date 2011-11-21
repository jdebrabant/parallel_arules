import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputController;
import org.apache.hadoop.mapred.Reporter;


public class RandomPartitionMapper extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>{

	public void map(LongWritable lineNum, Text value, OutputCollecter<IntWritable, Text> output, Reporter reporter) throws IOException
	{
		int key = lineNum % REDUCER_NUM;
		output.collect(new IntWritable(key), value);
	}
}
