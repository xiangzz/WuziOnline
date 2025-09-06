package com.wuzi.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自动匹配系统
 * 负责管理匹配队列，自动为玩家匹配对手并创建游戏房间
 */
public class MatchMaker {
    private final ConcurrentLinkedQueue<Player> matchQueue;
    private final RoomManager roomManager;
    private final ReentrantLock matchLock;
    private final ScheduledExecutorService scheduler;
    
    public MatchMaker(RoomManager roomManager) {
        this.matchQueue = new ConcurrentLinkedQueue<>();
        this.roomManager = roomManager;
        this.matchLock = new ReentrantLock();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // 启动匹配处理线程，每秒检查一次匹配队列
        startMatchProcessing();
    }
    
    /**
     * 将玩家加入匹配队列
     * 
     * @param player 要匹配的玩家
     * @return true如果成功加入队列，false如果玩家已在队列中
     */
    public boolean joinMatchQueue(Player player) {
        if (player == null) {
            return false;
        }
        
        // 检查玩家是否已在队列中
        if (matchQueue.contains(player)) {
            return false;
        }
        
        // 检查玩家是否已在房间中
        if (player.getCurrentRoom() != null) {
            return false;
        }
        
        matchQueue.offer(player);
        player.sendMessage("已加入匹配队列，当前队列中有 " + matchQueue.size() + " 名玩家等待匹配...");
        
        return true;
    }
    
    /**
     * 将玩家从匹配队列中移除
     * 
     * @param player 要移除的玩家
     * @return true如果成功移除，false如果玩家不在队列中
     */
    public boolean leaveMatchQueue(Player player) {
        if (player == null) {
            return false;
        }
        
        boolean removed = matchQueue.remove(player);
        if (removed) {
            player.sendMessage("已退出匹配队列");
        }
        
        return removed;
    }
    
    /**
     * 获取当前匹配队列中的玩家数量
     * 
     * @return 队列中的玩家数量
     */
    public int getQueueSize() {
        return matchQueue.size();
    }
    
    /**
     * 启动匹配处理线程
     */
    private void startMatchProcessing() {
        scheduler.scheduleAtFixedRate(this::processMatches, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 处理匹配逻辑
     * 每次从队列中取出两个玩家进行匹配
     */
    private void processMatches() {
        matchLock.lock();
        try {
            while (matchQueue.size() >= 2) {
                Player player1 = matchQueue.poll();
                Player player2 = matchQueue.poll();
                
                if (player1 != null && player2 != null) {
                    // 检查玩家是否仍然有效（连接未断开）
                    if (isPlayerValid(player1) && isPlayerValid(player2)) {
                        createMatchedGame(player1, player2);
                    } else {
                        // 如果有玩家无效，将有效的玩家重新加入队列
                        if (isPlayerValid(player1)) {
                            matchQueue.offer(player1);
                        }
                        if (isPlayerValid(player2)) {
                            matchQueue.offer(player2);
                        }
                    }
                }
            }
        } finally {
            matchLock.unlock();
        }
    }
    
    /**
     * 检查玩家是否仍然有效
     * 
     * @param player 要检查的玩家
     * @return true如果玩家有效，false如果玩家连接已断开或已在房间中
     */
    private boolean isPlayerValid(Player player) {
        return player != null && 
               !player.getSocket().isClosed() && 
               player.getCurrentRoom() == null;
    }
    
    /**
     * 为匹配成功的两个玩家创建游戏房间
     * 
     * @param player1 玩家1
     * @param player2 玩家2
     */
    private void createMatchedGame(Player player1, Player player2) {
        try {
            // 创建匹配房间
            String roomName = String.format("匹配房间 [%s vs %s]", player1.getName(), player2.getName());
            GameRoom room = roomManager.createRoom(roomName);
            
            // 将两个玩家加入房间
            if (room.addPlayer(player1) && room.addPlayer(player2)) {
                player1.setCurrentRoom(room);
                player2.setCurrentRoom(room);
                
                // 通知玩家匹配成功
                player1.sendMessage("\n🎯 匹配成功！🎯");
                String matchMessage = String.format("对手: %s，房间: %d [%s]", 
                    player2.getName(), room.getRoomId(), room.getRoomName());
                player1.sendMessage(matchMessage);
                player1.sendMessage("准备开始精彩对局！");
                
                player2.sendMessage("\n🎯 匹配成功！🎯");
                matchMessage = String.format("对手: %s，房间: %d [%s]", 
                    player1.getName(), room.getRoomId(), room.getRoomName());
                player2.sendMessage(matchMessage);
                player2.sendMessage("准备开始精彩对局！");
                
                // 自动开始游戏
                if (room.startGame()) {
                    String gameStartMessage = String.format("游戏自动开始！你是%s方", 
                        player1.getColor().equals("black") ? "黑" : "白");
                    player1.sendMessage(gameStartMessage);
                    
                    gameStartMessage = String.format("游戏自动开始！你是%s方", 
                        player2.getColor().equals("black") ? "黑" : "白");
                    player2.sendMessage(gameStartMessage);
                    
                    // 发送棋盘状态
                    String boardString = room.getBoardString();
                    player1.sendMessage(boardString);
                    player2.sendMessage(boardString);
                    
                    // 提示黑方先手
                    Player blackPlayer = player1.getColor().equals("black") ? player1 : player2;
                    blackPlayer.sendMessage("你是黑方，请先落子！");
                }
            } else {
                // 如果加入房间失败，移除房间并重新加入匹配队列
                roomManager.removeRoom(room.getRoomId());
                matchQueue.offer(player1);
                matchQueue.offer(player2);
            }
        } catch (Exception e) {
            // 匹配失败，重新加入队列
            player1.sendMessage("匹配过程中发生错误，已重新加入匹配队列");
            player2.sendMessage("匹配过程中发生错误，已重新加入匹配队列");
            matchQueue.offer(player1);
            matchQueue.offer(player2);
        }
    }
    
    /**
     * 关闭匹配系统
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}