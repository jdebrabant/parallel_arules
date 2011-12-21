import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class AggregateReducer extends MapReduceBase 
	implements Reducer<Text, DoubleWritable, Text, DoubleWritable>
{
	public static final int REDUCER_NUM = 64; 
	public static final int REQUIRED_NUM = REDUCER_NUM / 2 + 1;
	public static final double EPSILON = 0.02;

	@Override
	public void reduce(Text itemset, Iterator<DoubleWritable> values, 
			OutputCollector<Text,DoubleWritable> output, 
			Reporter reporter) throws IOException
	{
		ArrayList<Double> valuesArrList = new ArrayList<Double>();
		while (values.hasNext()) 
		{
			valuesArrList.add((values.next()).get());
		}
		/**
		 * Only consider the itemset as "global frequent" if it
		 * appears among the "local frequent" itemsets at least
		 * REQUIRED_NUM times.
		 */
		if (valuesArrList.size() >= REQUIRED_NUM)
		{
			Double[] valuesArr = (Double[]) valuesArrList.toArray();
			Arrays.sort(valuesArr);

			/**
			 * Compute the smallest frequency interval containing
			 * REQUIRED_NUM estimates of the frequency of the
			 * itemset. Use the center of this interval as global
			 * estimate for the frequency. The confidence interval
			 * is obtained by enlarging the above interva by EPSILON
			 * / 2 on both sides.
			 *
			 * XXX It seems to me that  it would actually be enough to
			 * require that the interval contains REDUCER_NUM / 2 +
			 * 1 estimates, independently on REQUIRED_NUM. MR
			 */
			double minIntervalLength = valuesArr[REQUIRED_NUM - 1] - valuesArr[0];
			double estimatedFreq = valuesArr[0] + minIntervalLength / 2;
			double confIntervalLowBound = valuesArr[0] - EPSILON / 2;
			double confIntervalUppBound = valuesArr[REQUIRED_NUM -1] + EPSILON / 2;
			for (int i = 1; i < valuesArr.length - REQUIRED_NUM; i++)
			{
				double intervalLength = valuesArr[REQUIRED_NUM + i] - valuesArr[i];
				if (intervalLength < minIntervalLength) 
				{
					minIntervalLength = intervalLength;
					estimatedFreq = valuesArr[i] + minIntervalLength / 2;
					confIntervalLowBound = valuesArr[i] - EPSILON / 2;
					confIntervalUppBound = valuesArr[REQUIRED_NUM + i] + EPSILON / 2;
				}
			}
			
			/**
			 * XXX We need to fix this output to include the bounds
			 * for the confidence interval. MR
			 */
			output.collect(itemset, new DoubleWritable(estimatedFreq));
		} // end if (valuesArrList.size() >= REQUIRED_NUM)
	}
}


