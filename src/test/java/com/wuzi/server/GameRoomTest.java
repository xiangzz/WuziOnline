package com.wuzi.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameRoomTest {
    private GameRoom gameRoom;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        gameRoom = new GameRoom(1);
        // 创建模拟的Player对象
        player1 = new Player("Player1", null, null, null);
        player2 = new Player("Player2", null, null, null);
    }

    @Test
    void testAddPlayer() {
        assertTrue(gameRoom.addPlayer(player1));
        assertEquals(1, gameRoom.getPlayerCount());
        assertTrue(gameRoom.addPlayer(player2));
        assertEquals(2, gameRoom.getPlayerCount());
        assertFalse(gameRoom.addPlayer(new Player("Player3", null, null, null)));
    }

    @Test
    void testRemovePlayer() {
        gameRoom.addPlayer(player1);
        gameRoom.addPlayer(player2);
        assertEquals(2, gameRoom.getPlayerCount());
        
        gameRoom.removePlayer(player1);
        assertEquals(1, gameRoom.getPlayerCount());
        assertNull(gameRoom.getPlayer1());
        
        gameRoom.removePlayer(player2);
        assertEquals(0, gameRoom.getPlayerCount());
        assertNull(gameRoom.getPlayer2());
    }

    @Test
    void testStartGame() {
        gameRoom.addPlayer(player1);
        gameRoom.addPlayer(player2);
        
        // 玩家1准备
        assertFalse(gameRoom.setPlayerReady(player1));
        assertFalse(gameRoom.isGameStarted());
        
        // 玩家2准备，游戏开始
        assertTrue(gameRoom.setPlayerReady(player2));
        assertTrue(gameRoom.isGameStarted());
        assertNotNull(player1.getColor());
        assertNotNull(player2.getColor());
        assertNotEquals(player1.getColor(), player2.getColor());
    }

    @Test
    void testCannotStartGameWithOnePlayer() {
        gameRoom.addPlayer(player1);
        assertFalse(gameRoom.setPlayerReady(player1));
        assertFalse(gameRoom.isGameStarted());
    }

    @Test
    void testMakeMove() {
        gameRoom.addPlayer(player1);
        gameRoom.addPlayer(player2);
        gameRoom.setPlayerReady(player1);
        gameRoom.setPlayerReady(player2);
        
        // Need to make sure we use the correct color for the first move (black)
        String blackPlayerColor = "black";
        String whitePlayerColor = "white";
        
        // Find out which player is black
        Player blackPlayer = player1.getColor().equals("black") ? player1 : player2;
        Player whitePlayer = player1.getColor().equals("black") ? player2 : player1;

        assertTrue(gameRoom.makeMove(0, 0, blackPlayer.getColor()));
        assertFalse(gameRoom.makeMove(0, 0, whitePlayer.getColor())); // 重复落子 (occupied)
        
        // Test turn order
        assertTrue(gameRoom.makeMove(0, 1, whitePlayer.getColor())); // White moves
        assertFalse(gameRoom.makeMove(0, 2, whitePlayer.getColor())); // White moves again (should fail)
        assertTrue(gameRoom.makeMove(0, 2, blackPlayer.getColor())); // Black moves
    }

    @Test
    void testCannotMakeMoveBeforeGameStart() {
        gameRoom.addPlayer(player1);
        gameRoom.addPlayer(player2);
        
        assertFalse(gameRoom.makeMove(0, 0, "black"));
    }

    @Test
    void testGameOver() {
        gameRoom.addPlayer(player1);
        gameRoom.addPlayer(player2);
        gameRoom.setPlayerReady(player1);
        gameRoom.setPlayerReady(player2);
        
        assertFalse(gameRoom.isGameOver());
        gameRoom.setGameOver(true);
        assertTrue(gameRoom.isGameOver());
    }
} 