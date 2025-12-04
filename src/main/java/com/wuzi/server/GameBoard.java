package com.wuzi.server;

public class GameBoard {
    private static final int BOARD_SIZE = 15;
    // 0: Empty, 1: Black, 2: White
    private final int[][] board;
    private int lastMoveX = -1;
    private int lastMoveY = -1;

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String GRID_COLOR = "\u001B[33m"; // Yellow for grid
    private static final String BLACK_PIECE_COLOR = "\u001B[36m"; // Cyan for Black
    private static final String WHITE_PIECE_COLOR = "\u001B[37m"; // White for White
    private static final String LAST_MOVE_BG = "\u001B[41m"; // Red background for last move

    public GameBoard() {
        this.board = new int[BOARD_SIZE][BOARD_SIZE];
        // int array defaults to 0, so no loop needed strictly, but explicit is fine
    }

    public void reset() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = 0;
            }
        }
        lastMoveX = -1;
        lastMoveY = -1;
    }

    public boolean makeMove(int x, int y, String color) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE || board[x][y] != 0) {
            return false;
        }
        board[x][y] = color.equals("black") ? 1 : 2;
        lastMoveX = x;
        lastMoveY = y;
        return true;
    }

    public boolean checkWin(int x, int y) {
        int piece = board[x][y];
        if (piece == 0) return false;

        // 检查八个方向
        int[][] directions = {
            {1, 0}, {1, 1}, {0, 1}, {-1, 1},
            {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
        };

        for (int i = 0; i < 4; i++) {
            int count = 1;
            // 正向检查
            count += countPieces(x, y, directions[i][0], directions[i][1], piece);
            // 反向检查
            count += countPieces(x, y, directions[i + 4][0], directions[i + 4][1], piece);
            if (count >= 5) return true;
        }
        return false;
    }

    private int countPieces(int x, int y, int dx, int dy, int piece) {
        int count = 0;
        int currentX = x + dx;
        int currentY = y + dy;

        while (currentX >= 0 && currentX < BOARD_SIZE && 
               currentY >= 0 && currentY < BOARD_SIZE && 
               board[currentX][currentY] == piece) {
            count++;
            currentX += dx;
            currentY += dy;
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Column Headers
        sb.append("  ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            sb.append(String.format("%X ", i));
        }
        sb.append("\n");

        for (int i = 0; i < BOARD_SIZE; i++) {
            // Row Header
            sb.append(String.format("%X ", i));
            
            for (int j = 0; j < BOARD_SIZE; j++) {
                String cell;
                int state = board[i][j];
                
                if (state == 0) {
                    // Grid character based on position
                    String gridChar;
                    if (i == 0) {
                        if (j == 0) gridChar = "┌─";
                        else if (j == BOARD_SIZE - 1) gridChar = "┐ ";
                        else gridChar = "┬─";
                    } else if (i == BOARD_SIZE - 1) {
                        if (j == 0) gridChar = "└─";
                        else if (j == BOARD_SIZE - 1) gridChar = "┘ ";
                        else gridChar = "┴─";
                    } else {
                        if (j == 0) gridChar = "├─";
                        else if (j == BOARD_SIZE - 1) gridChar = "┤ ";
                        else gridChar = "┼─";
                    }
                    // Apply Grid Color
                    cell = GRID_COLOR + gridChar + RESET;
                } else {
                    // Piece
                    String symbol = (state == 1) ? "●" : "○";
                    String color = (state == 1) ? BLACK_PIECE_COLOR : WHITE_PIECE_COLOR;
                    
                    // Highlight last move
                    if (i == lastMoveX && j == lastMoveY) {
                        // For last move, maybe add a background or just make it bold/different
                        // Let's use Red Background for the cell
                        cell = LAST_MOVE_BG + color + symbol + RESET + " "; 
                        // Note: The grid logic uses "─" or " " for spacing. 
                        // To align correctly:
                        // Grid chars are like "┌─". Takes 2 chars visual space usually?
                        // "┌" is 1 char. "─" is 1 char. Total 2.
                        // "●" is 1 char (wide). Space is 1 char. Total 2.
                        // Let's stick to "Char" + "Space" or "Char" + "Dash".
                        // My grid logic above: "┌─".
                        // So pieces should be "● " (Piece + Space).
                        
                        // Re-evaluating alignment:
                        // Headers are "0 1 2 ...". 2 chars per col.
                        // Grid: "┌─", "┬─". 2 chars.
                        // Last col: "┐ ". 2 chars.
                        // Piece: "● ". 2 chars.
                        
                        // Fix for Last Move BG:
                        // Apply BG only to the symbol
                        cell = LAST_MOVE_BG + color + symbol + RESET + " ";
                        
                        // Actually, resetting after symbol means space is not colored.
                        // That's fine.
                    } else {
                        cell = color + symbol + RESET + ((j == BOARD_SIZE - 1) ? " " : "─");
                        // Wait, if there is a piece, the line to the right should be drawn?
                        // If board[i][j] is a piece, we lose the grid line information.
                        // Standard text go boards usually just replace the intersection with the piece.
                        // But we need to maintain the horizontal line to the next intersection if we want it to look connected.
                        // However, "●─" looks weird. Usually just "● " is enough if spacing is tight.
                        // But my grid uses "─" for empty cells.
                        // If I use " " for pieces, the horizontal line breaks.
                        // Let's try "─" if the NEXT cell is also connected?
                        // Too complex. Let's just use "─" always after a piece unless it's the last column.
                        
                        cell = color + symbol + RESET + ((j == BOARD_SIZE - 1) ? " " : GRID_COLOR + "─" + RESET);
                    }
                }
                sb.append(cell);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
