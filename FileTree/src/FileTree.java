import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

public class FileTree extends JTree {

    public FileTree(File[] roots) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        for (File f : roots) {
            root.add(new DefaultMutableTreeNode(f));
        }

        setModel(new DefaultTreeModel(root));
        setRootVisible(false);

        setCellRenderer(new DefaultTreeCellRenderer() {

            @Override public Component getTreeCellRendererComponent(
                    JTree tree, Object value, 
                    boolean selected, boolean expanded, 
                    boolean leaf, int row, boolean hasFocus) {

                JLabel label = (JLabel) super.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
                File f = (File) n.getUserObject();

                label.setText(FileSystemView.getFileSystemView().
                        getSystemDisplayName(f));
                label.setIcon(FileSystemView.getFileSystemView().
                        getSystemIcon(f));

                return label;
            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(final TreeSelectionEvent e) {
                new SwingWorker() {

                    @Override protected Object doInBackground() {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode)
                                e.getPath().getLastPathComponent();

                        n.removeAllChildren();

                        for (File f : ((File) n.getUserObject()).listFiles()) {
                            n.add(new DefaultMutableTreeNode(f));
                        }

                        return null;
                    }
                }.execute();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame("FileTree Demo");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                final FileTree files = new FileTree(FileSystemView.
                        getFileSystemView().getRoots());
                f.add(new JScrollPane(files));

                JButton open = new JButton("Open");
                open.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode)
                                files.getSelectionPath().getLastPathComponent();
                        File f = (File) n.getUserObject();
                        try {
                            Desktop.getDesktop().open(f);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                f.add(open, BorderLayout.SOUTH);

                f.setSize(300, 300);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }
}
