import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;

public class VoiceChatClient {

    public static void main(String[] args) throws Exception {
        final Socket s = new Socket("127.0.01", 45678);
        final AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
        final TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
        microphone.open(format);
        microphone.start();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                try {
                    for (;;) {
                        byte[] b = new byte[microphone.available()];
                        int ch = microphone.read(b, 0, b.length);
                        if (ch == -1) break;
                        s.getOutputStream().write(b, 0, ch);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
        speaker.open();
        byte[] b = new byte[1024];
        for (int ch; (ch = s.getInputStream().read(b)) != -1;) {
            speaker.write(b, 0, ch);
            speaker.start();
        }
    }
}
