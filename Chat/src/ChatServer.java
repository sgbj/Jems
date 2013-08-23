import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    public static void main(String[] args) throws Exception {
        final List<Socket> clients = new CopyOnWriteArrayList<Socket>();
        ServerSocket ss = new ServerSocket(45678);
        for (;;) {
            final Socket s = ss.accept();
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override public void run() {
                    try {
                        clients.add(s);
                        byte[] b = new byte[1024];
                        for (int ch; (ch = s.getInputStream().read(b)) != -1;) {
                            for (Socket c : clients) {
                                if (c != s) {
                                    try {
                                        c.getOutputStream().write(b, 0, ch);
                                    } catch (IOException ex) {
                                        clients.remove(c);
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        clients.remove(s);
                    }
                }
            });
        }
    }
}
