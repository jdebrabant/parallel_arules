/*
  AboutDialog.java
 
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

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The ARtool About dialog
 *
 * @version 	1.1
 * @author	Laurentiu Cristofor
 */
public class AboutDialog extends CenteredJDialog 
  implements ActionListener
{
  // Variables declaration
  private JPanel jPanel_center;
  private JTextArea jTextArea_About;
  private JPanel jPanel_south;
  private JButton jButton_Close;
  // End of variables declaration

  /** Creates new form AboutDialog */
  public AboutDialog(Frame parent, boolean modal) 
  {
    super(parent, modal);
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents() 
  {
    jPanel_center = new JPanel();
    jTextArea_About = new JTextArea();
    jPanel_south = new JPanel();
    jButton_Close = new JButton();
        
    setTitle(Strings.ABOUT);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setModal(true);
    setResizable(false);

    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent evt) {
	  closeDialog();
	}
      });
        
    jTextArea_About.setLineWrap(true);
    jTextArea_About.setEditable(false);
    jTextArea_About.setText(Strings.ABOUT_TEXT);
    jTextArea_About.setBackground(Color.lightGray);
    jTextArea_About.setPreferredSize(new Dimension(350, 200));
        
    jPanel_center.setBorder(new EtchedBorder(EtchedBorder.RAISED));
    jPanel_center.add(jTextArea_About);
        
    getContentPane().add(jPanel_center, BorderLayout.CENTER);
        
    jButton_Close.setText(Strings.CLOSE);
    jButton_Close.addActionListener(this);
    jButton_Close.setMnemonic(KeyEvent.VK_C);
    jButton_Close.setToolTipText(Strings.CLOSE_TIP);
    jPanel_south.add(jButton_Close);
        
    getContentPane().add(jPanel_south, BorderLayout.SOUTH);

    pack();

    center();
  }

  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() == jButton_Close) 
      closeDialog();
  }

  /** Closes the dialog */
  private void closeDialog() 
  {
    setVisible(false);
    dispose();
  }
}
