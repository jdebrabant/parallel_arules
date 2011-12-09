/*
  FPgrowth.java

  (P)2001 by Laurentiu Cristofor
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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

import org.apache.hadoop.mapreduce.Reducer.Context;

/**

   This class implements the FPgrowth algorithm 
   for finding frequent itemsets.<P>

   (see "Mining Frequent Patterns without Candidate Generation"
    by Jiawei Han, Jian Pei, and Yiwen Yin
    from Simon Fraser University 2000)
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public class FPgrowth extends FrequentItemsetsMiner
{
	org.apache.hadoop.mapreduce.Reducer.Context context; 
	
  private static class FPTreeNode
  {
    // data
    int item;           // item id
    int count;          // item count

    // the following two are kept to speed up calculations
    int seq_num;        // sequence number of node, = (depth + 1)
    int header_index;   // index in header of the entry for item

    // links
    FPTreeNode parent;  // parent node
    FPTreeNode child;   // first child node
    FPTreeNode sibling; // next sibling node
    FPTreeNode next;    // next node in FPTree containing same item 

    FPTreeNode()
    {
    }

    FPTreeNode(int i, int c, int sn, int hi,
	       FPTreeNode p, FPTreeNode s, FPTreeNode n)
    {
      item = i;
      count = c;
      seq_num = sn;
      header_index = hi;
      parent = p;
      sibling = s;
      next = n;
    }
  }

  private static class FPTreeHeaderEntry
  {
    int item;
    int count; // total item count in tree
    FPTreeNode head;

    FPTreeHeaderEntry()
    {
    }

    FPTreeHeaderEntry(int i)
    {
      item = i;
    }
  }

  private class FPTree
  {
    FPTreeHeaderEntry[] header;
    FPTreeNode root;

    // we need to be able to figure out
    // the index of an FPTreeHeaderEntry
    // for a given item
    Map item2index;

    // to quickly tell whether we have a single path or not
    boolean hasMultiplePaths;

    // for statistics
    int count_nodes;

    // information we need about the database
    long num_rows;
    long min_weight;
    DBCacheWriter cache_writer;

    FPTree(int[] items, long num_rows, long min_weight,
	   DBCacheWriter cache_writer)
    {
      header = new FPTreeHeaderEntry[items.length];
      root = new FPTreeNode();
      item2index = new HashMap(items.length);

      this.num_rows = num_rows;
      this.min_weight = min_weight;
      this.cache_writer = cache_writer;

      for (int i = 0; i < items.length; i++)
	{
	  header[i] = new FPTreeHeaderEntry(items[i]);
	  item2index.put(new Integer(items[i]), new Integer(i));
	}
    }

    void insert(int[] items, int count)
    {
      // current_node will be the node below which we look to insert
      FPTreeNode current_node = root; 

      for (int index = 0; index < items.length; index++)
	{
	  // find header entry for items[index]
	  int entry_index 
	    = ((Integer)item2index.get(new Integer(items[index]))).intValue();

	  // update item count in header entry
	  header[entry_index].count += count;

	  // we look among the children of current_node
	  // for the one containing items[index]
	  FPTreeNode walker = current_node.child;
	  for ( ; walker != null; walker = walker.sibling)
	    if (walker.item == items[index])
	      break;

	  // case no child contained the item
	  // -> we need to insert a new node
	  if (walker == null)
	    {
	      // if we're creating a new branch
	      if (current_node.child != null)
		hasMultiplePaths = true;

	      count_nodes++;

	      // parent is current_node, sibling is current_node.child,
	      // next is head from header entry
	      // (we insert at the beginning of the 'next'
	      // and 'sibling' based linked lists)
	      FPTreeNode new_node = new FPTreeNode(items[index], count,
						   index + 1, entry_index,
						   current_node, 
						   current_node.child, 
						   header[entry_index].head);
	      header[entry_index].head = new_node;
	      current_node.child = new_node;
	      // and continue inserting from this new node
	      current_node = new_node;
	    }
	  // if we get here then walker points
	  // to a node containing items[index]
	  else
	    {
	      // update count of node
	      walker.count += count;
	      // and continue inserting from this node
	      current_node = walker;
	    }
	}
    }

    void fp_growth(Itemset is_suffix)
    {
      // check for user-requested abort
      checkAbort();

      if (!hasMultiplePaths)
	{
	  // collect items from the tree, least frequent item first!!!
	  int[] items = new int[header.length];
	  for (int i = 0; i < header.length; i++)
	    items[header.length - i - 1] = header[i].item;
	  // generate all item combinations of the tree's single branch
	  combine(items, is_suffix);
	}
      else
	for (int i = header.length - 1; i >= 0; i--)
	  {
	    Itemset is_new = new Itemset(is_suffix);
	    is_new.add(header[i].item);
	    is_new.setWeight(header[i].count);
	    is_new.setSupport((double)header[i].count / (double)num_rows);

		  context.setStatus("running FP-growth for item " + i); 
		  
	    // write itemset to the cache
	    try
	      {
		if (cache_writer != null)
		  cache_writer.writeItemset(is_new);
	      }
	    catch (IOException e)
	      {
		System.err.println("Error writing cache!!!\n" + e);
	      }

	    FPTree fpt = buildConditionalFPTree(header[i].item);
	    if (fpt != null)
	      fpt.fp_growth(is_new);
	  }
    }

    void combine(int[] items, Itemset is_combination)
    {
      int count;
      for (int i = 0; i < items.length; i++)
	{
	  Itemset is_new_combination = new Itemset(is_combination);
	  is_new_combination.add(items[i]);
	  // store in itemset the weight and support of all itemsets
	  // combined with items[i];
	  // note that we go through items in increasing order of their
	  // support so that we can set it up correctly
	  count = header[header.length - i - 1].count;
	  is_new_combination.setWeight(count);
	  is_new_combination.setSupport((double)count / (double)num_rows);

	  // write itemset to the cache
	  try
	    {
	      if (cache_writer != null)
		cache_writer.writeItemset(is_new_combination);
	    }
	  catch (IOException e)
	    {
	      System.err.println("Error writing cache!!!\n" + e);
	    }

	  combine(items, is_new_combination, i + 1);
	}
    }

    // create all combinations of elements in items[]
    // starting at index from and append them to is_combination
    void combine(int[] items, Itemset is_combination, int from)
    {
      for (int i = from; i < items.length; i++)
	{
	  Itemset is_new_combination = new Itemset(is_combination);
	  is_new_combination.add(items[i]);

	  // write itemset to the cache
	  try
	    {
	      if (cache_writer != null)
		cache_writer.writeItemset(is_new_combination);
	    }
	  catch (IOException e)
	    {
	      System.err.println("Error writing cache!!!\n" + e);
	    }

	  combine(items, is_new_combination, i + 1);
	}
    }

    FPTree buildConditionalFPTree(int item)
    {
      // find header entry for item
      int entry_index 
	= ((Integer)item2index.get(new Integer(item))).intValue();

      // we will see which of the remaining items are frequent
      // with respect to the conditional pattern base of item

      // we have a counter for each entry in the header that
      // comes before item's own entry
      int[] counts = new int[entry_index];
      for (FPTreeNode side_walker = header[entry_index].head;
	   side_walker != null; side_walker = side_walker.next)
	for (FPTreeNode up_walker = side_walker.parent;
	     up_walker != root; up_walker = up_walker.parent)
	  counts[up_walker.header_index] += side_walker.count;

      int num_frequent = 0;
      for (int i = 0; i < counts.length; i++)
	if (counts[i] >= min_weight)
	  num_frequent++;
      
      if (num_frequent == 0)
	return null;

      // put all frequent items in an array of Items
      Item[] item_objs = new Item[num_frequent];
      for (int i = 0, j = 0; i < counts.length; i++)
	if (counts[i] >= min_weight)
	  item_objs[j++] = new Item(header[i].item, counts[i]);

      // and sort them ascendingly according to weight
      Arrays.sort(item_objs);
      
      // then place the items in an array of ints in descending order
      int[] items = new int[num_frequent];
      for (int i = 0; i < num_frequent; i++)
	items[i] = item_objs[num_frequent - i - 1].item;
      
      // initialize FPTree
      FPTree fpt = new FPTree(items, num_rows, min_weight, cache_writer);

      for (FPTreeNode side_walker = header[entry_index].head;
	   side_walker != null; side_walker = side_walker.next)
	if (side_walker.parent != root)
	  {
	    int i = side_walker.parent.seq_num;
	    Item[] pattern = new Item[i];
	    for (FPTreeNode up_walker = side_walker.parent;
		 up_walker != root; up_walker = up_walker.parent)
	      // we store the header index in the count field
	      // so that we can use it later
	      // to access the count from counts[]
	      pattern[--i] = new Item(up_walker.item, up_walker.header_index);

	    processPattern(pattern, side_walker.count, fpt, counts);
	  }

      return fpt;
    }

    // NOTE: pattern[] elements contain in their count field the
    // header entry index of the corresponding item which also 
    // can be used to index the counts[] array
    void processPattern(Item[] pattern, int count, FPTree fpt,
			int[] counts)
    {
      int i, j, num_frequent;
      Item item_obj;
      int[] items;
      Item[] item_objs;

      // how many frequent items are in this pattern?
      for (i = 0, num_frequent = 0; i < pattern.length; i++)
	{
	  item_obj = pattern[i];
	  if (counts[item_obj.count] >= min_weight)
	    num_frequent++;
	}
	    
      if (num_frequent > 0)
	{
	  // select only frequent items into an array of Items
	  // these form a conditional pattern
	  item_objs = new Item[num_frequent];
	  for (i = 0, j = 0; i < pattern.length; i++)
	    {
	      item_obj = pattern[i];
	      if (counts[item_obj.count] >= min_weight)
		item_objs[j++] = new Item(item_obj.item, 
					  counts[item_obj.count]);
	    }
	
	  // sort them
	  Arrays.sort(item_objs);
	
	  // get the items in reverse order in an array of ints
	  items = new int[num_frequent];
	  for (i = 0; i < num_frequent; i++)
	    items[i] = item_objs[num_frequent - i - 1].item;

	  // insert them in the FPTree
	  fpt.insert(items, count);
	}
    }
  }

  private static class Item implements Comparable
  {
    int item;
    int count;

    Item(int i, int c)
    {
      item = i;
      count = c;
    }

    public int compareTo(Object o)
    {
      Item other = (Item)o;

      return (count - other.count);
    }

    public String toString()
    {
      return "<" + item + ", " + count + ">";
    }
  }

  // useful information
  private long num_rows;
  private int num_cols;
  private long min_weight;

  private int[] counts; // stores count of items starting from 1

  private int pass_num;

	public int findFrequentItemsets(DBReader dbReader, 
									DBCacheWriter cacheWriter,
									double minSupport)
	{
		
		return 0; 
	}
	
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
				  double minSupport, 
				org.apache.hadoop.mapreduce.Reducer.Context c)
  {
    // save the following into member fields
    db_reader = dbReader;
    cache_writer = cacheWriter;
    num_rows = dbReader.getNumRows();
    num_cols = (int)db_reader.getNumColumns();
    min_weight = (long)(num_rows * minSupport);
	  
	  context = c; 

    // check for user-requested abort
    checkAbort();

    // first pass counts item occurrences
    countItemOccurrences();

    // check for user-requested abort
    checkAbort();

    // second pass constructs FPTree
    FPTree fpt = constructFPTree();

    // finally we mine the FPTree using the FP-growth algorithm
    if (fpt != null)
      {
	//System.out.println("<FPgrowth>: FPTree has " + fpt.count_nodes 
		//	   + " nodes");
	fpt.fp_growth(new Itemset());
      }
    else
      System.err.println("<FPgrowth>: FPTree is empty");
      

    // there will usually be 2 passes unless there are
    // no frequent items, in which case we do only 1 pass
    return pass_num;
  }

  private void countItemOccurrences()
  {
    counts = new int[num_cols + 1];

    try
      {
	Itemset row = db_reader.getFirstRow();

	for (int i = 0; i < row.size(); i++)
	  counts[row.get(i)]++;

	while (db_reader.hasMoreRows())
	  {
	    row = db_reader.getNextRow();

	    for (int i = 0; i < row.size(); i++)
	      counts[row.get(i)]++;
	  }
      }
    catch (Exception e)
      {
	System.err.println("Error scanning database!!!\n" + e);
      }

    // we did one pass over database
    pass_num++;
  }

  private FPTree constructFPTree()
  {
    // see how many frequent items there are in the database
    int num_frequent = 0;
    for (int i = 1; i < counts.length; i++)
      if (counts[i] >= min_weight)
	num_frequent++;

    if (num_frequent == 0)
      return null;

    // put all frequent items in an array of Items
    Item[] item_objs = new Item[num_frequent];
    for (int i = 1, j = 0; i < counts.length; i++)
      if (counts[i] >= min_weight)
	item_objs[j++] = new Item(i, counts[i]);

    // and sort them ascendingly according to weight
    Arrays.sort(item_objs);

    // then place the items in an array of ints in descending order
    int[] items = new int[num_frequent];
    for (int i = 0; i < num_frequent; i++)
      items[i] = item_objs[num_frequent - i - 1].item;

    // initialize FPTree
    FPTree fpt = new FPTree(items, num_rows, min_weight, cache_writer);

    try
      {
	Itemset row = db_reader.getFirstRow();

	processRow(row, fpt);

	while (db_reader.hasMoreRows())
	  {
	    row = db_reader.getNextRow();

	    processRow(row, fpt);
	  }
      }
    catch (Exception e)
      {
	System.err.println("Error scanning database!!!\n" + e);
      }

    pass_num++;

    return fpt;
  }

  private void processRow(Itemset row, FPTree fpt)
  {
    int i, j, item, num_frequent;
    int[] items;
    Item[] item_objs;

    // how many frequent items are in this row?
    for (i = 0, num_frequent = 0; i < row.size(); i++)
      {
	item = row.get(i);
	if (counts[item] >= min_weight)
	  num_frequent++;
      }
	    
    if (num_frequent > 0)
      {
	// select only frequent items into an array of Items
	item_objs = new Item[num_frequent];
	for (i = 0, j = 0; i < row.size(); i++)
	  {
	    item = row.get(i);
	    if (counts[item] >= min_weight)
	      {
		item_objs[j++] = new Item(item, counts[item]);
	      }
	  }
	
	// sort them
	Arrays.sort(item_objs);
	
	// get the items in reverse order into an array of ints
	items = new int[num_frequent];
	for (i = 0; i < num_frequent; i++)
	  items[i] = item_objs[num_frequent - i - 1].item;

	// insert them in the FPTree
	fpt.insert(items, 1);
      }
  }
}
