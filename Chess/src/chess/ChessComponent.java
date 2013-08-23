package chess;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

public class ChessComponent extends JComponent {

    private Chess chess = new Chess();
    
    private int mouseX, mouseY;
    private int movingRow = -1, movingCol = -1;
    private int offsetX, offsetY;

    public ChessComponent() {
        setPreferredSize(new Dimension(360, 360));
        enableEvents(-1);
    }

    @Override protected void processMouseEvent(MouseEvent e) {
        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
            mouseX = e.getX();
            mouseY = e.getY();
            int r = mouseY / 45;
            int c = mouseX / 45;
            if (chess.pieceAt(r, c) != Chess.NO) {
                movingRow = r;
                movingCol = c;
            }
            offsetX = mouseX - (c * 45);
            offsetY = mouseY - (r * 45);
            break;
        case MouseEvent.MOUSE_RELEASED:
            chess.movePiece(movingRow, movingCol, e.getY() / 45, e.getX() / 45);
            movingRow = -1;
            movingCol = -1;
            break;
        }
        repaint();
    }

    @Override protected void processMouseMotionEvent(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean light = true;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                // draw board
                String square = (light ? "light" : "dark") + "_square";
                BufferedImage bi = getImage(square);
                g.drawImage(bi, c * bi.getWidth(), r * bi.getHeight(), this);
                light = !light;

                // draw piece
                char p = chess.pieceAt(r, c);
                if (p == Chess.NO || (r == movingRow && c == movingCol)) continue;
                bi = getImage(p);
                g.drawImage(bi, c * bi.getWidth(), r * bi.getHeight(), this);
            }
            light = !light;
        }

        // draw moving piece
        if (movingRow != -1 && movingCol != -1) {
            BufferedImage bi = getImage(chess.pieceAt(movingRow, movingCol));
            g.drawImage(bi, mouseX - offsetX, mouseY - offsetY, this);
        }
    }

    private static Map<String, BufferedImage> images =
            new HashMap<String, BufferedImage>();

    private static BufferedImage getImage(String key) {
        if (!images.containsKey(key)) {
            try {
                String name = "images/" + key + ".png";
                URL url = ChessComponent.class.getResource(name);
                images.put(key, ImageIO.read(url));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return images.get(key);
    }

    private static BufferedImage getImage(char p) {
        String key = Chess.isWhite(p) ? "white_" : "black_";
        if (Chess.isKing(p)) key += "king";
        if (Chess.isQueen(p)) key += "queen";
        if (Chess.isRook(p)) key += "rook";
        if (Chess.isBishop(p)) key += "bishop";
        if (Chess.isKnight(p)) key += "knight";
        if (Chess.isPawn(p)) key += "pawn";
        return getImage(key);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Chess");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.getContentPane().add(new ChessComponent());
        f.pack();
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
