/*
 * ImportOptionPane.java
 *
 * Created on September 5, 2008, 8:52 PM
 */

package superabbrevs.gui;

import javax.swing.table.TableModel;
import org.gjt.sp.jedit.AbstractOptionPane;
import superabbrevs.ImportExportOptionPaneController;

/**
 *
 * @author  sune
 */
public class ImportExportOptionPane extends AbstractOptionPane {

    private TableModel tableModel;
    private ImportExportOptionPaneController controller = 
            new ImportExportOptionPaneController();
    
    /** Creates new form ImportOptionPane */
    public ImportExportOptionPane() {
        super("importOptionPane");
        initComponents();
       
        //tableModel = new AbbrevsTableModel(
        //        controller.loadsAbbrevs(getCurrentMode()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();
        exportAbbrevsJTable = new javax.swing.JTable();

        exportAbbrevsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane.setViewportView(exportAbbrevsJTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable exportAbbrevsJTable;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

}
