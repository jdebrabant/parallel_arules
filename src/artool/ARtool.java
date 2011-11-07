/*
  ARtool.java
 
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

import laur.dm.ar.*;
import laur.tools.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * The ARtool GUI
 *
 * @version 	1.1
 * @author	Laurentiu Cristofor
 */
public class ARtool extends JFrame 
  implements ItemListener, ActionListener, MenuListener, ThreadMonitor
{
  // Constants
  // tab pane indices
  private static final int DB_TAB_INDEX = 0;
  private static final int IS_TAB_INDEX = 1;
  private static final int AR_TAB_INDEX = 2;

  private static final int BAS_TAB_INDEX = 0;
  private static final int ADV_TAB_INDEX = 1;

  // if input is larger than the following values, then the respective
  // operations will run in separate threads
  private static final int REFRESH_THREAD_THRESHOLD = 3000;
  private static final int SORT_THREAD_THRESHOLD    = 50000;

  // Variables declaration
  private ButtonGroup buttonGroup_measures;

  private JMenuBar jMenuBar_Main;
  private JMenu jMenu_Program;
  private JMenuItem jMenuItem_DiscardItemsets;
  private JMenu jMenu_Measures;
  private JRadioButtonMenuItem jRadioButtonMenuItem_Confidence;
  private JRadioButtonMenuItem jRadioButtonMenuItem_Piatetsky;
  private JRadioButtonMenuItem jRadioButtonMenuItem_Lift;
  private JRadioButtonMenuItem jRadioButtonMenuItem_Influence;
  private JMenuItem jMenuItem_DiscardRules;
  private JMenuItem jMenuItem_ClearLog;
  private JMenuItem jMenuItem_ForceGC;
  private JMenuItem jMenuItem_Exit;

  private JMenu jMenu_Tools;
  private JMenuItem jMenuItem_GenSynDB;

  private JMenu jMenu_Help;
  private JMenuItem jMenuItem_Topics;
  private JMenuItem jMenuItem_About;

  private ButtonGroup buttonGroup_radv;

  private JSplitPane jSplitPane_Main;
  private JScrollPane jScrollPane_Log;
  private JTextArea jTextArea_Log;
  private JTabbedPane jTabbedPane_Main;

  private JPanel jPanel_Database;
  private JPanel jPanel_db_north;
  private JLabel jLabel_DB_Name;
  private JPanel jPanel_db_center;
  private JPanel jPanel_db_center_north;
  private JLabel jLabel_description;
  private JPanel jPanel_db_center_center;
  private JPanel jPanel_db_ccn;
  private JLabel jLabel_Num_Cols;
  private JPanel jPanel_db_ccc;
  private JScrollPane jScrollPane_Cols;
  private JList jList_Cols;
  private JPanel jPanel_db_ccs;
  private JLabel jLabel_Num_Rows;
  private JPanel jPanel_db_center_south;
  private JButton jButton_OpenDB;
  private JButton jButton_CheckDB;

  private JPanel jPanel_Itemsets;
  private JSplitPane jSplitPane_Itemsets;
  private JPanel jPanel_Itemsets_Params;
  private JPanel jPanel_ipar_north;
  private JLabel jLabel_Sel_AlgIS;
  private JComboBox jComboBox_AlgIS;
  private JPanel jPanel_ipar_center;
  private JLabel jLabel_minsup;
  private JTextField jTextField_minsup;
  private JPanel jPanel_ipar_south;
  private JButton jButton_GoIS;
  private JButton jButton_AbortIS;
  private JScrollPane jScrollPane_itemsets;
  private JTable jTable_Itemsets;

  private JPanel jPanel_Rules;
  private JSplitPane jSplitPane_Rules;
  private JPanel jPanel_Rules_Params;
  private JPanel jPanel_rpar_center;
  private JTabbedPane jTabbedPane_Rules;

  private JPanel jPanel_Basic;
  private JPanel jPanel_rbas_north;
  private JLabel jLabel_Sel_AlgAR;
  private JComboBox jComboBox_AlgAR;
  private JPanel jPanel_rbas_center;
  private JLabel jLabel_minconf;
  private JTextField jTextField_minconf;

  private JPanel jPanel_Advanced;
  private JPanel jPanel_radv_center;
  private JPanel jPanel_radv_center_west;
  private JLabel jLabel_items;
  private JScrollPane jScrollPane_items;
  private JList jList_items;
  private JPanel jPanel_radv_center_center;
  private JPanel jPanel_radv_cc1;
  private JPanel jPanel_radv_cc2;
  private JPanel jPanel_radv_cc2w;
  private JButton jButton_LR;
  private JPanel jPanel_radv_cc2e;
  private JButton jButton_RL;
  private JPanel jPanel_radv_cc3;
  private JPanel jPanel_radv_center_east;
  private JPanel jPanel_radv_ce1;
  private JRadioButton jRadioButton_in_antc;
  private JScrollPane jScrollPane_in_antc;
  private JList jList_in_antc;
  private JPanel jPanel_radv_ce2;
  private JRadioButton jRadioButton_in_cons;
  private JScrollPane jScrollPane_in_cons;
  private JList jList_in_cons;
  private JPanel jPanel_radv_ce3;
  private JRadioButton jRadioButton_to_ignore;
  private JScrollPane jScrollPane_to_ignore;
  private JList jList_to_ignore;
  private JPanel jPanel_radv_south;
  private JLabel jLabel_max_antc_size;
  private JTextField jTextField_max_antc_size;
  private JLabel jLabel_min_cons_size;
  private JTextField jTextField_min_cons_size;
  private JPanel jPanel_rpar_south;
  private JButton jButton_GoAR;
  private JButton jButton_AbortAR;
  private JScrollPane jScrollPane_rules;
  private JTable jTable_Rules;

  private JFileChooser jfc;

  // table models for our two result tables
  private ItemsetsTableModel itemsets_table_model;
  private RulesTableModel rules_table_model;

  // a ThreadMonitor reference used to refer to the monitor of a
  // thread that is executing at some point
  private ThreadMonitor monitor;

  // the application workspace
  private Workspace ws;
  // End of variables declaration

  /** Creates new form ARtool */
  public ARtool(String title) 
  {
    super(title);

    ws = Workspace.getWorkspace();

    initComponents();

    ws.log(Strings.INTRO);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) 
  {
    new ARtool(Strings.TITLE).show();
  }

  // called when user closes application
  private void exitFrame() 
  {
    // don't allow user to exit while an algorithm is still running
    if (ws.status != Workspace.IDLE)
      return;

    ws.cleanUp();

    hide();
    dispose();

    System.exit(0);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents() 
  {
    // create components
    buttonGroup_measures = new ButtonGroup();

    jMenuBar_Main = new JMenuBar();
    jMenu_Program = new JMenu();
    jMenuItem_DiscardItemsets = new JMenuItem();
    jMenu_Measures = new JMenu();
    jRadioButtonMenuItem_Confidence = new JRadioButtonMenuItem();
    jRadioButtonMenuItem_Piatetsky = new JRadioButtonMenuItem();
    jRadioButtonMenuItem_Lift = new JRadioButtonMenuItem();
    jRadioButtonMenuItem_Influence = new JRadioButtonMenuItem();
    jMenuItem_DiscardRules = new JMenuItem();
    jMenuItem_ClearLog = new JMenuItem();
    jMenuItem_ForceGC = new JMenuItem();
    jMenuItem_Exit = new JMenuItem();

    jMenu_Tools = new JMenu();
    jMenuItem_GenSynDB = new JMenuItem();

    jMenu_Help = new JMenu();
    jMenuItem_Topics = new JMenuItem();
    jMenuItem_About = new JMenuItem();

    buttonGroup_radv = new ButtonGroup();

    jSplitPane_Main = new JSplitPane();
    jScrollPane_Log = new JScrollPane();
    jTextArea_Log = new JTextArea();
    jTabbedPane_Main = new JTabbedPane();

    jPanel_Database = new JPanel();
    jPanel_db_north = new JPanel();
    jLabel_DB_Name = new JLabel();
    jPanel_db_center = new JPanel();
    jPanel_db_center_north = new JPanel();
    jLabel_description = new JLabel();
    jPanel_db_center_center = new JPanel();
    jPanel_db_ccn = new JPanel();
    jLabel_Num_Cols = new JLabel();
    jPanel_db_ccc = new JPanel();
    jScrollPane_Cols = new JScrollPane();
    jList_Cols = new JList();
    jPanel_db_ccs = new JPanel();
    jLabel_Num_Rows = new JLabel();
    jPanel_db_center_south = new JPanel();
    jButton_OpenDB = new JButton();
    jButton_CheckDB = new JButton();

    jPanel_Itemsets = new JPanel();
    jSplitPane_Itemsets = new JSplitPane();
    jPanel_Itemsets_Params = new JPanel();
    jPanel_ipar_north = new JPanel();
    jLabel_Sel_AlgIS = new JLabel();
    jComboBox_AlgIS = new JComboBox();
    jPanel_ipar_center = new JPanel();
    jLabel_minsup = new JLabel();
    jTextField_minsup = new JTextField();
    jPanel_ipar_south = new JPanel();
    jButton_GoIS = new JButton();
    jButton_AbortIS = new JButton();
    jTable_Itemsets = new JTable();
    jScrollPane_itemsets = new JScrollPane(jTable_Itemsets);

    jPanel_Rules = new JPanel();
    jSplitPane_Rules = new JSplitPane();
    jPanel_Rules_Params = new JPanel();
    jPanel_rpar_center = new JPanel();
    jTabbedPane_Rules = new JTabbedPane();

    jPanel_Basic = new JPanel();
    jPanel_rbas_north = new JPanel();
    jLabel_Sel_AlgAR = new JLabel();
    jComboBox_AlgAR = new JComboBox();
    jPanel_rbas_center = new JPanel();
    jLabel_minconf = new JLabel();
    jTextField_minconf = new JTextField();

    jPanel_Advanced = new JPanel();
    jPanel_radv_center = new JPanel();
    jPanel_radv_center_west = new JPanel();
    jLabel_items = new JLabel();
    jScrollPane_items = new JScrollPane();
    jList_items = new JList();
    jPanel_radv_center_center = new JPanel();
    jPanel_radv_cc1 = new JPanel();
    jPanel_radv_cc2 = new JPanel();
    jPanel_radv_cc2w = new JPanel();
    jButton_LR = new JButton();
    jPanel_radv_cc2e = new JPanel();
    jButton_RL = new JButton();
    jPanel_radv_cc3 = new JPanel();
    jPanel_radv_center_east = new JPanel();
    jPanel_radv_ce1 = new JPanel();
    jRadioButton_in_antc = new JRadioButton();
    jScrollPane_in_antc = new JScrollPane();
    jList_in_antc = new JList();
    jPanel_radv_ce2 = new JPanel();
    jRadioButton_in_cons = new JRadioButton();
    jScrollPane_in_cons = new JScrollPane();
    jList_in_cons = new JList();
    jPanel_radv_ce3 = new JPanel();
    jRadioButton_to_ignore = new JRadioButton();
    jScrollPane_to_ignore = new JScrollPane();
    jList_to_ignore = new JList();
    jPanel_radv_south = new JPanel();
    jLabel_max_antc_size = new JLabel();
    jTextField_max_antc_size = new JTextField();
    jLabel_min_cons_size = new JLabel();
    jTextField_min_cons_size = new JTextField();
    jPanel_rpar_south = new JPanel();
    jButton_GoAR = new JButton();
    jButton_AbortAR = new JButton();
    jTable_Rules = new JTable();
    jScrollPane_rules = new JScrollPane(jTable_Rules);

    jfc = new JFileChooser();

    itemsets_table_model = new ItemsetsTableModel();
    rules_table_model = new RulesTableModel();


    // set up components
    jMenu_Program.setText(Strings.PROGRAM);
    jMenu_Program.addMenuListener(this);
    jMenu_Program.setMnemonic(KeyEvent.VK_P);

    jMenuItem_DiscardItemsets.setText(Strings.DISCARD_IS);
    jMenuItem_DiscardItemsets.addActionListener(this);
    jMenuItem_DiscardItemsets.setMnemonic(KeyEvent.VK_I);
    jMenu_Program.add(jMenuItem_DiscardItemsets);

    jMenu_Program.addSeparator();

    jMenu_Measures.setText(Strings.MEASURES);
    jMenu_Measures.setMnemonic(KeyEvent.VK_M);

    buttonGroup_measures.add(jRadioButtonMenuItem_Confidence);
    jRadioButtonMenuItem_Confidence.setText(Strings.CONFIDENCE);
    jRadioButtonMenuItem_Confidence.setMnemonic(KeyEvent.VK_C);
    jRadioButtonMenuItem_Confidence.addActionListener(this);
    jRadioButtonMenuItem_Confidence.setSelected(true);
    ws.measure_id = Workspace.M_CONFIDENCE;
    jMenu_Measures.add(jRadioButtonMenuItem_Confidence);

    buttonGroup_measures.add(jRadioButtonMenuItem_Piatetsky);
    jRadioButtonMenuItem_Piatetsky.setText(Strings.PIATETSKY);
    jRadioButtonMenuItem_Piatetsky.setMnemonic(KeyEvent.VK_P);
    jRadioButtonMenuItem_Piatetsky.addActionListener(this);
    jMenu_Measures.add(jRadioButtonMenuItem_Piatetsky);

    buttonGroup_measures.add(jRadioButtonMenuItem_Lift);
    jRadioButtonMenuItem_Lift.setText(Strings.LIFT);
    jRadioButtonMenuItem_Lift.setMnemonic(KeyEvent.VK_L);
    jRadioButtonMenuItem_Lift.addActionListener(this);
    jMenu_Measures.add(jRadioButtonMenuItem_Lift);

    buttonGroup_measures.add(jRadioButtonMenuItem_Influence);
    jRadioButtonMenuItem_Influence.setText(Strings.INFLUENCE);
    jRadioButtonMenuItem_Influence.setMnemonic(KeyEvent.VK_I);
    jRadioButtonMenuItem_Influence.addActionListener(this);
    jMenu_Measures.add(jRadioButtonMenuItem_Influence);
    jMenu_Program.add(jMenu_Measures);

    jMenuItem_DiscardRules.setText(Strings.DISCARD_AR);
    jMenuItem_DiscardRules.addActionListener(this);
    jMenuItem_DiscardRules.setMnemonic(KeyEvent.VK_R);
    jMenu_Program.add(jMenuItem_DiscardRules);

    jMenu_Program.addSeparator();

    jMenuItem_ClearLog.setText(Strings.CLEAR_LOG);
    jMenuItem_ClearLog.addActionListener(this);
    jMenuItem_ClearLog.setMnemonic(KeyEvent.VK_C);
    jMenu_Program.add(jMenuItem_ClearLog);

    jMenuItem_ForceGC.setText(Strings.FORCE_GC);
    jMenuItem_ForceGC.addActionListener(this);
    jMenuItem_ForceGC.setMnemonic(KeyEvent.VK_G);
    jMenu_Program.add(jMenuItem_ForceGC);

    jMenu_Program.addSeparator();

    jMenuItem_Exit.setText(Strings.EXIT);
    jMenuItem_Exit.addActionListener(this);
    jMenuItem_Exit.setMnemonic(KeyEvent.VK_X);
    jMenu_Program.add(jMenuItem_Exit);
    jMenuBar_Main.add(jMenu_Program);
    
    jMenu_Tools.setText(Strings.TOOLS);
    jMenu_Tools.setMnemonic(KeyEvent.VK_T);
    jMenuItem_GenSynDB.setText(Strings.GENSYNDB + Strings.dots);
    jMenuItem_GenSynDB.addActionListener(this);
    jMenuItem_GenSynDB.setMnemonic(KeyEvent.VK_G);
    jMenu_Tools.add(jMenuItem_GenSynDB);
    jMenuBar_Main.add(jMenu_Tools);

    jMenuBar_Main.add(Box.createHorizontalGlue());

    jMenu_Help.setText(Strings.HELP);
    jMenu_Help.setMnemonic(KeyEvent.VK_H);
    jMenuItem_Topics.setText(Strings.TOPICS + Strings.dots);
    jMenuItem_Topics.addActionListener(this);
    jMenuItem_Topics.setMnemonic(KeyEvent.VK_T);
    jMenu_Help.add(jMenuItem_Topics);

    jMenu_Help.addSeparator();

    jMenuItem_About.setText(Strings.ABOUT + Strings.dots);
    jMenuItem_About.addActionListener(this);
    jMenuItem_About.setMnemonic(KeyEvent.VK_A);
    jMenu_Help.add(jMenuItem_About);
    jMenuBar_Main.add(jMenu_Help);
        
    getContentPane().add(jMenuBar_Main, BorderLayout.NORTH);

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent evt)
	{
	  exitFrame();
	}
      });

    jSplitPane_Main.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane_Main.setOneTouchExpandable(true);
    jTextArea_Log.setEditable(false);
    jTextArea_Log.setLineWrap(true);
    jScrollPane_Log.setViewportView(jTextArea_Log);

    // showing the last entries in the log
    jScrollPane_Log.getViewport().addChangeListener(new ChangeListener() {
	private int last_height;
	public void stateChanged(ChangeEvent e) 
	{
	  JViewport jvp = (JViewport)e.getSource();
	  int height = jvp.getViewSize().height; 
	  if (height != last_height) 
	    {
	      last_height = height;
	      int y = height - jvp.getExtentSize().height;
	      jvp.setViewPosition(new Point(0, y));
	    }
	}
      });
        
    jSplitPane_Main.setRightComponent(jScrollPane_Log);

    // set workspace log
    ws.log = jTextArea_Log;

    ///////////////
    // database tab
    jPanel_Database.setLayout(new BorderLayout());
        
    jLabel_DB_Name.setText(Strings.CURR_DB + Strings.NO_DB);
    jPanel_db_north.add(jLabel_DB_Name);
        
    jPanel_Database.add(jPanel_db_north, BorderLayout.NORTH);
        
    jPanel_db_center.setLayout(new BorderLayout());
        
    jLabel_description.setText(Strings.DB_DESCR + Strings.NO_DESCR);
    jPanel_db_center_north.add(jLabel_description);
        
    jPanel_db_center.add(jPanel_db_center_north, BorderLayout.NORTH);
        
    jPanel_db_center_center.setLayout(new BorderLayout());
        
    jLabel_Num_Cols.setText(Strings.DB_NUM_COLS + Strings.NA 
			    + Strings.DB_COLS);
    jPanel_db_ccn.add(jLabel_Num_Cols);
        
    jPanel_db_center_center.add(jPanel_db_ccn, BorderLayout.NORTH);

    jList_Cols.setToolTipText(Strings.COLS_TIP);
        
    jScrollPane_Cols.setPreferredSize(new Dimension(150, 131));
    jScrollPane_Cols.setViewportView(jList_Cols);
        
    jPanel_db_ccc.add(jScrollPane_Cols);
        
    jPanel_db_center_center.add(jPanel_db_ccc, BorderLayout.CENTER);
        
    jLabel_Num_Rows.setText(Strings.DB_NUM_ROWS + Strings.NA);
    jPanel_db_ccs.add(jLabel_Num_Rows);
        
    jPanel_db_center_center.add(jPanel_db_ccs, BorderLayout.SOUTH);
        
    jPanel_db_center.add(jPanel_db_center_center, BorderLayout.CENTER);

    jButton_OpenDB.setText(Strings.SEL_DB);
    jButton_OpenDB.addActionListener(this);
    jButton_OpenDB.setMnemonic(KeyEvent.VK_S);
    jButton_OpenDB.setToolTipText(Strings.DB_TIP);
    jPanel_db_center_south.add(jButton_OpenDB);
        
    jButton_CheckDB.setText(Strings.CHECK_DB);
    jButton_CheckDB.addActionListener(this);
    jButton_CheckDB.setMnemonic(KeyEvent.VK_C);
    jButton_CheckDB.setToolTipText(Strings.CHECK_DB_TIP);
    jPanel_db_center_south.add(jButton_CheckDB);
        
    jPanel_db_center.add(jPanel_db_center_south, BorderLayout.SOUTH);
        
    jPanel_Database.add(jPanel_db_center, BorderLayout.CENTER);
        
    jTabbedPane_Main.addTab(Strings.DB, null, jPanel_Database,
			    Strings.DB_TIP);
        
    ///////////////
    // itemsets tab
    jPanel_Itemsets.setLayout(new BorderLayout());
        
    jSplitPane_Itemsets.setOneTouchExpandable(true);
    jPanel_Itemsets_Params.setLayout(new BorderLayout());
        
    jLabel_Sel_AlgIS.setText(Strings.SEL_ALG);
    jPanel_ipar_north.add(jLabel_Sel_AlgIS);
      
    jComboBox_AlgIS.addItem(Strings.APRIORI);
    jComboBox_AlgIS.addItem(Strings.CLOSURE);
    jComboBox_AlgIS.addItem(Strings.CLOSURE_OPT);
    jComboBox_AlgIS.addItem(Strings.FP_GROWTH);
    jComboBox_AlgIS.addItem(Strings.CACHE);
    jComboBox_AlgIS.setSelectedIndex(0);
    ws.algIS_type = Strings.APRIORI;
    jComboBox_AlgIS.addItemListener(this);
    jComboBox_AlgIS.setToolTipText(Strings.SEL_ALG_TIP);
    jPanel_ipar_north.add(jComboBox_AlgIS);
        
    jPanel_Itemsets_Params.add(jPanel_ipar_north, BorderLayout.NORTH);
        
    jLabel_minsup.setText(Strings.MINSUP);
    jPanel_ipar_center.add(jLabel_minsup);
        
    jTextField_minsup.setText(Strings.DEF_MINSUP);
    jTextField_minsup.setToolTipText(Strings.MINSUP_TIP);
    jTextField_minsup.setPreferredSize(new Dimension(30, 21));
    jPanel_ipar_center.add(jTextField_minsup);
        
    jPanel_Itemsets_Params.add(jPanel_ipar_center, BorderLayout.CENTER);
        
    jButton_GoIS.setText(Strings.GO);
    jButton_GoIS.addActionListener(this);
    jButton_GoIS.setMnemonic(KeyEvent.VK_G);
    jButton_GoIS.setToolTipText(Strings.GO_TIP);
    jPanel_ipar_south.add(jButton_GoIS);
        
    jButton_AbortIS.setText(Strings.ABORT);
    jButton_AbortIS.addActionListener(this);
    jButton_AbortIS.setMnemonic(KeyEvent.VK_A);
    jButton_AbortIS.setToolTipText(Strings.ABORT_TIP);
    jPanel_ipar_south.add(jButton_AbortIS);
        
    jPanel_Itemsets_Params.add(jPanel_ipar_south, BorderLayout.SOUTH);
        
    jSplitPane_Itemsets.setLeftComponent(jPanel_Itemsets_Params);

    jTable_Itemsets.setModel(itemsets_table_model);
    jTable_Itemsets.getTableHeader().setReorderingAllowed(false); 
    jTable_Itemsets.setToolTipText(Strings.TABLE_TIP);

    // sort the itemsets by double-clicking the table header
    jTable_Itemsets.getTableHeader().addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{  
	  if (ws.status != Workspace.IDLE || event.getClickCount() < 2)
	    return;

	  int table_column = jTable_Itemsets.columnAtPoint(event.getPoint());

	  // decide whether to use a thread or not
	  if (ws.itemsets.size() < SORT_THREAD_THRESHOLD)
	    itemsets_table_model.sort(table_column);
	  else
	    sortThreaded(itemsets_table_model, table_column);
	}
      });

    // display an item in the log view by double-clicking a row
    jTable_Itemsets.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{  
	  if (ws.status != Workspace.IDLE || event.getClickCount() < 2)
	    return;

	  int table_row = jTable_Itemsets.rowAtPoint(event.getPoint());

	  ws.log(Strings.endl);
	  for (int i = 0; i < itemsets_table_model.getColumnCount(); i++)
	    ws.log(itemsets_table_model.getColumnName(i)
		   + Strings.colon + Strings.space
		   + itemsets_table_model.getValueAt(table_row, i)
		   + Strings.endl);
	}
      });

    jSplitPane_Itemsets.setRightComponent(jScrollPane_itemsets);
        
    jPanel_Itemsets.add(jSplitPane_Itemsets, BorderLayout.CENTER);
        
    jTabbedPane_Main.addTab(Strings.IS, null, jPanel_Itemsets,
			    Strings.IS_TIP);
        
    ////////////////////////
    // association rules tab
    jPanel_Rules.setLayout(new BorderLayout());
        
    jSplitPane_Rules.setOneTouchExpandable(true);
    jPanel_Rules_Params.setLayout(new BorderLayout());
        
    jPanel_rpar_center.setLayout(new BorderLayout());
        
    // basic tab
    jPanel_Basic.setLayout(new BorderLayout());
        
    jLabel_Sel_AlgAR.setText(Strings.SEL_ALG);
    jPanel_rbas_north.add(jLabel_Sel_AlgAR);
        
    jComboBox_AlgAR.addItem(Strings.APRIORI_RULES);
    jComboBox_AlgAR.addItem(Strings.COVER_RULES);
    jComboBox_AlgAR.addItem(Strings.COVER_RULES_OPT);
    jComboBox_AlgAR.setSelectedIndex(0);
    ws.algAR_type = Strings.APRIORI_RULES;
    jComboBox_AlgAR.addItemListener(this);
    jComboBox_AlgAR.setToolTipText(Strings.SEL_ALG_TIP);
    jPanel_rbas_north.add(jComboBox_AlgAR);
        
    jPanel_Basic.add(jPanel_rbas_north, BorderLayout.NORTH);
        
    jLabel_minconf.setText(Strings.MINCONF);
    jPanel_rbas_center.add(jLabel_minconf);
        
    jTextField_minconf.setText(Strings.DEF_MINCONF);
    jTextField_minconf.setToolTipText(Strings.MINCONF_TIP);
    jTextField_minconf.setPreferredSize(new Dimension(30, 21));
    jPanel_rbas_center.add(jTextField_minconf);
        
    jPanel_Basic.add(jPanel_rbas_center, BorderLayout.CENTER);
        
    jTabbedPane_Rules.addTab(Strings.BASIC, null, jPanel_Basic,
			     Strings.BASIC_TIP);

    // advanced tab
    jPanel_Advanced.setLayout(new BorderLayout());
        
    jPanel_radv_center.setLayout(new BorderLayout());
        
    jPanel_radv_center_west.setLayout(new BoxLayout(jPanel_radv_center_west,
						    BoxLayout.Y_AXIS));
        
    jLabel_items.setText(Strings.ITEMS);
    jPanel_radv_center_west.add(jLabel_items);

    jList_items.setToolTipText(Strings.ITEMS_TIP);
        
    jScrollPane_items.setPreferredSize(new Dimension(150, 300));
    jScrollPane_items.setViewportView(jList_items);
        
    jPanel_radv_center_west.add(jScrollPane_items);
        
    jPanel_radv_center.add(jPanel_radv_center_west, BorderLayout.WEST);
        
    jPanel_radv_center_center.setLayout(new GridLayout(3, 1));
        
    jPanel_radv_center_center.add(jPanel_radv_cc1);
        
    jPanel_radv_cc2.setLayout(new BorderLayout());
        
    jButton_LR.setText(Strings.LR);
    jButton_LR.addActionListener(this);
    jButton_LR.setToolTipText(Strings.LR_TIP);
    jPanel_radv_cc2w.add(jButton_LR);
        
    jPanel_radv_cc2.add(jPanel_radv_cc2w, BorderLayout.WEST);
        
    jButton_RL.setText(Strings.RL);
    jButton_RL.addActionListener(this);
    jButton_RL.setToolTipText(Strings.RL_TIP);
    jPanel_radv_cc2e.add(jButton_RL);
        
    jPanel_radv_cc2.add(jPanel_radv_cc2e, BorderLayout.EAST);
        
    jPanel_radv_center_center.add(jPanel_radv_cc2);
        
    jPanel_radv_center_center.add(jPanel_radv_cc3);
        
    jPanel_radv_center.add(jPanel_radv_center_center, BorderLayout.CENTER);
        
    jPanel_radv_center_east.setLayout(new GridLayout(3, 1));
        
    jPanel_radv_ce1.setLayout(new BoxLayout(jPanel_radv_ce1,
					    BoxLayout.Y_AXIS));
        
    jRadioButton_in_antc.setText(Strings.ANTC_CONTAINS);
    jRadioButton_in_antc.setSelected(true);
    buttonGroup_radv.add(jRadioButton_in_antc);
    jPanel_radv_ce1.add(jRadioButton_in_antc);

    jList_in_antc.setToolTipText(Strings.IN_ANTC_TIP);
        
    // selecting a list as destination by clicking on it
    jList_in_antc.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{  
	  jRadioButton_in_antc.setSelected(true);
	}
      });
        
    jScrollPane_in_antc.setPreferredSize(new Dimension(150, 100));
    jScrollPane_in_antc.setViewportView(jList_in_antc);
        
    jPanel_radv_ce1.add(jScrollPane_in_antc);
        
    jPanel_radv_center_east.add(jPanel_radv_ce1);
        
    jPanel_radv_ce2.setLayout(new BoxLayout(jPanel_radv_ce2,
					    BoxLayout.Y_AXIS));
        
    jRadioButton_in_cons.setText(Strings.CONS_CONTAINS);
    buttonGroup_radv.add(jRadioButton_in_cons);
    jPanel_radv_ce2.add(jRadioButton_in_cons);
        
    jList_in_cons.setToolTipText(Strings.IN_CONS_TIP);

    // selecting a list as destination by clicking on it
    jList_in_cons.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{  
	  jRadioButton_in_cons.setSelected(true);
	}
      });

    jScrollPane_in_cons.setPreferredSize(new Dimension(150, 100));
    jScrollPane_in_cons.setViewportView(jList_in_cons);
        
    jPanel_radv_ce2.add(jScrollPane_in_cons);
        
    jPanel_radv_center_east.add(jPanel_radv_ce2);
        
    jPanel_radv_ce3.setLayout(new BoxLayout(jPanel_radv_ce3,
					    BoxLayout.Y_AXIS));
        
    jRadioButton_to_ignore.setText(Strings.IGNORED_ITEMS);
    buttonGroup_radv.add(jRadioButton_to_ignore);
    jPanel_radv_ce3.add(jRadioButton_to_ignore);
        
    jList_to_ignore.setToolTipText(Strings.IGNORED_TIP);

    // selecting a list as destination by clicking on it
    jList_to_ignore.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{  
	  jRadioButton_to_ignore.setSelected(true);
	}
      });

    jScrollPane_to_ignore.setPreferredSize(new Dimension(150, 100));
    jScrollPane_to_ignore.setViewportView(jList_to_ignore);
        
    jPanel_radv_ce3.add(jScrollPane_to_ignore);
        
    jPanel_radv_center_east.add(jPanel_radv_ce3);
        
    jPanel_radv_center.add(jPanel_radv_center_east, BorderLayout.EAST);
        
    jPanel_Advanced.add(jPanel_radv_center, BorderLayout.CENTER);
        
    jLabel_max_antc_size.setText(Strings.ANTC_SIZE);
    jPanel_radv_south.add(jLabel_max_antc_size);
        
    jTextField_max_antc_size.setText(Strings.ZERO);
    jTextField_max_antc_size.setToolTipText(Strings.MAX_ANTC_TIP);
    jTextField_max_antc_size.setPreferredSize(new Dimension(21, 21));
    jPanel_radv_south.add(jTextField_max_antc_size);
        
    jLabel_min_cons_size.setText(Strings.CONS_SIZE);
    jPanel_radv_south.add(jLabel_min_cons_size);
        
    jTextField_min_cons_size.setText(Strings.ZERO);
    jTextField_min_cons_size.setToolTipText(Strings.MIN_CONS_TIP);
    jTextField_min_cons_size.setPreferredSize(new Dimension(21, 21));
    jPanel_radv_south.add(jTextField_min_cons_size);
        
    jPanel_Advanced.add(jPanel_radv_south, BorderLayout.SOUTH);
        
    jTabbedPane_Rules.addTab(Strings.ADVANCED, null, jPanel_Advanced,
			     Strings.ADVANCED_TIP);

    // buttons
    jPanel_rpar_center.add(jTabbedPane_Rules, BorderLayout.CENTER);
        
    jPanel_Rules_Params.add(jPanel_rpar_center, BorderLayout.CENTER);
        
    jButton_GoAR.setText(Strings.GO);
    jButton_GoAR.addActionListener(this);
    jButton_GoAR.setMnemonic(KeyEvent.VK_G);
    jButton_GoAR.setToolTipText(Strings.GO_TIP);
    jPanel_rpar_south.add(jButton_GoAR);
        
    jButton_AbortAR.setText(Strings.ABORT);
    jButton_AbortAR.addActionListener(this);
    jButton_AbortAR.setMnemonic(KeyEvent.VK_A);
    jButton_AbortAR.setToolTipText(Strings.ABORT_TIP);
    jPanel_rpar_south.add(jButton_AbortAR);
        
    jPanel_Rules_Params.add(jPanel_rpar_south, BorderLayout.SOUTH);
        
    jSplitPane_Rules.setLeftComponent(jPanel_Rules_Params);

    // rules table
    jTable_Rules.setModel(rules_table_model);
    jTable_Rules.getTableHeader().setReorderingAllowed(false); 
    jTable_Rules.setToolTipText(Strings.TABLE_TIP);

    // sort the rules by double-clicking the table header
    jTable_Rules.getTableHeader().addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{  
	  if (ws.status != Workspace.IDLE || event.getClickCount() < 2)
	    return;

	  int table_column = jTable_Rules.columnAtPoint(event.getPoint());

	  // decide whether to use a thread or not
	  if (ws.rules.size() < SORT_THREAD_THRESHOLD)
	    rules_table_model.sort(table_column);
	  else
	    sortThreaded(rules_table_model, table_column);
	}
      });

    // display an item in the log view by double-clicking a row
    jTable_Rules.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent event)
	{
	  if (ws.status != Workspace.IDLE || event.getClickCount() < 2)
	    return;

	  int table_row = jTable_Rules.rowAtPoint(event.getPoint());

	  ws.log(Strings.endl);
	  for (int i = 0; i < rules_table_model.getColumnCount() - 1; i++)
	    ws.log(rules_table_model.getColumnName(i)
		   + Strings.colon + Strings.space
		   + rules_table_model.getValueAt(table_row, i)
		   + Strings.endl);

	  AssociationRule rule = (AssociationRule)ws.rules.get(table_row);

	  ws.log(Strings.CONFIDENCE + Strings.colon + Strings.space
		 + rule.computeConfidence() + Strings.endl);

	  ws.log(Strings.PIATETSKY + Strings.colon + Strings.space
		 + rule.computePiatetskyShapiro() + Strings.endl);

	  ws.log(Strings.LIFT + Strings.colon + Strings.space
		 + rule.computeLift() + Strings.endl);

	  ws.log(Strings.INFLUENCE + Strings.colon + Strings.space
		 + rule.computeInfluence() + Strings.endl);
	}
      });

    jSplitPane_Rules.setRightComponent(jScrollPane_rules);
        
    jPanel_Rules.add(jSplitPane_Rules, BorderLayout.CENTER);
        
    jTabbedPane_Main.addTab(Strings.AR, null, jPanel_Rules,
			    Strings.AR_TIP);
        
    jSplitPane_Main.setLeftComponent(jTabbedPane_Main);
        
    getContentPane().add(jSplitPane_Main, BorderLayout.CENTER);

    // disable all tabs until the user chooses a database
    setEnabledBegin(false);

    // we'll enable these buttons only when algorithms are running
    setEnabledAborts(false);

    // set file chooser to start from current directory
    File f = new File(".");
    jfc.setCurrentDirectory(f);
        
    pack();
  }

  // methods for enabling/disabling controls

  // this method disables the advanced options pane and enables it
  // only if the selected algorithm accepts advanced options
  private void setEnabledAdvTabPane(boolean bEnable)
  {
    if (!bEnable)
      jTabbedPane_Rules.setEnabledAt(ADV_TAB_INDEX, bEnable);
    else
      if (ws.algAR_type.equals(Strings.COVER_RULES) 
	  || ws.algAR_type.equals(Strings.COVER_RULES_OPT))
	jTabbedPane_Rules.setEnabledAt(ADV_TAB_INDEX, false);
      else
	jTabbedPane_Rules.setEnabledAt(ADV_TAB_INDEX, true);
  }

  private void setEnabledTabPanes(boolean bEnable)
  {
    jTabbedPane_Main.setEnabledAt(DB_TAB_INDEX, bEnable);
    jTabbedPane_Main.setEnabledAt(IS_TAB_INDEX, bEnable);
    jTabbedPane_Main.setEnabledAt(AR_TAB_INDEX, bEnable);

    jTabbedPane_Rules.setEnabledAt(BAS_TAB_INDEX, bEnable);
    setEnabledAdvTabPane(bEnable);
  }

  private void setEnabledMenus(boolean bEnable)
  {
    jMenu_Program.setEnabled(bEnable);
    jMenu_Tools.setEnabled(bEnable);
    jMenu_Help.setEnabled(bEnable);
  }

  private void setEnabledAborts(boolean bEnable)
  {
    jButton_AbortIS.setEnabled(bEnable);
    jButton_AbortAR.setEnabled(bEnable);
  }

  // enable/disable controls at program beginning
  private void setEnabledBegin(boolean bEnable)
  {
    setEnabledTabPanes(bEnable);
    jButton_CheckDB.setEnabled(bEnable);
  }

  // enable/disable controls during database integrity check
  private void setEnabledForCheckDB(boolean bEnable)
  {
    setEnabledTabPanes(bEnable);

    jButton_OpenDB.setEnabled(bEnable);
    jButton_CheckDB.setEnabled(bEnable);

    setEnabledMenus(bEnable);
  }

  // enable/disable most controls
  private void setEnabledAll(boolean bEnable)
  {
    setEnabledTabPanes(bEnable);

    jComboBox_AlgIS.setEnabled(bEnable);
    jTextField_minsup.setEnabled(bEnable);
    jButton_GoIS.setEnabled(bEnable);

    jComboBox_AlgAR.setEnabled(bEnable);
    jTextField_minconf.setEnabled(bEnable);
    jButton_GoAR.setEnabled(bEnable);

    jButton_RL.setEnabled(bEnable);
    jButton_LR.setEnabled(bEnable);
    jTextField_max_antc_size.setEnabled(bEnable);
    jTextField_min_cons_size.setEnabled(bEnable);

    setEnabledMenus(bEnable);
  }

  // enable/disable controls during a menu operation
  private void setEnabledForMenuAction(boolean bEnable)
  {
    // check to see what we need to enable
    if (ws.db_name == null)
      {
	setEnabledMenus(bEnable);
	jButton_OpenDB.setEnabled(bEnable);
      }
    else
      setEnabledAll(bEnable);
  }

  // enable/disable controls during mining
  private void setEnabledForMining(boolean bEnable)
  {
    setEnabledAll(bEnable);

    setEnabledAborts(!bEnable);
  }

  // listeners

  public void itemStateChanged(ItemEvent event) 
  {
    if (event.getSource() == jComboBox_AlgIS) 
      {
	ws.algIS_type = (String)event.getItem();
      }
    if (event.getSource() == jComboBox_AlgAR) 
      {
	ws.algAR_type = (String)event.getItem();

	// enable or disable the advanced options pane, according to
	// algorithm selection, the method figures this out!
	setEnabledAdvTabPane(true);
      }
  }

  public void actionPerformed(ActionEvent event)
  {
    // controls won't work if there is an algorithm executing, they
    // should be disabled anyway at this point, with the exception of
    // the Abort buttons
    if (ws.status != Workspace.IDLE)
      {
	if (event.getSource() == jButton_AbortIS
	    && ws.status == Workspace.EXEC_ALG_IS
	    && !ws.bAbortRequested) 
	  {
	    ws.algIS.abort();
	    ws.log(Strings.ABORT_REQUEST);
	    ws.bAbortRequested = true;
	  }
	else if (event.getSource() == jButton_AbortAR
		 && ws.status == Workspace.EXEC_ALG_AR
		 && !ws.bAbortRequested) 
	  {
	    ws.algAR.abort();
	    ws.log(Strings.ABORT_REQUEST);
	    ws.bAbortRequested = true;
	  }
	else
	  // ignore all other user actions while computing
	  return;

	setEnabledAborts(!ws.bAbortRequested);
      }

    if (event.getSource() == jButton_OpenDB) 
      {
	int returnVal = jfc.showOpenDialog(this);
	
	if (returnVal == JFileChooser.APPROVE_OPTION)
	  {
	    File file = jfc.getSelectedFile();
	    openDB(file);
	  }
      }
    else if (event.getSource() == jButton_CheckDB) 
      {
	checkDBThreaded();
      }
    else if (event.getSource() == jButton_GoIS) 
      {
	try
	  {
	    // get parameters
	    ws.minsup = JTextTools.getDouble(jTextField_minsup);
	    if (ws.minsup <= 0 || ws.minsup > 1)
	      JTextTools.dealWithInvalidValue(jTextField_minsup);

	    executeAlgIS();
	  }
	catch (IllegalArgumentException invalid_input)
	  {
	    // wait for user to input something reasonable
	  }
      }
    else if (event.getSource() == jButton_GoAR)
      {
	try
	  {
	    // get parameters
	    ws.minsup = JTextTools.getDouble(jTextField_minsup);
	    if (ws.minsup <= 0 || ws.minsup > 1)
	      JTextTools.dealWithInvalidValue(jTextField_minsup);

	    ws.minconf = JTextTools.getDouble(jTextField_minconf);
	    if (ws.minconf <= 0 || ws.minconf > 1)
	      JTextTools.dealWithInvalidValue(jTextField_minconf);

	    if (jTabbedPane_Rules.getSelectedIndex() == ADV_TAB_INDEX)
	      {
		ws.max_antc_size = JTextTools.getInt(jTextField_max_antc_size);
		if (ws.max_antc_size < 0)
		  JTextTools.dealWithInvalidValue(jTextField_max_antc_size);

		ws.min_cons_size = JTextTools.getInt(jTextField_min_cons_size);
		if (ws.min_cons_size < 0)
		  JTextTools.dealWithInvalidValue(jTextField_min_cons_size);

		ws.is_in_antc = getItemsetFromList(jList_in_antc);
		ws.is_in_cons = getItemsetFromList(jList_in_cons);
		ws.is_to_ignore = getItemsetFromList(jList_to_ignore);
	      }

	    executeAlgAR();
	  }
	catch (IllegalArgumentException invalid_input)
	  {
	    // wait for user to input something reasonable
	  }
      }
    else if (event.getSource() == jButton_LR)
      {
	// if there is a selection, move it from the Left list to a
	// Right list
	if (jList_items.getSelectedIndices().length != 0)
	  {
	    if (jRadioButton_in_antc.isSelected())
	      ws.v_in_antc = moveSelectionLR(jList_in_antc, ws.v_in_antc);
	    else if (jRadioButton_in_cons.isSelected())
	      ws.v_in_cons = moveSelectionLR(jList_in_cons, ws.v_in_cons);
	    else if (jRadioButton_to_ignore.isSelected())
	      ws.v_to_ignore = moveSelectionLR(jList_to_ignore,
					       ws.v_to_ignore);
	  }
      }
    else if (event.getSource() == jButton_RL)
      {
	// move a selection from a Right list to the Left list
	if (jRadioButton_in_antc.isSelected())
	  ws.v_in_antc = moveSelectionRL(jList_in_antc, ws.v_in_antc);
	else if (jRadioButton_in_cons.isSelected())
	  ws.v_in_cons = moveSelectionRL(jList_in_cons, ws.v_in_cons);
	else if (jRadioButton_to_ignore.isSelected())
	  ws.v_to_ignore = moveSelectionRL(jList_to_ignore, ws.v_to_ignore);
      }
    // menu actions
    else if (event.getSource() == jMenuItem_DiscardItemsets)
      {
	if (ws.itemsets == null || ws.itemsets.size() == 0)
	  return;

	ws.itemsets = new ArrayList(0);
	itemsets_table_model.refresh();
      }
    else if (event.getSource() == jRadioButtonMenuItem_Confidence)
      {
	if (ws.measure_id == Workspace.M_CONFIDENCE)
	  return;

	ws.measure_id = Workspace.M_CONFIDENCE;
	computeMeasureThreaded();
      }
    else if (event.getSource() == jRadioButtonMenuItem_Piatetsky)
      {
	if (ws.measure_id == Workspace.M_PIATETSKY)
	  return;

	ws.measure_id = Workspace.M_PIATETSKY;
	computeMeasureThreaded();
      }
    else if (event.getSource() == jRadioButtonMenuItem_Lift)
      {
	if (ws.measure_id == Workspace.M_LIFT)
	  return;

	ws.measure_id = Workspace.M_LIFT;
	computeMeasureThreaded();
      }
    else if (event.getSource() == jRadioButtonMenuItem_Influence)
      {
	if (ws.measure_id == Workspace.M_INFLUENCE)
	  return;

	ws.measure_id = Workspace.M_INFLUENCE;
	computeMeasureThreaded();
      }
    else if (event.getSource() == jMenuItem_DiscardRules)
      {
	if (ws.rules == null || ws.rules.size() == 0)
	  return;

	ws.rules = new ArrayList(0);
	rules_table_model.refresh();
      }
    else if (event.getSource() == jMenuItem_ClearLog)
      {
	ws.clearLog();
      }
    else if (event.getSource() == jMenuItem_ForceGC)
      {
	gcThreaded();
      }
    else if (event.getSource() == jMenuItem_Exit)
      {
	exitFrame();
      }
    else if (event.getSource() == jMenuItem_GenSynDB)
      {
	new GenDBDialog(this, true).show();
      }
    else if (event.getSource() == jMenuItem_Topics)
      {
	new HelpDialog(this, true).show();
      }
    else if (event.getSource() == jMenuItem_About)
      {
	new AboutDialog(this, true).show();
      }
  }

  public void menuSelected(MenuEvent event)
  {
    if (event.getSource() == jMenu_Program)
      {
	if (ws.itemsets == null || ws.itemsets.size() == 0)
	  jMenuItem_DiscardItemsets.setEnabled(false);
	else
	  jMenuItem_DiscardItemsets.setEnabled(true);
	
	if (ws.rules == null || ws.rules.size() == 0)
	  jMenuItem_DiscardRules.setEnabled(false);
	else
	  jMenuItem_DiscardRules.setEnabled(true);

	if (jTabbedPane_Main.getSelectedIndex() == AR_TAB_INDEX
	    && ws.rules != null && ws.rules.size() > 0)
	  jMenu_Measures.setEnabled(true);
	else
	  jMenu_Measures.setEnabled(false);
      }
  }

  public void menuDeselected(MenuEvent event)
  {
  }

  public void menuCanceled(MenuEvent event)
  {
  }

  // return an Itemset representing the items from jList, if jList is
  // empty then the method returns null
  private Itemset getItemsetFromList(JList jList)
  {
    ListModel lm = jList.getModel();

    if (lm.getSize() == 0)
      return null;
    else
      {
	Itemset is = new Itemset();
	for (int i = 0; i < lm.getSize(); i++)
	  is.add(((Integer)ws.name2item.get(lm.getElementAt(i))).intValue());
	return is;
      }
  }

  // move selected items from jList_items to the list passed as
  // parameter, where v represents the current contents of that list
  private ArrayList moveSelectionLR(JList jList, ArrayList v)
  {
    int[] indices = jList_items.getSelectedIndices();

    ArrayList v_tmp = (ArrayList)ws.item_names.clone();

    ArrayList v_selection = new ArrayList();
    for (int i = 0; i < indices.length; i++)
      v_selection.add(ws.v_items.get(indices[i])); 

    ws.v_items.removeAll(v_selection);
    jList_items.setListData(ws.v_items.toArray());

    v.addAll(v_selection);

    // we do the next steps to maintain the order of the
    // item names
    v_tmp.removeAll(v);
    v = (ArrayList)ws.item_names.clone();
    v.removeAll(v_tmp);

    jList.setListData(v.toArray());

    return v;
  }

  // move selected items from the list passed as parameter to
  // jList_items, where v represents the current contents of jList
  private ArrayList moveSelectionRL(JList jList, ArrayList v)
  {
    int[] indices = jList.getSelectedIndices();

    if (indices.length != 0)
      {
	ArrayList v_tmp = (ArrayList)ws.item_names.clone();
	
	ArrayList v_selection = new ArrayList();
	for (int i = 0; i < indices.length; i++)
	  v_selection.add(v.get(indices[i])); 
	
	v.removeAll(v_selection);
	jList.setListData(v.toArray());
	
	ws.v_items.addAll(v_selection);
	
	// we do the next steps to maintain the order of the
	// item names
	v_tmp.removeAll(ws.v_items);
	ws.v_items = (ArrayList)ws.item_names.clone();
	ws.v_items.removeAll(v_tmp);
	
	jList_items.setListData(ws.v_items.toArray());
      }

    return v;
  }

  // open database
  private void openDB(File f)
  {
    try
      {
	String pathname = f.getPath();
	String cache_name;

	// construct cache name
	int index = pathname.length() - Strings.DB_EXT.length();
	if (index > 0 && pathname.lastIndexOf(Strings.DB_EXT) == index)
	  cache_name = pathname.substring(0, index) + Strings.CACHE_EXT;
	else
	  cache_name = pathname + Strings.CACHE_EXT;

	// open db and cache
	DBReader dbr = new DBReader(pathname);

	// reset workspace values
	ws.cleanUp();

	ws.db_name = pathname;
	ws.db_size = dbr.getNumRows();
	ws.cache_name = cache_name;
	ws.dbr = dbr;
	ws.item_names = dbr.getColumnNames(); 

	ws.v_items = (ArrayList)ws.item_names.clone();
	ws.v_in_antc = new ArrayList();
	ws.v_in_cons = new ArrayList();
	ws.v_to_ignore = new ArrayList();

	// create a mapping from item name to the item integer value
	ws.name2item = new HashMap();
	for (int i = 0; i < ws.item_names.size(); i++)
	  ws.name2item.put(ws.item_names.get(i),
			   new Integer(i + 1));

	// display db info
	jLabel_DB_Name.setText(Strings.CURR_DB + pathname);
	jLabel_description.setText(Strings.DB_DESCR + dbr.getDescription());
	jLabel_Num_Cols.setText(Strings.DB_NUM_COLS + dbr.getNumColumns() 
				+ Strings.DB_COLS);
	jList_Cols.setListData(ws.item_names.toArray());
	jLabel_Num_Rows.setText(Strings.DB_NUM_ROWS + ws.db_size);

	// reset the controls
	resetControls();

	// enable all tabs now that a database was chosen
	setEnabledBegin(true);

	// log operation
	ws.log(Strings.endl + Strings.OPENED 
	       + ws.db_name + Strings.endl);
	ws.log(Strings.CACHE_SET + ws.cache_name + Strings.endl);
      }
    catch (Exception e)
      {
	ws.log(Strings.ERROR + e + Strings.endl);
      }
  }

  // this method resets the controls when a new database is selected
  private void resetControls()
  {
    jList_items.setListData(ws.item_names.toArray());

    ArrayList v = new ArrayList();
    jList_in_antc.setListData(v.toArray());
    jList_in_cons.setListData(v.toArray());
    jList_to_ignore.setListData(v.toArray());

    jTextField_max_antc_size.setText(Strings.ZERO);
    jTextField_min_cons_size.setText(Strings.ZERO);

    setMeasureConfidence();

    itemsets_table_model.refresh();
    rules_table_model.refresh();
  }

  // reset measure to confidence
  private void setMeasureConfidence()
  {
    if (ws.measure_id == Workspace.M_CONFIDENCE)
      return;

    jRadioButtonMenuItem_Confidence.setSelected(true);
    ws.measure_id = Workspace.M_CONFIDENCE;
    rules_table_model.setMeasureName(Strings.CONFIDENCE);
    rules_table_model.fireTableStructureChanged();
  }
  
  // execute selected algorithm for finding frequent itemsets
  private void executeAlgIS()
  {
    try
      {
	if (ws.algIS_type.equals(Strings.APRIORI))
	  ws.algIS = new Apriori();
	else if (ws.algIS_type.equals(Strings.CLOSURE))
	  ws.algIS = new Closure();
	else if (ws.algIS_type.equals(Strings.CLOSURE_OPT))
	  ws.algIS = new ClosureOpt();
	else if (ws.algIS_type.equals(Strings.FP_GROWTH))
	  ws.algIS = new FPgrowth();
	else if (ws.algIS_type.equals(Strings.CACHE))
	  {
	    displayCacheThreaded();
	    return;
	  }

	setEnabledForMining(false);

	ws.dbcw = new DBCacheWriter(ws.cache_name);
	ws.algIS.setParameters(this, ws.dbr,
			       ws.dbcw, ws.minsup);

	ws.log(Strings.endl 
	       + Strings.EXEC + ws.algIS.getClass().getName() 
	       + Strings.EXEC2 + ws.db_name
	       + Strings.EXEC3 + ws.minsup
	       + Strings.dots);
	ws.timer.reset();
	ws.timer.start();
	ws.status = Workspace.EXEC_ALG_IS;

	ws.algIS.start();
      }
    catch (Throwable e)
      {
	ws.log(Strings.UNXP_ERROR + "executeAlgIS(): " 
	       + e + Strings.endl);
	ws.status = Workspace.IDLE;

	setEnabledForMining(true);
      }
  }

  // execute selected algorithm for finding association rules
  private void executeAlgAR()
  {
    try
      {
	if (ws.algAR_type.equals(Strings.APRIORI_RULES))
	  ws.algAR = new AprioriRules();
	else if (ws.algAR_type.equals(Strings.COVER_RULES))
	  ws.algAR = new CoverRules();
	else if (ws.algAR_type.equals(Strings.COVER_RULES_OPT))
	  ws.algAR = new CoverRulesOpt();

	setEnabledForMining(false);

	setMeasureConfidence();

	ws.dbcr = new DBCacheReader(ws.cache_name);

	if (jTabbedPane_Rules.getSelectedIndex() == BAS_TAB_INDEX)
	  ws.algAR.setParameters(this, ws.dbcr, ws.minsup, ws.minconf);
	else
	  {
	    ws.log(Strings.endl + Strings.ADV1 + ws.v_in_antc + Strings.endl);
	    ws.log(Strings.ADV2 + ws.v_in_cons + Strings.endl);
	    ws.log(Strings.ADV3 + ws.v_to_ignore + Strings.endl);
	    ws.log(Strings.ADV4 + ws.max_antc_size + Strings.endl);
	    ws.log(Strings.ADV5 + ws.min_cons_size + Strings.endl);
	    ws.algAR.setParameters(this, ws.dbcr, ws.minsup, ws.minconf,
				   ws.is_in_antc, ws.is_in_cons,
				   ws.is_to_ignore,
				   ws.max_antc_size, ws.min_cons_size);
	  }

	ws.log(Strings.endl 
	       + Strings.EXEC + ws.algAR.getClass().getName() 
	       + Strings.EXEC2 + ws.db_name
	       + Strings.EXEC3 + ws.minsup
	       + Strings.EXEC4 + ws.minconf
	       + Strings.dots);
	ws.timer.reset();
	ws.timer.start();
	ws.status = Workspace.EXEC_ALG_AR;

	ws.algAR.start();
      }
    catch (Throwable e)
      {
	ws.log(Strings.UNXP_ERROR + "executeAlgAR(): " 
	       + e + Strings.endl);
	ws.status = Workspace.IDLE;

	setEnabledForMining(true);
      }
  }

  // called upon algorithm termination
  public void threadTermination(Thread t)
  {
    try
      {
	ws.timer.stop();
	ws.log(Strings.DONE);

	if (!ws.bAbortRequested)
	  ws.log(Strings.TIME + ws.timer.time() + Strings.endl);

	setEnabledForMining(true);

	if (ws.status == Workspace.EXEC_ALG_IS)
	  {
	    if (!ws.bAbortRequested)
	      {
		ws.log(Strings.empty + ws.algIS.getResult() 
		       + Strings.PASSES + Strings.endl);

		displayCacheThreaded();
	      }

	    ws.dbcw.close();
	  }
	else if (ws.status == Workspace.EXEC_ALG_AR)
	  {
	    if (!ws.bAbortRequested)
	      {
		ws.rules = ws.algAR.getResult();
		ws.log(Strings.empty + ws.rules.size()
		       + Strings.COUNT_AR + Strings.endl);

		// decide whether to use a thread or not
		if (ws.rules.size() < REFRESH_THREAD_THRESHOLD)
		  rules_table_model.refresh();
		else
		  refreshThreaded(rules_table_model);
	      }

	    ws.dbcr.close();
	  }
      }
    catch (IllegalStateException e)
      {
	ws.log(Strings.UNXP_ERROR + "threadTermination(): " 
	       + e + Strings.endl);
	ws.log(Strings.OUT_OF_MEMORY);
      }
    catch (Throwable e)
      {
	ws.log(Strings.UNXP_ERROR + "threadTermination(): " 
	       + e + Strings.endl);
      }
    finally 
      {
	if (ws.status == Workspace.EXEC_ALG_IS)
	  ws.algIS = null;
	else if (ws.status == Workspace.EXEC_ALG_AR)
	  ws.algAR = null;
	ws.status = Workspace.IDLE;
	ws.bAbortRequested = false;
      }
  }

  // display the contents of a cache file in the frequent itemsets table
  // this is done in a separate thread, so as not to "freeze" the display
  // display contents of cache in frequent itemsets table
  private void displayCache()
  {
    try
      {
	ws.log(Strings.endl + Strings.READ_CACHE);

	ws.dbcr = new DBCacheReader(ws.cache_name);
	ws.itemsets = new ArrayList();
	
	int count = 0;
	Itemset is;
	try
	  {
	    while (true)
	      {
		is = ws.dbcr.getNextItemset();
		if (is.getSupport() >= ws.minsup)
		  {
		    ws.itemsets.add(is);
		    count++;
		  }
	      }
	  }
	catch (EOFException e)
	  {
	    ws.dbcr.close();
	  }
	
	ws.log(Strings.DONE + count + Strings.COUNT_IS + Strings.endl);

	// this method is normally called from displayCacheThreaded,
	// so we don't need another thread

	// decide whether to use a thread or not
	//if (ws.itemsets.size() < REFRESH_THREAD_THRESHOLD)
	itemsets_table_model.refresh();
	//else
	//refreshThreaded(itemsets_table_model);
      }
    catch (FileNotFoundException e)
      {
	ws.log(Strings.ERROR + e + Strings.endl);
      }
    catch (Throwable e)
      {
	ws.log(Strings.UNXP_ERROR + "displayCache(): " 
	       + e + Strings.endl);
      }
  }


  // some threads for actions that can take a long time
  // and the methods that start and monitor them

  // this thread performs a database integrity check
  private class IntegrityChecker extends MonitoredThread
  {
    boolean res;
    
    public IntegrityChecker(ThreadMonitor m)
    {
      this.monitor = m;
    }

    public void execute()
    {
      try
	{
	  res = ws.dbr.checkIntegrity();
	}
      catch (Exception e)
	{
	  ws.log(Strings.UNXP_ERROR
		 + "IntegrityChecker.execute(): "
		 + e + Strings.endl);
	}
    }

    public boolean getResult()
    {
      return res;
    }
  }

  private void checkDBThreaded()
  {
    try
      {
	ws.log(Strings.endl + Strings.CHECKING);
    
	setEnabledForCheckDB(false);
	ws.status = Workspace.EXEC_SOME;
    
	// create a monitor to display the test result
	monitor = new ThreadMonitor() {
	    public void threadTermination(Thread t)
	    {
	      ws.status = Workspace.IDLE;
	      setEnabledForCheckDB(true);

	      IntegrityChecker ic = (IntegrityChecker)t;
	      ws.log(ic.getResult() ? Strings.PASSED : Strings.FAILED);

	      monitor = null; // we don't need the monitor anymore
	    }
	  };

	new IntegrityChecker(monitor).start();
      }
    catch (Exception e)
      {
	ws.log(Strings.ERROR + e + Strings.endl);
      }
  }

  // this thread reads the contents of a cache file
  private class CacheReader extends MonitoredThread
  {
    public CacheReader(ThreadMonitor m)
    {
      this.monitor = m;
    }
    
    public void execute()
    {
      displayCache();
    }
  }

  private void displayCacheThreaded()
  {
    setEnabledAll(false);
    ws.status = Workspace.EXEC_SOME;

    // create a monitor to re-enable controls
    monitor = new ThreadMonitor() {
	public void threadTermination(Thread t)
	{
	  ws.status = Workspace.IDLE;
	  setEnabledAll(true);

	  monitor = null; // we don't need the monitor anymore
	}
      };

    new CacheReader(monitor).start();
  }

  // this thread calls the garbage collector
  private class GarbageCollector extends MonitoredThread
  {
    public GarbageCollector(ThreadMonitor m)
    {
      this.monitor = m;
    }
    
    public void execute()
    {
      System.gc();
    }
  }

  private void gcThreaded()
  {
    ws.log(Strings.endl + Strings.GC);

    setEnabledForMenuAction(false);
    ws.status = Workspace.EXEC_SOME;

    // create a monitor to re-enable controls
    monitor = new ThreadMonitor() {
	public void threadTermination(Thread t)
	{
	  ws.status = Workspace.IDLE;
	  setEnabledForMenuAction(true);

	  ws.log(Strings.DONE);

	  monitor = null; // we don't need the monitor anymore
	}
      };

    new GarbageCollector(monitor).start();
  }

  // this thread refreshes the contents of a table
  private class TableRefresher extends MonitoredThread
  {
    ARtoolTableModel table_model;
    
    public TableRefresher(ThreadMonitor m, ARtoolTableModel tm)
    {
      this.monitor = m;
      table_model = tm;
    }
    
    public void execute()
    {
      table_model.refresh();
    }
  }

  private void refreshThreaded(ARtoolTableModel table_model)
  {
    setEnabledAll(false);
    ws.status = Workspace.EXEC_SOME;

    // create a monitor to re-enable controls
    monitor = new ThreadMonitor() {
	public void threadTermination(Thread t)
	{
	  ws.status = Workspace.IDLE;
	  setEnabledAll(true);

	  monitor = null; // we don't need the monitor anymore
	}
      };

    new TableRefresher(monitor, table_model).start();
  }

  // this thread sorts the contents of a table
  private class TableSorter extends MonitoredThread
  {
    ARtoolTableModel table_model;
    int table_column;
    
    public TableSorter(ThreadMonitor m, ARtoolTableModel tm, int col)
    {
      this.monitor = m;
      table_model = tm;
      table_column = col;
    }
    
    public void execute()
    {
      table_model.sort(table_column);
    }
  }

  private void sortThreaded(ARtoolTableModel table_model, int table_column)
  {
    setEnabledAll(false);
    ws.status = Workspace.EXEC_SOME;

    // create a monitor to re-enable controls
    monitor = new ThreadMonitor() {
	public void threadTermination(Thread t)
	{
	  ws.status = Workspace.IDLE;
	  setEnabledAll(true);

	  monitor = null; // we don't need the monitor anymore
	}
      };

    new TableSorter(monitor, table_model, table_column).start();
  }

  // this thread computes a new measure for the rules
  private class MeasureComputer extends MonitoredThread
  {
    public MeasureComputer(ThreadMonitor m)
    {
      this.monitor = m;
    }
    
    public void execute()
    {
      computeMeasure();
    }
  }

  private void computeMeasureThreaded()
  {
    setEnabledAll(false);
    ws.status = Workspace.EXEC_SOME;

    // create a monitor to re-enable controls
    monitor = new ThreadMonitor() {
	public void threadTermination(Thread t)
	{
	  ws.status = Workspace.IDLE;
	  setEnabledAll(true);

	  monitor = null; // we don't need the monitor anymore
	}
      };

    new MeasureComputer(monitor).start();
  }

  private void computeMeasure()
  {
    AssociationRule rule;

    ws.log(Strings.endl + Strings.COMPUTING + Strings.space);

    switch (ws.measure_id)
      {
      case Workspace.M_CONFIDENCE:
	ws.log(Strings.CONFIDENCE + Strings.dots);
	rules_table_model.setMeasureName(Strings.CONFIDENCE);
	for (int i = 0; i < ws.rules.size(); i++)
	  {
	    rule = (AssociationRule)ws.rules.get(i);
	    rule.setConfidence(rule.computeConfidence());
	  }
	break;

      case Workspace.M_PIATETSKY:
	ws.log(Strings.PIATETSKY + Strings.dots);
	rules_table_model.setMeasureName(Strings.PIATETSKY);
	for (int i = 0; i < ws.rules.size(); i++)
	  {
	    rule = (AssociationRule)ws.rules.get(i);
	    rule.setConfidence(rule.computePiatetskyShapiro());
	  }
	break;

      case Workspace.M_LIFT:
	ws.log(Strings.LIFT + Strings.dots);
	rules_table_model.setMeasureName(Strings.LIFT);
	for (int i = 0; i < ws.rules.size(); i++)
	  {
	    rule = (AssociationRule)ws.rules.get(i);
	    rule.setConfidence(rule.computeLift());
	  }
	break;

      case Workspace.M_INFLUENCE:
	ws.log(Strings.INFLUENCE + Strings.dots);
	rules_table_model.setMeasureName(Strings.INFLUENCE);
	for (int i = 0; i < ws.rules.size(); i++)
	  {
	    rule = (AssociationRule)ws.rules.get(i);
	    rule.setConfidence(rule.computeInfluence());
	  }
	break;

      default:
	ws.log(Strings.INT_ERROR + "computeMeasure()" + Strings.INT_ERROR_MSG);
	return;
      }

    ws.log(Strings.DONE);

    // we changed a column name so force repaint of table header
    rules_table_model.fireTableStructureChanged();

    // refresh the table to display the new measure
    rules_table_model.refresh();
  }



  //*** Table Models ***

  // Abstract table model for ARtool results tables
  private abstract class ARtoolTableModel extends AbstractTableModel
  {
    protected ArrayList cells;
    protected ArrayList column_names;

    // for toggling column sorting order
    protected int[] col_order;
    
    public int getRowCount()
    {
      return cells.size();
    }
  
    public int getColumnCount()
    {
      return column_names.size();
    }
  
    public Object getValueAt(int row, int column)
    {
      ArrayList cellRow = (ArrayList)cells.get(row);
      Object cell = cellRow.get(column);
      return cell;
    }
  
    public String getColumnName(int i)
    {
      try
	{
	  return column_names.get(i).toString();
	}
      catch(Exception e)
	{
	  ws.log(Strings.UNXP_ERROR + "getColumnName(): " 
		 + e + Strings.endl);
	  return "";
	}
    }

    protected void reset_col_order()
    {
      col_order = new int[getColumnCount()];
    }

    public abstract void refresh();

    public abstract void sort(int tableColumn);
  }

  // Table model for itemsets table
  private class ItemsetsTableModel extends ARtoolTableModel
  {
    public ItemsetsTableModel()
    {
      cells = new ArrayList();
      column_names = new ArrayList();
      column_names.add(Strings.ITEMSET);
      column_names.add(Strings.SUPPORT);
      reset_col_order();
    }
  
    public void refresh()
    {
      if (ws.itemsets == null)
	return;

      if (ws.itemsets.size() == 0)
	{
	  cells = new ArrayList(0);
	  fireTableDataChanged();
	  return;
	}

      ws.log(Strings.DISPLAYING);

      int num_itemsets = ws.itemsets.size();
      ArrayList innerCell;

      cells = new ArrayList();

      for (int i = 0; i < num_itemsets; i++)
	{
	  Itemset is = (Itemset)ws.itemsets.get(i);
	  innerCell = new ArrayList();

	  String itemset = new String();
	  int j;
	  for (j = 0; j < is.size() - 1; j++)
	    itemset = itemset 
	      + ws.item_names.get(is.get(j) - 1).toString() + ", " ;

	  itemset = itemset 
	    + ws.item_names.get(is.get(j) - 1).toString();

	  innerCell.add(itemset);
	  innerCell.add(String.valueOf(is.getSupport()));

	  cells.add(innerCell);
	}

      fireTableDataChanged();

      ws.log(Strings.DONE);
    }

    public void sort(int tableColumn)
    {
      if (ws.itemsets == null || ws.itemsets.size() < 2)
	return;

      int sortOrder = col_order[tableColumn];

      // update col_order
      col_order[tableColumn] = (col_order[tableColumn] + 1) % 2;

      int sortBy = tableColumn;

      ws.log(Strings.SORT_IS 
	     + ((sortOrder == 0) ? Strings.DESC : Strings.ASC) 
	     + column_names.get(sortBy) + Strings.dots);

      CriteriaSorter.sort(ws.itemsets, sortBy, sortOrder);

      ws.log(Strings.DONE);

      refresh();
    }
  }

  // Table model for rules table
  private class RulesTableModel extends ARtoolTableModel
  {
    public RulesTableModel()
    {
      cells = new ArrayList();
      column_names = new ArrayList();
      column_names.add(Strings.ANTECEDENT);
      column_names.add(Strings.CONSEQUENT);
      column_names.add(Strings.SUPPORT);
      column_names.add(Strings.CONFIDENCE);
      reset_col_order();
    }

    public void setMeasureName(String name)
    {
      column_names.set(3, name);
    }
  
    public void refresh()
    {
      if (ws.rules == null)
	return;

      if (ws.rules.size() == 0)
	{
	  cells = new ArrayList(0);
	  fireTableDataChanged();
	  return;
	}

      ws.log(Strings.DISPLAYING);

      int num_rules = ws.rules.size();
      ArrayList innerCell;

      cells = new ArrayList();

      for (int i = 0; i < num_rules; i++)
	{
	  AssociationRule rule = (AssociationRule)ws.rules.get(i);
	  innerCell = new ArrayList();

	  String ant = new String();
	  int j;
	  for (j = 0; j < rule.antecedentSize() - 1; j++)
	    ant = ant 
	      + ws.item_names.get(rule.getAntecedentItem(j) - 1)
	      .toString() + ", " ;

	  ant = ant 
	    + ws.item_names.get(rule.getAntecedentItem(j) - 1)
	    .toString();
		
	  String con = new String();
	  for (j = 0; j < rule.consequentSize() - 1; j++)
	    con = con 
	      + ws.item_names.get(rule.getConsequentItem(j) - 1)
	      .toString() + ", " ;

	  con = con 
	    + ws.item_names.get(rule.getConsequentItem(j) - 1)
	    .toString();

	  innerCell.add(ant);
	  innerCell.add(con);
	  innerCell.add(String.valueOf(rule.getSupport()));
	  innerCell.add(String.valueOf(rule.getConfidence()));

	  cells.add(innerCell);
	}

      fireTableDataChanged();

      ws.log(Strings.DONE);
    }

    public void sort(int tableColumn)
    {
      if (ws.rules == null || ws.rules.size() < 2)
	return;

      int sortOrder = col_order[tableColumn];

      // update col_order
      col_order[tableColumn] = (col_order[tableColumn] + 1) % 2;

      int sortBy = tableColumn;

      ws.log(Strings.SORT_AR 
	     + ((sortOrder == 0) ? Strings.DESC : Strings.ASC) 
	     + column_names.get(sortBy) + Strings.dots);

      CriteriaSorter.sort(ws.rules, sortBy, sortOrder);

      ws.log(Strings.DONE);

      refresh();
    }
  }
}
