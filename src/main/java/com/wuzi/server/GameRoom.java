package com.wuzi.server;

import java.util.concurrent.atomic.AtomicInteger;

public class GameRoom {
    private final int roomId;
    private final AtomicInteger playerCount;
    private final GameBoard gameBoard;
    private Player player1;
    private Player player2;
    private boolean player1Ready;
    private boolean player2Ready;
    private boolean isGameStarted;
    private boolean isGameOver;
    private String currentTurnColor;

    public GameRoom(int roomId) {
        this.roomId = roomId;
        this.playerCount = new AtomicInteger(0);
        this.gameBoard = new GameBoard();
        this.isGameStarted = false;
        this.isGameOver = false;
        this.player1Ready = false;
        this.player2Ready = false;
    }

    public synchronized boolean addPlayer(Player player) {
        if (playerCount.get() >= 2) {
            return false;
        }
        if (player1 == null) {
            player1 = player;
            player1Ready = false;
        } else {
            player2 = player;
            player2Ready = false;
        }
        playerCount.incrementAndGet();
        return true;
    }

    public synchronized void removePlayer(Player player) {
        if (player == player1) {
            player1 = null;
            player1Ready = false;
        } else if (player == player2) {
            player2 = null;
            player2Ready = false;
        }
        playerCount.decrementAndGet();
        isGameStarted = false;
    }

    public synchronized boolean setPlayerReady(Player player) {
        if (player == player1) {
            player1Ready = true;
        } else if (player == player2) {
            player2Ready = true;
        }
        
        if (player1Ready && player2Ready) {
            return startGame();
        }
        return false;
    }

    private synchronized boolean startGame() {
        if (playerCount.get() == 2 && (!isGameStarted || isGameOver)) {
            if (isGameOver) {
                gameBoard.reset();
                isGameOver = false;
            }
            isGameStarted = true;
            player1Ready = false;
            player2Ready = false;
            // 随机分配黑白
            boolean player1IsBlack = Math.random() < 0.5;
            player1.setColor(player1IsBlack ? "black" : "white");
            player2.setColor(player1IsBlack ? "white" : "black");
            currentTurnColor = "black";
            return true;
        }
        return false;
    }

    public boolean makeMove(int x, int y, String color) {
        if (!isGameStarted || isGameOver) {
            return false;
        }
        if (!color.equals(currentTurnColor)) {
            return false;
        }
        if (gameBoard.makeMove(x, y, color)) {
            currentTurnColor = currentTurnColor.equals("black") ? "white" : "black";
            return true;
        }
        return false;
    }

    public String getCurrentTurnColor() {
        return currentTurnColor;
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