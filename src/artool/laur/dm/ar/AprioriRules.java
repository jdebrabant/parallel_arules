/*
  AprioriRules.java

  (P)2000-2002 Laurentiu Cristofor
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

      v1.2   simplified the algorithm implementation, uses now
               fewer lines of code

      v1.1   now sets the support of rule antecedents and consequents

      v1.0   first version

 */

/**

   This class implements the Apriori ap_genrules algorithm 
   for finding association rules.<P>

   (see "Fast Algorithms for Mining Association Rules"
   by Rakesh Agrawal and Ramakrishnan Srikant
   from IBM Almaden Research Center 1994)

   We also implement a variant of ap_genrules that generates only
   rules having the user specified characteristics.
   
   @version 1.2
   @author Laurentiu Cristofor

*/
public class AprioriRules extends AssociationsMiner
{
  private SET supports;
  private ArrayList rules;

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
   * @return   an ArrayList containing all association rules found
   */
  public ArrayList findAssociations(DBCacheReader cacheReader,
				    double minSupport,
				    double minConfidence)
  {
    min_support = minSupport;
    min_confidence = minConfidence;

    // create the vector where we'll put the rules
    rules = new ArrayList();
    
    // create new SET
    supports = new SET();

    // read from cache the supports of frequent itemsets
    SET.initializeSET(supports, min_support, cacheReader);

    // get the frequent itemsets
    ArrayList frequent = supports.getItemsets();

    // generate rules from each frequent itemset
    for (int i = 0; i < frequent.size(); i++)
      {
	// check for user-requested abort
	checkAbort();

	// get a frequent itemset
	Itemset is_frequent = (Itemset)frequent.get(i);

	// skip it if it's too small
	if (is_frequent.size() <= 1)
	  continue;

	// get all possible 1 item consequents
	ArrayList consequents = new ArrayList(is_frequent.size());
	for (int k = 0; k < is_frequent.size(); k++)
	  {
	    int item = is_frequent.get(k);
	    Itemset is_consequent = new Itemset(1);
	    is_consequent.add(item);
	    consequents.add(is_consequent);
	  }

	// call the ap_genrules procedure for generating all rules
	// out of this frequent itemset
	ap_genrules(is_frequent, consequents);
      }

    return rules;
  }

  // this is the ap-genrules procedure that generates rules out
  // of a frequent itemset.
  private void ap_genrules(Itemset is_frequent, ArrayList consequents)
  {
    if (consequents.size() == 0)
      return;

    // the size of frequent must be bigger than the size of the itemsets
    // in consequents by at least 1, in order to be able to generate
    // a rule in this call
    if (is_frequent.size() > ((Itemset)(consequents.get(0))).size())
      {
	AssociationRule ar;

	for (int i = 0; i < consequents.size(); i++)
	  {
	    Itemset is_consequent = (Itemset)consequents.get(i);
	    Itemset is_antecedent = Itemset.subtraction(is_frequent, 
							is_consequent);
	    double antecedent_support = 0;
	    try
	      {
		antecedent_support = supports.getSupport(is_antecedent);
	      }
	    catch (SETException e)
	      {
		System.err.println("Error getting support from SET!!!\n" + e);
	      }
	    double confidence = is_frequent.getSupport() / antecedent_support;
	    
	    // if the rule satisfies our requirements we add it
	    // to our collection
	    if (confidence >= min_confidence)
	      {
		setSupport(is_antecedent);
		setSupport(is_consequent);

		rules.add(new AssociationRule(is_antecedent, is_consequent,
					      is_frequent.getSupport(), 
					      confidence));
	      }
	    // otherwise we remove the consequent from the collection
	    // and we update the index such that we don't skip a consequent
	    else
	      consequents.remove(i--);
	  }

	// try to generate larger possible consequents
	consequents = apriori_gen(consequents);
	ap_genrules(is_frequent, consequents);
      }
  }

  // this is the apriori_gen procedure that generates starting from
  // a k-itemset collection a new collection of (k+1)-itemsets.
  private ArrayList apriori_gen(ArrayList itemsets)
  {
    if (itemsets.size() == 0)
      return new ArrayList(0);

    // create a hashtree so that we can check more efficiently the
    // number of subsets
    // this may not really be necessary when generating rules since
    // itemsets will probably be a small collection, but just in case
    HashTree ht_itemsets = new HashTree(itemsets);
    for (int i = 0; i < itemsets.size(); i++)
      ht_itemsets.add(i);
    ht_itemsets.prepareForDescent();

    ArrayList result = new ArrayList();
    Itemset is_i, is_j;
    for (int i = 0; i < itemsets.size() - 1; i++)
      for (int j = i + 1; j < itemsets.size(); j++)
	{
	  is_i = (Itemset)itemsets.get(i);
	  is_j = (Itemset)itemsets.get(j);

	  // if we cannot combine element i with j then we shouldn't
	  // waste time for bigger j's. This is because we keep the
	  // collections ordered, an important detail in this implementation
	  if (!is_i.canCombineWith(is_j))
	    break;
	  else
	    {
	      Itemset is = is_i.combineWith(is_j);

	      // a real k-itemset has k (k-1)-subsets
	      // so we test that this holds before adding to result
	      if (ht_itemsets.countSubsets(is) 
		  == is.size())
		result.add(is);
	    }
	}

    return result;
  }

