package com.wuzi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8888;
    private final RoomManager roomManager;
    private final ExecutorService executorService;

    public GameServer() {
        this.roomManager = new RoomManager();
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ServerLogger.success("五子棋服务器启动，监听端口：" + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerLogger.info("新客户端连接：" + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, roomManager);
                executorService.execute(clientHandler);
            }
        } catch (Exception e) {
            ServerLogger.error("服务器启动失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GameServer().start();
    }
} 