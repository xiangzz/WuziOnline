package com.wuzi.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {
    private GameBoard gameBoard;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard();
    }

    @Test
    void testInitialBoard() {
        String boardString = gameBoard.toString();
        assertTrue(boardString.contains("┼"));
        assertTrue(boardString.contains("0 1 2 3 4 5 6 7 8 9 A B C D E"));
    }

    @Test
    void testValidMove() {
        assertTrue(gameBoard.makeMove(0, 0, "black"));
        assertTrue(gameBoard.makeMove(1, 1, "white"));
    }

    @Test
    void testInvalidMove() {
        assertFalse(gameBoard.makeMove(-1, 0, "black"));
        assertFalse(gameBoard.makeMove(0, 15, "white"));
        assertTrue(gameBoard.makeMove(7, 7, "black"));
        assertFalse(gameBoard.makeMove(7, 7, "white")); // 重复落子
    }

    @Test
    void testHorizontalWin() {
        // 黑子横向连成5子
        gameBoard.makeMove(0, 0, "black");
        gameBoard.makeMove(0, 1, "black");
        gameBoard.makeMove(0, 2, "black");
        gameBoard.makeMove(0, 3, "black");
        gameBoard.makeMove(0, 4, "black");
        assertTrue(gameBoard.checkWin(0, 0));
    }

    @Test
    void testVerticalWin() {
        // 白子纵向连成5子
        gameBoard.makeMove(0, 0, "white");
        gameBoard.makeMove(1, 0, "white");
        gameBoard.makeMove(2, 0, "white");
        gameBoard.makeMove(3, 0, "white");
        gameBoard.makeMove(4, 0, "white");
        assertTrue(gameBoard.checkWin(0, 0));
    }

    @Test
    void testDiagonalWin() {
        // 黑子斜向连成5子
        gameBoard.makeMove(0, 0, "black");
        gameBoard.makeMove(1, 1, "black");
        gameBoard.makeMove(2, 2, "black");
        gameBoard.makeMove(3, 3, "black");
        gameBoard.makeMove(4, 4, "black");
        assertTrue(gameBoard.checkWin(0, 0));
    }
} 