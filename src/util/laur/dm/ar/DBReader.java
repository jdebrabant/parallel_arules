/*
  DBReader.java

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

import java.io.*;
import java.util.ArrayList;

/**

   A DBReader is used to read data from a database.<P>
   
   @version 1.0
   @author Laurentiu Cristofor

*/
/*
  Database header specification:

  1. Identifier - 3 characters - CKL
  2. Database format version number - 3 integers - 1 12 3 (meaning 1.12.3)
  3. Header size - 1 long - the size of the header in bytes, lets us know 
  where the data starts
  4. Number of rows - 1 long
  5. Number of columns - 1 long
  6. Column names - fixed length fields (32 characters). There are as many 
  such entries as we have columns in the database.
  7. Identifier - 3 characters - CKL : for verification only, it indicates 
  the end of the column names enumeration.
  8. Database description - fixed length field (256 characters).
  9. Identifier - 3 characters - CKL : again for verifying the end of the 
  description
  10. CRC field - 1 int - CRC of the data part of the file only

  Database contents: each row consists of ints: no_items, item 1, ... item n
*/
public class DBReader
{
  private static final int COLUMN_LENGTH      = DBWriter.COLUMN_LENGTH;
  private static final int DESCRIPTION_LENGTH = DBWriter.DESCRIPTION_LENGTH;

  private static final String ID              = DBWriter.ID;

  private RandomAccessFile inStream;
  private String version;
  private String description;
  private ArrayList names;
  private long numRows;
  private long numColumns;
  private int CRC; 
  private long headerSize;
  private boolean needReposition;
  private long lastPosition;
  private long currRow;

  // call this method to check the existence of the ID;
  // the file pointer must be already positioned
  private void checkID()
    throws IOException
  {
    for (int i = 0; i < ID.length(); i++)
      if (inStream.readChar() != ID.charAt(i))
	throw new IOException("Attempting to load invalid database");
  }

  /**
   * Create a new DBReader according to the input file name.
   *
   * @param fileName   the name of the file
   * @exception  IOException   from library call
   */
  public DBReader(String fileName) 
    throws IOException
  {	
    inStream = new RandomAccessFile(fileName, "r");

    checkID();

    version = "";
    version += inStream.readInt() + "."
      + inStream.readInt() + "." + inStream.readInt();

    headerSize = inStream.readLong();
    numRows = inStream.readLong();
    numColumns = inStream.readLong();

    names = new ArrayList((int)numColumns);
    for (int i = 0; i < numColumns; i++)
      {
	String aString = "";
	for (int j = 0; j < COLUMN_LENGTH; j++)
	  aString += inStream.readChar();
	aString = aString.trim();
	names.add(aString);
      }

    checkID();

    description = "";
    for (int i = 0; i < DESCRIPTION_LENGTH; i++)
      description += inStream.readChar();
    description = description.trim();

    checkID();
    
    CRC = inStream.readInt();

    currRow = 1;
    needReposition = false;
  }

  /**
   * Close the I/O stream.
   */
  public void close() 
    throws IOException 
  {
      inStream.close();
  }
 
  /**
   * Get the file version number.
   *
   * @return   a String containing the version (ex: 1.1.7)
   */
  public String getVersion()
  {
    return version;
  }
  
  /**
   * Get the number of the rows in database.
   *
   * @return   number of rows in database
   */
  public long getNumRows()
  {
    return numRows;
  }
	
  /**
   * Get the number of the columns in database.
   *
   * @return   number of columns in database
   */
  public long getNumColumns()
  {
    return numColumns;
  }
			
  /**
   * Get all the column names for database.
   *
   * @return   a ArrayList containing the column names
   */
  public ArrayList getColumnNames() 
  {
    return names;
  }
  	
  /**
   * Get the i-th column name from database.
   *
   * @param i   the index of the column requested
   * @return   name of column
   */
  public String getColumnName(int i) 
    throws DBException
  {
    if (i < 0 || i >= numColumns)
      throw new DBException("Invalid column index");

    return (String)names.get(i);
  }
  
  /**
   * Get the description of the database.
   */
  public String getDescription() 
  {
    return description;
  }
  
