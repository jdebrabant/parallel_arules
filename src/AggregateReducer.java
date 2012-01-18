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
	}



	@Override
	public void reduce(Text itemset, Iterator<DoubleWritable> values, 
			OutputCollector<Text,Text> output, 
			Reporter reporter) throws IOException
	{
		ArrayList<Double> valuesArrList = new ArrayList<Double>();
		while (values.hasNext()) 
		{
			String valueString = (values.next()).toString();
			valuesArrList.add(Double.parseDouble(valueString));
		}
		/**
		 * Only consider the itemset as "global frequent" if it
		 * appears among the "local frequent" itemsets a sufficient
		 * number of times.
		 */
		if (valuesArrList.size() >= reqApproxNum)
		{
			Double[] valuesArr = new Double[valuesArrList.size()];
			valuesArrList.toArray(valuesArr);
			Arrays.sort(valuesArr);

			/**
			 * Compute the smallest frequency interval containing
			 * requiredNum estimates of the frequency of the
			 * itemset. Use the center of this interval as global
			 * estimate for the frequency. The confidence interval
			 * is obtained by enlarging the above interval by
			 * epsilon/2 on both sides.
			 */
			int intervalPoints = reducersNum - reqApproxNum + 1;
			double minIntervalLength = valuesArr[intervalPoints- 1] - valuesArr[0];
			double estimatedFreq = (valuesArr[0] + minIntervalLength / 2);
			for (int i = 1; i < valuesArr.length - intervalPoints; i++)
			{
				double intervalLength = valuesArr[intervalPoints + i] - valuesArr[i];
				if (intervalLength < minIntervalLength) 
				{
					minIntervalLength = intervalLength;
					estimatedFreq = valuesArr[i] + minIntervalLength / 2;
				}
			}
			double confIntervalLowBound = (estimatedFreq - minIntervalLength / 2) / sampleSize - epsilon / 2;
			double confIntervalUppBound = (estimatedFreq + minIntervalLength / 2) / sampleSize + epsilon / 2;
			estimatedFreq = estimatedFreq / sampleSize;
			
			String estFreqAndBoundsStr = "(" + estimatedFreq + "," + confIntervalLowBound + "," + confIntervalUppBound + ")"; 
			output.collect(itemset, new Text(estFreqAndBoundsStr));
		} // end if (valuesArrList.size() >= requiredNum)
	}
}


