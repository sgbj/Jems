import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.concurrent.*;

public class Mandelbrot extends JComponent {
    BufferedImage image;
    int[] r, g, b;
    final int R = 1000, G = 500, B = 250;
    double xMin = -2.5, xMax = 1.0, yMin = -1.5, yMax = 0.7;
    boolean clear;
    
    public Mandelbrot() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        r = new int[image.getWidth() * image.getHeight()];
        g = new int[image.getWidth() * image.getHeight()];
        b = new int[image.getWidth() * image.getHeight()];
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                for (;;) {
                    if (clear) {
                        Arrays.fill(r, 0);
                        Arrays.fill(g, 0);
                        Arrays.fill(b, 0);
                        clear = false;
                    }
                    plot();
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
    
    private void plot() {
        for (int i = 0; i < image.getWidth() * image.getHeight(); i++) {
            double ix = scale(i % image.getWidth(), 0, image.getWidth(), xMin, xMax);
            double iy = scale((double) i / image.getHeight(), 0, image.getHeight(), yMin, yMax);

            double x = 0, y = 0;
            for (int it = 0; x * x + y * y < 2 * 2 && it < R; it++) {
                double tx = x * x - y * y + ix;
                y = 2 * x * y + iy;
                x = tx;
                if (it < R) r[i]++;
                if (it < G) g[i]++;
                if (it < B) b[i]++;
            }
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
            double ramt = Math.min(1, r[i] / (rmax / 3.5));
            double gamt = Math.min(1, g[i] / (gmax / 3.5));
            double bamt = Math.min(1, b[i] / (bmax / 3.5));
            rgbArray[i] = ((int) (ramt * 0xff) << 16) |
                          ((int) (gamt * 0xff) <<  8) |
                          ((int) (bamt * 0xff)) | 0xff000000;
        }
        
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), 
                rgbArray, 0, image.getWidth());
        
        repaint();
    }
    
    @Override protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }
}