  /**
   * Check the integrity of the database.
   *
   * @exception IOException   from library call
   * @return   true if test passed, false otherwise
   */
  public boolean checkIntegrity() 
    throws IOException
  {
    int crc = 0;

    if (needReposition == false)
      {
	lastPosition = inStream.getFilePointer();
	needReposition = true;
      }

    inStream.seek(headerSize);

    for (int i = 0; i < numRows; i++)
      {
	int numItems = inStream.readInt();
	crc = DBWriter.updateCRC(crc, numItems);
	for (int j = 0; j < numItems; j++)
	  crc = DBWriter.updateCRC(crc, inStream.readInt());
      }

    if (crc == CRC)
      return true;
    else
      return false;
  }

  /**
   * Get first row of the table from the data file.
   *
   * @exception  IOException   from library call
   * @exception  DBException   an invalid item has been met
   * @return   first Itemset from database
   */
  public Itemset getFirstRow() 
    throws IOException, DBException
  {
    currRow = 1;

    inStream.seek(headerSize);
    needReposition = false;

    return getNextRow();
  }
		
  /**
   * Get the next row of data since last reading.
   *
   * @exception  IOException   from library call
   * @exception  DBException   an invalid item has been met
   */
  public Itemset getNextRow() 
    throws IOException, DBException
  {
    if (currRow > numRows)
      throw new IOException("EOF reached");

    if (needReposition == true)
      {
	inStream.seek(lastPosition);
	needReposition = false;
      }

    int numItems = inStream.readInt();
    int item;
    Itemset is = new Itemset(numItems);

    for (int i = 0; i < numItems; i++)
      {
	item = inStream.readInt();

	if (item > numColumns)
	  throw new DBException("Read an invalid item");
	
	is.add(item);
      }

    currRow++;

    return is;
  }

  /**
   * Tell whether there are more rows to be read from the database.
   *
   * @return   true if there are more rows, false otherwise
   */
  public boolean hasMoreRows()
  {
    return (currRow <= numRows);
  }

  /**
   * sample usage and testing
   */
  public static void main(String arg[])
  {
    System.out.println("\n\nTrying to open nonexistent database:");
    try
      {
	DBReader dbr = new DBReader("nonexistent.db");
      }
    catch (Exception e)
      {
	System.out.println(e);
      }

    System.out.println("\n\nTrying to open invalid database:");
    try
      {
	DBReader dbr = new DBReader("invalid.db");
      }
    catch (Exception e)
      {
	System.out.println(e);
      }

    System.out.println("\n\nTrying to open corrupted database:");
    try
      {
	DBReader dbr = new DBReader("corrupted.db");

	System.out.println("No rows: " + dbr.getNumRows());
	System.out.println("No columns: " + dbr.getNumColumns());
	System.out.println("Third column name is: " + dbr.getColumnName(2));
	System.out.println("Description: "  + dbr.getDescription());
	System.out.println("Version: "  + dbr.getVersion());
	System.out.println("Integrity check result: " + dbr.checkIntegrity());

	while (dbr.hasMoreRows())
	  System.out.println(dbr.getNextRow());
      }
    catch (Exception e)
      {
	System.out.println(e);
      }

    System.out.println("\n\nTrying to open empty database:");
    try
      {
	DBReader dbr = new DBReader("empty.db");

	System.out.println("No rows: " + dbr.getNumRows());
	System.out.println("No columns: " + dbr.getNumColumns());
	System.out.println("Third column name is: " + dbr.getColumnName(2));
	System.out.println("Description: "  + dbr.getDescription());
	System.out.println("Version: "  + dbr.getVersion());
	System.out.println("Integrity check result: " + dbr.checkIntegrity());

	while (dbr.hasMoreRows())
	  System.out.println(dbr.getNextRow());
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }

    System.out.println("\n\nTrying to open correct database:");
    try
      {
	DBReader dbr = new DBReader("correct.db");

	System.out.println("No rows: " + dbr.getNumRows());
	System.out.println("No columns: " + dbr.getNumColumns());
	System.out.println("Third column name is: " + dbr.getColumnName(2));
	System.out.println("Description: "  + dbr.getDescription());
	System.out.println("Version: "  + dbr.getVersion());
	System.out.println("Integrity check result: " + dbr.checkIntegrity());

	while (dbr.hasMoreRows())
	  System.out.println(dbr.getNextRow());
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }
  }
}
