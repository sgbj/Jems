package chess;

public class Chess {

    public static final char 
        WK = '\u2654', WQ = '\u2655', WR = '\u2656', 
        WB = '\u2657', WN = '\u2658', WP = '\u2659',
        BK = '\u265A', BQ = '\u265B', BR = '\u265C', 
        BB = '\u265D', BN = '\u265E', BP = '\u265F',
        NO = '\u0000';

    public static boolean isWhite(char p)  { return p < BK;             }
    public static boolean isBlack(char p)  { return p > WP;             }
    public static boolean isKing(char p)   { return p == WK || p == BK; }
    public static boolean isQueen(char p)  { return p == WQ || p == BQ; }
    public static boolean isRook(char p)   { return p == WR || p == BR; }
    public static boolean isBishop(char p) { return p == WB || p == BB; }
    public static boolean isKnight(char p) { return p == WN || p == BN; }
    public static boolean isPawn(char p)   { return p == WP || p == BP; }
    
    private final char[][] board = {
        {BR, BN, BB, BQ, BK, BB, BN, BR}, 
        {BP, BP, BP, BP, BP, BP, BP, BP}, 
        {NO, NO, NO, NO, NO, NO, NO, NO},
        {NO, NO, NO, NO, NO, NO, NO, NO},
        {NO, NO, NO, NO, NO, NO, NO, NO},
        {NO, NO, NO, NO, NO, NO, NO, NO},
        {WP, WP, WP, WP, WP, WP, WP, WP}, 
        {WR, WN, WB, WQ, WK, WB, WN, WR}
    };

    public char pieceAt(int r, int c) {
        // out of bounds
        if (r < 0 || r >= 8 || c < 0 || c >= 8) return NO;
        return board[r][c];
    }

    public boolean movePiece(int r1, int c1, int r2, int c2) {
        // out of bounds
        if (r1 < 0 || r1 >= 8 || c1 < 0 || c1 >= 8 ||
            r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8) return false;

        // you didn't move
        if (r1 == r2 && c1 == c2) return false;

        // you're so dumb...
        char p1 = board[r1][c1];
        if (p1 == NO) return false;

        // can't take your own pieces fool!!
        char p2 = board[r2][c2];
        if (p2 != NO && isWhite(p1) == isWhite(p2)) return false;

        // validate moves
        int r = Math.abs(r1 - r2);
        int c = Math.abs(c1 - c2);
        if (isKing(p1) && (r > 1 || c > 1)) return false;
        if (isQueen(p1) && (r != c && r != 0 && c != 0)) return false;
        if (isRook(p1) && (r > 0 && c > 0)) return false;
        if (isBishop(p1) && (r != c)) return false;
        if (isKnight(p1) && !((r == 1 && c == 2) || (r == 2 && c == 1))) return false;
        if (isPawn(p1) && (c1 != c2 || r2 - r1 != (isWhite(p1) ? -1 : 1))) return false;

        // only knights can jump over other pieces
        if (!isKnight(p1)) {
            r = r1;
            c = c1;
            while (r != r2 || c != c2) {
                if (r < r2) r++;
                else if (r > r2) r--;
                if (c < c2) c++;
                else if (c > c2) c--;
                if ((r != r2 || c != c2) && board[r][c] != NO) return false;
            }
        }

        // success
        board[r2][c2] = board[r1][c1];
        board[r1][c1] = NO;
        return true;
    }
}
