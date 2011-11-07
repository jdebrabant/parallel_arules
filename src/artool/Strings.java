/*
  Strings.java
 
  (P)2002 Laurentiu Cristofor
*/

/*

ARtool - Association Rules tools
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


ARtool was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

/**
 * The string resources for ARtool
 *
 * @version 	1.2
 * @author	Laurentiu Cristofor
 */
public class Strings
{
  // program info
  public static final String TITLE     = "ARtool";
  public static final String VERSION   = "v1.1.2";
  public static final String COPYRIGHT = "(C)2002 Laurentiu Cristofor";
  public static final String DESCRIPT  = "An application for mining association rules in binary databases.";
  public static final String DISTRIB   = " is distributed under the GNU General Public License.\nSee file 0LICENSE.TXT for more details on licensing terms.";
  public static final String URL       = "url: http://www.cs.umb.edu/~laur/ARtool/";
  public static final String EMAIL     = "email: laur@cs.umb.edu";

  // miscellaneous
  public static final String endl  = "\n";
  public static final String empty = ""; 
  public static final String space = " "; 
  public static final String tab   = "       ";
  public static final String dots  = "...";
  public static final String colon = ":";
  public static final String and   = "and";

  // file extensions
  public static final String DB_EXT    = ".db";
  public static final String CACHE_EXT = ".cache";

  // subdirectories
  public static final String HELP_DIR = "help";

  // help titles
  public static final String INTRO_TITLE      = "Introduction to Association Rules";
  public static final String DEFS_TITLE       = "Definitions";
  public static final String REFERENCE_TITLE  = "References";
  public static final String MANUAL_TITLE     = "ARtool Manual";
  public static final String OVERVIEW_TITLE   = "Overview of ARtool";
  public static final String USER_GUIDE_TITLE = "ARtool User's Guide";
  public static final String MISC_INFO_TITLE  = "ARtool Miscellaneous Information";

  // help files
  public static final String INTRO_HTML      = "intro.html";
  public static final String DEFS_HTML       = "defs.html";
  public static final String REFERENCE_HTML  = "reference.html";
  public static final String OVERVIEW_HTML   = "overview.html";
  public static final String USER_GUIDE_HTML = "userguide.html";
  public static final String MISC_INFO_HTML  = "miscinfo.html";

  // menu
  public static final String PROGRAM     = "Program";
  public static final String DISCARD_IS  = "Discard itemsets";
  public static final String MEASURES    = "Compute measure";
  public static final String DISCARD_AR  = "Discard rules";
  public static final String CLEAR_LOG   = "Clear log";
  public static final String FORCE_GC    = "Force garbage collection";
  public static final String EXIT        = "Exit";
  public static final String TOOLS       = "Tools";
  public static final String GENSYNDB    = "Generate a synthetic database";
  public static final String HELP        = "Help";
  public static final String TOPICS      = "Help Topics";
  public static final String ABOUT       = "About ARtool";

  // controls
  public static final String CURR_DB       = "Current database: ";
  public static final String NO_DB         = "no database selected";
  public static final String SEL_DB        = "Select a database...";
  public static final String CHECK_DB      = "Check database integrity";
  public static final String DB_DESCR      = "Description: ";
  public static final String NO_DESCR      = "no description available";
  public static final String DB_NUM_COLS   = "Number of columns: ";
  public static final String DB_COLS       = ", column names are:";
  public static final String DB_NUM_ROWS   = "Number of rows: ";
  public static final String NA            = "N/A";
  public static final String DB            = "Database";
  public static final String SEL_ALG       = "Select algorithm: ";
  public static final String MINSUP        = "Minimum support: ";
  public static final String MINCONF       = "Minimum confidence: ";
  public static final String DEF_MINSUP    = "0.9";
  public static final String DEF_MINCONF   = DEF_MINSUP;
  public static final String GO            = "Go";
  public static final String ABORT         = "Abort";
  public static final String ITEMSET       = "Itemset";
  public static final String ANTECEDENT    = "Antecedent";
  public static final String CONSEQUENT    = "Consequent";
  public static final String SUPPORT       = "Support";
  public static final String CONFIDENCE    = "Confidence";
  public static final String PIATETSKY     = "Piatetsky-Shapiro";
  public static final String LIFT          = "Lift";
  public static final String INFLUENCE     = "Influence";
  public static final String IS            = "Frequent Itemsets";
  public static final String AR            = "Association Rules";
  public static final String BASIC         = "Basic";
  public static final String ADVANCED      = "Advanced";
  public static final String ITEMS         = "Items: ";
  public static final String LR            = ">>";
  public static final String RL            = "<<";
  public static final String ANTC_CONTAINS = "Antecedent must contain: ";
  public static final String CONS_CONTAINS = "Consequent must contain: ";
  public static final String IGNORED_ITEMS = "Items to ignore: ";
  public static final String ANTC_SIZE     = "Maximum antecedent size: ";
  public static final String CONS_SIZE     = "Minimum consequent size: ";
  public static final String ZERO          = "0"; 

