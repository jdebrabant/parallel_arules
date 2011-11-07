/*
  CenteredJDialog.java
 
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
import java.awt.*;

/**
 * A centered JDialog. Constructor comments are straight from the JDialog
 * source.
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
public class CenteredJDialog extends JDialog
{
  /**
   * Creates a non-modal dialog without a title and without a
   * specified <code>Frame</code> owner.  A shared, hidden frame will
   * be set as the owner of the dialog.
   */
  public CenteredJDialog() 
  {
    super();
  }

  /**
   * Creates a non-modal dialog without a title with the
   * specifed <code>Frame</code> as its owner.
   *
   * @param owner the <code>Frame</code> from which the dialog is displayed
   */
  public CenteredJDialog(Frame owner) 
  {
    super(owner);
  }

  /**
   * Creates a modal or non-modal dialog without a title and
   * with the specified owner <code>Frame</code>.
   *
   * @param owner the <code>Frame</code> from which the dialog is displayed
   * @param modal  true for a modal dialog, false for one that allows
   *               others windows to be active at the same time
   */
  public CenteredJDialog(Frame owner, boolean modal) 
  {
    super(owner, modal);
  }

  /**
   * Creates a non-modal dialog with the specified title and
   * with the specified owner frame.
   *
   * @param owner the <code>Frame</code> from which the dialog is displayed
   * @param title  the <code>String</code> to display in the dialog's
   *			title bar
   */
  public CenteredJDialog(Frame owner, String title) 
  {
    super(owner, title);     
  }

  /**
   * Creates a modal or non-modal dialog with the specified title 
   * and the specified owner <code>Frame</code>.  All constructors
   * defer to this one.
   * <p>
   * NOTE: Any popup components (<code>JComboBox</code>,
   * <code>JPopupMenu</code>, <code>JMenuBar</code>)
   * created within a modal dialog will be forced to be lightweight.
   *
   * @param owner the <code>Frame</code> from which the dialog is displayed
   * @param title  the <code>String</code> to display in the dialog's
   *			title bar
   * @param modal  true for a modal dialog, false for one that allows
   *               other windows to be active at the same time
   */
  public CenteredJDialog(Frame owner, String title, boolean modal) 
  {
    super(owner, title, modal);
  }

  /**
   * Creates a non-modal dialog without a title with the
   * specifed <code>Dialog</code> as its owner.
   *
   * @param owner the <code>Dialog</code> from which the dialog is displayed
   */
  public CenteredJDialog(Dialog owner) 
  {
    super(owner);
  }

  /**
   * Creates a modal or non-modal dialog without a title and
   * with the specified owner dialog.
   * <p>
   *
   * @param owner the <code>Dialog</code> from which the dialog is displayed
   * @param modal  true for a modal dialog, false for one that allows
   *               other windows to be active at the same time
   */
  public CenteredJDialog(Dialog owner, boolean modal) 
  {
    super(owner, modal);
  }

  /**
   * Creates a non-modal dialog with the specified title and
   * with the specified owner dialog.
   *
   * @param owner the <code>Dialog</code> from which the dialog is displayed
   * @param title  the <code>String</code> to display in the dialog's
   *			title bar
   */
  public CenteredJDialog(Dialog owner, String title) 
  {
    super(owner, title);     
  }

  /**
   * Creates a modal or non-modal dialog with the specified title 
   * and the specified owner frame.
   *
   * @param owner the <code>Dialog</code> from which the dialog is displayed
   * @param title  the <code>String</code> to display in the dialog's
   *			title bar
   * @param modal  true for a modal dialog, false for one that allows
   *               other windows to be active at the same time
   */
  public CenteredJDialog(Dialog owner, String title, boolean modal) 
  {
    super(owner, title, modal);
  }

  /**
   * The following method should be called to center the dialog.
   */
  public CenteredJDialog center()
  {
    if (getParent() == null)
      return this;

    Point p = getParent().getLocation();
    Dimension p_d = getParent().getSize();
    Dimension d = getSize();

    int x = p.x + (p_d.width - d.width) / 2;
    int y = p.y + (p_d.height - d.height) / 2;

    setLocation(x, y);

    return this;
  }
}
