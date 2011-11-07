/*
  SyntheticDataGenerator.java
   
  (P)1999-2001 Laurentiu Cristofor
*/

/*

laur.dm.ar - A Java package for association rule mining 
Copyright (C) 2002  Laurentiu Cristofor


This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA


The laur.dm.ar package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.dm.ar;

import laur.rand.*;

import java.util.*;

/**

   This class implements a synthetic data generator that generates
   data by simulating transactions in a supermarket. The algorithm is
   described in the article "Fast Algorithms for Mining Association
   Rules" by Rakesh Agrawal and Ramakrishnan Srikant from IBM Almaden
   Research Center, 1994. I have also used as additional information
   the C++ source code of the generator that is kindly distributed by
   Mr. Rakesh Agrawal, and the Master Thesis of Mr. Andreas Mueller.

   @version 1.0
   @author Laurentiu Cristofor
   
*/
public class SyntheticDataGenerator
{
  private long num_transactions;
  private int avg_transaction_size;
  private int num_large_itemsets;
  private int avg_large_itemset_size;
  private int num_items;
  private double correlation_mean;
  private double corruption_mean;

  private long current_transaction;
  private Random rand_item;
  private Random rand_large;
  private Random rand_sampling;
  private Random rand_transaction;
  private RandomPoissonDistribution poisson_transaction_size;

  private class LargeItemset
  {
    Itemset is;
    double weight;
    double corruption;
  }

  private ArrayList large_itemsets;
  private double[] item_probabilities;
  private int last_large;

  /**
   * Create a new synthetic data generator with mean correlation 0.5
   * and mean corruption 0.5.
   *
   * @param num_transactions   the number of transactions to generate
   * @param avg_transaction_size   the average size of a transaction
   * @param num_large_itemsets   the number of large itemsets to be used
   * as patterns in the generation of transactions
   * @param avg_large_itemset_size   the average size of a large itemset
   * @param num_items   the number of items to appear in transactions
   * @exception  IllegalArgumentException   if the integer arguments
   * are not strictly positive
   */
  public SyntheticDataGenerator(long num_transactions,
				int avg_transaction_size,
				int num_large_itemsets,
				int avg_large_itemset_size,
				int num_items)
  {
    this (num_transactions,
	  avg_transaction_size,
	  num_large_itemsets,
	  avg_large_itemset_size,
	  num_items, 
	  0.5,
	  0.5);
  }

  /**
   * Create a new synthetic data generator.
   *
   * @param num_transactions   the number of transactions to generate
   * @param avg_transaction_size   the average size of a transaction
   * @param num_large_itemsets   the number of large itemsets to be used
   * as patterns in the generation of transactions
   * @param avg_large_itemset_size   the average size of a large itemset
   * @param num_items   the number of items to appear in transactions
   * @param correlation_mean   the mean correlation between the large
   * itemsets
   * @param corruption_mean   the mean of the corruption coefficient
   * that will indicate how much a large itemset will be corrupted before
   * being used.
   * @exception  IllegalArgumentException   if the integer arguments
   * are not strictly positive or if the floating point arguments are
   * not between 0 and 1.
   */
  public SyntheticDataGenerator(long num_transactions,
				int avg_transaction_size,
				int num_large_itemsets,
				int avg_large_itemset_size,
				int num_items,
				double correlation_mean,
				double corruption_mean)
  {
    if (num_transactions < 1
	|| avg_transaction_size < 1
	|| num_large_itemsets < 1
	|| avg_large_itemset_size < 1
	|| num_items < 1
	|| correlation_mean < 0 || correlation_mean > 1
	|| corruption_mean < 0 || corruption_mean > 1
	|| avg_transaction_size > num_items
	|| avg_large_itemset_size > num_items)
      throw new IllegalArgumentException("Invalid arguments!");

    this.num_transactions = num_transactions;
    this.avg_transaction_size = avg_transaction_size;
    this.num_large_itemsets = num_large_itemsets;
    this.avg_large_itemset_size = avg_large_itemset_size;
    this.num_items = num_items;
    this.correlation_mean = correlation_mean;
    this.corruption_mean = corruption_mean;

    current_transaction = 0;

    poisson_transaction_size 
      = new RandomPoissonDistribution(avg_transaction_size - 1);

    // used for selecting a random large itemset
    rand_large = new Random();
    // used by RandomSample
    rand_sampling = new Random();
    // used in getNextTransaction
    rand_transaction = new Random();

    initLargeItemsets();
  }

  /**
   * Tell whether there are more transactions to generate.
   *
   * @return   true if there are more transactions, false otherwise
   */
  public boolean hasMoreTransactions()
  {
    return (current_transaction < num_transactions);
  }

