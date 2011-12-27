/*
  DBWriter.java

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

   A DBWriter is used to write itemsets into a database. 
   
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
public class DBWriter 
{
  public static final int CHAR_SIZE          = 2;
  public static final int INT_SIZE           = 4;
  public static final int LONG_SIZE          = 8;
  public static final int CRC_SIZE           = INT_SIZE;

  public static final int COLUMN_LENGTH      = 32;
  public static final int DESCRIPTION_LENGTH = 256;

  public static final int ID_SIZE            = CHAR_SIZE * 3;
  public static final int VERSION_SIZE       = INT_SIZE * 3;
  public static final int HEADER_SIZE        = LONG_SIZE;
  public static final int NUMROWS_SIZE       = LONG_SIZE;
  public static final int NUMCOLUMNS_SIZE    = LONG_SIZE;
  public static final int COLUMN_SIZE        = COLUMN_LENGTH * CHAR_SIZE;
  public static final int DESCRIPTION_SIZE   = DESCRIPTION_LENGTH * CHAR_SIZE;

  public static final int HEAD_SIZE_OFFSET   = ID_SIZE + VERSION_SIZE;
  public static final int NUMROWS_OFFSET     = HEAD_SIZE_OFFSET + HEADER_SIZE;
  public static final int COLUMN_NAME_OFFSET = NUMROWS_OFFSET + NUMROWS_SIZE 
    + NUMCOLUMNS_SIZE;

  public static final int MIN_DATA_OFFSET    = COLUMN_NAME_OFFSET + COLUMN_SIZE
    + ID_SIZE + DESCRIPTION_SIZE + ID_SIZE + CRC_SIZE;

  public static final String ID              = "CKL";

  private RandomAccessFile outStream;
  private String description;
  private long numRows;
  private long numColumns;
  private int CRC;
  private long headerSize;
  private boolean wroteColumnNames;
  private boolean needReposition;
  private long lastPosition;

  // call this method to check the existence of the ID
  // the file pointer must be already positioned
  private void checkID()
    throws IOException
  {
    for (int i = 0; i < ID.length(); i++)
      if (outStream.readChar() != ID.charAt(i))
	throw new IOException("Attempting to load invalid database");
  }

  // call this method to write the column names to the file
  // the file pointer must be already positioned
  private void writeColumnNames(ArrayList names)
    throws IOException
  {
    String column;
    for (int i = 0, numCols = names.size(); i < numCols; i++)
      {
	column = (String)names.get(i);
	if (column.length() > COLUMN_LENGTH)
	  column = column.substring(0, COLUMN_LENGTH);
	
	outStream.writeChars(column);
	for (int j = 0; j < (COLUMN_LENGTH - column.length()); j++)
	  outStream.writeChar(' ');
      }
  }

  // call this method to write the description to the file
  // the file pointer must be already positioned
  // the description must have been set
  private void writeDescription()
    throws IOException
  {
    if (description.length() > DESCRIPTION_LENGTH)
      description = description.substring(0, DESCRIPTION_LENGTH);
    
    outStream.writeChars(description);
    for (int j = 0; j < (DESCRIPTION_LENGTH - description.length()); 
	 j++)
      outStream.writeChar(' ');
  }

  /**
   * Create a new DBWriter according to the input file name.
   *
   * @param fileName   the name of the file
   * @exception FileNotFoundException   from library call
   * @exception  IOException   from library call or if file is corrupted
   */
  public DBWriter(String fileName) 
    throws IOException
  {	
    outStream = new RandomAccessFile(fileName, "rw");

    lastPosition = outStream.length();

    // case this is a new database
    if (lastPosition == 0)
      {
	wroteColumnNames = false;
	
	headerSize     = 0;
	numRows        = 0;
	CRC            = 0;
	needReposition = false;
      }
    // case this might be an old database
    else if (lastPosition > MIN_DATA_OFFSET)
      {
	// check that we have a valid database here
	checkID();

	wroteColumnNames = true;
	
	// read header data
	outStream.seek(HEAD_SIZE_OFFSET);
	headerSize = outStream.readLong();
	numRows = outStream.readLong();
	numColumns = outStream.readLong();

	// read description
	outStream.seek(headerSize - DESCRIPTION_SIZE - ID_SIZE - CRC_SIZE);
	description = "";
	for (int i = 0; i < DESCRIPTION_LENGTH; i++)
	  description += outStream.readChar();
	description = description.trim();

	// read CRC
	outStream.seek(headerSize - CRC_SIZE);
	CRC = outStream.readInt();

	// by default we append to an existing database
	needReposition = true;
      }
    // case this is not a database
    else
      throw new IOException("Attempting to load invalid database");
  }

  /**
   * Set the column names for the database.
   *
   * @param names   the column names
   * @exception  IOException   from library call
   * @exception  DBException   size of <code>names</code> does not match 
   * number of columns
   */
  public void setColumnNames(ArrayList names) 
    throws IOException, DBException 
  {
    long namesSize = names.size();

    if (wroteColumnNames == false)
      {
	numColumns = namesSize;

	// ID
	outStream.writeChars(ID);

	// version number
	outStream.writeInt(1);
	outStream.writeInt(0);
	outStream.writeInt(0);

	headerSize = COLUMN_NAME_OFFSET + COLUMN_SIZE * numColumns 
	  + ID_SIZE + DESCRIPTION_SIZE + ID_SIZE + CRC_SIZE;
	outStream.writeLong(headerSize);

	// number of rows
	outStream.writeLong(0);

	// number of columns
	outStream.writeLong(numColumns);

	// columns
	writeColumnNames(names);

	// rest of the header
	outStream.writeChars(ID);

	// check if description has been set, otherwise fill in with blanks
	if (description != null)
	  writeDescription();
	else
	  {
	    description = " ";
	    writeDescription();
	  }

	outStream.writeChars(ID);

	// CRC
	outStream.writeInt(0);

	wroteColumnNames = true;
      }
    else
      {
	if (namesSize != numColumns)
	  throw new DBException("Cannot change the number of columns");
	else
	  {
	    if (needReposition == false)
	      {
		lastPosition = outStream.getFilePointer();
		needReposition = true;
	      }

	    outStream.seek(COLUMN_NAME_OFFSET);
	    
	    writeColumnNames(names);
	  }
      }
  }

  /**
   * Set the description of the database.
   *
   * @param description   the description of the database
   * @exception  IOException   from library call
   */
  public void setDescription (String description) 
    throws IOException
  {
    this.description = description;

    if (wroteColumnNames)
      {
	if (needReposition == false)
	  {
	    lastPosition = outStream.getFilePointer();
	    needReposition = true;
	  }

	outStream.seek(headerSize - DESCRIPTION_SIZE - ID_SIZE - CRC_SIZE);

	writeDescription();
      }
  }

  /**
   * Add a new row to the database. If this is to be the first row
   * added to the database you must have called setColumnNames() 
   * before.
   *
   * @param itemset   the new row to be added to the data file
   * @exception  IOException   from library call
   * @exception  DBException   column names have not been set
   * or an invalid item was contained in the itemset
   */
  public void addRow(Itemset itemset) 
    throws IOException, DBException
  {
    if (wroteColumnNames == false)
      throw new DBException("Column names must be set first");

    int size = itemset.size();
    for (int i = 0; i < size; i++)
      if (itemset.get(i) > numColumns)
	throw new DBException("Attempt to write invalid item");

    if (needReposition == true)
      {
	outStream.seek(lastPosition);
	needReposition = false;
      }

    outStream.writeInt(size);
    CRC = updateCRC(CRC, size);

    int item;
    for (int i = 0; i < size; i++)
      {
	item = itemset.get(i);
	outStream.writeInt(item);
	CRC = updateCRC(CRC, item);
      }
    
    numRows++;
  }		
	
  /**
   * Close the I/O stream and save any unsaved data.
   *
   * @exception  IOException from library call
   */
  public void close() 
    throws IOException
  {
    if (headerSize > 0)
      {
	// write number of rows, CRC
	outStream.seek(NUMROWS_OFFSET);
	outStream.writeLong(numRows);

	outStream.seek(headerSize - CRC_SIZE);
	outStream.writeInt(CRC);
      }

    outStream.close();
  }
	
  /**
   * Update a CRC-16 value.
   *
   * @param crc   the previous CRC value
   * @param value   the value for which we update the CRC
   * @return   the updated CRC value
   */
  public static synchronized int updateCRC(int crc, int value)
  {
    int mask;
    int i;

    mask = 0xA001;
    value <<= 8;

    for (i = 0; i < 8; i++)
      {
	if (((crc ^ value) & 0x8000) > 0) 
	  crc = (crc << 1) ^ mask;
	else 
	  crc <<= 1;

	value <<= 1;
      }

    return crc & 0xFFFF;
  }

  /**
   * sample usage and testing
   */
  public static void main(String [] args)
  {
    Itemset is1 = new Itemset();
    is1.add(1);
    is1.add(2);
    Itemset is2 = new Itemset();
    is2.add(3);
    is2.add(2);
    Itemset is3 = new Itemset();
    is3.add(3);
    is3.add(1);
    Itemset is4 = new Itemset();
    is4.add(33);
    is4.add(3);

    ArrayList colNames = new ArrayList(3);
    colNames.add("cheese");
    colNames.add("pizza");
    colNames.add("beer");

    System.out.println("\n\nCreating invalid database:");
    try
      {
	RandomAccessFile invalid = new RandomAccessFile("invalid.db", "rw");
	invalid.writeChars(ID + " - a bogus file that looks like a valid one");
	invalid.close();
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }

    System.out.println("\n\nCreating corrupted database:");
    try
      {
	DBWriter corrupted = new DBWriter("corrupted.db");

	try
	  {
	    corrupted.addRow(is1);
	  }
	catch (DBException e)
	  {
	    System.out.println(e);
	  }

	corrupted.setDescription("a corrupted database");
	corrupted.setColumnNames(colNames);

	corrupted.addRow(is1);
	corrupted.setDescription("a corrupted database - 2");
	corrupted.setColumnNames(colNames);
	corrupted.addRow(is2);
	corrupted.setDescription("a corrupted database - 3");
	corrupted.addRow(is3);

	try
	  {
	    corrupted.addRow(is4);
	  }
	catch (DBException e)
	  {
	    System.out.println(e);
	  }

	corrupted.close();

	System.out.println("corrupting file");

	RandomAccessFile raf = new RandomAccessFile("corrupted.db", "rw");
	raf.seek(770);
	// replace the 2 in the second itemset with a 3
	raf.writeInt(3);
	raf.close();
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }

    System.out.println("\n\nCreating empty database:");
    try 
      {
	DBWriter empty = new DBWriter("empty.db");

	empty.setDescription("an empty database");
	empty.setColumnNames(colNames);
	empty.close();
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }

    System.out.println("\n\nCreating correct database:");
    try 
      {
	DBWriter correct = new DBWriter("correct.db");

	correct.setDescription("a correct database");
	correct.setColumnNames(colNames);

	correct.addRow(is1);
	correct.setDescription("a correct database - 2");
	correct.setColumnNames(colNames);
	correct.addRow(is2);
	correct.setDescription("a correct database - 3");
	correct.addRow(is3);

	correct.close();

	correct = new DBWriter("correct.db");

	correct.setColumnNames(colNames);

	correct.addRow(is1);
	correct.setDescription("a correct database - 4");
	correct.setColumnNames(colNames);
	correct.addRow(is2);
	correct.setDescription("a correct database - 5");
	correct.addRow(is3);

	correct.close();
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }

    System.out.println("\n\nOpening and closing DBWriter:");
    try 
      {
	DBWriter bummer = new DBWriter("bummer.db");
	bummer.close();
      }
    catch (Exception e)
      {
	System.out.println("Shouldn't have happened: " + e);
      }
  }
}
