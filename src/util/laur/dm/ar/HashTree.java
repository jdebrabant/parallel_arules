/*
  HashTree.java
   
  (P)1999-2002 Laurentiu Cristofor
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

/*

  HISTORY:

      v1.1   now keeps track of the size of the itemsets added to
               the tree. Can't add itemsets of different size.
             optimized the for loop in traverse() to terminate
               sooner
             added better comments to traverse()

      v1.0   first version

 */

/**

   A HashTree is a special data structure that is used to index
   an ArrayList of Itemset objects for more efficient processing.
   
   @version 1.1
   @author Laurentiu Cristofor

*/
public class HashTree
{
  private static final int LIST_NODE = 1;
  private static final int HASH_NODE = 2;

  private static final int DEFAULT_LIST_SIZE = 20;
  private static final int DEFAULT_HASH_SIZE = 40;

  private int LIST_SIZE;
  private int HASH_SIZE;

  private static class Node
  {
    int type; // LIST_NODE or HASH_NODE
  }

  private class ListNode extends Node
  {
    int[] indexes;   // index in ArrayList of Itemsets
    int size;        // how many indexes we keep in above array
    boolean visited; // have we seen this node?

    ListNode()
    {
      type = LIST_NODE;
      indexes = new int[LIST_SIZE];
      size = 0;
      visited = false;
    }
  }

  private class HashNode extends Node
  {
    MyHashtable children;

    HashNode()
    {
      type = HASH_NODE;
      children = new MyHashtable(HASH_SIZE);
    }
  }

  private static class MyHashtable
  {
    Node[] contents;

    MyHashtable(int size)
    {
      contents = new Node[size];
    }

    void put(int key, Node n)
    {
      int index = key % contents.length;
      contents[index] = n;
    }

    Node get(int key)
    {
      int index = key % contents.length;
      return contents[index];
    }

    Enumeration elements()
    {
      return new Enumeration()
	{
	  int i = 0;

	  public boolean hasMoreElements()
	  {
	    while (i < contents.length 
		   && contents[i] == null)
	      i++;

	    if (i >= contents.length)
	      return false;
	    else
	      return true;
	  }

	  public Object nextElement()
	  {
	    while (i < contents.length 
		   && contents[i] == null)
	      i++;

	    if (i >= contents.length)
	      throw new NoSuchElementException();
	    else
	      return contents[i++];
	  }
	};
    }
  }
  
  private int counter;        // used for some computations

  private ArrayList leaves;   // keeps all leaves of the HashTree
  private ArrayList itemsets; // the ArrayList of Itemsets that we index

  private Node theRoot;       // the root of the HashTree

  private int order;          // keeps track of the size of the
			      // itemsets in the tree

  private void unvisitLeaves()
  {
    for (int i = 0; i < leaves.size(); i++)
      ((ListNode)leaves.get(i)).visited = false;
  }

  /**
   * Create a new HashTree. The <code>listSize</code> parameter determines
   * after how many inserts in a ListNode we have to change it to a 
   * HashNode (i.e. perform a split). The <code>hashSize</code> parameter
   * can be specified to improve the efficiency of the structure.
   *
   * @param listSize   the size of the internal lists in the list nodes
   * @param hashSize   the size of the internal hashtables in the hash nodes
   * @param itemsets   the ArrayList of Itemsets that we should index
   * @exception IllegalArgumentException   <code>itemsets</code> is null
   * or <code>listSize <= 0</code> or <code>hashSize <= 0</code>  
   */
  public HashTree(int listSize, int hashSize, ArrayList itemsets)
  {
    if (itemsets == null || listSize <= 0 || hashSize <= 0)
      throw new IllegalArgumentException("invalid arguments to constructor");

    LIST_SIZE = listSize;
    HASH_SIZE = hashSize;
    this.itemsets = itemsets;

    theRoot = new ListNode();
    leaves = new ArrayList();

    order = 0;
  }

  /**
   * Create a new HashTree. This initializes the HashTree with
   * default parameters.
   *
   * @param itemsets   the ArrayList of Itemsets that we should index
   * @exception IllegalArgumentException   <code>itemsets</code> is null
   */
  public HashTree(ArrayList itemsets)
  {
    this(DEFAULT_LIST_SIZE, DEFAULT_HASH_SIZE, itemsets);
  }

  /**
   * This method needs to be called after filling the tree and before
   * any other processing (like update(), count*(), or
   * checkLargeness()). It gathers all leaves of the HashTree for more
   * efficient processing.
   */
  public void prepareForDescent()
  {
    leaves.clear();
    prepare(theRoot);
  }

  // private recursive method
  private void prepare(Node node)
  {
    if (node.type == HASH_NODE)
      {
	Enumeration e = ((HashNode)node).children.elements();
	while (e.hasMoreElements())
	  prepare((Node)e.nextElement());
      }
    else // LIST_NODE
      leaves.add(node);
  }