  /**
   * Get next transaction.
   *
   * @exception  NoSuchElementException   if all transactions were generated
   * @return   an Itemset representing the transaction
   */
  public Itemset getNextTransaction()
  {
    if (current_transaction >= num_transactions)
      throw new NoSuchElementException("No more transactions to generate!");

    // the transaction size is obtained from a Poisson distribution
    // with mean avg_transaction_size
    int transaction_size = (int)poisson_transaction_size.nextLong() + 1;

    if (transaction_size > num_items)
      transaction_size = num_items;

    Itemset transaction = new Itemset(transaction_size);

    while (transaction.size() < transaction_size)
      {
	LargeItemset pattern = nextRandomLargeItemset();

	// we corrupt the pattern by reducing its size for as long
	// as a uniformly distributed random number is less than
	// the corruption of the large itemset.
	int pattern_length = pattern.is.size();
	while (pattern_length > 0
	       && rand_transaction.nextDouble() < pattern.corruption)
	  pattern_length--;

	// in case the large itemset does not fit in the transaction
	// we will put the itemset in the transaction anyway in 50%
	// of the cases, and in the rest we'll keep the itemset for
	// the next transaction
	if (pattern_length + transaction.size() > transaction_size)
	  if (transaction.size() > 0 && rand_transaction.nextDouble() < 0.5)
	    {
	      // keep the itemset for next transaction
	      ungetRandomLargeItemset();
	      break;
	    }

	// now we have to pick pattern_length items at random from
	// the pattern
	if (pattern_length > 0)
	  {
	    RandomSample rand_sample = new RandomSample(pattern.is.size(),
							pattern_length,
							rand_sampling);
	    long[] sample = rand_sample.nextSample();
	    for (int j = 0; j < sample.length; j++)
	      transaction.add(pattern.is.get((int)sample[j] - 1));
	  }
      }
    
    current_transaction++;

    return transaction;
  }

  /**
   * Return the large itemsets used in the generation of transactions.
   * This can be useful for debugging.
   *
   * @return   an ArrayList containing the large itemsets as Itemset objects.
   */
  public ArrayList getLargeItemsets()
  {
    ArrayList large = new ArrayList();
    for (int i = 0; i < large_itemsets.size(); i++)
      large.add(((LargeItemset)large_itemsets.get(i)).is);
    return large;
  }

  // this method creates a random pool of large itemsets that will
  // be used in the generation of transactions
  private void initLargeItemsets()
  {
    // used for selecting a random item
    rand_item = new Random();

    // assign probabilities to items
    initItemProbabilities();

    large_itemsets = new ArrayList(num_large_itemsets);

    RandomPoissonDistribution poisson_large_size 
      = new RandomPoissonDistribution(avg_large_itemset_size - 1);
    RandomExponentialDistribution exp_correlation
      = new RandomExponentialDistribution(correlation_mean);
    RandomExponentialDistribution exp_weight
      = new RandomExponentialDistribution();
    Random normal_corruption = new Random();

    for (int i = 0; i < num_large_itemsets; i++)
      {
	// the large itemset size is obtained from a Poisson distribution
	// with mean avg_large_itemset_size
	int large_itemset_size = (int)poisson_large_size.nextLong() + 1;

	if (large_itemset_size > num_items)
	  large_itemset_size = num_items;

	LargeItemset large = new LargeItemset();
	large.is = new Itemset(large_itemset_size);

	if (i > 0)
	  {
	    // get previous large itemset
	    LargeItemset prev_large = (LargeItemset)large_itemsets.get(i - 1);

	    // we determine the fraction of items to use from the
	    // previous itemset using an exponential distribution
	    // with mean equal to correlation_mean
	    // (we add 0.5 to round off)
	    int fraction = (int)(((double)large_itemset_size) 
				 * exp_correlation.nextDouble() 
				 + 0.5);

	    // make adjustments if necessary
	    if (fraction > large_itemset_size)
	      fraction = large_itemset_size;
	    if (fraction > prev_large.is.size())
	      fraction = prev_large.is.size();

	    // select randomly the fraction of items from the previous
	    // large itemset
	    if (fraction > 0)
	      {
		RandomSample rand_sample 
		  = new RandomSample(prev_large.is.size(), 
				     fraction, rand_sampling);
		long[] sample = rand_sample.nextSample();
		for (int j = 0; j < sample.length; j++)
		  large.is.add(prev_large.is.get((int)sample[j] - 1));
	      }
	  }

	// add items randomly until we fill the itemset
	while (large.is.size() < large_itemset_size)
	  large.is.add(nextRandomItem());

	// we associate to this itemset a weight picked from
	// an exponential distribution with unit mean
	large.weight = exp_weight.nextDouble();

	// we also assign a corruption level obtained from a
	// normal distribution with mean corruption_mean and variance 0.1
	large.corruption = normal_corruption.nextGaussian() * 0.1 
	  + corruption_mean;

	large_itemsets.add(large);
      }

    // now we have to normalize the weights of the large itemsets
    // such that their sum will total 1. This is done by dividing 
    // each weight by their sum
    double sum = 0.0;
    for (int i = 0; i < num_large_itemsets; i++)
      sum += ((LargeItemset)large_itemsets.get(i)).weight;
    for (int i = 0; i < num_large_itemsets; i++)
      ((LargeItemset)large_itemsets.get(i)).weight /= sum;

    // finally we cumulate the probabilities in order to make it easier
    // to select one item randomly
    for (int i = 1; i < num_large_itemsets - 1; i++)
      {
	LargeItemset prev_large = (LargeItemset)large_itemsets.get(i - 1);
	LargeItemset large = (LargeItemset)large_itemsets.get(i);
	large.weight += prev_large.weight;
      }
    ((LargeItemset)large_itemsets.get(num_large_itemsets - 1)).weight = 1.0;
  }

