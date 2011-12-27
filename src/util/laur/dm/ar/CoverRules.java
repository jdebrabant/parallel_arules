/*
  CoverRules.java

  (P)2001 Laurentiu Cristofor
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

      v1.1   now sets the support of rule consequents

      v1.0   first version

 */

/**

   This class implements an algorithm 
   for finding the cover of all valid association rules.
   
   @version 1.1
   @author Laurentiu Cristofor

*/
public class CoverRules extends AssociationsMiner
{
  private SET supports;
  private ArrayList cover;

  // sets the support of an itemset to the value obtained from the
  // supports SET
  private void setSupport(Itemset is)
  {
    try
      {
	is.setSupport(supports.getSupport(is));
      }
    catch (SETException e)
      {
	System.err.println("Error getting support from SET!!!\n" + e);
      }
  }

  /**
   * Find association rules in a database, given the set of
   * frequent itemsets.
   *
   * @param cacheReader   the object used to read from the cache
   * @param minSupport   the minimum support
   * @param minConfidence   the minimum confidence
   * @return   a ArrayList containing all association rules found
   */
  public ArrayList findAssociations(DBCacheReader cacheReader,
				    double minSupport,
				    double minConfidence)
  {
    min_support = minSupport;
    min_confidence = minConfidence;

    supports = new SET();

    // read from cache the supports of frequent itemsets
    SET.initializeSET(supports, min_support, cacheReader);

    // create the vector where we'll put the cover rules
    cover = new ArrayList();

    findCover();

    return cover;
  }

  private void findCover()
  {
    // get the maximal frequent itemsets
    ArrayList large = supports.getLargeItemsets();

    // remove large itemsets of size 1
    for (int i = 0; i < large.size(); i++)
      if (((Itemset)large.get(i)).size() == 1)
	{
	  large.set(i, large.get(large.size() - 1));
	  large.remove(large.size() - 1);
	  i--;
	}

    // sort large itemsets in decreasing order of their size
    CriteriaSorter.sort(large, Itemset.BY_SIZE, CriteriaSorter.DESC);

    // initialize the queue, I call it rule_bases because each itemset
    // it contains will serve as a base for generating some rule
    Queue rule_bases = new Queue();
    for (int i = 0; i < large.size(); i++)
      rule_bases.enQueue((Itemset)large.get(i));

    // start generating the cover rules
    while (!rule_bases.isEmpty())
      processRuleBase(rule_bases.deQueue(), rule_bases);
  }

  private void processRuleBase(Itemset rule_base, Queue rule_bases)
  {
    // check for user-requested abort
    checkAbort();

    Itemset is;

    // if a candidate antecedent has support bigger than the following
    // threshold, then the rule determined by it will not be valid.
    double support_sup_threshold 
      = rule_base.getSupport()/min_confidence;

    // this vector will hold subsets of rule_base that have size 1
    ArrayList subsets_1 = new ArrayList();

    // generate the subsets of size 1
    for (int i = 0; i < rule_base.size(); i++)
      {
	is = new Itemset(1);
	is.add(rule_base.get(i));
	setSupport(is);
	subsets_1.add(is);
      }

    // this vector will hold all subsets of rule_base
    ArrayList subsets = new ArrayList();

    // generate the subsets of larger cardinalities
    subsets = (ArrayList)subsets_1.clone();
    ArrayList k_subsets = subsets;
    for (int card = 2; card < rule_base.size(); card++)
      {
	k_subsets = generateNextSubsets(k_subsets);
	subsets.addAll(k_subsets);
      }

    CriteriaSorter.sort(subsets, Itemset.BY_SUPPORT, CriteriaSorter.ASC);

    // do a binary search for the subset with the largest support
    // smaller or equal than support_sup_threshold; this will become
    // the candidate for a cover rule antecedent based on this
    // rule_base
    int index = binarySearch(subsets, support_sup_threshold);

    // if all subsets had support < support_sup_treshold, then select
    // the last one
    if (index > subsets.size() - 1)
      index--;

    // if there was no subset with support = support_sup_threshold,
    // but there were itemsets with greater support, then the search
    // led to the first such subset. We select the one to its left if
    // such a subset exists, otherwise we add new rule bases.
    if (((Itemset)subsets.get(index)).getSupport() > support_sup_threshold)
      if (index > 0)
	index--;
      else
	{
	  addNewRuleBases(rule_bases, rule_base, subsets_1);
	  return;
	}

    // we found a subset with largest support <=
    // support_sup_threshold. If there are more of them, then we want
    // to get the one with smallest cardinality, so we go left (since
    // the subsets were added in increasing order of their
    // cardinalities and the sorting was stable!).
    double supp = ((Itemset)subsets.get(index)).getSupport();
    while (index > 0 
	   && ((Itemset)subsets.get(index - 1)).getSupport() == supp)
      index--;

    is = (Itemset)subsets.get(index);
    AssociationRule rule = generateRule(rule_base, is);
    checkAndAddCandidateCoverRule(rule);

    // in the case that the most frequent 1-itemset in rule_base
    // cannot be used as an antecedent, we need to add all subsets of
    // rule_base to rule_bases. They only need be added if they have
    // sizes at least 2, so rule_base must have size above 2.
    
    // We use the observation that the most frequent subset of size 1
    // is also the most frequent among all subsets, so its support is
    // equal to the support of the last itemset in subsets.
    if ((is.getSupport() 
	 < ((Itemset)subsets.get(subsets.size() - 1)).getSupport()))
      addNewRuleBases(rule_bases, rule_base, subsets_1);
  }

