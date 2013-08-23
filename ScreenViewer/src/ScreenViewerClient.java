import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

public class ScreenViewerClient {
    public static void main(String[] args) throws Exception {
        final ServerSocket statusServer = new ServerSocket(12345);
        final ServerSocket screenServer = new ServerSocket(12345);
        final Socket statusSocket = statusServer.accept();
        final Socket screenSocket = screenServer.accept();
        DataInputStream in = new DataInputStream(screenSocket.getInputStream());
        final DataOutputStream out = new DataOutputStream(statusSocket.getOutputStream());
        JFrame f = new JFrame("Screen Viewer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final BufferedImage[] bi = new BufferedImage[1];
        JPanel p = new JPanel() {
            {
                enableEvents(-1);
                setFocusable(true);
            }
            @Override protected void processKeyEvent(KeyEvent e) {
                try {
                    switch (e.getID()) {
                        case KeyEvent.KEY_PRESSED: out.writeInt(1); break;
                        case KeyEvent.KEY_RELEASED: out.writeInt(2); break;
                        default: return;
                    }
                    out.writeInt(e.getKeyCode());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            @Override protected void processMouseMotionEvent(MouseEvent e) {
                try {
                    out.writeInt(3);
                    out.writeInt(e.getX());
                    out.writeInt(e.getY());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            @Override protected void processMouseEvent(MouseEvent e) {
                try {
                    switch (e.getID()) {
                    case MouseEvent.MOUSE_PRESSED: out.writeInt(4); break;
                    case MouseEvent.MOUSE_RELEASED: out.writeInt(5); break;
                    default: return;
                    }
                    switch (e.getButton()) {
                    case MouseEvent.BUTTON1: out.writeInt(MouseEvent.BUTTON1_MASK); break;
                    case MouseEvent.BUTTON2: out.writeInt(MouseEvent.BUTTON2_MASK); break;
                    case MouseEvent.BUTTON3: out.writeInt(MouseEvent.BUTTON3_MASK); break;
                    default: out.writeInt(e.getButton());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            @Override protected void processMouseWheelEvent(MouseWheelEvent e) {
                try {
                    out.writeInt(6);
                    out.writeInt(e.getUnitsToScroll());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bi[0], 0, 0, this);
            }
        };
        f.add(new JScrollPane(p));
        f.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().
                getMaximumWindowBounds().getSize());
        f.setVisible(true);
        for (;;) {
            byte[] b = new byte[in.readInt()];
            in.readFully(b);
            bi[0] = ImageIO.read(new ByteArrayInputStream(b));
            p.setPreferredSize(new Dimension(bi[0].getWidth(), bi[0].getHeight()));
            p.repaint();
        }
    }
}
