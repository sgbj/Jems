import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.concurrent.*;

public class BurningShip extends JComponent {
    BufferedImage image;
    int[] r, g, b;
    
    public BurningShip() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int size = Math.min(d.width, d.height);
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        r = new int[image.getWidth() * image.getHeight()];
        g = new int[image.getWidth() * image.getHeight()];
        b = new int[image.getWidth() * image.getHeight()];
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                for (;;) {
                    plot(r, 50);
                    plot(g, 150);
                    plot(b, 250);
                    render();
                }
            }
        });
    }
    
    private void plot(int[] plot, int depth) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int N = Math.min(d.width, d.height);
        Point2D.Double midpoint = new Point2D.Double(0.45, 0.5);
        double range = 1.7;
        int i, j, k;
        Point2D.Double c = new Point2D.Double(), 
                p0 = new Point2D.Double(),
                p = new Point2D.Double();

        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                p0.x = 0;
                p0.y = 0;
                c.x = midpoint.x + 2 * range * (i / (double) N - 0.5);
                c.y = midpoint.y + 2 * range * (j / (double) N - 0.5);
                for (k = 0; k < depth; k++) {
                    p.x = p0.x * p0.x - p0.y * p0.y - c.x;
                    p.y = 2 * Math.abs(p0.x * p0.y) - c.y;
                    p0 = p;
                    if (p.x * p.x + p.y * p.y > 10) {
                        break;
                    }
                }
                plot[j * N + i] = k;
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
            double ramt = Math.min(1, r[i] / (rmax / 4.5));
            double gamt = Math.min(1, g[i] / (gmax / 4.5));
            double bamt = Math.min(1, b[i] / (bmax / 4.5));
            rgbArray[i] = ((int) (ramt * 0xff) << 16) |
                          ((int) (gamt * 0xff) <<  8) |
                          ((int) (bamt * 0xff)) | 0xff000000;
        }
        
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), 
                rgbArray, 0, image.getWidth());
        
        repaint();
    }
    
    @Override protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, image.getHeight(), image.getWidth(), 0, this);
    }
}
/*
Create the burning ship fractal
Whole ship        -w 1.7 -c 0.45 0.5
First small ship  -w 0.04 -c 1.755 0.03
Second small ship -w .04 -c 1.625 0.035
Tiny ship in tail -w 0.005 -c 1.941 0.004
Another small one -w 0.008 -c 1.861 0.005
 */