  // reset HashTree and reAdd all itemsets added previously
  // this method may be called by add()
  private void reAddAll()
  {
    // increase size of hash nodes
    HASH_SIZE = 2 * HASH_SIZE + 1;

    // first save leaf nodes
    prepareForDescent();

    // then reset HashTree
    theRoot = new ListNode();

    // reAdd everything that we added before
    for (int i = 0; i < leaves.size(); i++)
      {
	ListNode ln = (ListNode)leaves.get(i);
	for (int j = 0; j < ln.size; j++)
	  add(ln.indexes[j]);
      }

    // clean up
    leaves.clear();
  }

  private static class HashTreeOverflowException extends RuntimeException
  {
  }

  /**
   * This method indexes in the HashTree the Itemset at index
   * <code>index</code> from ArrayList <code>itemsets</code> which
   * was passed to the constructor of this HashTree.
   *
   * @param index   the index of the Itemset that we need to index in
   * this HashTree.
   * @exception IllegalArgumentException   if the itemset added has
   * different size from previous itemsets added.
   */
  public void add(int index)
  {
    // set and verify order
    if (order == 0)
      order = ((Itemset)itemsets.get(index)).size();
    else if (order != ((Itemset)itemsets.get(index)).size())
      throw new IllegalArgumentException("attempt to add itemset of different size!");

    // repeat the operation if it results in a HashTree overflow
    while (true)
      try 
	{
	  theRoot = add(theRoot, 0, index);

	  // if we get here then the add() was successful and we can
	  // exit the loop
	  break;
	}
      catch (HashTreeOverflowException e)
	{
	  // call reAddAll to increase the size of hash nodes 
	  // and reAdd all itemsets that were previously added
	  reAddAll();
	}
  }

  // private recursive method
  private Node add(Node node, int level, int index)
  {
    if (node.type == LIST_NODE)
      {
	ListNode ln = (ListNode)node;

	if (ln.size == LIST_SIZE) // list is full
	  {
	    // if the level is equal to the itemsets size and we
	    // filled the list node, then we overflowed the HashTree.
	    if (((Itemset)itemsets.get(index)).size() == level)
	      throw new HashTreeOverflowException();

	    // else, must split!
	    HashNode hn = new HashNode();

	    // hash the list elements
	    for (int i = 0; i < LIST_SIZE; i++)
	      add(hn, level, ln.indexes[i]);

	    // add our node
	    add(hn, level, index);

	    // return this HashNode to replace old ListNode
	    return hn;
	  }
	else // append index at end of list
	  {
	    ln.indexes[ln.size++] = index;
	  }
      }
    else // HASH_NODE
      {
	HashNode hn = (HashNode)node;

	// compute hash key
	Itemset is = (Itemset)itemsets.get(index);
	int key = is.get(level);

	// try to get next node
	Node n = hn.children.get(key);
	if (n == null) // no node, must create a new ListNode
	  {
	    ListNode ln = new ListNode();
	    ln.indexes[ln.size++] = index; 
	    hn.children.put(key, ln);
	  }
	else // found a node, do a recursive call
	  {
	    n = add(n, level + 1, index);
	    hn.children.put(key, n);
	  }
      }

    return node;
  }

  private interface Visitor
  {
    void visit(int indx_itemset);
  }

  // generic recursive traversal methods whose actions are determined
  // by the implementation of the Visitor parameter
  //
  // this traversal method looks in the hashtree for all subsets of the 
  // itemset row, and performs on each such subset the action
  // encapsulated in the object visitor. This is the core method of the
  // HashTree class, and the reason why we used the HashTree in the
  // first place. The idea is that based on the HashTree's structure,
  // this method will examine a limited number of itemsets instead of
  // all of them.
  private void traverse(Node node,       // node traversed
			Itemset row,     // row processed
			int index,       // row index that we consider
			int level,       // tree level
			Visitor visitor) // object encapsulating action
  {
    // if we reached a leaf, we perform the visitor action on each
    // itemset stored at this node
    if (node.type == LIST_NODE)
      {
	ListNode ln = (ListNode)node;

	if (ln.visited)
	  return;

	for (int i = 0; i < ln.size; i++)
	  {
	    visitor.visit(ln.indexes[i]);
	  }

	ln.visited = true; // now we've seen this node
      }
    else // HASH_NODE
      {
	HashNode hn = (HashNode)node;

	// this is a tricky piece of algorithm that ensures we
	// look for all possible subsets of the row itemset
	//
	// we consider all possible combinations of items starting with
	// index. Note that the tree contains itemsets of size 'order',
	// the itemsets keep their items in order, and the first level
	// of the tree is level 0. Therefore, when we get here we have
	// already used level items for the combination, and one more
	// item we will use now, giving (level + 1) items used. Since
	// we look for combinations of order items, we need to still
	// have (order - level - 1) items left to form a combination.
	for (int i = index; i < (row.size() - (order - level - 1)); i++)
	  {
	    int key = row.get(i);
	    Node n = hn.children.get(key);
	    if (n != null)
	      traverse(n, row, i + 1, level + 1, visitor);
	  }
      }
  }

