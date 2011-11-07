/*
  CriteriaComparable.java

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

/**
 
   This interface is implemented by classes who accept a criteria int
   parameter to their compareTo() method.
 
   @version 1.0
   @author Laurentiu Cristofor

 */
public interface CriteriaComparable
{
  /**
   * Compare the receiver with the <code>obj</code> argument according
   * to the criteria specified by <code>criteria</code>
   *
   * @return -1 if this is less than obj, 0 if this is equal to obj,
   * and 1 if this is greater than obj
   */
  int compareTo(Object obj, int criteria);
}
