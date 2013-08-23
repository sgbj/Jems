
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Fractals {
    public static void main(String[] args) {
        final JFrame w = new JFrame();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setUndecorated(true);
        final KeyAdapter key = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                    case KeyEvent.VK_1:
                        w.setContentPane(new Buddhabrot());
                        break;
                    case KeyEvent.VK_2:
                        w.setContentPane(new Mandelbrot());
                        break;
                    case KeyEvent.VK_3:
                        w.setContentPane(new BarnsleyFern());
                        break;
                    case KeyEvent.VK_4:
                        w.setContentPane(new BurningShip());
                        break;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                    case KeyEvent.VK_1:
                    case KeyEvent.VK_2:
                    case KeyEvent.VK_3:
                    case KeyEvent.VK_4:
w.getContentPane().setBackground(Color.black);
                w.getContentPane().setFocusable(true);
                w.getContentPane().addKeyListener(this);
                w.getContentPane().requestFocus();
                w.getContentPane().requestFocusInWindow();
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().setFullScreenWindow(null);
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().setFullScreenWindow(w);
                }
                
            }
        };
        w.getContentPane().setBackground(Color.black);
        w.getContentPane().setFocusable(true);
        w.getContentPane().addKeyListener(key);
        w.getContentPane().requestFocus();
        w.getContentPane().requestFocusInWindow();
        GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().setFullScreenWindow(w);
    }
}
