import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class AggregateReducer extends MapReduceBase 
	implements Reducer<Text, DoubleWritable, Text, Text>
{
	int id;
	int reducersNum;
	int reqApproxNum;
	int sampleSize;
	float epsilon;

	@Override
	public void configure(JobConf conf) 
	{
		reducersNum = conf.getInt("PARMM.reducersNum", 64);
		reqApproxNum = conf.getInt("PARMM.reqApproxNum", reducersNum / 2 +1);
		epsilon = conf.getFloat("PARMM.epsilon", (float) 0.02);
		sampleSize = conf.getInt("PARMM.sampleSize", 1000);
		id = conf.getInt("mapred.task.partition", -1);
	}



	@Override
	public void reduce(Text itemset, Iterator<DoubleWritable> values, 
			OutputCollector<Text,Text> output, 
			Reporter reporter) throws IOException
	{
		reporter.incrCounter("AggregateReducerStart", id, System.nanoTime());

		ArrayList<Double> valuesArrList = new ArrayList<Double>(reqApproxNum);
		while (values.hasNext()) 
		{
			valuesArrList.add(new Double(values.next().get()));
		}
		//System.out.println("Itemset: " + itemset.toString() + " in: " + valuesArrList.size());
		/**
		 * Only consider the itemset as "global frequent" if it
		 * appears among the "local frequent" itemsets a sufficient
		 * number of times.
		 */
		if (valuesArrList.size() >= reqApproxNum)
		{
			Double[] valuesArr = new Double[valuesArrList.size()];
			valuesArr = valuesArrList.toArray(valuesArr);
			Arrays.sort(valuesArr);

			/**
			 * Compute the smallest frequency interval containing
			 * reducersNum-requiredApproxNum+1 estimates of the
			 * frequency of the itemset. Use the center of this
			 * interval as global estimate for the frequency. The
			 * confidence interval is obtained by enlarging the
			 * above interval by epsilon/2 on both sides.
			 */
			double minIntervalLength = valuesArr[reducersNum-reqApproxNum] - valuesArr[0];
			int startIndex = 0;
			for (int i = 1; i < valuesArr.length - reducersNum + reqApproxNum; i++)
			{
				double intervalLength = valuesArr[reducersNum-reqApproxNum + i] - valuesArr[i];
				if (intervalLength < minIntervalLength) 
				{
					minIntervalLength = intervalLength;
					startIndex = i;
				}
			}
			
			double estimatedFreq = (valuesArr[startIndex] + ((double) (valuesArr[startIndex + reducersNum -
					 reqApproxNum] - valuesArr[startIndex])/2)) / sampleSize;
			double confIntervalLowBound = Math.max(0, valuesArr[startIndex] / sampleSize - epsilon / 2);
			double confIntervalUppBound = Math.min(1, valuesArr[startIndex + reducersNum - reqApproxNum] / sampleSize + epsilon / 2);
			
			String estFreqAndBoundsStr = "(" + estimatedFreq + "," + confIntervalLowBound + "," + confIntervalUppBound + ")"; 
			output.collect(itemset, new Text(estFreqAndBoundsStr));
		} // end if (valuesArrList.size() >= requiredNum)

		reporter.incrCounter("AggregateReducerEnd", id, System.nanoTime());
	}
}