  /**
   * Find association rules in a database, given the set of
   * frequent itemsets and a set of restrictions.
   *
   * @param cacheReader   the object used to read from the cache
   * @param minSupport   the minimum support
   * @param minConfidence   the minimum confidence
   * @param inAntecedent   the items that must appear in the antecedent 
   * of each rule, if null then this constraint is ignored 
   * @param inConsequent   the items that must appear in the consequent
   * of each rule, if null then this constraint is ignored 
   * @param ignored   the items that should be ignored,
   * if null then this constraint is ignored 
   * @param maxAntecedent   the maximum number of items that can appear
   * in the antecedent of each rule, if 0 then this constraint is ignored 
   * @param minConsequent   the minimum number of items that should appear
   * in the consequent of each rule, if 0 then this constraint is ignored 
   * @return   an ArrayList containing all association rules found
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
    min_support = minSupport;
    min_confidence = minConfidence;

    is_in_antecedent = inAntecedent;
    is_in_consequent = inConsequent;
    is_ignored = ignored;
    max_antecedent = maxAntecedent;
    min_consequent = minConsequent;

    // create the vector where we'll put the rules
    rules = new ArrayList();
    
    // create new SET
    supports = new SET();

    // read from cache the supports of frequent itemsets
    SET.initializeSET(supports, min_support, cacheReader);

    // get the frequent itemsets
    ArrayList frequent = supports.getItemsets();

    if (frequent.size() == 0)
      return rules;

    // if we need to ignore some items
    if (ignored != null)
      {
	// remove all frequent itemsets that contain
	// items to be ignored; their subsets that do
	// not contain those items will remain
	for (int i = 0; i < frequent.size(); i++)
	  {
	    Itemset is = (Itemset)frequent.get(i);
	    if (is.intersects(ignored))
	      {
		// replace this element with last, delete last,
		// and don't advance index
		frequent.set(i, frequent.get(frequent.size() - 1));
		frequent.remove(frequent.size() - 1);
		i--;
	      }
	  }

	if (frequent.size() == 0)
	  return rules;
      }

    // if we need to have some items in the antecedent or consequent
    if (inAntecedent != null || inConsequent != null)
      {
	// remove frequent itemsets that don't have the
	// required items
	for (int i = 0; i < frequent.size(); i++)
	  {
	    Itemset is = (Itemset)frequent.get(i);
	    if (inAntecedent != null && !inAntecedent.isIncludedIn(is))
	      {
		// replace this element with last, delete last,
		// and don't advance index
		frequent.set(i, frequent.get(frequent.size() - 1));
		frequent.remove(frequent.size() - 1);
		i--;
	      }
	    else if (inConsequent != null && !inConsequent.isIncludedIn(is))
	      {
		// replace this element with last, delete last,
		// and don't advance index
		frequent.set(i, frequent.get(frequent.size() - 1));
		frequent.remove(frequent.size() - 1);
		i--;
	      }
	  }

	if (frequent.size() == 0)
	  return rules;
      }

    // generate rules from each frequent itemset
    for (int i = 0; i < frequent.size(); i++)
      {
	// check for user-requested abort
	checkAbort();

	// get a frequent itemset
	Itemset is_frequent = (Itemset)frequent.get(i);

	// skip it if it's too small
	if (is_frequent.size() <= 1 ||
	    is_frequent.size() <= minConsequent)
	  continue;

	// get all possible 1 item consequents
	ArrayList consequents = new ArrayList(is_frequent.size());
	for (int k = 0; k < is_frequent.size(); k++)
	  {
	    int item = is_frequent.get(k);
	    Itemset is_consequent = new Itemset(1);
	    is_consequent.add(item);
	    consequents.add(is_consequent);
	  }

	// call the ap-genrules procedure for generating all rules
	// out of this frequent itemset
	ap_genrules_constraint(is_frequent, consequents);
      }

    return rules;
  }

  // this is the ap-genrules procedure that generates rules out
  // of a frequent itemset.
  private void ap_genrules_constraint(Itemset is_frequent, 
				      ArrayList consequents)
  {
    if (consequents.size() == 0)
      return;

    // the size of frequent must be bigger than the size of the itemsets
    // in consequents by at least 1, in order to be able to generate
    // a rule in this call
    if (is_frequent.size() > ((Itemset)(consequents.get(0))).size())
      {
	AssociationRule ar;

	for (int i = 0; i < consequents.size(); i++)
	  {
	    Itemset is_consequent = (Itemset)consequents.get(i);
	    Itemset is_antecedent = Itemset.subtraction(is_frequent,
							is_consequent);
	    double antecedent_support = 0;
	    try
	      {
		antecedent_support = supports.getSupport(is_antecedent);
	      }
	    catch (SETException e)
	      {
		System.err.println("Error getting support from SET!!!\n" + e);
	      }
	    double confidence = is_frequent.getSupport() / antecedent_support;
	    
	    // if the rule does not satisfy our confidence requirements,
	    // then we remove the consequent from the collection
	    // and we update the index such that we don't skip a consequent
	    if (confidence < min_confidence)
	      consequents.remove(i--);
	    // else we verify whether the rule satisfies the other
	    // conditions specified by user
	    else
	      {
		if (is_in_antecedent != null
		    && !is_in_antecedent.isIncludedIn(is_antecedent))
		  {
		    // if the antecedent lacks required items,
		    // then they went in the consequent, abandon it
		    consequents.remove(i--);
		    continue;
		  }

		if (is_in_consequent != null
		    && !is_in_consequent.isIncludedIn(is_consequent))
		  continue;

		if (max_antecedent > 0
		    && is_antecedent.size() > max_antecedent)
		  continue;

		if (min_consequent > 0 
		    && is_consequent.size() < min_consequent)
		  continue;

		// if the rule satisifes all requirements then
		// we add it to the rules collection
		setSupport(is_antecedent);
		setSupport(is_consequent);

		rules.add(new AssociationRule(is_antecedent, 
					      is_consequent,
					      is_frequent.getSupport(), 
					      confidence));
	      }
	  }

	// try to generate larger possible consequents
	consequents = apriori_gen(consequents);
	ap_genrules_constraint(is_frequent, consequents);
      }
  }
}