  /**
   * Update the weights of all indexed Itemsets that are included
   * in <code>row</code>
   *
   * @param row   the Itemset (normally a database row) against which 
   * we test for inclusion
   */
  public void update(Itemset row)
  {
    traverse(theRoot, row, 0, 0,
	     new WeightUpdater(row));
    unvisitLeaves();
  }

  private class WeightUpdater implements Visitor
  {
    Itemset row;

    WeightUpdater(Itemset row)
    {
      this.row = row;
    }

    public void visit(int indx_itemset)
    {
      Itemset is = (Itemset)itemsets.get(indx_itemset);
      if (is.isIncludedIn(row))
	is.incrementWeight();
    }
  }

  /**
   * Update the weights of all indexed Itemsets that are included
   * in <code>row</code> and also update the matrix <code>counts</code>
   *
   * @param row   the Itemset (normally a database row) against which 
   * we test for inclusion
   * @param counts   a matrix used by some algorithms to speed up
   * computations; its rows correspond to Itemsets and its columns
   * correspond to items; each value in the matrix tells for how many
   * times had an item appeared together with an itemset in the rows
   * of the database.
   */
  public void update(Itemset row, long[][] counts)
  {
    traverse(theRoot, row, 0, 0,
	     new WeightAndMatrixUpdater(row, counts));
    unvisitLeaves();
  }

  private class WeightAndMatrixUpdater implements Visitor
  {
    Itemset row;
    long[][] counts;

    WeightAndMatrixUpdater(Itemset row, long[][] counts)
    {
      this.row = row;
      this.counts = counts;
    }

    public void visit(int indx_itemset)
    {
      Itemset is = (Itemset)itemsets.get(indx_itemset);
      if (is.isIncludedIn(row))
	{
	  is.incrementWeight();
	  
	  for (int j = 0; j < row.size(); j++)
	    counts[indx_itemset][row.get(j) - 1]++;
	}
    }
  }

  /**
   * Count how many frequent Itemsets (frequent = having weight 
   * greater than a specified minimum weight) are included in 
   * <code>itemset</code>
   *
   * @param itemset   the Itemset for which we count the subsets
   * @param minWeight   the minimum weight
   */
  public long countFrequentSubsets(Itemset itemset, long minWeight)
  {
    counter = 0;
    traverse(theRoot, itemset, 0, 0, 
	     new FrequentSubsetCounter(itemset, minWeight));
    unvisitLeaves();
    return counter;
  }

  private class FrequentSubsetCounter implements Visitor
  {
    Itemset itemset;
    long minWeight;

    FrequentSubsetCounter(Itemset itemset, long minWeight)
    {
      this.itemset = itemset;
      this.minWeight = minWeight;
    }

    public void visit(int indx_itemset)
    {
      Itemset is = (Itemset)itemsets.get(indx_itemset);
      if (is.isIncludedIn(itemset) && is.getWeight() >= minWeight)
	counter++;
    }
  }

  /**
   * Count how many Itemsets are included in <code>itemset</code>
   *
   * @param itemset   the Itemset for which we count the subsets
   */
  public long countSubsets(Itemset itemset)
  {
    counter = 0;
    traverse(theRoot, itemset, 0, 0,
	     new SubsetCounter(itemset));
    unvisitLeaves();
    return counter;
  }

  private class SubsetCounter implements Visitor
  {
    Itemset itemset;

    SubsetCounter(Itemset itemset)
    {
      this.itemset = itemset;
    }

    public void visit(int indx_itemset)
    {
      Itemset is = (Itemset)itemsets.get(indx_itemset);
      if (is.isIncludedIn(itemset))
	counter++;
    }
  }

  /**
   * Verifies if any of the indexed Itemsets is not large by checking
   * whether they're included in the frequent itemset <code>itemset</code>.
   * If an Itemset is not large then it will be marked.
   *
   * @param itemset   the Itemset we check
   */
  public void checkLargeness(Itemset itemset)
  {
    traverse(theRoot, itemset, 0, 0,
	     new LargeChecker(itemset));
    unvisitLeaves();
  }

  private class LargeChecker implements Visitor
  {
    Itemset itemset;

    LargeChecker(Itemset itemset)
    {
      this.itemset = itemset;
    }

    public void visit(int indx_itemset)
    {
      Itemset is = (Itemset)itemsets.get(indx_itemset);
      if (is.isIncludedIn(itemset))
	is.mark();
    }
  }
}
