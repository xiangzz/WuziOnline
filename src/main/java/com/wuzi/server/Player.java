package com.wuzi.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Player {
    private final String name;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String color;
    private GameRoom currentRoom;
    private final AtomicBoolean isConnected = new AtomicBoolean(true);
    private final AtomicLong lastHeartbeat = new AtomicLong(System.currentTimeMillis());
    private final String playerId; // 用于重连识别

    public Player(String name, Socket socket, BufferedReader reader, PrintWriter writer) {
        this.name = name;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.color = null;
        this.currentRoom = null;
        this.playerId = name + "_" + System.currentTimeMillis(); // 生成唯一ID
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
    }

    public void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    public String readMessage() throws Exception {
        return reader.readLine();
    }

    public void close() {
        isConnected.set(false);
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 断线重连相关方法
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return isConnected.get() && socket != null && !socket.isClosed();
    }
    
    public void setDisconnected() {
        isConnected.set(false);
    }
    
    public void updateHeartbeat() {
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat.get();
    }
    
    public boolean isTimeout(long timeoutMs) {
        return System.currentTimeMillis() - lastHeartbeat.get() > timeoutMs;
    }
    
    // 重连方法
    public boolean reconnect(Socket newSocket, BufferedReader newReader, PrintWriter newWriter) {
        try {
            // 关闭旧连接
            close();
            
            // 设置新连接
            this.socket = newSocket;
            this.reader = newReader;
            this.writer = newWriter;
            this.isConnected.set(true);
            this.lastHeartbeat.set(System.currentTimeMillis());
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}