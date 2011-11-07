/*
  HelpDialog.java
 
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
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

/**
 * A help dialog
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
public class HelpDialog extends CenteredJDialog 
{
  // Variables declaration
  private JSplitPane jSplitPane_Main;
  private JScrollPane jScrollPane_Topics;
  private JTree jTree_Topics;
  private JScrollPane jScrollPane_View;
  private JEditorPane jEditorPane_View;
  private URL homeURL;
  // End of variables declaration

  /** Creates new form HelpDialog */
  public HelpDialog(Frame parent, boolean modal) 
  {
    super(parent, modal);
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents() 
  {
    setTitle(Strings.TOPICS);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setModal(true);

    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent evt) {
	  closeDialog();
	}
      });

    // Create the nodes.
    DefaultMutableTreeNode top
      = new DefaultMutableTreeNode(Strings.TITLE + Strings.space
				   + Strings.TOPICS);
    createNodes(top);

    // Create a tree that allows one selection at a time.
    jTree_Topics = new JTree(top);
    jTree_Topics.getSelectionModel().setSelectionMode
      (TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Listen for when the selection changes.
    jTree_Topics.addTreeSelectionListener(new TreeSelectionListener() {
	public void valueChanged(TreeSelectionEvent e) {
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode)
	    jTree_Topics.getLastSelectedPathComponent();

	  if (node == null)
	    return;

	  Object nodeInfo = node.getUserObject();
	  if (node.isLeaf())
	    {
	      TopicInfo topic = (TopicInfo)nodeInfo;
	      displayURL(topic.topicURL);
	    }
	  else
	    displayURL(homeURL); 
	}
      });

    jScrollPane_Topics = new JScrollPane(jTree_Topics);

    jEditorPane_View = new JEditorPane();
    jEditorPane_View.setEditable(false);
    initHelp();

    jScrollPane_View = new JScrollPane(jEditorPane_View);

    jSplitPane_Main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    jSplitPane_Main.setOneTouchExpandable(true);
    jSplitPane_Main.setTopComponent(jScrollPane_Topics);
    jSplitPane_Main.setBottomComponent(jScrollPane_View);

    Dimension min_dim = new Dimension(100, 50);
    jScrollPane_Topics.setMinimumSize(min_dim);
    jScrollPane_View.setMinimumSize(min_dim);
    jSplitPane_Main.setDividerLocation(150);

    jSplitPane_Main.setPreferredSize(new Dimension(600, 400));

    getContentPane().add(jSplitPane_Main, BorderLayout.CENTER);

    pack();

    center();
  }

  // this method makes a URL for a given filename
  private URL makeURL(String filename)
  {
    try 
      {
	String s = "file:" 
	  + System.getProperty("user.dir")
	  + System.getProperty("file.separator")
	  + Strings.HELP_DIR
	  + System.getProperty("file.separator")
	  + filename;
	return new URL(s);
      }
    catch (Exception e)
      {
	Workspace.getWorkspace().log(Strings.UNXP_ERROR + "makeURL(): "
				     + e + Strings.endl);
	return null;
      }
  }

  private void initHelp() 
  {
    try 
      {
	homeURL = makeURL(Strings.INTRO_HTML);
	displayURL(homeURL);
      } 
    catch (Exception e) 
      {
	Workspace.getWorkspace().log(Strings.UNXP_ERROR + "initHelp(): "
				     + e + Strings.endl);
      }
  }

  private void displayURL(URL url) 
  {
    try 
      {
	jEditorPane_View.setPage(url);
      } 
    catch (IOException e) 
      {
	Workspace.getWorkspace().log(Strings.UNXP_ERROR + "displayURL(): "
				     + e + Strings.endl);
      }
  }

  private class TopicInfo 
  {
    public String topicName;
    public URL topicURL;

    public TopicInfo(String topic, String filename) 
    {
      topicName = topic;
      topicURL = makeURL(filename);
    }

    public String toString()
    {
      return topicName;
    }
  }

  private void createNodes(DefaultMutableTreeNode top) 
  {
    DefaultMutableTreeNode category = null;
    DefaultMutableTreeNode topic = null;

    ////////
    // Intro
    category = new DefaultMutableTreeNode(Strings.INTRO_TITLE);
    top.add(category);

    // definitions
    topic = new DefaultMutableTreeNode(new TopicInfo(Strings.DEFS_TITLE,
						     Strings.DEFS_HTML));
    category.add(topic);

    // references
    topic = new DefaultMutableTreeNode(new TopicInfo(Strings.REFERENCE_TITLE,
						     Strings.REFERENCE_HTML));
    category.add(topic);

    /////////
    // Manual
    category = new DefaultMutableTreeNode(Strings.MANUAL_TITLE);
    top.add(category);

    // overview
    topic = new DefaultMutableTreeNode(new TopicInfo(Strings.OVERVIEW_TITLE,
						     Strings.OVERVIEW_HTML));
    category.add(topic);

    // user guide
    topic = new DefaultMutableTreeNode(new TopicInfo(Strings.USER_GUIDE_TITLE,
						     Strings.USER_GUIDE_HTML));
    category.add(topic);

    // technical information
    topic = new DefaultMutableTreeNode(new TopicInfo(Strings.MISC_INFO_TITLE,
						     Strings.MISC_INFO_HTML));
    category.add(topic);
  }

  /** Closes the dialog */
  private void closeDialog() 
  {
    setVisible(false);
    dispose();
  }
}
