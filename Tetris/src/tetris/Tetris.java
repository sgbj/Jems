package tetris;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Presents a version of the game "Tetris."
 */
public class Tetris extends JFrame {

    /** Responsible for all game functionality. */
    private TetrisComponent tetris;

    /** Initializes and displays the tetris component. */
    public Tetris() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tetris = new TetrisComponent();
        add(tetris);
        pack();
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                new Tetris().setVisible(true);
            }
        });
    }

    /**
     * Handles all input, rendering, and logic like the game "Tetris."
     */
    private static class TetrisComponent
            extends JComponent implements Runnable {

        /** Used to randomize the next tetromino. */
        private Random rng = new Random();

        /** 
         * Each possible tetromino is stored as a two-dimensional char array 
         * referenced by a Character which describes its shape.
         */
        private Map<Character, char[][]> tetrominoes = createTetrominoes();

        /** Is the game over? */
        private boolean gameOver;
        /** Is the game paused? */
        private boolean paused = true;

        /** The number of lines cleared by the player. */
        private int lines;

        /** The number of rows the "well" has. */
        private int wellRows = 20;
        /** The number of columns the "well" has. */
        private int wellCols = 10;
        /** The width of one block. */
        private int blockW = 30;
        /** The height of one block. */
        private int blockH = 30;

        /** The "well" is the area in which the tetrominoes are placed. */
        private char[][] well;
        /** The current tetromino. */
        private char[][] current;
        /** The current tetromino's row. */
        private int row;
        /** The current tetromino's column. */
        private int col;

        /** The update delay (represented in nanoseconds). */
        private long delay = TimeUnit.MILLISECONDS.toNanos(250);
        /** The preferred frames per second. */
        private int fps = 60;
        /** The offscreen image being drawn to. */
        private BufferedImage screen = new BufferedImage(
                wellCols * blockW, wellRows * blockH,
                BufferedImage.TYPE_INT_ARGB);

        /** Should the help be displayed? */
        private boolean displayHelp;
        /** Contains information on the controls used to play this game. */
        private String[] help = {
            "          Help (H)  ",
            "Key     Action      ",
            "------------------  ",
            "H       Help        ",
            "P       Play/Pause  ",
            "Left    Move Left   ",
            "Right   Move Right  ",
            "Down    Move Down   ",
            "Space   Rotate      ",
            "Enter   Hard Drop   "
        };

        /** Constructs a new game. */
        public TetrisComponent() {
            // Reset properties to represent a new game.
            newGame();
            
            // A Timer isn't pedagogically correct here, and manipulating
            // Threads (and Timers to an extent) directly is no longer
            // encouraged since the integration of the Executor Framework
            // in Java 1.5.
            Executors.newSingleThreadExecutor().submit(this);

            // This is a special component, so we should override the
            // processKeyEvent method instead of using a KeyListener.
            enableEvents(AWTEvent.KEY_EVENT_MASK);
            setFocusable(true);
            setPreferredSize(new Dimension(300, 600));
        }

        /** Updates and renders the game. */
        public void run() {
            // This game implements time-based updating and rendering.
            // Nanoseconds are used as the "preferred" method of determining
            // elapsed time.
            long lastDraw = 0, lastUpdate = 0;

            try {
                for (;;) {
                    // Compute elapsed time for last update.
                    long now = System.nanoTime();
                    long elapsed = now - lastUpdate;

                    // Update only if our preferred delay has been achieved.
                    if (elapsed >= delay) {
                        update();
                        lastUpdate = now;
                    }

                    // Compute the elapsed time for last redraw.
                    now = System.nanoTime();
                    elapsed = now - lastDraw;
                    long nanosPerSec = TimeUnit.SECONDS.toNanos(1);

                    // Redraw only if elapsed time is acceptable to the
                    // fps rate.
                    if (elapsed >= (nanosPerSec / fps) - elapsed) {
                        draw();
                        repaint();
                        lastDraw = now;
                    }

                    // Preferred approach rather than to yield, or sleep for
                    // zero milliseconds (which is undefined).
                    Thread.sleep(1);
                }
            } catch (InterruptedException ex) {
                System.err.println("Interrupted!");
            }
        }

        /** Updates the game state. */
        private void update() {
            if (gameOver || paused) {
                return;
            }

            // If it's a valid move, the tetromino is lowered one row.
            // Otherwise, it's in place, and a new one is needed.
            if (canMoveTo(row + 1, col)) {
                row++;
                kick();
            } else {
                copyInto(current, well, row, col);

                doLineClears();

                current = randomTetromino();
                row = 0;
                col = (wellCols / 2) - (current[0].length / 2);

                if (!canMoveTo(row, col)) {
                    gameOver = true;
                    return;
                }
            }
        }

        /** Draws graphics to the offscreen image. */
        private void draw() {
            // This provides the trailing effect. It's just making a
            // slightly translucent version of the last draw.
            float[] scales = {1f, 1f, 1f, 0.8f};
            float[] offsets = new float[4];
            RescaleOp rop = new RescaleOp(scales, offsets, null);
            BufferedImage bi = rop.filter(screen, null);

            // Initialize graphics.
            Graphics2D g = screen.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Paints the radient background.
            g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 75),
                    0, screen.getHeight(), Color.BLACK));
            g.fillRect(0, 0, screen.getWidth(), screen.getHeight());

            // Draws the slightly translucent version of the last draw.
            g.drawImage(bi, 0, 0, this);

            // Combines the well and current tetromino into a copy.
            char[][] wellCopy = new char[wellRows][wellCols];
            copyInto(well, wellCopy, 0, 0);
            copyInto(current, wellCopy, row, col);

            // Draws each block in the well.
            for (int r = 0; r < wellRows; r++) {
                for (int c = 0; c < wellCols; c++) {
                    if (Character.isLetter(wellCopy[r][c])) {
                        int x = c * blockW;
                        int y = r * blockH;

                        Color c1 = new Color(100, 100, 255);
                        Color c2 = new Color(20, 20, 180);

                        g.setPaint(new GradientPaint(x, y, c1,
                                x, y + (blockH / 2), c2, true));

                        g.fillRect(x, y, blockW, blockH);
                        g.setColor(c2);
                        g.drawRect(x, y, blockW, blockH);
                    }
                }
            }

            // Adds a slight blur and bloom effect.
            if (row == 0) {
                // Release old resources because we have to recreate the
                // graphics afterward.
                g.dispose();

                float[] blur = {
                    0.15f, 0.2f, 0.15f,
                    0.1f, 0.1f, 0.1f,
                    0.1f, 0.1f, 0.1f
                };

                ConvolveOp cop = new ConvolveOp(new Kernel(3, 3, blur),
                        ConvolveOp.EDGE_NO_OP, null);
                screen = cop.filter(screen, null);

                g = screen.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            }

            // Draws the number of lines the player has made.
            g.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
            int strh = g.getFontMetrics().getHeight();
            drawString(g, "Lines: " + lines, 5, strh);

            // Displays help options.
            if (displayHelp) {
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
                strh = g.getFontMetrics().getHeight();
                int y = strh;
                for (int i = 1; i < help.length; i++) {
                    int strw = g.getFontMetrics().stringWidth(help[i]);
                    drawString(g, help[i], screen.getWidth() - strw, y);
                    y += strh;
                }
            } else {
                int strw = g.getFontMetrics().stringWidth(help[0]);
                drawString(g, help[0], screen.getWidth() - strw, strh);
            }

            // Displays the game state, whether it's paused or over.
            String s = (paused ? "Paused" : (gameOver ? "Game Over" : null));
            if (s != null) {
                g.setFont(new Font(Font.DIALOG, Font.PLAIN, 32));
                int strw = g.getFontMetrics().stringWidth(s);
                strh = g.getFontMetrics().getHeight();
                drawString(g, s, (screen.getWidth() / 2) - (strw / 2),
                        (screen.getHeight() / 2) - (strh / 2));
            }

            // Release the graphics resources.
            g.dispose();
        }

        /** Draws a stroked, gradient string. */
        private void drawString(Graphics2D g, String s, int x, int y) {
            g.setColor(Color.WHITE);

            // A "cheap" way to create a stroke.
            for (int xoff = -1; xoff <= 1; xoff++)
                for (int yoff = -1; yoff <= 1; yoff++)
                    g.drawString(s, x + xoff, y + yoff);

            g.setPaint(new GradientPaint(x, y, new Color(255, 69, 0),
                    x, y + (g.getFontMetrics().getHeight() / 2),
                    new Color(255, 215, 0), true));
            g.drawString(s, x, y);
        }

        /** Paints the offscreen image to this component. */
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(screen, 0, 0, getWidth(), getHeight(), this);
        }

        /** Handles KeyEvents relevant to this component. */
        @Override protected void processKeyEvent(KeyEvent e) {
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return;
            }

            switch (e.getKeyCode()) {

            case KeyEvent.VK_H:             // H - Help.
                displayHelp = !displayHelp;
                break;

            case KeyEvent.VK_P:             // P - New, pause, play.
                if (gameOver)
                    newGame();
                else if (paused)
                    play();
                else
                    pause();
                break;

            case KeyEvent.VK_LEFT:          // Left - Move left.
                if (!gameOver && !paused && canMoveTo(row, col - 1))
                    col--;
                break;

            case KeyEvent.VK_RIGHT:         // Right - Move right.
                if (!gameOver && !paused && canMoveTo(row, col + 1))
                    col++;
                break;

            case KeyEvent.VK_DOWN:          // Down - Move down.
                if (!gameOver && !paused && canMoveTo(row + 1, col))
                    row++;
                break;

            case KeyEvent.VK_SPACE:         // Space - Rotate.
                if (!gameOver && !paused) {
                    current = trim(rotate(current));
                    kick();
                }
                break;

            case KeyEvent.VK_ENTER:         // Enter - Hard drop.
                if (!gameOver && !paused)
                    hardDrop();
                break;
            }
        }

        /** Requests that the game commence. */
        public void play() {
            paused = false;
        }

        /** Requests that the game pause. */
        public void pause() {
            paused = true;
        }

        /** Creates and returns all of the tetrominoes used in "Tetris." */
        private Map<Character, char[][]> createTetrominoes() {
            Map<Character, char[][]> map =
                    new HashMap<Character, char[][]>();

            map.put('t', new char[][] {
                    { ' ', ' ', ' ' },
                    { 't', 't', 't' },
                    { ' ', 't', ' ' }
            });
            map.put('s', new char[][] {
                    { ' ', ' ', ' ' },
                    { ' ', 's', 's' },
                    { 's', 's', ' ' }
            });
            map.put('o', new char[][] {
                    { 'o', 'o' },
                    { 'o', 'o' }
            });
            map.put('l', new char[][] {
                    { ' ', ' ', ' ' },
                    { 'l', 'l', 'l' },
                    { 'l', ' ', ' ' }
            });
            map.put('j', new char[][] {
                    { ' ', ' ', ' ' },
                    { 'j', 'j', 'j' },
                    { ' ', ' ', 'j' }
            });
            map.put('i', new char[][] {
                    { ' ', ' ', ' ', ' ' },
                    { ' ', ' ', ' ', ' ' },
                    { 'i', 'i', 'i', 'i' },
                    { ' ', ' ', ' ', ' ' }
            });

            return map;
        }

        /** Resets this game's properties to represent a new game. */
        private void newGame() {
            well = new char[wellRows][wellCols];
            lines = 0;
            current = randomTetromino();
            row = 0;
            col = (wellCols / 2) - (current[0].length / 2);
            gameOver = false;
        }

        /** Returns a random tetromino.  */
        private char[][] randomTetromino() {
            int i = rng.nextInt(tetrominoes.size());
            char letter = tetrominoes.keySet().toArray(new Character[0])[i];
            char[][] tetromino = tetrominoes.get(letter);
            // A copy is made because arrays are immutable.
            char[][] copy = new char[tetromino.length][tetromino[0].length];
            copyInto(tetromino, copy, 0, 0);
            return trim(tetromino);
        }

        /** Utility method to easily copy tetrominoes and wells. */
        private void copyInto(char[][] src, char[][] dst,
                int rowPos, int colPos) {
            for (int r = 0; r < src.length; r++)
                for (int c = 0; c < src[0].length; c++)
                    if (Character.isLetter(src[r][c]))
                        dst[rowPos + r][colPos + c] = src[r][c];
        }

        /** Trims the tetromino array of all surrounding space. */
        private char[][] trim(char[][] tetromino) {
            Rectangle r = getTetrominoBounds(tetromino);
            char[][] t = new char[r.height][r.width];
            copyInto(tetromino, t, -(r.y + 1), -r.x);
            return t;
        }

        /** Returns the bounds of the occupied space in a tetromino array. */
        private Rectangle getTetrominoBounds(char[][] tetromino) {
            Rectangle bounds = new Rectangle(-1, -1, -1, -1);
            boolean foundY = false;

            for (int r = 0; r < tetromino.length; r++) {
                boolean foundX = false;

                for (int c = 0; c < tetromino[0].length; c++) {
                    if (Character.isLetter(tetromino[r][c])) {
                        foundY = true;
                        foundX = true;

                        if (bounds.x == -1 || c < bounds.x)
                            bounds.x = c;

                        if ((c + 1) > bounds.width)
                            bounds.width = c + 1;
                    }
                }

                if (!foundY)
                    bounds.y = r;

                if (foundX && r > bounds.height)
                    bounds.height = r;
            }

            bounds.height -= bounds.y;
            bounds.width -= bounds.x;

            return bounds;
        }

        /** Returns a rotated copy of the tetromino array. */
        private char[][] rotate(char[][] tetromino) {
            char[][] t = new char[tetromino[0].length][tetromino.length];
            for (int c = 0; c < tetromino[0].length; c++)
                for (int r = tetromino.length - 1; r >= 0; r--)
                    t[c][tetromino.length - 1 - r] = tetromino[r][c];
            return t;
        }

        /** Returns whether the row or column are invalid or occupied. */
        private boolean canMoveTo(int row, int col) {
            if (row < 0 || ((row + current.length) > wellRows))
                return false;
            if (col < 0 || ((col + current[0].length) > wellCols))
                return false;

            // Checks for whether the current tetromino would be on top
            // of any other tetromino in the well.
            for (int r = 0; r < current.length; r++)
                for (int c = 0; c < current[0].length; c++)
                    if (Character.isLetter(well[row + r][col + c])
                            && Character.isLetter(current[r][c]))
                        return false;
            return true;
        }

        /** Moves the tetromino all the way down. */
        private void hardDrop() {
            int r = row;
            while (canMoveTo(++r, col));                        // Empty.
            row = r - 1;
        }

        /**
         * Corrects the current tetromino's position if it is out of bounds
         * or it is on top of another tetromino.
         */
        private void kick() {
            if ((row + current.length) >= wellRows)
                row = wellRows - current.length;
            if ((col + current[0].length) >= wellCols)
                col = wellCols - current[0].length;

            if (!canMoveTo(row, col))
                while (row != 0 && !canMoveTo(--row, col));     // Empty.
        }

        /** Clears and counts all completely lines. */
        private void doLineClears() {
            char[][] wellCopy = new char[wellRows][wellCols];
            copyInto(well, wellCopy, 0, 0);

            for (int r = 0; r < wellRows; r++) {
                boolean cleared = true;

                // Checks the entire row for a cleared line.
                for (int c = 0; c < wellCols; c++)
                    if (!Character.isLetter(well[r][c]))
                        cleared = false;

                if (cleared) {
                    lines++;

                    // Shifts all rows down.
                    System.arraycopy(wellCopy, 0, wellCopy, 1, r);
                    Arrays.fill(wellCopy[0], 0, wellCols, '\u0000');
                }
            }

            well = wellCopy;
        }
    }
}
