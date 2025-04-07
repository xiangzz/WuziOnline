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
        
        assertTrue(gameRoom.startGame());
        assertTrue(gameRoom.isGameStarted());
        assertNotNull(player1.getColor());
        assertNotNull(player2.getColor());
        assertNotEquals(player1.getColor(), player2.getColor());
    }

    @Test
    void testCannotStartGameWithOnePlayer() {
        gameRoom.addPlayer(player1);
        assertFalse(gameRoom.startGame());
        assertFalse(gameRoom.isGameStarted());
    }

    @Test
    void testMakeMove() {
        gameRoom.addPlayer(player1);
        gameRoom.addPlayer(player2);
        gameRoom.startGame();
        
        assertTrue(gameRoom.makeMove(0, 0, player1.getColor()));
        assertFalse(gameRoom.makeMove(0, 0, player2.getColor())); // 重复落子
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
        gameRoom.startGame();
        
        assertFalse(gameRoom.isGameOver());
        gameRoom.setGameOver(true);
        assertTrue(gameRoom.isGameOver());
    }
} 