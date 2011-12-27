/*
  AbortThreadRequest.java
   
  (P)2001 Laurentiu Cristofor
*/

/*

laur.tools - A Java package containing general purpose classes
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


The laur.tools package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.tools;

/**

   AbortThreadRequest is the exception thrown in a thread when the
   user requested that its execution be aborted.

   @version 1.0
   @author Laurentiu Cristofor
   
*/
public class AbortThreadRequest extends RuntimeException
{
}
