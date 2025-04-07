package com.wuzi.server;

public class GameBoard {
    private static final int BOARD_SIZE = 15;
    private final String[][] board;

    public GameBoard() {
        this.board = new String[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = "┼";
            }
        }
    }

    public boolean makeMove(int x, int y, String color) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE || !board[x][y].equals("┼")) {
            return false;
        }
        board[x][y] = color.equals("black") ? "●" : "○";
        return true;
    }

    public boolean checkWin(int x, int y) {
        String piece = board[x][y];
        if (piece.equals("┼")) return false;

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

    private int countPieces(int x, int y, int dx, int dy, String piece) {
        int count = 0;
        int currentX = x + dx;
        int currentY = y + dy;

        while (currentX >= 0 && currentX < BOARD_SIZE && 
               currentY >= 0 && currentY < BOARD_SIZE && 
               board[currentX][currentY].equals(piece)) {
            count++;
            currentX += dx;
            currentY += dy;
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 添加列号
        sb.append("   ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            sb.append(String.format("%X ", i));
        }
        sb.append("\n");

        // 添加棋盘
        for (int i = 0; i < BOARD_SIZE; i++) {
            // 添加行号
            sb.append(String.format("%X ", i));
            // 添加棋盘行
            for (int j = 0; j < BOARD_SIZE; j++) {
                sb.append(board[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
} 