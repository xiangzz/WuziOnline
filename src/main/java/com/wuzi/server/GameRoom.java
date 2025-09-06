package com.wuzi.server;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameRoom {
    private final int roomId;
    private final String roomName;
    private final AtomicInteger playerCount;
    private final GameBoard gameBoard;
    private Player player1;
    private Player player2;
    private boolean isGameStarted;
    private boolean isGameOver;
    private final ReentrantLock roomLock = new ReentrantLock();
    private static final long DISCONNECT_TIMEOUT_MS = 60000; // 60秒超时
    private final ScheduledExecutorService disconnectChecker = Executors.newSingleThreadScheduledExecutor();

    public GameRoom(int roomId) {
        this(roomId, "房间" + roomId);
        
        // 启动断线检测定时任务
        startDisconnectChecker();
    }

    public GameRoom(int roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.playerCount = new AtomicInteger(0);
        this.gameBoard = new GameBoard();
        this.isGameStarted = false;
        this.isGameOver = false;
    }

    public boolean addPlayer(Player player) {
        roomLock.lock();
        try {
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
        } finally {
            roomLock.unlock();
        }
    }

    public void removePlayer(Player player) {
        roomLock.lock();
        try {
            if (player1 == player) {
                player1 = null;
            } else if (player2 == player) {
                player2 = null;
            }
            playerCount.decrementAndGet();
            
            // 如果游戏正在进行中，结束游戏
            if (isGameStarted && !isGameOver) {
                isGameOver = true;
                Player winner = (player1 != null) ? player1 : player2;
                if (winner != null) {
                    winner.sendMessage("对手离开，你获胜了！");
                }
            }
        } finally {
            roomLock.unlock();
        }
    }

    public boolean startGame() {
        roomLock.lock();
        try {
            if (playerCount.get() == 2 && !isGameStarted) {
                isGameStarted = true;
                isGameOver = false;
                gameBoard.reset();
                currentPlayer = player1; // 玩家1先手
                
                player1.sendMessage("游戏开始！你是黑子，先手。");
                player2.sendMessage("游戏开始！你是白子，后手。");
                return true;
            }
            return false;
        } finally {
            roomLock.unlock();
        }
    }
    
    // 断线检测和重连相关方法
    private void startDisconnectChecker() {
        disconnectChecker.scheduleAtFixedRate(() -> {
            roomLock.lock();
            try {
                checkPlayerConnections();
            } finally {
                roomLock.unlock();
            }
        }, 10, 10, TimeUnit.SECONDS); // 每10秒检查一次
    }
    
    private void checkPlayerConnections() {
        if (player1 != null && !player1.isConnected()) {
            if (player1.isTimeout(DISCONNECT_TIMEOUT_MS)) {
                handlePlayerDisconnect(player1);
            } else {
                // 通知对手玩家断线
                if (player2 != null && player2.isConnected()) {
                    player2.sendMessage(player1.getName() + " 断线中，等待重连...");
                }
            }
        }
        
        if (player2 != null && !player2.isConnected()) {
            if (player2.isTimeout(DISCONNECT_TIMEOUT_MS)) {
                handlePlayerDisconnect(player2);
            } else {
                // 通知对手玩家断线
                if (player1 != null && player1.isConnected()) {
                    player1.sendMessage(player2.getName() + " 断线中，等待重连...");
                }
            }
        }
    }
    
    private void handlePlayerDisconnect(Player player) {
        if (isGameStarted && !isGameOver) {
            isGameOver = true;
            Player opponent = (player == player1) ? player2 : player1;
            if (opponent != null && opponent.isConnected()) {
                opponent.sendMessage(player.getName() + " 超时断线，你获胜了！");
            }
        }
        removePlayer(player);
    }
    
    public boolean reconnectPlayer(String playerId, Player newPlayer) {
        roomLock.lock();
        try {
            if (player1 != null && player1.getPlayerId().equals(playerId)) {
                // 重连player1
                if (player1.reconnect(newPlayer.socket, newPlayer.reader, newPlayer.writer)) {
                    if (player2 != null && player2.isConnected()) {
                        player2.sendMessage(player1.getName() + " 重新连接成功");
                    }
                    player1.sendMessage("重连成功，欢迎回来！");
                    return true;
                }
            } else if (player2 != null && player2.getPlayerId().equals(playerId)) {
                // 重连player2
                if (player2.reconnect(newPlayer.socket, newPlayer.reader, newPlayer.writer)) {
                    if (player1 != null && player1.isConnected()) {
                        player1.sendMessage(player2.getName() + " 重新连接成功");
                    }
                    player2.sendMessage("重连成功，欢迎回来！");
                    return true;
                }
            }
            return false;
        } finally {
            roomLock.unlock();
        }
    }
    
    public void shutdown() {
        if (disconnectChecker != null) {
            disconnectChecker.shutdown();
        }
    }
}

    public boolean makeMove(Player player, int x, int y) {
        roomLock.lock();
        try {
            if (!isGameStarted || isGameOver) {
                return false;
            }
            
            if (currentPlayer != player) {
                player.sendMessage("不是你的回合！");
                return false;
            }
            
            if (gameBoard.makeMove(x, y, player == player1 ? 1 : 2)) {
                // 广播落子信息
                String moveMessage = String.format("%s 在 (%d,%d) 落子", player.getName(), x, y);
                player1.sendMessage(moveMessage);
                player2.sendMessage(moveMessage);
                
                // 检查胜利条件
                if (gameBoard.checkWin(x, y)) {
                    isGameOver = true;
                    player.sendMessage("\n🎉 恭喜你获胜！🎉");
                    player.sendMessage("感谢你的精彩对局！");
                    Player opponent = (player == player1) ? player2 : player1;
                    opponent.sendMessage("\n😔 很遗憾，你败了！");
                    opponent.sendMessage("不要气馁，再来一局吧！");
                    
                    // 向双方发送游戏结束提示
                    player1.sendMessage("\n游戏结束！输入 'start' 开始新游戏，或 'leave' 离开房间");
                    player2.sendMessage("\n游戏结束！输入 'start' 开始新游戏，或 'leave' 离开房间");
                } else {
                    // 切换玩家
                    currentPlayer = (currentPlayer == player1) ? player2 : player1;
                }
                return true;
            }
            return false;
        } finally {
            roomLock.unlock();
        }
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

    public String getRoomName() {
        return roomName;
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