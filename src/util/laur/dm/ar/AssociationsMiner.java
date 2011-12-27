/*
  AssociationsMiner.java

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

import laur.tools.*;

import java.util.*;

/**

   This abstract class must be extended by the algorithms that will look
   for associations.
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public abstract class AssociationsMiner extends MonitoredThread
{
  /**
   * With this object we read from the cache
   */
  protected DBCacheReader cache_reader;

  /**
   * Minimum support value.
   */
  protected double min_support;

  /**
   * Minimum confidence value.
   */
  protected double min_confidence;

  /**
   * The items required to be in antecedent.
   */
  protected Itemset is_in_antecedent;

  /**
   * The items required to be in consequent.
   */
  protected Itemset is_in_consequent;

  /**
   * The items to ignore.
   */
  protected Itemset is_ignored;

  /**
   * Maximum number of items allowed in antecedent.
   */
  protected int max_antecedent;

  /**
   * Minimum number of items required in consequent.
   */
  protected int min_consequent;

  // this will be true if we use the advanced options
  private boolean bAdvanced;

  // this will keep the mining result (the rules)
  private ArrayList result;


  /**
   * Find association rules in a database, given the set of
   * frequent itemsets.
   *
   * @param cacheReader   the object used to read from the cache
   * @param minSupport   the minimum support
   * @param minConfidence   the minimum confidence
   * @return   an ArrayList containing all association rules found
   */
  public abstract ArrayList findAssociations(DBCacheReader cacheReader,
					     double minSupport,
					     double minConfidence);

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
  public abstract ArrayList findAssociations(DBCacheReader cacheReader, 
					     double minSupport,
					     double minConfidence,
					     Itemset inAntecedent,
					     Itemset inConsequent,
					     Itemset ignored,
					     int maxAntecedent, 
					     int minConsequent);


  /**
   * Sets the parameters for the mining algorithm. This method should
   * be used before starting the thread.
   *
   * @param monitor   an object that we should notify about important
   * events 
   * @param cacheReader   the object used to read from the cache
   * @param minSupport   the minimum support
   * @param minConfidence   the minimum confidence
   * @exception IllegalStateException   if the thread is already running
   */
  public void setParameters(ThreadMonitor monitor,
			    DBCacheReader cacheReader,
			    double minSupport,
			    double minConfidence)
  {
    if (isRunning)
      throw new IllegalStateException();

    bAdvanced = false;

    this.monitor = monitor;

    cache_reader = cacheReader;
    min_support = minSupport;
    min_confidence = minConfidence;
  }

  /**
   * Sets the parameters for the mining algorithm. This method should
   * be used before starting the thread.
   *
   * @param monitor   an object that we should notify about important
   * events 
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
   * @exception IllegalStateException   if the thread is already running
   */
  public void setParameters(ThreadMonitor monitor,
			    DBCacheReader cacheReader,
			    double minSupport,
			    double minConfidence,
			    Itemset inAntecedent,
			    Itemset inConsequent,
			    Itemset ignored,
			    int maxAntecedent, 
			    int minConsequent)
  {
    if (isRunning)
      throw new IllegalStateException();

    bAdvanced = true;

    this.monitor = monitor;

    cache_reader = cacheReader;
    min_support = minSupport;
    min_confidence = minConfidence;

    is_in_antecedent = inAntecedent;
    is_in_consequent = inConsequent;
    is_ignored = ignored;
    max_antecedent = maxAntecedent;
    min_consequent = minConsequent;
  }

  /**
   * Executes the findAssociations() method.
   */
  protected void execute()
  {
    if (bAdvanced)
      result = findAssociations(cache_reader, min_support, min_confidence,
				is_in_antecedent, is_in_consequent,
				is_ignored,
				max_antecedent, min_consequent);
    else
      result = findAssociations(cache_reader, min_support, min_confidence);
  }

  /**
   * Gets the value returned by findAssociations() after the thread
   * completed its execution.
   *
   * @return   the rules discovered in the database
   * @exception IllegalStateException   if the thread is still running
   * or if no result is available
   */
  public ArrayList getResult()
  {
    if (isRunning)
      throw new IllegalStateException("thread still running");

    if (result == null)
      throw new IllegalStateException("thread was never run or it was aborted");

    return result;
  }
}