  // we use the rand_large and the weights of the large itemsets to
  // select a large itemset randomly
  private LargeItemset nextRandomLargeItemset()
  {
    // this is a nice trick (courtesy Agrawal) for reusing
    // large itemsets in case they won't be used now
    // (just change the sign of last_large to "push back" 
    // the choice - see ungetRandomLargeItemset())
    if (last_large < 0)
      {
	last_large = -last_large;
	return (LargeItemset)large_itemsets.get(last_large);
      }

    double val = rand_large.nextDouble();

    // do a binary search for the location i such that
    // weight(i - 1) < val <= weight(i)
    int i = 0;
    int left = 0;
    int right = num_large_itemsets - 1;
    while (right >= left)
      {
	int middle = (left + right) / 2;
	LargeItemset large = (LargeItemset)large_itemsets.get(middle);
	if (val < large.weight)
	  right = middle - 1;
	else if (val > large.weight)
	  left = middle + 1;
	else
	  {
	    i = middle;
	    break;
	  }
      }
    if (right < left)
      i = left;

    // in the case there were neighboring items with probability 0
    while (i > 0 
	   && ((LargeItemset)large_itemsets.get(i - 1)).weight == val)
      i--;

    // store last index chosed in case the itemset is not used now
    last_large = i;

    return (LargeItemset)large_itemsets.get(last_large);
  }

  // this method allows us to "push back" a selected large itemset
  // such that we use this choice the next time we need a large itemset
  private void ungetRandomLargeItemset()
  {
    if (last_large >= 0)
      last_large = -last_large;
    else
      System.err.println("Invalid call to ungetRandomLargeItemset()!");
  }

  // we give probabilities to each item, these will be used to
  // choose the items to add to a large itemset
  private void initItemProbabilities()
  {
    item_probabilities = new double[num_items];

    // the probabilities are generated with exponential distribution
    // with unit mean
    RandomExponentialDistribution exp 
      = new RandomExponentialDistribution();
    for (int i = 0; i < num_items; i++)
      item_probabilities[i] = exp.nextDouble();

    // now we have to normalize these probabilities such that their
    // sum will total 1. This is done by dividing each probability 
    // by their sum
    double sum = 0.0;
    for (int i = 0; i < num_items; i++)
      sum += item_probabilities[i];
    for (int i = 0; i < num_items; i++)
      item_probabilities[i] /= sum;

    // finally we cumulate the probabilities in order to make it easier
    // to select one item randomly
    for (int i = 1; i < num_items - 1; i++)
      item_probabilities[i] += item_probabilities[i - 1];
    item_probabilities[num_items - 1] = 1.0;
  }

  // we use the rand_item and the item_probabilities array to
  // select an item randomly
  private int nextRandomItem()
  {
    double val = rand_item.nextDouble();

    // do a binary search for the location i such that
    // item_probabilities[i - 1] < val <= item_probabilities[i]
    int i = 0;
    int left = 0;
    int right = num_items - 1;
    while (right >= left)
      {
	int middle = (left + right) / 2;
	if (val < item_probabilities[middle])
	  right = middle - 1;
	else if (val > item_probabilities[middle])
	  left = middle + 1;
	else
	  {
	    i = middle;
	    break;
	  }
      }
    if (right < left)
      i = left;

    // in the case there were neighboring items with probability 0
    while (i > 0 && item_probabilities[i - 1] == val)
      i--;

    return (i + 1);
  }

  /**
   * sample usage and testing
   */
  public static void main(String[] args)
  {
    SyntheticDataGenerator syndatgen 
      = new SyntheticDataGenerator(20, 7, 5, 5, 100);

    System.out.println(syndatgen.getLargeItemsets());

    while (syndatgen.hasMoreTransactions())
      System.out.println(syndatgen.getNextTransaction());
  }
}
