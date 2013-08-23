import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.concurrent.*;

public class Buddhabrot extends JComponent {
    BufferedImage image;
    int[] r, g, b;

    public Buddhabrot() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        r = new int[image.getWidth() * image.getHeight()];
        g = new int[image.getWidth() * image.getHeight()];
        b = new int[image.getWidth() * image.getHeight()];
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                for (;;) {
                    plot(r, 50000);
                    plot(g, 5000);
                    plot(b, 500);
                    render();
                }
            }
        });
    }
    
    private void plot(int[] plot, int depth) {
        java.util.List<Point> pts = new ArrayList<Point>();
        
        for (int i = 0; i < 1000; i++) {
            
            double x0 = random(-2.0, 1.0), y0 = random(-1.5, 1.5), x = 0, y = 0;
            boolean escapes = false;
            
            for (int j = 0; j < depth && !(escapes = x * x + y * y > 4); j++) {
                
                double px = x * x - y * y + x0;
                double py = 2 * x * y + y0;
                x = px;
                y = py;
                
                int ix = (int) (image.getHeight() * (px + 2.0) / 3.0);
                int iy = (int) (image.getWidth() * (py + 1.5) / 3.0);
                
                if (ix >= 0 && iy >= 0 && iy < image.getWidth() && ix < image.getHeight())
                    pts.add(new Point(iy, ix));
            }
            
            if (escapes) 
                for (Point p : pts) 
                    plot[p.y * image.getWidth() + p.x]++;
            
            pts.clear();
        }
    }
    
    private double random(double min, double max) {
        return min + (Math.random() * Math.abs(max - min));
    }
    
    private void render() {
        int rmax = 0; for (int i : r) rmax = Math.max(rmax, i);
        int gmax = 0; for (int i : g) gmax = Math.max(gmax, i);
        int bmax = 0; for (int i : b) bmax = Math.max(bmax, i);
        
        int[] rgbArray = image.getRGB(0, 0, image.getWidth(), image.getHeight(), 
                null, 0, image.getWidth());
        
        for (int i = 0; i < rgbArray.length; i++) {
            double ramt = Math.min(1, r[i] / (rmax / 1.5));
            double gamt = Math.min(1, g[i] / (gmax / 1.5));
            double bamt = Math.min(1, b[i] / (bmax / 1.5));
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
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new Buddhabrot());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}