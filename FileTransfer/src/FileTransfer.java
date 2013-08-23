import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.List;

public class FileTransfer extends Thread {
    private static final int PORT = 12345;
    private final String host;
    private final File file;
    
    public FileTransfer(String host, File file) {
        this.host = host;
        this.file = file;
    }

    @Override public void run() {
        try {
            Socket s = new Socket(host, PORT);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF(file.getName());
            out.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[(int) file.length()];
            fis.read(b);
            out.write(b);
            fis.close();
            s.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void listen() {
        try {
            ServerSocket ss = new ServerSocket(PORT);
            for (;;) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(s.getInputStream());
                String name = in.readUTF();
                long length = in.readLong();
                FileOutputStream fos = new FileOutputStream(name);
                byte[] b = new byte[(int) length];
                in.readFully(b);
                fos.write(b);
                fos.close();
                s.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Transfer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.add(new JLabel("Host:"));
        final JTextField hostField = new JTextField(10);
        p.add(hostField);
        frame.add(p, BorderLayout.NORTH);
        JPanel dropPanel = new JPanel();
        DropTarget dt = new DropTarget(dropPanel, new DropTargetAdapter() {
            @Override public void drop(DropTargetDropEvent dtde) {
                if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.rejectDrop();
                    return;
                }
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    List<File> files = (List<File>) dtde.getTransferable().
                            getTransferData(DataFlavor.javaFileListFlavor);
                    dtde.dropComplete(true);
                    for (File f : files) {
                        new FileTransfer(hostField.getText(), f).start();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        dropPanel.setDropTarget(dt);
        dropPanel.setPreferredSize(new Dimension(300, 300));
        frame.add(dropPanel);
        frame.pack();
        frame.setVisible(true);
        listen();
    }
}
