/*
  Itemset.java
   
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

import java.util.ArrayList;

/**

   An itemset is an ordered list of integers that identify items
   coupled with a double value representing the support of the itemset
   as a percentage.
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public class Itemset 
  implements java.io.Serializable, CriteriaComparable
{
  /**
   * Specifies sorting should be performed according to itemset size.
   */
  public static final int BY_SIZE    = 0;

  /**
   * Specifies sorting should be performed according to itemset support.
   */
  public static final int BY_SUPPORT = 1;


  private static final int SIZE_INCR = 7;

  /** 
   * The capacity of the itemset.
   *
   * @serial
   */
  private int capacity;

  /** 
   * The number of items in the itemset.
   *
   * @serial
   */
  private int size;

  /** 
   * The itemset.
   *
   * @serial
   */
  private int[] set;

  /** 
   * The support of the itemset.
   *
   * @serial
   */
  private double support;

  /** 
   * The weight of the itemset.
   *
   * @serial
   */
  private long weight;

  /** 
   * The mark of the itemset. Can be used to mark the itemset for
   * various purposes
   *
   * @serial
   */
  private boolean mark;

  /**
   * Create a new empty itemset of specified capacity.
   *
   * @param c   the capacity of the itemset
   * @exception IllegalArgumentException   <code>c</code> is negative or zero
   */
  public Itemset(int c)
  {
    if (c < 1)
      throw new IllegalArgumentException("initial capacity must be a positive value");

    capacity = c;

    set = new int[capacity];
    size = 0;
    support = 0;
    weight = 0;
    mark = false;
  }

  /**
   * Creates a new empty itemset.
   */
  public Itemset()
  {
    this(SIZE_INCR);
  }

  /**
   * Create a new itemset by copying a given one.
   *
   * @param itemset   the itemset to be copied
   * @exception IllegalArgumentException   <code>itemset</code> is null
   */
  public Itemset(Itemset itemset)
  {
    if (itemset == null)
      throw new IllegalArgumentException("null itemset");

    capacity = itemset.capacity;
    set = new int[capacity];
    size = itemset.size;
    for (int i = 0; i < size; i++)
      set[i] = itemset.set[i];
    support = itemset.support;
    weight = itemset.weight;
    mark = itemset.mark;
  }

  /**
   * Return support of itemset.
   */
  public double getSupport()
  {
    return support;
  }

  /**
   * Set the support of the itemset.
   *
   * @param newSupport   the support of the itemset
   * @exception IllegalArgumentException   <code>newSupport</code> is < 0
   * or > 100
   */
  public void setSupport(double newSupport)
  {
    if (newSupport < 0 || newSupport > 1)
      throw new IllegalArgumentException("support must be between 0 and 1");

    support = newSupport;
  }

  /**
   * Return weight of itemset.
   */
  public long getWeight()
  {
    return weight;
  }

  /**
   * Set the weight of the itemset.
   *
   * @param newWeight   the weight of the itemset
   * @exception IllegalArgumentException   <code>newWeight</code> is < 0
   */
  public void setWeight(long newWeight)
  {
    if (newWeight < 0)
      throw new IllegalArgumentException("weight must be >= 0");

    weight = newWeight;
  }

  /**
   * Increment the weight of the itemset.
   */
  public void incrementWeight()
  {
    weight++;
  }

  /**
   * Return size of itemset.
   *
   * @return   size of itemset
   */
  public int size()
  {
    return size;
  }

  /**
   * Return i-th item in itemset. The count starts from 0.
   *
   * @param i   the index of the item to get
   * @exception IndexOutOfBoundsException   <code>i</code> is an invalid index
   * @return   the <code>i</code>-th item
   */
  public int get(int i)
  {
    if (i < 0 || i >= size)
      throw new IndexOutOfBoundsException("invalid index");

    return set[i];
  }

  /**
   * Add a new item to the itemset.
   *
   * @param item   the item to be added
   * @exception IllegalArgumentException   <code>item</code> is <= 0
   * @return   true if item was added, false if it wasn't added (was
   * already there!)
   */
  public boolean add(int item)
  {
    if (item <= 0)
      throw new IllegalArgumentException("item must be a positive value");

    if (size == 0)
      set[0] = item;
    else
      {
	// look for place to insert item
	int index;
	for (index = 0; index < size && item > set[index]; index++)
	  ;

	// if item was already in itemset then return
	if (index < size && item == set[index])
	  return false;

	// if set is full then allocate new array
	if (size == capacity)
	  {
	    capacity = size + SIZE_INCR;
	    int[] a = new int[capacity];

	    int i;
	    for (i = 0; i < index; i++)
	      a[i] = set[i];
	    // insert new item
	    a[i] = item;
	    for ( ; i < size; i++)
	      a[i + 1] = set[i];

	    set = a;
	  }
	// otherwise make place and insert new item
	else
	  {
	    int i;
	    for (i = size; i > index; i--)
	      set[i] = set[i - 1];

	    set[i] = item;
	  }
      }

    // update size
    size++;
    return true;
  }

  /**
   * Removes a given item from the itemset.
   *
   * @param item   the item to remove
   * @exception IllegalArgumentException   <code>item</code> is <= 0
   * @return   true if item was removed, false if it wasn't removed (was
   * not found in itemset!)
   */
  public boolean remove(int item)
  {
    if (item <= 0)
      throw new IllegalArgumentException("item must be a positive value");

    int index;
    for (index = 0; index < size && item != set[index] ; index++)
      ;

    if (item == set[index])
      {
	for (++index; index < size; index++)
	  set[index - 1] = set[index];
	size--;
	return true;
      }
    else 
      return false;
  }

  /**
   * Removes last item (which has the greatest value) from the itemset. 
   *
   * @return   true if item was removed, false if it wasn't removed (the
   * itemset was empty)
   */
  public boolean removeLast()
  {
    if (size > 0)
      {
	size--;
	return true;
      }
    else
      return false;
  }

  /**
   * Compare two Itemset objects on one of several criteria.
   *
   * @param is   the Itemset object with which we want to compare this
   * object
   * @param criteria   the criteria on which we want to compare, can 
   * be one of SIZE or SUPPORT.
   * @exception IllegalArgumentException   <code>obj</code> is not
   * an Itemset or criteria is invalid
   * @return   a negative value if this object is smaller than 
   * <code>is</code>, 0 if they are equal, and a positive value if this
   * object is greater.
   */
  public int compareTo(Object obj, int criteria)
  {
    if (!(obj instanceof Itemset))
      throw new IllegalArgumentException("not an itemset");

    Itemset is = (Itemset)obj;

    double diff;

    if (criteria == BY_SIZE)
      return size() - is.size();
    else if (criteria == BY_SUPPORT)
      diff = support - is.support;
    else
      throw new IllegalArgumentException("invalid criteria");

    if (diff < 0)
      return -1;
    else if (diff > 0)
      return 1;
    else 
      return 0;
  }

  /**
   * Checks equality with another object.
   *
   * @param o   the object against which we test for equality
   * @return true if object is equal to our itemset, false otherwise
   */
  public boolean equals(Object o)
  {
    if (o == this)
      return true;

    if (!(o instanceof Itemset))
      return false;

    Itemset itemset = (Itemset)o;

    if (size != itemset.size())
      return false;

    for (int i = 0; i < size; i++)
      if (set[i] != itemset.set[i])
	return false;

    return true;
  }

  /**
   * Checks inclusion in a given itemset.
   *
   * @param itemset   the itemset against which we test for inclusion
   * @exception IllegalArgumentException   <code>itemset</code> is null
   */
  public boolean isIncludedIn(Itemset itemset)
  {
    if (itemset == null)
      throw new IllegalArgumentException("null itemset");

    if (itemset.size() < size)
      return false;

    int i, j;

    for (i = 0, j = 0; 
	 i < size && j < itemset.size() && set[i] >= itemset.set[j]; 
	 j++)
      if (set[i] == itemset.set[j])
	i++;

    if (i == size)
      return true;
    else 
      return false;
  }

  /**
   * Return true if this itemset has items in common
   * with <code>itemset</code>.
   *
   * @param itemset   the itemset with which we compare
   * @exception IllegalArgumentException   <code>itemset</code> is null
   * @return   true if <code>itemset</code> contains items of this 
   * itemset, false otherwise.
   */
  public boolean intersects(Itemset itemset)
  {
    if (itemset == null)
      throw new IllegalArgumentException("null itemset");

    Itemset result = new Itemset(capacity);
    int i = 0;
    int j = 0;
    for ( ; i < size && j < itemset.size; )
      {
	// if elements are equal, return true
	if (set[i] == itemset.set[j])
	  return true;
	// if the element in this Itemset is bigger then
	// we need to move to the next item in itemset.
	else if (set[i] > itemset.set[j])
	  j++;
	// the element in this Itemset does not appear
	// in itemset so we need to add it to result
	else
	  i++;
      }

    return false;
  }

  /**
   * Return a new Itemset that contains only those items from
   * <code>is1</code> that do not appear in <code>is2</code>.
   *
   * @param is1   the itemset from which we want to subtract
   * @param is2   the itemset whose items we want to subtract
   * @exception IllegalArgumentException   <code>is1</code> or 
   * <code>is2</code> is null
   * @return   an Itemset containing only those items of
   * <code>is1</code> that do not appear in <code>is2</code>.
   */
  public static synchronized Itemset subtraction(Itemset is1, Itemset is2)
  {
    if (is1 == null || is2 == null)
      throw new IllegalArgumentException("null itemset");

    Itemset result = new Itemset(is1.size);
    int i = 0;
    int j = 0;
    for ( ; i < is1.size && j < is2.size; )
      {
	// if elements are equal, move to next ones
	if (is1.set[i] == is2.set[j])
	  {
	    i++;
	    j++;
	  }
	// if the element in is1 is bigger then
	// we need to move to the next item in is2.
	else if (is1.set[i] > is2.set[j])
	  j++;
	// the element in is1 does not appear
	// in is2 so we need to add it to result
	else
	  result.set[result.size++] = is1.set[i++];
      }

    // copy any remaining items from is1
    while (i < is1.size)
      result.set[result.size++] = is1.set[i++];

    // NOTE: the size of the resulting itemset
    // has been automatically updated
    return result;
  }

  /**
   * Return a new Itemset that contains all those items that appear
   * in <code>is1</code> and in <code>is2</code>.
   *
   * @param is1   the first itemset participating to the union
   * @param is2   the second itemset participating to the union
   * @exception IllegalArgumentException   <code>is1</code> or 
   * <code>is2</code> is null
   * @return   an Itemset containing all those items that appear
   * in <code>is1</code> and in <code>is2</code>.
   */
  public static synchronized Itemset union(Itemset is1, Itemset is2)
  {
    if (is1 == null || is2 == null)
      throw new IllegalArgumentException("null itemset");

    Itemset result = new Itemset(is1.size + is2.size);
    int i = 0;
    int j = 0;
    for ( ; i < is1.size && j < is2.size; )
      {
	// if elements are equal, copy then move to next ones
	if (is1.set[i] == is2.set[j])
	  {
	    result.set[result.size++] = is1.set[i++];
	    j++;
	  }
	// if the element in is1 is bigger then
	// we need to copy from is2 and then move to the next item.
	else if (is1.set[i] > is2.set[j])
	  result.set[result.size++] = is2.set[j++];
	// else we need to copy from is1
	else
	  result.set[result.size++] = is1.set[i++];
      }

    // copy any remaining items from is1 
    while (i < is1.size)
      result.set[result.size++] = is1.set[i++];
    // copy any remaining items from is2 
    while (j < is2.size)
      result.set[result.size++] = is2.set[j++];

    // NOTE: the size of the resulting itemset
    // has been automatically updated
    return result;
  }

  /**
   * Check whether two itemsets can be combined. Two itemsets can be
   * combined if they differ only in the last item.
   *
   * @param itemset   itemset with which to combine
   * @exception IllegalArgumentException   <code>itemset</code> is null
   * @return   true if the itemsets can be combined, false otherwise
   */
  public boolean canCombineWith(Itemset itemset)
  {
    if (itemset == null)
      throw new IllegalArgumentException("null itemset");

    if (size != itemset.size)
      return false;

    if (size == 0)
      return false;

    for (int i = 0; i < size - 1; i++)
      if (set[i] != itemset.set[i])
	return false;

    return true;
  }

  /**
   * Combine two itemsets into a new one that will contain all the
   * items in the first itemset plus the last item in the second
   * itemset.
   *
   * @param itemset   itemset with which to combine
   * @exception IllegalArgumentException   <code>itemset</code> is null
   * @return   an itemset that combines the two itemsets as described
   * above 
   */
  public Itemset combineWith(Itemset itemset)
  {
    if (itemset == null)
      throw new IllegalArgumentException("null itemset");

    Itemset is = new Itemset(this);
    is.support = 0;
    is.weight = 0;

    is.add(itemset.set[itemset.size - 1]);

    return is;
  }

  /**
   * Mark the itemset.
   *
   * @return   true if itemset was already marked, false otherwise
   */
  public boolean mark()
  {
    boolean old_mark = mark;
    mark = true;
    return old_mark;
  }

  /**
   * Unmark the itemset.
   *
   * @return   true if itemset was marked, false otherwise
   */
  public boolean unmark()
  {
    boolean old_mark = mark;
    mark = false;
    return old_mark;
  }

  /**
   * Return itemset mark.
   *
   * @return   true if itemset is marked, false otherwise
   */
  public boolean isMarked()
  {
    return mark;
  }

  /**
   * Return a String representation of the Itemset.
   *
   * @return   String representation of Itemset
   */
  public String toString()
  {
    String s = "";

    for (int i = 0; i < size; i++)
      s += set[i] + " ";
    //s += "(" + support + ")";

    return s;
  }

  /**
   * Remove all non-maximal itemsets from the vector v
   *
   * @param v   the collection of itemsets
   */
  public static synchronized void pruneNonMaximal(ArrayList v)
  {
    int i, j;
    int size = v.size();

    for (i = 0; i < size; i++)
      {
	// see if anything is included in itemset at index i
	for (j = i + 1; j < size; j++)
	  if (((Itemset)v.get(j)).isIncludedIn((Itemset)v.get(i)))
	      {
		// replace this element with last, delete last,
		// and don't advance index
		v.set(j, v.get(v.size() - 1));
		v.remove(--size);
		j--;
	      }

	// see if itemset at index i is included in another itemset
	for (j = i + 1; j < size; j++)
	  if (((Itemset)v.get(i)).isIncludedIn((Itemset)v.get(j)))
	      {
		// replace this element with last, delete last,
		// and don't advance index
		v.set(i, v.get(v.size() - 1));
		v.remove(--size);
		i--;
		break;
	      }
      }
  }

  /**
   * Remove all duplicate itemsets from the vector v
   *
   * @param v   the collection of itemsets
   */
  public static synchronized void pruneDuplicates(ArrayList v)
  {
    int i, j;
    int size = v.size();

    for (i = 0; i < size; i++)
      {
	// see if anything is equal to itemset at index i
	for (j = i + 1; j < size; j++)
	  if (((Itemset)v.get(j)).equals((Itemset)v.get(i)))
	      {
		// replace this element with last, delete last,
		// and don't advance index
		v.set(j, v.get(v.size() - 1));
		v.remove(--size);
		j--;
	      }
      }
  }

  /**
   * sample usage and testing
   */
  public static void main(String[] args)
  {
    Itemset is1 = new Itemset();
    Itemset is2 = new Itemset();

    is1.add(7);
    is1.add(3);
    is1.add(15);
    is1.add(5);
    is1.add(12);
    is1.add(12);

    System.out.println("is1: " + is1);

    is2.add(12);
    is2.add(15);
    is2.add(7);
    is2.add(5);
    is2.add(3);
    is2.add(8);

    System.out.println("is2: " + is2);

    System.out.println("do is1 and is2 share items: " 
		       + is1.intersects(is2));
    System.out.println("do is2 and is1 share items: " 
		       + is2.intersects(is1));

    Itemset is3 = Itemset.subtraction(is1, is2);
    System.out.println("is3 <= subtracting is2 from is1:" + is3);

    System.out.println("do is1 and is3 share items: " 
		       + is1.intersects(is3));
    System.out.println("do is3 and is1 share items: " 
		       + is3.intersects(is1));

    is3 = Itemset.subtraction(is2, is1);
    System.out.println("is3 <= subtracting is1 from is2:" + is3);

    System.out.println("do is1 and is3 share items: " 
		       + is1.intersects(is3));
    System.out.println("do is3 and is1 share items: " 
		       + is3.intersects(is1));

    System.out.println("do is3 and is2 share items: " 
		       + is3.intersects(is2));
    System.out.println("do is2 and is3 share items: " 
		       + is2.intersects(is3));

    is1.add(17);
    System.out.println("is1: " + is1);
    System.out.println("is2: " + is2);
    System.out.println("adding is2 to is1:" + Itemset.union(is1, is2));
    System.out.println("adding is1 to is2:" + Itemset.union(is2, is1));

    System.out.println("is1: " + is1);
    System.out.println("is2: " + is2);
    System.out.println("is1 equal to is2: " + is1.equals(is2));
    System.out.println("is1 included in is2: " + is1.isIncludedIn(is2));
    System.out.println("is2 included in is1: " + is2.isIncludedIn(is1));

    is1.add(8);

    System.out.println("is1: " + is1);

    System.out.println("is1 equal to is2: " + is1.equals(is2));
    System.out.println("is1 included in is2: " + is1.isIncludedIn(is2));
    System.out.println("is2 included in is1: " + is2.isIncludedIn(is1));

    is1.add(1);

    System.out.println("is1: " + is1);

    System.out.println("is1 equal to is2: " + is1.equals(is2));
    System.out.println("is1 included in is2: " + is1.isIncludedIn(is2));
    System.out.println("is2 included in is1: " + is2.isIncludedIn(is1));

    is1.add(50);

    System.out.println("is1: " + is1);

    System.out.println("is1 equal to is2: " + is1.equals(is2));
    System.out.println("is1 included in is2: " + is1.isIncludedIn(is2));
    System.out.println("is2 included in is1: " + is2.isIncludedIn(is1));

    is1.add(100);

    System.out.println("is1: " + is1);

    System.out.println("is1 equal to is2: " + is1.equals(is2));
    System.out.println("is1 included in is2: " + is1.isIncludedIn(is2));
    System.out.println("is2 included in is1: " + is2.isIncludedIn(is1));

    System.out.println("adding 70 to is2: " + is2.add(70));
    System.out.println("adding 70 to is2: " + is2.add(70));

    System.out.println("is2: " + is2);

    System.out.println("is1 equal to is2: " + is1.equals(is2));
    System.out.println("is1 included in is2: " + is1.isIncludedIn(is2));
    System.out.println("is2 included in is1: " + is2.isIncludedIn(is1));

    System.out.println("removing 1 from is1: " + is1.remove(1));
    System.out.println("removing 1 from is1: " + is1.remove(1));
    System.out.println("is1: " + is1);
    System.out.println("removing 50 from is1: " + is1.remove(50));
    System.out.println("is1: " + is1);
    System.out.println("removing 70 from is2: " + is2.remove(70));
    System.out.println("is2: " + is2);

    System.out.print("going through items of is1:");
    for (int i = 0; i < is1.size(); i++)
      System.out.print(" " + is1.get(i));
    System.out.println("");

    System.out.print("going through items of is2:");
    for (int i = 0; i < is2.size(); i++)
      System.out.print(" " + is2.get(i));
    System.out.println("");

    while (is2.removeLast())
      ;
    System.out.println("is2: " + is2);

    System.out.println("mark is1, previous state: " + is1.mark());
    System.out.println("mark is1, previous state: " + is1.mark());
    System.out.println("is1 mark state: " + is1.isMarked());
    System.out.println("unmark is1, previous state: " + is1.unmark());
    System.out.println("unmark is1, previous state: " + is1.unmark());
  }
}
