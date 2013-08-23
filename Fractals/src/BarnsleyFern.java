import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.concurrent.*;
import java.util.*;

public class BarnsleyFern extends JComponent {
    BufferedImage image;
    int[] r, g, b;
    double xMin = -2.1818, xMax = 2.6556, yMin = 9.95851, yMax = 0;
    boolean clear;

    public BarnsleyFern() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        r = new int[image.getWidth() * image.getHeight()];
        g = new int[image.getWidth() * image.getHeight()];
        b = new int[image.getWidth() * image.getHeight()];
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                for(;;) {
                    if (clear) {
                        Arrays.fill(r, 0);
                        Arrays.fill(g, 0);
                        Arrays.fill(b, 0);
                        clear = false;
                    }
                    plot(r, 100);
                    plot(g, 50000);
                    plot(b, 100);
                    render();
                }
            }
        });
        setFocusable(true);
        enableEvents(-1);
    }
    
    @Override protected void processMouseEvent(MouseEvent e) {
        if (e.getID() != MouseEvent.MOUSE_RELEASED) return;
        
        double x = scale(e.getX(), 0, getWidth(), 0, image.getWidth());
        double y = scale(e.getY(), 0, getHeight(), 0, image.getWidth());

        double xmin = scale(x - image.getWidth() * 0.2, 0, image.getWidth(), xMin, xMax);
        double xmax = scale(x + image.getWidth() * 0.2, 0, image.getWidth(), xMin, xMax);
        double ymin = scale(y - image.getHeight() * 0.2, 0, image.getHeight(), yMin, yMax);
        double ymax = scale(y + image.getHeight() * 0.2, 0, image.getHeight(), yMin, yMax);

        xMin = xmin;
        xMax = xmax;
        yMin = ymin;
        yMax = ymax;
        
        clear = true;
    }
    
    private void plot(int[] plot, int depth) {
        double x = 0, y = 0;
        for (int i = 0; i < depth; i++) {
            
            if (Math.random() < 0.85){
                x = 0.85 * x + 0.04 * y;
                y = -0.04 * x + 0.85 * y + 1.6;
            }
            if (Math.random() < 0.07) {
                x = 0.2 * x - 0.26 * y;
                y = 0.23 * x + 0.22 * y + 1.6;
            }
            if (Math.random() < 0.07) {
                x = -0.15 * x + 0.28 * y;
                y = 0.26 * x + 0.24 * y + 0.44;
            }
            if (Math.random() < 0.01) {
                x = 0;
                y = 0.16 * y;
            } 
            
            if (x < xMin || x > xMax || y > yMin || y < yMax)
                continue;

            int px = (int) scale(x, xMin, xMax, 0, image.getWidth());
            int py = (int) scale(y, yMin, yMax, 0, image.getHeight());
            int idx = py * image.getWidth() + px;
            if (idx < 0 || idx >= image.getWidth() * image.getHeight()) 
                continue;
            plot[idx]++;
        }
    }
    
    private double scale(double x, double min, double max, 
                         double a, double b) {
        return (b - a) * (x - min) / (max - min) + a;
    }
    
    private void render() {
        int rmax = 0; for (int i : r) rmax = Math.max(rmax, i);
        int gmax = 0; for (int i : g) gmax = Math.max(gmax, i);
        int bmax = 0; for (int i : b) bmax = Math.max(bmax, i);
        
        int[] rgbArray = image.getRGB(0, 0, image.getWidth(), image.getHeight(), 
                null, 0, image.getWidth());
        
        for (int i = 0; i < rgbArray.length; i++) {
            double ramt = Math.min(1, r[i] / (rmax / 4.0));
            double gamt = Math.min(1, g[i] / (gmax / 4.0));
            double bamt = Math.min(1, b[i] / (bmax / 4.0));
            rgbArray[i] = ((int) (ramt * 0xff) << 16) |
                          ((int) (gamt * 0xff) <<  8) |
                          ((int) (bamt * 0xff)) | 0xff000000;
        }
        
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), 
                rgbArray, 0, image.getWidth());
        
        repaint();
    }
    
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }
}
