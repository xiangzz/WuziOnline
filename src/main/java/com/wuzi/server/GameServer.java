package com.wuzi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8888;
    private final RoomManager roomManager;
    private final MatchMaker matchMaker;
    private final ExecutorService executorService;

    public GameServer() {
        this.roomManager = new RoomManager();
        this.matchMaker = new MatchMaker(roomManager);
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("五子棋服务器启动，监听端口：" + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接：" + clientSocket.getInetAddress());
                
                // 这里需要先获取玩家名称并创建Player对象
                // 暂时使用clientSocket直接创建ClientHandler
                ClientHandler clientHandler = new ClientHandler(clientSocket, roomManager, matchMaker);
                executorService.execute(clientHandler);
            }
        } catch (Exception e) {
            System.err.println("服务器启动失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    public void shutdown() {
        System.out.println("正在关闭服务器...");
        if (matchMaker != null) {
            matchMaker.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        System.out.println("服务器已关闭");
    }
    }

    public static void main(String[] args) {
        new GameServer().start();
    }
}