  // perform a binary search for an itemset of given support
  // value in a vector of itemsets sorted by support
  // return index where itemset was found or should be inserted
  private int binarySearch(ArrayList itemsets, double support)
  {
    int left = 0;
    int right = itemsets.size() - 1;
    int middle;
    double current_support;
    Itemset is;

    while (right >= left)
      {
	middle = (left + right)/2;
	is = (Itemset)itemsets.get(middle);
	current_support = is.getSupport();

	if (support > current_support)
	  left = middle + 1;
	else if (support < current_support)
	  right = middle - 1;
	else
	  return middle;
      }

    return left;
  }

  // generate a rule from a rule base and a specified antecedent
  private AssociationRule generateRule(Itemset rule_base, Itemset antecedent)
  {
    Itemset consequent 
      = Itemset.subtraction(rule_base, antecedent);
    setSupport(consequent);
    double support = rule_base.getSupport();
    double confidence 
      = rule_base.getSupport()/antecedent.getSupport();
	    
    return new AssociationRule(antecedent, consequent,
			       support, confidence);
  }

  // add rule to cover if it cannot be infered from a rule in cover
  // return true if rule was added to cover, false otherwise
  private boolean checkAndAddCandidateCoverRule(AssociationRule rule)
  {
    // verify that the rule is indeed a cover rule; we check
    // that it cannot be infered from an already generated
    // cover rule.
    boolean bCovered = false;
    for (int i = 0; i < cover.size(); i++)
      if (rule.isCoveredBy((AssociationRule)cover.get(i)))
	{
	  bCovered = true;
	  break;
	}
    if (!bCovered)
      {    
	// the rule is a cover rule so we add it
	cover.add(rule);
	return true;
      }
    else
      return false;
  }

  // this method adds to rule_bases all subsets of rule_base that have
  // cardinality one less the cardinality of rule_base
  private void addNewRuleBases(Queue rule_bases, 
			       Itemset rule_base, ArrayList subsets_1)
  {
    Itemset new_rule_base;

    if (rule_base.size() > 2)
      for (int i = 0; i < rule_base.size(); i++)
	{
	  new_rule_base = Itemset.subtraction(rule_base, 
					      (Itemset)subsets_1.get(i));
	  
	  if (!rule_bases.includes(new_rule_base))
	    {
	      setSupport(new_rule_base);
	      rule_bases.enQueue(new_rule_base);
	    }
	}
  }

  // this method generates subsets of cardinality k+1 starting from
  // subsets of cardinality k, using the apriori_gen method
  private ArrayList generateNextSubsets(ArrayList subsets)
  {
    ArrayList new_subsets = new ArrayList();

    for (int i = 0; i < subsets.size() - 1; i++)
      for (int j = i + 1; j < subsets.size(); j++)
	{
	  Itemset is_i = (Itemset)subsets.get(i);
	  Itemset is_j = (Itemset)subsets.get(j);

	  if (!is_i.canCombineWith(is_j))
	    break;
	  else
	    {
	      Itemset is = is_i.combineWith(is_j);
	      setSupport(is);
	      new_subsets.add(is);
	    }
	}

    return new_subsets;
  }

  // a SLL implementation of a queue for holding itemsets.
  private class Queue
  {
    private class Node
    {
      Itemset data;
      Node next;

      Node(Itemset is)
      {
	data = is;
      }
    }

    private Node head, tail;

    boolean isEmpty()
    {
      return head == null;
    }

    void enQueue(Itemset is)
    {
      if (isEmpty())
	head = tail = new Node(is);
      else
	{
	  tail.next = new Node(is);
	  tail = tail.next;
	}
    }

    Itemset deQueue()
    {
      if (isEmpty())
	throw new EmptyQueueException();

      Itemset is = head.data;
      if (head == tail)
	head = tail = null;
      else
	head = head.next;
      return is;
    }

    boolean includes(Itemset is)
    {
      for (Node walker = head; walker != null; walker = walker.next)
	if (is.isIncludedIn(walker.data))
	  return true;

      return false;
    }
  }

  private class EmptyQueueException extends RuntimeException
  {
  }

  /**
   * Find association rules in a database, given the set of
   * frequent itemsets and a set of restrictions.
   *
   * This method is not supported.
   */
  public ArrayList findAssociations(DBCacheReader cacheReader, 
				    double minSupport,
				    double minConfidence,
				    Itemset inAntecedent,
				    Itemset inConsequent,
				    Itemset ignored,
				    int maxAntecedent, 
				    int minConsequent)
  {
    // Not supported, we only find the cover with this algorithm
    throw new UnsupportedOperationException();
  }
}
