/*
 * DualDiffDialog.java - Port of the Show_Dual_Diff.bsh BeanShell macro
 * script - provides a front end to the JDiff plugin
 * Copyright (C) 2001 John Gellene, Andre Kaplan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jEdit program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id$
 */


package jdiff;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSManager;


public class DualDiffDialog extends JDialog {
    private View view;
    private HistoryTextField baseFileField;
    private HistoryTextField newFileField;


    public DualDiffDialog(View view) {
        super(view, "Show dual diff display", true);

        this.view = view;

        JPanel content = new JPanel(new GridLayout(5, 1, 0, 0));
        content.setPreferredSize(new Dimension(390, 180));
        content.setBorder(new EmptyBorder(0, 10, 5, 10));
        this.setContentPane(content);

        JLabel baseLabel = new JLabel("Base file:");
        baseLabel.setForeground(Color.black);
        content.add(baseLabel);

        JPanel baseFilePanel = new JPanel(new BorderLayout(5, 5));
        baseFilePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        this.baseFileField = new HistoryTextField("user.showDualDiff.basefile");
        this.baseFileField.setText(view.getBuffer().getName());
        baseFilePanel.add(this.baseFileField, BorderLayout.CENTER);
        JButton baseChooseButton = new JButton("Choose");
        baseFilePanel.add(baseChooseButton, BorderLayout.EAST);
        content.add(baseFilePanel);

        JLabel newLabel = new JLabel("New file:");
        newLabel.setForeground(Color.black);
        content.add(newLabel);

        JPanel newFilePanel = new JPanel(new BorderLayout(5, 5));
        newFilePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        this.newFileField = new HistoryTextField("user.showDualDiff.newfile");
        newFilePanel.add(this.newFileField, BorderLayout.CENTER);
        JButton newChooseButton = new JButton("Choose");
        newFilePanel.add(newChooseButton, BorderLayout.EAST);
        content.add(newFilePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBorder(new EmptyBorder(5, 50, 0, 50));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.setPreferredSize(cancel.getPreferredSize());
        this.getRootPane().setDefaultButton(ok);
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        content.add(buttonPanel);

        baseChooseButton.addActionListener(new FieldHandler(this.baseFileField));
        newChooseButton.addActionListener(new FieldHandler(this.newFileField));
        ok.addActionListener(new OkActionHandler());
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                DualDiffDialog.this.dispose();
            }
        });

        this.addKeyListener(new KeyHandler());

        this.pack();
        this.setLocationRelativeTo(this.view);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.show();
    }


    private class OkActionHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent ae) {
            String basePath = DualDiffDialog.this.baseFileField.getText();
            String newPath = DualDiffDialog.this.newFileField.getText();
            if (basePath.length() == 0 || newPath.length() == 0) {
                GUIUtilities.message(
                    DualDiffDialog.this, "jdiff.two-files-needed", null
                );
                return;
            }

            VFS vfs = DualDiffDialog.this.view.getBuffer().getVFS();
            String currentDir = vfs.getParentOfPath(
                DualDiffDialog.this.view.getBuffer().getPath()
            );

            String err = "";
            int errCount = 0;

            VFS baseVFS = VFSManager.getVFSForPath(basePath);
            VFS.DirectoryEntry baseEntry = null;

            try {
                baseEntry = baseVFS._getDirectoryEntry(null, basePath, null);
            } catch (IOException ioe) {}

            if (baseEntry == null || baseEntry.type != VFS.DirectoryEntry.FILE) {
                if (!MiscUtilities.isURL(basePath)) {
                    basePath = vfs.constructPath(currentDir, basePath);
                    try {
                        baseEntry = vfs._getDirectoryEntry(null, basePath, null);
                    } catch (IOException ioe) {}
                }
            }

            if (baseEntry == null || baseEntry.type != VFS.DirectoryEntry.FILE) {
                err += "Base file ";
                ++errCount;
            }

            VFS newVFS = VFSManager.getVFSForPath(newPath);
            VFS.DirectoryEntry newEntry = null;

            try {
                newEntry = newVFS._getDirectoryEntry(null, newPath, null);
            } catch (IOException ioe) {}

            if (newEntry == null || newEntry.type != VFS.DirectoryEntry.FILE) {
                if (!MiscUtilities.isURL(newPath)) {
                    newPath = vfs.constructPath(currentDir, newPath);
                    try {
                        newEntry = vfs._getDirectoryEntry(null, newPath, null);
                    } catch (IOException ioe) {}
                }
            }

            if (newEntry == null || newEntry.type != VFS.DirectoryEntry.FILE) {
                err += ((errCount == 1) ?
                    "and new file do not exist." : "New file ");
                ++errCount;
            }

            if (errCount > 0) {
                if (errCount == 1) {
                    err += "does not exist.";
                }
                Macros.error(view, err);
                return;
            }

            DualDiffDialog.this.dispose();
            // here is where JDiff gets activated
            DualDiffDialog.this.view.unsplit();
            jEdit.openFile(DualDiffDialog.this.view, basePath);
            DualDiffDialog.this.view.splitVertically();
            jEdit.openFile(DualDiffDialog.this.view, newPath);
            DualDiff.toggleFor(DualDiffDialog.this.view);
        }
    }


    private class FieldHandler implements ActionListener {
        private HistoryTextField field;


        public FieldHandler(HistoryTextField field) {
            this.field = field;
        }


        public void actionPerformed(ActionEvent ae) {
            String fieldText = this.field.getText();

            String path = null;

            if (fieldText.length() != 0) {
                VFS fieldVFS = VFSManager.getVFSForPath(fieldText);
                VFS.DirectoryEntry fieldEntry = null;

                try {
                    fieldEntry = fieldVFS._getDirectoryEntry(null, fieldText, null);
                } catch (IOException ioe) {}

                if (fieldEntry == null) {
                    if (!MiscUtilities.isURL(fieldText)) {
                        VFS vfs = DualDiffDialog.this.view.getBuffer().getVFS();
                        String currentDir = vfs.getParentOfPath(
                            DualDiffDialog.this.view.getBuffer().getPath()
                        );
                        fieldText = vfs.constructPath(currentDir, fieldText);
                        try {
                            fieldEntry = vfs._getDirectoryEntry(null, fieldText, null);
                        } catch (IOException ioe) {}
                    }
                }

                if (fieldEntry != null) {
                    path = fieldText;
                }
            }

            String[] result = GUIUtilities.showVFSFileDialog(
                DualDiffDialog.this.view, path, 0, false
            );
            if (result != null) {
                this.field.setText(result[0]);
            }
        }
    }


    private class KeyHandler implements KeyListener
    {
        public void keyPressed(KeyEvent ke) {
            if (KeyEvent.VK_ESCAPE == ke.getKeyChar()) {
                ke.consume();
                DualDiffDialog.this.dispose();
            }
        }


        public void keyReleased(KeyEvent ke) {}


        public void keyTyped(KeyEvent ke) {}
    }
}

