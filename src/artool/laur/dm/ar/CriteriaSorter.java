/*
  CriteriaSorter.java

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

/**
 
   This class implements a method for sorting a List of
   ComparableCriteria objects. It uses Collections.sort() so it
   provides a stable sorting procedure.

   @version 1.0
   @author Laurentiu Cristofor

 */
public class CriteriaSorter
{
  /**
   * Specifies descending order.
   */
  public static final int DESC = 0;

  /**
   * Specifies ascending order.
   */
  public static final int ASC  = 1;

  /**
   * Sort a List of ComparableCriteria objects according to criteria
   * <code>criteria</code> ad in the order specified by
   * <code>order</code>. Uses Collections.sort().
   *
   * @param l   the List to be sorted
   * @param criteria   the criteria based on which we will sort the
   * List elements, varies depending on the ComparableCriteria
   * implementation.
   * @param order   the order in which the sorting will be performed,
   * will be either ASC or DESC.
   */
  public static void sort(List l, int criteria, int order)
  {
    Collections.sort(l, new CriteriaComparator(criteria, order));
  }

  private static class CriteriaComparator
    implements Comparator
  {
    int criteria;
    int order;

    CriteriaComparator(int criteria, int order)
    {
      this.criteria = criteria;
      this.order = order;
    }

    public int compare(Object o1, Object o2)
    {
      CriteriaComparable c1 = (CriteriaComparable)o1;
      CriteriaComparable c2 = (CriteriaComparable)o2;

      if (order == ASC)
	return c1.compareTo(c2, criteria);
      else
	return c2.compareTo(c1, criteria);
    }

    public boolean equals(Object o)
    {
      if (o == this)
	return true;

      if (!(o instanceof CriteriaComparator))
	return false;

      CriteriaComparator c = (CriteriaComparator)o;

      return (criteria == c.criteria && order == c.order);
    }
  }
}