  public static final String BORD_NAME     = "Database name and description";
  public static final String DB_NAME       = "Name: ";
  public static final String DEF_NAME      = "T1000_AT10_I100_P50_AP5";
  public static final String CB_AUTO       = "Auto naming";
  public static final String BORD_CHAR     = "Database characteristics";
  public static final String NUM_ITEMS     = "Number of items: ";
  public static final String DEF_NUM_ITEMS = "100";
  public static final String NUM_TRANS     = "Number of transactions: ";
  public static final String DEF_NUM_TRANS = "1000";
  public static final String AVG_TRANS     = "Average size of transactions: ";
  public static final String DEF_AVG_TRANS = "10";
  public static final String NUM_PATT      = "Number of patterns: ";
  public static final String DEF_NUM_PATT  = "50";
  public static final String AVG_PATT      = "Average size of patterns: ";
  public static final String DEF_AVG_PATT  = "5";
  public static final String CORREL        = "Correlation: ";
  public static final String DEF_CORREL    = "0.5";
  public static final String CORRUP        = "Corruption: ";
  public static final String DEF_CORRUP    = DEF_CORREL;
  public static final String GENERATE      = "Generate"; 

  public static final String CLOSE         = "Close"; 
  public static final String ABOUT_TEXT    = TITLE + space + VERSION + endl
    + COPYRIGHT + endl + endl + DESCRIPT + endl + endl 
    + TITLE + DISTRIB + endl + endl 
    + URL + endl + EMAIL + endl;

  // tooltips
  public static final String DB_TIP        = "Select a database";
  public static final String CHECK_DB_TIP  = "Check the integrity of the selected database";
  public static final String IS_TIP        = "Mine frequent itemsets";
  public static final String AR_TIP        = "Mine association rules";
  public static final String BASIC_TIP     = "Basic mining options";
  public static final String ADVANCED_TIP  = "Advanced mining options";
  public static final String SEL_ALG_TIP   = "Select the algorithm to use";
  public static final String MINSUP_TIP    = "Enter a value greater than 0 and less or equal than 1";
  public static final String MINCONF_TIP   = MINSUP_TIP;
  public static final String GO_TIP        = "Start algorithm execution";
  public static final String ABORT_TIP     = "Abort algorithm execution";
  public static final String TABLE_TIP     = "Double-click on a column header to sort results or on a row to display it in the log window";
  public static final String COLS_TIP      = "Column names";
  public static final String ITEMS_TIP     = "Items";
  public static final String IN_ANTC_TIP   = "Items that must appear in the rules antecedent";
  public static final String IN_CONS_TIP   = "Items that must appear in the rule consequent";
  public static final String IGNORED_TIP   = "Items to ignore";
  public static final String LR_TIP        = "Move selected items from the left list to the selected right list";
  public static final String RL_TIP        = "Move selected items from the selected right list to the left list";
  public static final String MAX_ANTC_TIP  = "Rules must have no more than this number of items in their antecedent";
  public static final String MIN_CONS_TIP  = "Rules must have at least this number of items in their consequent"; 

  public static final String DB_NAME_TIP   = "Enter a name for the database";
  public static final String DB_DESCR_TIP  = "Enter a description for the database";
  public static final String CB_AUTO_TIP   = "Let the program choose the name and description";
  public static final String NUM_ITEMS_TIP = "Enter the number of items, at least 1";
  public static final String NUM_TRANS_TIP = "Enter the number of transactions, at least 1";
  public static final String AVG_TRANS_TIP = "Enter the average size of transactions, must be less than the number of items";
  public static final String NUM_PATT_TIP  = "Enter number of patterns, at least 1";
  public static final String AVG_PATT_TIP  = "Enter the average size of patterns, must be less than the number of items";
  public static final String CORREL_TIP    = "Enter a value between 0 and 1";
  public static final String CORRUP_TIP    = CORREL_TIP;
  public static final String GENERATE_TIP  = "Generate the synthetic database";
  public static final String ABORT_GEN_TIP = "Abort generation of synthetic database";
  public static final String CLOSE_TIP     = "Close dialog";

