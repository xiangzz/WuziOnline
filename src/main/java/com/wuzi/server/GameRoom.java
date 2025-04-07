package com.wuzi.server;

import java.util.concurrent.atomic.AtomicInteger;

public class GameRoom {
    private final int roomId;
    private final AtomicInteger playerCount;
    private final GameBoard gameBoard;
    private Player player1;
    private Player player2;
    private boolean isGameStarted;
    private boolean isGameOver;

    public GameRoom(int roomId) {
        this.roomId = roomId;
        this.playerCount = new AtomicInteger(0);
        this.gameBoard = new GameBoard();
        this.isGameStarted = false;
        this.isGameOver = false;
    }

    public synchronized boolean addPlayer(Player player) {
        if (playerCount.get() >= 2) {
            return false;
        }
        if (player1 == null) {
            player1 = player;
        } else {
            player2 = player;
        }
        playerCount.incrementAndGet();
        return true;
    }

    public synchronized void removePlayer(Player player) {
        if (player == player1) {
            player1 = null;
        } else if (player == player2) {
            player2 = null;
        }
        playerCount.decrementAndGet();
    }

    public synchronized boolean startGame() {
        if (playerCount.get() == 2 && !isGameStarted) {
            isGameStarted = true;
            // 随机分配黑白
            boolean player1IsBlack = Math.random() < 0.5;
            player1.setColor(player1IsBlack ? "black" : "white");
            player2.setColor(player1IsBlack ? "white" : "black");
            return true;
        }
        return false;
    }

    public boolean makeMove(int x, int y, String color) {
        if (!isGameStarted || isGameOver) {
            return false;
        }
        return gameBoard.makeMove(x, y, color);
    }

    public boolean checkWin(int x, int y) {
        return gameBoard.checkWin(x, y);
    }

    public String getBoardString() {
        return gameBoard.toString();
    }

    public int getRoomId() {
        return roomId;
    }

    public int getPlayerCount() {
        return playerCount.get();
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
} 