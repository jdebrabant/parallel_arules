/*
  Closure.java

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

import java.util.*;
import java.io.IOException;

/**

   This class implements the Closure algorithm 
   for finding frequent itemsets. This implementation is
   actually simpler and more elegant than the one
   described in the article.<P>

   (see "Galois Connections and Data Mining"
    by Dana Cristofor, Laurentiu Cristofor, and Dan Simovici
    from UMass/Boston 2000)
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public class Closure extends FrequentItemsetsMiner
{
  private static final int INITIAL_CAPACITY = 10000;

  // our collections of itemsets
  private ArrayList candidates;
  private ArrayList k_frequent;
  /// private ArrayList frequent;

  // the counts matrix
  private long[][] counts;

  // the hashtrees associated with candidates and k_frequent
  private HashTree ht_candidates;
  private HashTree ht_k_frequent;

  // this remembers the number of passes over the dataset
  private int pass_num;

  // this keeps track of the current step which also represents
  // the cardinality of the candidate itemsets
  private int step;

  // useful information
  private long num_rows;
  private int num_cols;
  private long min_weight;

  /**
   * Find the frequent itemsets in a database
   *
   * @param dbReader   the object used to read from the database
   * @param cacheWriter   the object used to write to the cache
   * if this is null, then nothing will be saved, this is useful
   * for benchmarking
   * @param minSupport   the minimum support
   * @return   the number of passes executed over the database
   */
  public int findFrequentItemsets(DBReader dbReader, 
				  DBCacheWriter cacheWriter,
				  double minSupport)
  {
    // save the following into member fields
    db_reader = dbReader;
    cache_writer = cacheWriter;
    num_rows = dbReader.getNumRows();
    num_cols = (int)dbReader.getNumColumns();
    min_weight = (long)(num_rows * minSupport);

    // initialize the collections
    candidates = new ArrayList(INITIAL_CAPACITY);
    k_frequent = new ArrayList(INITIAL_CAPACITY);
    /// frequent = new ArrayList(INITIAL_CAPACITY);

    // initialize the hash trees
    ht_k_frequent = new HashTree(k_frequent);
    ht_candidates = new HashTree(candidates);

    // The initial candidates are all 1-itemsets
    Itemset is;
    for (int i = 1; i <= db_reader.getNumColumns(); i++)
      {
	is = new Itemset(1);
	is.add(i);
	candidates.add(is);
	ht_candidates.add(candidates.size() - 1);
      }
    
    // we start with first pass
    for (step = 1, pass_num = 1; ; step++, pass_num++)
      {
	// reinitialize the counts matrix
	counts = new long[candidates.size()][num_cols];

	// check for user-requested abort
	checkAbort();

	// compute the weight of each candidate
	weighCandidates();

	// check for user-requested abort
	checkAbort();

	// during this step if candidates are of cardinality k
	// we will find out all k+1 frequent itemsets and place
	// them in k_frequent. Thus we skip one pass over the
	// dataset compared to the Apriori approach.
	evaluateCandidates();

	// we increment the step according to the above optimization
	step ++;

	// if last pass didn't produce any frequent itemsets
	// then we can stop the algorithm
	if (k_frequent.size() == 0)
	  break;

	// if we also examined the top itemset (the one containing
	// all items) then we're done, nothing more to do.
	if (step >= db_reader.getNumColumns())
	  break;

	// check for user-requested abort
	checkAbort();

	// generate new candidates from frequent itemsets
	generateCandidates();

	// exit if no more candidates
	if (candidates.size() == 0)
	  break;
      }

    return pass_num;
  }

  // this procedure scans the database and computes the weight of each
  // candidate
  private void weighCandidates()
  {
    ht_candidates.prepareForDescent();

    try
      {
	Itemset row = db_reader.getFirstRow();
	// also we update the counts table here
	ht_candidates.update(row, counts);

	while (db_reader.hasMoreRows())
	  {
	    row = db_reader.getNextRow();
	    // also we update the counts table here
	    ht_candidates.update(row, counts);
	  }
      }
    catch (Exception e)
      {
	System.err.println("Error scanning database!!!\n" + e);
      }
  }

  // this procedure checks to see which itemsets are frequent
  private void evaluateCandidates()
  {
    Itemset is, is_cl;

    for (int i = 0; i < candidates.size(); i++)
      // if this is a frequent itemset
      if ((is = (Itemset)candidates.get(i)).getWeight() >= min_weight)
	{
	  // compute support of itemset
	  is.setSupport((double)is.getWeight()/(double)num_rows);

	  // write itemset to the cache
	  try
	    {
	      if (cache_writer != null)
		cache_writer.writeItemset(is);
	    }
	  catch (IOException e)
	    {
	      System.err.println("Error writing cache!!!\n" + e);
	    }

	  // then add it to the frequent collection
	  /// frequent.add(is);

	  // now look for closures of this itemset.
	  // we're looking to add to this itemset new
	  // items that appear more than min_weight times
	  // together with this itemset.
	  // we start looking for such items starting from
	  // the next item after the last item of this itemset
	  // (this is in accordance with the way we generate candidates)
	  for (int item = is.get(is.size() - 1) + 1; 
	       item <= num_cols; item++)
	    if (counts[i][item - 1] >= min_weight)
	      {
		// create a new itemset
		is_cl = new Itemset(is);
		is_cl.add(item);
		// set the weight and support of this new itemset
		is_cl.setWeight(counts[i][item - 1]);
		is_cl.setSupport((double)is_cl.getWeight()/(double)num_rows);

		// write itemset to the cache
		try
		  {
		    if (cache_writer != null)
		      cache_writer.writeItemset(is_cl);
		  }
		catch (IOException e)
		  {
		    System.err.println("Error writing cache!!!\n" + e);
		  }

		// add it to the frequent and k_frequent collections
		/// frequent.add(is_cl);
		k_frequent.add(is_cl);
		ht_k_frequent.add(k_frequent.size() - 1);
	      }
	}

    // reinitialize candidates for next step
    candidates.clear();
    ht_candidates = new HashTree(candidates);
  }

  // this procedure generates new candidates out of itemsets
  // that are frequent according to the procedure described
  // by Agrawal a.o.
  private void generateCandidates()
  {
    ht_k_frequent.prepareForDescent();

    if (k_frequent.size() == 0)
      return;

    for (int i = 0; i < k_frequent.size() - 1; i++)
      for (int j = i + 1; j < k_frequent.size(); j++)
	if (!getCandidate(i, j))
	  break;

    // reinitialize k_frequent for next step
    k_frequent.clear();
    ht_k_frequent = new HashTree(k_frequent);
  }

  // this procedure tries to combine itemsets i and j and returns
  // true if succesful, false if it can't combine them
  private boolean getCandidate(int i, int j)
  {
    Itemset is_i = (Itemset)k_frequent.get(i);
    Itemset is_j = (Itemset)k_frequent.get(j);

    // if we cannot combine element i with j then we shouldn't
    // waste time for bigger j's. This is because we keep the
    // collections ordered, an important detail in this implementation
    if (!is_i.canCombineWith(is_j))
      return false;
    else
      {
	Itemset is = is_i.combineWith(is_j);

	// a real k-frequent itemset has k (k-1)-frequent subsets
	if (ht_k_frequent.countSubsets(is) == is.size())
	  {
	    candidates.add(is);
	    ht_candidates.add(candidates.size() - 1);
	  }

	return true;
      }
  }
}