  // messages
  public static final String QUICKSTART = "\nHere's a quickstart for " 
    + TITLE 
    + ":\nUsing ARtool usually involves three simple steps:\n" 
    + tab 
    + "Step 1: select a database using the " + DB 
    + " tab. Doing this will enable the other two tabs.\n" 
    + tab 
    + "Step 2: use the " + IS 
    + " tab to mine frequent itemsets from the selected database.\n" 
    + tab 
    + "Step 3: use the " + AR 
    + " tab to mine association rules from the selected database.\n" 
    + "Keep an eye on this window for status information.\n" 
    + "To learn more about " + TITLE 
    + " access the " + HELP + "/" + TOPICS + " menu item.\n\n" 
    + "Have fun using " + TITLE + "!" + endl + endl;

  public static final String INTRO = TITLE + space + VERSION + tab 
    + COPYRIGHT + endl + DESCRIPT + endl + URL + endl + EMAIL + endl 
    + QUICKSTART;

  public static final String CHECKING      = "Performing database integrity check...";
  public static final String PASSED        = " PASSED!\n";
  public static final String FAILED        = " FAILED!\n";
  public static final String OPENED        = "Opened database: ";
  public static final String CACHE_SET     = "Cache file is: ";
  public static final String ERROR         = "\n*** An error occurred: ";
  public static final String UNXP_ERROR    = "\n*** An unexpected error occurred in ";
  public static final String INT_ERROR     = "\n*** An internal error occurred in ";
  public static final String INT_ERROR_MSG = ". Please let me know about this problem by sending a message to laur@cs.umb.edu. I will provide a fix as soon as possible. Thank you!\n";
  public static final String OUT_OF_MEMORY = "\nIt is possible that "
    + TITLE
    + " ran out of memory. Try to free some space by discarding results"
    + " and invoking the garbage collector, then retry the operation. If " 
    + TITLE + " continues to ran out of memory, then you should restart "
    + TITLE + " with more memory allocated to the Java Virtual Machine.\n";
  public static final String SORT_IS       = "Sorting itemsets in ";
  public static final String SORT_AR       = "Sorting association rules in ";
  public static final String DESC          = "descending order on "; 
  public static final String ASC           = "ascending order on ";
  public static final String DISPLAYING    = "Displaying results...";
  public static final String COMPUTING     = "Computing";
  public static final String EXEC          = "Executing algorithm ";
  public static final String EXEC2         = " on database ";
  public static final String EXEC3         = " for minimum support ";
  public static final String EXEC4         = " and minimum confidence "; 
  public static final String ADV1          = "Items in antecedent: ";
  public static final String ADV2          = "Items in consequent: ";
  public static final String ADV3          = "Items ignored: ";
  public static final String ADV4          = "Maximum antecedent size: ";
  public static final String ADV5          = "Minimum consequent size: ";
  public static final String ABORT_REQUEST = " abort was requested...";
  public static final String READ_CACHE    = "Reading cache contents...";
  public static final String DONE          = " done!\n";
  public static final String TIME          = "Time elapsed (ms): ";
  public static final String PASSES        = " passes were performed over the database";
  public static final String COUNT_IS      = " itemsets were found";
  public static final String COUNT_AR      = " association rules were found";
  public static final String GC            = "Invoking garbage collector...";
  public static final String GENERATING    = "Generating synthetic database ";

  public static final String DUMP          = "******* WORKSPACE DUMP *******";

  // algorithms
  public static final String APRIORI     = "Apriori";
  public static final String CLOSURE     = "Closure";
  public static final String CLOSURE_OPT = "ClosureOpt";
  public static final String FP_GROWTH   = "FPgrowth";
  public static final String CACHE       = "Use Cache";

  public static final String APRIORI_RULES   = "AprioriRules";
  public static final String COVER_RULES     = "CoverRules";
  public static final String COVER_RULES_OPT = "CoverRulesOpt";
}
