/*
  AssociationRule.java

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

/*

  HISTORY:

      v1.1   added setConfidence() and the compute...() methods

      v1.0   first version

 */

/**

   An association rule has two parts: the antecedent of the rule
   and the consequent of the rule, both of which are itemsets.
   Associated with these are a support and a confidence. The support
   tells how many rows of a database support this rule, the 
   confidence tells what percentage of the rows that contain the
   antecedent also contain the consequent.

   @version 1.1
   @author Laurentiu Cristofor

*/
public class AssociationRule 
  implements java.io.Serializable, CriteriaComparable
{
  /**
   * Specifies sorting should be performed according to antecedent size.
   */
  public static final int BY_ANTECEDENT_SIZE = 0;

  /**
   * Specifies sorting should be performed according to consequent size.
   */
  public static final int BY_CONSEQUENT_SIZE = 1;

  /**
   * Specifies sorting should be performed according to rule support.
   */
  public static final int BY_SUPPORT         = 2;

  /**
   * Specifies sorting should be performed according to rule confidence.
   */
  public static final int BY_CONFIDENCE      = 3;

  /** 
   * The antecedent.
   *
   * @serial
   */
  private Itemset antecedent;

  /** 
   * The consequent.
   *
   * @serial
   */
  private Itemset consequent;

  /** 
   * The support of the association rule.
   *
   * @serial
   */
  private double support;

  /** 
   * The confidence of the association rule.
   *
   * @serial
   */
  private double confidence;

  /**
   * Creates a new association rule.
   *
   * @param antecedent   the antecedent of the association rule
   * @param consequent   the consequent of the association rule
   * @param support   the support of the association rule
   * @param confidence   the confidence of the association rule
   * @exception IllegalArgumentException   <code>antecedent</code> 
   * or <code>consequent</code> are null or <code>support</code>
   * or <code>confidence</code> are not between 0 and 1
   */
  public AssociationRule(Itemset antecedent, Itemset consequent,
			 double support, double confidence)
  {
    if (antecedent == null || consequent == null
	|| support < 0 || support > 1
	|| confidence < 0 || confidence > 1)
      throw new IllegalArgumentException("constructor requires itemsets as arguments");

    this.antecedent = antecedent;
    this.consequent = consequent;
    this.support = support;
    this.confidence = confidence;
  }

  /**
   * Return size of antecedent.
   *
   * @return   size of antecedent
   */
  public int antecedentSize()
  {
    return antecedent.size();
  }

  /**
   * Return size of consequent.
   *
   * @return   size of consequent
   */
  public int consequentSize()
  {
    return consequent.size();
  }

  /**
   * Return antecedent of association rule.
   */
  public Itemset getAntecedent()
  {
    return antecedent;
  }

  /**
   * Return consequent of association rule.
   */
  public Itemset getConsequent()
  {
    return consequent;
  }

  /**
   * Return support of association rule.
   */
  public double getSupport()
  {
    return support;
  }

  /**
   * Return confidence of association rule.
   */
  public double getConfidence()
  {
    return confidence;
  }

  /**
   * Set confidence of association rule.
   *
   * @param confidence   the new value of the confidence
   */
  public void setConfidence(double confidence)
  {
    this.confidence = confidence;
  }

  /**
   * Return i-th item in antecedent.
   *
   * @param i   the index of the item to get
   * @exception IndexOutOfBoundsException   <code>i</code> is an invalid index
   * @return   the <code>i</code>-th item in antecedent
   */
  public int getAntecedentItem(int i)
  {
    return antecedent.get(i);
  }
  
  /**
   * Return i-th item in consequent.
   *
   * @param i   the index of the item to get
   * @exception IndexOutOfBoundsException   <code>i</code> is an invalid index
   * @return   the <code>i</code>-th item in consequent
   */
  public int getConsequentItem(int i)
  {
    return consequent.get(i);
  }

  /**
   * Compare two AssociationRule objects on one of several criteria.
   *
   * @param ar   the AssociationRule object with which we want to
   * compare this object
   * @param criteria   the criteria on which we want to compare, can 
   * be one of ANTECEDENT_SIZE, CONSEQUENT_SIZE, SUPPORT or CONFIDENCE.
   * @exception IllegalArgumentException   <code>obj</code> is not an
   * AssociationRule or criteria is invalid
   * @return   a negative value if this object is smaller than 
   * <code>ar</code>, 0 if they are equal, and a positive value if this
   * object is greater.
   */
  public int compareTo(Object obj, int criteria)
  {
    if (!(obj instanceof AssociationRule))
      throw new IllegalArgumentException("not an association rule");

    AssociationRule ar = (AssociationRule)obj;

    double diff;

    if (criteria == BY_ANTECEDENT_SIZE)
      return this.antecedent.size() - ar.antecedent.size();
    else if (criteria == BY_CONSEQUENT_SIZE)
      return this.consequent.size() - ar.consequent.size();
    else if (criteria == BY_SUPPORT)
      diff = this.support - ar.support;
    else if (criteria == BY_CONFIDENCE)
      diff = this.confidence - ar.confidence;
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
   * Compare two AssociationRule objects on one of several criteria.
   *
   * @param ar   the AssociationRule object with which we want to
   * compare this object
   * @param criteria   the criteria on which we want to compare, can 
   * be one of ANTECEDENT_SIZE, CONSEQUENT_SIZE, SUPPORT or CONFIDENCE.
   * @return   true if the objects are equal in terms of antecedent
   * and consequent items; false otherwise.
   */
  public boolean equals(Object obj)
  {
    if (obj == this)
      return true;

    if (!(obj instanceof AssociationRule))
      return false;

    AssociationRule other = (AssociationRule)obj;

    if (antecedent.size() != other.antecedent.size())
      return false;

    if (consequent.size() != other.consequent.size())
      return false;

    for (int i = 0; i < antecedent.size(); i++)
      if (antecedent.get(i) != other.antecedent.get(i))
	return false;

    for (int i = 0; i < consequent.size(); i++)
      if (consequent.get(i) != other.consequent.get(i))
	return false;

    return true;
  }

  /**
   * Find out if this rule is covered (can be inferred from) the
   * <code>ar</code> rule.
   *
   * @param ar   the rule that we test against
   * @return true if our rule is covered by <code>ar</code> and false
   * otherwise.
   */
  public boolean isCoveredBy(AssociationRule ar)
  {
    Itemset is_ar = Itemset.union(ar.antecedent, ar.consequent);
    Itemset is_this = Itemset.union(antecedent, consequent);

    return (is_this.isIncludedIn(is_ar)
	    && antecedent.getSupport() <= ar.antecedent.getSupport());
  }

  /**
   * Compute and return the confidence of the rule.
   *
   * The confidence of rule A -> C is defined by sup(AC)/sup(A)
   *
   * @return   confidence of rule
   */
  public double computeConfidence()
  {
    return support / antecedent.getSupport();
  }

  /**
   * Compute and return the Piatetsky-Shapiro measure of the rule.
   *
   * The Piatetsky-Shapiro measure of rule A -> C is defined by
   * sup(AC) - sup(A)*sup(C)
   *
   * @return   Piatetsky-Shapiro measure of rule
   */
  public double computePiatetskyShapiro()
  {
    return support - antecedent.getSupport() * consequent.getSupport();
  }

  /**
   * Compute and return the lift of the rule.
   *
   * The lift of rule A -> C is defined by sup(AC) / (sup(A)*sup(C))
   *
   * @return   lift of rule
   */
  public double computeLift()
  {
    return support / (antecedent.getSupport() * consequent.getSupport());
  }

  /**
   * Compute and return the influence of the rule.
   *
   * The influence of rule A -> C is defined by sup(AC)/sup(A) - sup(C)
   *
   * @return   influence of rule
   */
  public double computeInfluence()
  {
    return support / antecedent.getSupport() - consequent.getSupport();
  }

  /**
   * Return a String representation of the AssociationRule.
   *
   * @return   String representation of AssociationRule
   */
  public String toString()
  {
    String s = "{";

    for (int i = 0; i < antecedent.size(); i++)
      s += antecedent.get(i) + " ";
    s += "}->{";

    for (int i = 0; i < consequent.size(); i++)
      s += consequent.get(i) + " ";
    s += "} (" + support + ", " + confidence + ")";

    return s;
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

    System.out.println("is1: " + is1);

    is2.add(12);
    is2.add(5);
    is2.add(8);

    System.out.println("is2: " + is2);

    AssociationRule ar = new AssociationRule(is1, is2, 
					     0.5055, 
					     0.3033); 

    System.out.println("ar: " + ar);
  }
}
