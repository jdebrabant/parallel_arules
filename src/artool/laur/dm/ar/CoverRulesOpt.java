/*
  CoverRulesOpt.java

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

   This class implements an optimized algorithm 
   for finding the cover of all valid association rules.
   
   @version 1.1
   @author Laurentiu Cristofor

*/
public class CoverRulesOpt extends AssociationsMiner
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
      rule_bases.enQueue(new RuleBase((Itemset)large.get(i), 0));

    // start generating the cover rules
    while (!rule_bases.isEmpty())
      processRuleBase(rule_bases.deQueue(), rule_bases);
  }

  private void processRuleBase(RuleBase rule_base, Queue rule_bases)
  {
    // check for user-requested abort
    checkAbort();

    Itemset is = null;

    // if a candidate antecedent has support bigger than the following
    // threshold, then the rule determined by it will not be valid.
    double support_sup_threshold 
      = rule_base.itemset.getSupport()/min_confidence;

    // if a candidate antecedent has support smaller or equal than the
    // following threshold, then the rule determined by it can be
    // infered from a rule in cover.
    double support_inf_threshold
      = rule_base.support_inf_threshold;

    // this flag will remind us if we need to add new rule bases
    boolean bAddNewRuleBases = false;

    // this vector will hold subsets of rule_base that have size 1
    ArrayList subsets_1 = new ArrayList();

    // generate the subsets of size 1
    for (int i = 0; i < rule_base.itemset.size(); i++)
      {
	is = new Itemset(1);
	is.add(rule_base.itemset.get(i));
	setSupport(is);
	subsets_1.add(is);
      }

    // this vector will hold subsets of rule_base that have equal size
    ArrayList subsets = new ArrayList();

    // eliminate the subsets which, if used as antecedents, would
    // generate redundant rules
    subsets = filter(subsets_1, support_inf_threshold);
    
    if (subsets.isEmpty())
      return;

    AssociationRule candidate_cover_rule = null;

    // card keeps track of the cardinality of the subsets elements
    for (int card = 1; card < rule_base.itemset.size(); card++)
      {
	// sort the subsets collection, we keep subsets vector
	// unchanged because it will be used in the generation of
	// subsets of superior cardinality
	ArrayList ordered_subsets = (ArrayList)subsets.clone();
	CriteriaSorter.sort(ordered_subsets, 
			    Itemset.BY_SUPPORT, CriteriaSorter.ASC);

	// do a binary search for a subset with the largest support
	// smaller or equal than support_sup_threshold; this will
	// become the candidate for a cover rule antecedent based on
	// this rule_base
	int index = binarySearch(ordered_subsets, support_sup_threshold);

	// if all subsets had support < support_sup_treshold, then select
	// the last one
	if (index > ordered_subsets.size() - 1)
	  index--;

	// if there was no subset with support =
	// support_sup_threshold, but there were itemsets with greater
	// support, then the search led to the first such subset. We
	// select the one to its left. If such a subset does not
	// exist, index will become invalid.
	if (((Itemset)ordered_subsets.get(index)).getSupport()
	    > support_sup_threshold)
	  index--;

	// if we found an itemset that can be used as an antecedent,
	// then use it
	if (index >= 0)
	  {
	    is = (Itemset)ordered_subsets.get(index);
	    candidate_cover_rule = generateRule(rule_base, is);
	    
	    // set support_inf_threshold to the support of the
	    // antecedent of the newly generated candidate cover
	    // rule
	    support_inf_threshold = is.getSupport();
	  }

	// in the case that the most frequent 1-itemset in rule_base
	// cannot be used as an antecedent, we need to add all subsets
	// of rule_base to rule_bases. They only need be added if they
	// have sizes at least 2, so rule_base must have size above 2.
	if (card == 1)
	  {
	    Itemset most_frequent
	      = (Itemset)ordered_subsets.get(ordered_subsets.size() - 1);

	    // if we didn't generate a candidate cover rule, then is
	    // is not set to any antecedent
	    if (candidate_cover_rule == null 
		|| is.getSupport() < most_frequent.getSupport())
	      bAddNewRuleBases = true;
	  }

	subsets = generateNextSubsets(subsets);
	subsets = filter(subsets, support_inf_threshold);
	if (subsets.isEmpty())
	  break;
      }

    if (candidate_cover_rule != null)
      checkAndAddCandidateCoverRule(candidate_cover_rule);

    if (bAddNewRuleBases)
      addNewRuleBases(rule_bases, rule_base, subsets_1,
		      support_inf_threshold);
  }

  // this method eliminates from the array v all the itemsets whose
  // support is <= inf_supp
  private ArrayList filter(ArrayList v, double inf_supp)
  {
    ArrayList result = new ArrayList();
    Itemset is;
    double supp;

    for (int i = 0; i < v.size(); i++)
      {
	is = (Itemset)v.get(i);
	supp = is.getSupport();

	if (supp > inf_supp)
	  result.add(is);
      }

    return result;
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
  private AssociationRule generateRule(RuleBase rule_base, Itemset antecedent)
  {
    Itemset consequent 
      = Itemset.subtraction(rule_base.itemset, antecedent);
    setSupport(consequent);
    double support = rule_base.itemset.getSupport();
    double confidence 
      = rule_base.itemset.getSupport()/antecedent.getSupport();
	    
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
			       RuleBase rule_base, ArrayList subsets_1,
			       double support_inf_threshold)
  {
    Itemset new_rule_base;

    if (rule_base.itemset.size() > 2)
      for (int i = 0; i < rule_base.itemset.size(); i++)
	{
	  new_rule_base = Itemset.subtraction(rule_base.itemset, 
					      (Itemset)subsets_1.get(i));
	  
	  if (!rule_bases.includes(new_rule_base))
	    {
	      setSupport(new_rule_base);
	      rule_bases.enQueue(new RuleBase(new_rule_base,
					      support_inf_threshold));
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

  // this class keeps an itemset and a threshold indicating the
  // desired minimum antecedent support we search for a potential
  // cover rule based on the itemset
  private class RuleBase
  {
    Itemset itemset;
    double support_inf_threshold;

    RuleBase(Itemset is, double inf_supp)
    {
      itemset = is;
      support_inf_threshold = inf_supp;
    } 
  }

  // a SLL implementation of a queue for holding itemsets.
  private class Queue
  {
    private class Node
    {
      RuleBase data;
      Node next;

      Node(RuleBase rb)
      {
	data = rb;
      }
    }

    private Node head, tail;

    boolean isEmpty()
    {
      return head == null;
    }

    void enQueue(RuleBase rb)
    {
      if (isEmpty())
	head = tail = new Node(rb);
      else
	{
	  tail.next = new Node(rb);
	  tail = tail.next;
	}
    }

    RuleBase deQueue()
    {
      if (isEmpty())
	throw new EmptyQueueException();

      RuleBase rb = head.data;
      if (head == tail)
	head = tail = null;
      else
	head = head.next;
      return rb;
    }

    boolean includes(Itemset is)
    {
      for (Node walker = head; walker != null; walker = walker.next)
	if (is.isIncludedIn(walker.data.itemset))
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
