/*
  GenDBDialog.java
 
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
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
 * The dialog for generating a synthetic database.
 *
 * @version 	1.1
 * @author	Laurentiu Cristofor
 */
public class GenDBDialog extends CenteredJDialog
  implements ActionListener, ThreadMonitor
{
  // Variables declaration
  private JPanel jPanel_north;
  private JPanel jPanel_north_center;
  private JPanel jPanel_nc11;
  private JLabel jLabel_name;
  private JPanel jPanel_nc12;
  private JTextField jTextField_name;
  private JPanel jPanel_nc21;
  private JLabel jLabel_descr;
  private JPanel jPanel_nc22;
  private JTextField jTextField_descr;

  private JPanel jPanel_north_south;
  private JCheckBox jCheckBox_Auto;

  private JPanel jPanel_center;
  private JPanel jPanel_center_center;
  private JPanel jPanel_cc11;
  private JLabel jLabel_num_items;
  private JPanel jPanel_cc12;
  private JTextField jTextField_num_items;
  private JPanel jPanel_cc21;
  private JLabel jLabel_num_trans;
  private JPanel jPanel_cc22;
  private JTextField jTextField_num_trans;
  private JPanel jPanel_cc31;
  private JLabel jLabel_avg_trans;
  private JPanel jPanel_cc32;
  private JTextField jTextField_avg_trans;
  private JPanel jPanel_cc41;
  private JLabel jLabel_num_patt;
  private JPanel jPanel_cc42;
  private JTextField jTextField_num_patt;
  private JPanel jPanel_cc51;
  private JLabel jLabel_avg_patt;
  private JPanel jPanel_cc52;
  private JTextField jTextField_avg_patt;
  private JPanel jPanel_cc61;
  private JLabel jLabel_correlation;
  private JPanel jPanel_cc62;
  private JTextField jTextField_correlation;
  private JPanel jPanel_cc71;
  private JLabel jLabel_corruption;
  private JPanel jPanel_cc72;
  private JTextField jTextField_corruption;

  private JPanel jPanel_center_south;
  private JProgressBar jProgressBar;

  private JPanel jPanel_south;
  private JButton jButton_Generate;
  private JButton jButton_Abort;
  private JButton jButton_Close;

  private boolean isGenerating;

  private AbortableThread generatorThread;

  private DBWriter dbw;
  // End of variables declaration

  /** Creates new form GenDBDialog */
  public GenDBDialog(Frame parent, boolean modal)
  {
    super(parent, modal);
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents()
  {
    jPanel_north = new JPanel();
    jPanel_north_center = new JPanel();
    jPanel_nc11 = new JPanel();
    jLabel_name = new JLabel();
    jPanel_nc12 = new JPanel();
    jTextField_name = new JTextField();
    jPanel_nc21 = new JPanel();
    jLabel_descr = new JLabel();
    jPanel_nc22 = new JPanel();
    jTextField_descr = new JTextField();

    jPanel_north_south = new JPanel();
    jCheckBox_Auto = new JCheckBox();

    jPanel_center = new JPanel();
    jPanel_center_center = new JPanel();
    jPanel_cc11 = new JPanel();
    jLabel_num_items = new JLabel();
    jPanel_cc12 = new JPanel();
    jTextField_num_items = new JTextField();
    jPanel_cc21 = new JPanel();
    jLabel_num_trans = new JLabel();
    jPanel_cc22 = new JPanel();
    jTextField_num_trans = new JTextField();
    jPanel_cc31 = new JPanel();
    jLabel_avg_trans = new JLabel();
    jPanel_cc32 = new JPanel();
    jTextField_avg_trans = new JTextField();
    jPanel_cc41 = new JPanel();
    jLabel_num_patt = new JLabel();
    jPanel_cc42 = new JPanel();
    jTextField_num_patt = new JTextField();
    jPanel_cc51 = new JPanel();
    jLabel_avg_patt = new JLabel();
    jPanel_cc52 = new JPanel();
    jTextField_avg_patt = new JTextField();
    jPanel_cc61 = new JPanel();
    jLabel_correlation = new JLabel();
    jPanel_cc62 = new JPanel();
    jTextField_correlation = new JTextField();
    jPanel_cc71 = new JPanel();
    jLabel_corruption = new JLabel();
    jPanel_cc72 = new JPanel();
    jTextField_corruption = new JTextField();

    jPanel_center_south = new JPanel();
    jProgressBar = new JProgressBar();

    jPanel_south = new JPanel();
    jButton_Generate = new JButton();
    jButton_Abort = new JButton();
    jButton_Close = new JButton();
        
    setTitle(Strings.GENSYNDB);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setModal(true);
    setResizable(false);

    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent evt) {
	  closeDialog();
	}
      });
        
    jPanel_north.setLayout(new BorderLayout());
        
    jPanel_north.setBorder(new TitledBorder(Strings.BORD_NAME));
    jPanel_north_center.setLayout(new GridLayout(2, 2));
        
    jLabel_name.setText(Strings.DB_NAME);
    jPanel_nc11.add(jLabel_name);
        
    jPanel_north_center.add(jPanel_nc11);
        
    jTextField_name.setText(Strings.DEF_NAME + Strings.DB_EXT);
    jTextField_name.setPreferredSize(new Dimension(200, 21));
    jTextField_name.setToolTipText(Strings.DB_NAME_TIP);
    jPanel_nc12.add(jTextField_name);
        
    jPanel_north_center.add(jPanel_nc12);
        
    jLabel_descr.setText(Strings.DB_DESCR);
    jPanel_nc21.add(jLabel_descr);
        
    jPanel_north_center.add(jPanel_nc21);
        
    jTextField_descr.setText(Strings.DEF_NAME);
    jTextField_descr.setPreferredSize(new Dimension(200, 21));
    jTextField_descr.setToolTipText(Strings.DB_DESCR_TIP);
    jPanel_nc22.add(jTextField_descr);
        
    jPanel_north_center.add(jPanel_nc22);
        
    jPanel_north.add(jPanel_north_center, BorderLayout.CENTER);
        
    jCheckBox_Auto.setText(Strings.CB_AUTO);
    jCheckBox_Auto.setMnemonic(KeyEvent.VK_N);
    jCheckBox_Auto.addActionListener(this);
    jCheckBox_Auto.setToolTipText(Strings.CB_AUTO_TIP);
    jPanel_north_south.add(jCheckBox_Auto);
        
    jPanel_north.add(jPanel_north_south, BorderLayout.SOUTH);
        
    getContentPane().add(jPanel_north, BorderLayout.NORTH);
        
    jPanel_center.setLayout(new BorderLayout());
        
    jPanel_center_center.setLayout(new GridLayout(7, 2));
        
    jPanel_center_center.setBorder(new TitledBorder(Strings.BORD_CHAR));
    jLabel_num_items.setText(Strings.NUM_ITEMS);
    jPanel_cc11.add(jLabel_num_items);
        
    jPanel_center_center.add(jPanel_cc11);

    Dimension dim_txt = new Dimension(100, 21);
        
    jTextField_num_items.setText(Strings.DEF_NUM_ITEMS);
    jTextField_num_items.setPreferredSize(dim_txt);
    jTextField_num_items.setToolTipText(Strings.NUM_ITEMS_TIP);
    jPanel_cc12.add(jTextField_num_items);
        
    jPanel_center_center.add(jPanel_cc12);
        
    jLabel_num_trans.setText(Strings.NUM_TRANS);
    jPanel_cc21.add(jLabel_num_trans);
        
    jPanel_center_center.add(jPanel_cc21);
        
    jTextField_num_trans.setText(Strings.DEF_NUM_TRANS);
    jTextField_num_trans.setPreferredSize(dim_txt);
    jTextField_num_trans.setToolTipText(Strings.NUM_TRANS_TIP);
    jPanel_cc22.add(jTextField_num_trans);
        
    jPanel_center_center.add(jPanel_cc22);
        
    jLabel_avg_trans.setText(Strings.AVG_TRANS);
    jPanel_cc31.add(jLabel_avg_trans);
        
    jPanel_center_center.add(jPanel_cc31);
        
    jTextField_avg_trans.setText(Strings.DEF_AVG_TRANS);
    jTextField_avg_trans.setPreferredSize(dim_txt);
    jTextField_avg_trans.setToolTipText(Strings.AVG_TRANS_TIP);
    jPanel_cc32.add(jTextField_avg_trans);
        
    jPanel_center_center.add(jPanel_cc32);
        
    jLabel_num_patt.setText(Strings.NUM_PATT);
    jPanel_cc41.add(jLabel_num_patt);
        
    jPanel_center_center.add(jPanel_cc41);
        
    jTextField_num_patt.setText(Strings.DEF_NUM_PATT);
    jTextField_num_patt.setPreferredSize(dim_txt);
    jTextField_num_patt.setToolTipText(Strings.NUM_PATT_TIP);
    jPanel_cc42.add(jTextField_num_patt);
        
    jPanel_center_center.add(jPanel_cc42);
        
    jLabel_avg_patt.setText(Strings.AVG_PATT);
    jPanel_cc51.add(jLabel_avg_patt);
        
    jPanel_center_center.add(jPanel_cc51);
        
    jTextField_avg_patt.setText(Strings.DEF_AVG_PATT);
    jTextField_avg_patt.setPreferredSize(dim_txt);
    jTextField_avg_patt.setToolTipText(Strings.AVG_PATT_TIP);
    jPanel_cc52.add(jTextField_avg_patt);
        
    jPanel_center_center.add(jPanel_cc52);
        
    jLabel_correlation.setText(Strings.CORREL);
    jPanel_cc61.add(jLabel_correlation);
        
    jPanel_center_center.add(jPanel_cc61);
        
    jTextField_correlation.setText(Strings.DEF_CORREL);
    jTextField_correlation.setPreferredSize(dim_txt);
    jTextField_correlation.setToolTipText(Strings.CORREL_TIP);
    jPanel_cc62.add(jTextField_correlation);
        
    jPanel_center_center.add(jPanel_cc62);
        
    jLabel_corruption.setText(Strings.CORRUP);
    jPanel_cc71.add(jLabel_corruption);
        
    jPanel_center_center.add(jPanel_cc71);
        
    jTextField_corruption.setText(Strings.DEF_CORRUP);
    jTextField_corruption.setPreferredSize(dim_txt);
    jTextField_corruption.setToolTipText(Strings.CORRUP_TIP);
    jPanel_cc72.add(jTextField_corruption);
        
    jPanel_center_center.add(jPanel_cc72);
        
    jPanel_center.add(jPanel_center_center, BorderLayout.CENTER);
        
    jProgressBar.setStringPainted(true);
    jPanel_center_south.add(jProgressBar);
        
    jPanel_center.add(jPanel_center_south, BorderLayout.SOUTH);
        
    getContentPane().add(jPanel_center, BorderLayout.CENTER);
        
    jButton_Generate.setText(Strings.GENERATE);
    jButton_Generate.addActionListener(this);
    jButton_Generate.setMnemonic(KeyEvent.VK_G);
    jButton_Generate.setToolTipText(Strings.GENERATE_TIP);
    jPanel_south.add(jButton_Generate);
        
    jButton_Abort.setText(Strings.ABORT);
    jButton_Abort.addActionListener(this);
    jButton_Abort.setMnemonic(KeyEvent.VK_A);
    jButton_Abort.setToolTipText(Strings.ABORT_GEN_TIP);
    jPanel_south.add(jButton_Abort);
        
    jButton_Close.setText(Strings.CLOSE);
    jButton_Close.addActionListener(this);
    jButton_Close.setMnemonic(KeyEvent.VK_C);
    jButton_Close.setToolTipText(Strings.CLOSE_TIP);
    jPanel_south.add(jButton_Close);

    getContentPane().add(jPanel_south, BorderLayout.SOUTH);

    jButton_Abort.setEnabled(false);
        
    pack();

    center();
  }

  private void enableControls(boolean bEnable)
  {
    jTextField_name.setEnabled(bEnable);
    jTextField_descr.setEnabled(bEnable);
    jCheckBox_Auto.setEnabled(bEnable);
    jTextField_num_items.setEnabled(bEnable);
    jTextField_num_trans.setEnabled(bEnable);
    jTextField_avg_trans.setEnabled(bEnable);
    jTextField_num_patt.setEnabled(bEnable);
    jTextField_avg_patt.setEnabled(bEnable);
    jTextField_correlation.setEnabled(bEnable);
    jTextField_corruption.setEnabled(bEnable);
    jButton_Generate.setEnabled(bEnable);
    jButton_Close.setEnabled(bEnable);

    if (jCheckBox_Auto.isSelected())
      {
	jTextField_name.setEnabled(false);
	jTextField_descr.setEnabled(false);
      }

    jButton_Abort.setEnabled(!bEnable);
  }

  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() == jButton_Generate)
      {
	generateDB();
      } 
    else if (event.getSource() == jButton_Abort)
      {
	if (isGenerating)
	  {
	    generatorThread.abort();
	    Workspace.getWorkspace().log(Strings.ABORT_REQUEST);
	  }
      } 
    else if (event.getSource() == jButton_Close)
      {
	closeDialog();
      }
    else if (event.getSource() == jCheckBox_Auto)
      {
	jTextField_name.setEnabled(!jCheckBox_Auto.isSelected());
	jTextField_descr.setEnabled(!jCheckBox_Auto.isSelected());
      } 
  }

  // generate the synthetic database
  private void generateDB()
  {
    try
      {
	// get generation parameters
	int num_items = JTextTools.getInt(jTextField_num_items);
	if (num_items < 1)
	  JTextTools.dealWithInvalidValue(jTextField_num_items);

	int num_trans = JTextTools.getInt(jTextField_num_trans);
	if (num_trans < 1)
	  JTextTools.dealWithInvalidValue(jTextField_num_trans);

	int avg_trans = JTextTools.getInt(jTextField_avg_trans);
	if (avg_trans < 1 || avg_trans > num_items)
	  JTextTools.dealWithInvalidValue(jTextField_avg_trans);
	
	int num_patt = JTextTools.getInt(jTextField_num_patt);
	if (num_patt < 1)
	  JTextTools.dealWithInvalidValue(jTextField_num_patt);

	int avg_patt = JTextTools.getInt(jTextField_avg_patt);
	if (avg_patt < 1 || avg_patt > num_items)
	  JTextTools.dealWithInvalidValue(jTextField_avg_patt);
	
	double correlation = JTextTools.getDouble(jTextField_correlation);
	if (correlation < 0 || correlation > 1)
	  JTextTools.dealWithInvalidValue(jTextField_correlation);

	double corruption = JTextTools.getDouble(jTextField_corruption);
	if (corruption < 0 || corruption > 1)
	  JTextTools.dealWithInvalidValue(jTextField_corruption);

	enableControls(false);
	isGenerating = true;

	String db_descr;
	String db_name;

	// if user supplied a name and description, then use those
	if (!jCheckBox_Auto.isSelected())
	  {
	    db_name = jTextField_name.getText();
	    db_descr = jTextField_descr.getText();
	  }
	// otherwise build "auto" name and description
	else
	  {
	    db_descr = "T" + num_trans + "_AT" + avg_trans
	      + "_I" + num_items
	      + "_P" + num_patt + "_AP" + avg_patt;
	    db_name = db_descr + Strings.DB_EXT;

	    jTextField_name.setText(db_name);
	    jTextField_descr.setText(db_descr);
	  }

	// initialize the synthetic data generator
	SyntheticDataGenerator sdg 
	  = new SyntheticDataGenerator(num_trans, avg_trans,
				       num_patt, avg_patt,
				       num_items,
				       correlation, corruption);

	// we're going to update the progress bar for each transaction
	// generated
	jProgressBar.setMaximum(num_trans);

	// create column names
	ArrayList col_names = new ArrayList();
	for (int i = 0; i < num_items; i++)
	  col_names.add("C" + (i + 1));

	// if file exists, delete it, otherwise DBWriter will append
	// to it
	File f = new File(db_name);
	if (f.exists())
	  f.delete();

	// open the database and write the column names and description
	dbw = new DBWriter(db_name);
	dbw.setColumnNames(col_names);
	dbw.setDescription(db_descr);

	Workspace.getWorkspace().log(Strings.endl
				     + Strings.NUM_ITEMS + num_items
				     + Strings.endl
				     + Strings.NUM_TRANS + num_trans
				     + Strings.endl
				     + Strings.AVG_TRANS + avg_trans
				     + Strings.endl
				     + Strings.NUM_PATT + num_patt
				     + Strings.endl
				     + Strings.AVG_PATT + avg_patt
				     + Strings.endl
				     + Strings.CORREL + correlation
				     + Strings.endl
				     + Strings.CORRUP + corruption
				     + Strings.endl);

	Workspace.getWorkspace().log(Strings.endl + Strings.GENERATING
				     + db_name + Strings.dots);

	// create a thread to generate and write the rest of the database
	generatorThread = new GenDBThread(this, sdg, dbw, jProgressBar);

	generatorThread.start();
      }
    catch (IllegalArgumentException invalid_input)
      {
	// wait for user to input something reasonable
      }
    catch (Throwable e)
      {
	Workspace.getWorkspace().log(Strings.UNXP_ERROR
				     + "actionPerformed(): " 
				     + e + Strings.endl);
	isGenerating = false;
	closeDialog();
      }
  }

  // called upon algorithm termination
  public void threadTermination(Thread t)
  {
    Workspace.getWorkspace().log(Strings.DONE);

    try
      {
	dbw.close();
      }
    catch (Throwable e)
      {
	Workspace.getWorkspace().log(Strings.UNXP_ERROR
				     + "threadTermination: " 
				     + e + Strings.endl);
	isGenerating = false;
	closeDialog();
      }

    isGenerating = false;
    enableControls(true);
  }

  /** Closes the dialog */
  private void closeDialog()
  {
    // do not allow the user to exit dialog if we're still generating
    // the database
    if (isGenerating)
      return;

    hide();
    dispose();
  }
}
