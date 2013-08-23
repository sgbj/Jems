import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        final Socket s = new Socket("127.0.0.1", 45678);
        final String name = JOptionPane.showInputDialog("Name");
        JFrame f = new JFrame("Chat Client");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea text = new JTextArea(15, 30);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        f.add(new JScrollPane(text));
        final JTextField input = new JTextField();
        input.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent ae) {
                try {
                    String str = name + ": " + input.getText() + "\n";
                    s.getOutputStream().write(str.getBytes("UTF-8"));
                    text.append(str);
                    input.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        f.add(input, BorderLayout.NORTH);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        byte[] b = new byte[1024];
        for (int ch; (ch = s.getInputStream().read(b)) != -1;) {
            text.append(new String(b, 0, ch, "UTF-8"));
        }
    }
}
