package com.wuzi.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            // 读取服务器欢迎消息
            String welcomeMessage = reader.readLine();
            System.out.println(welcomeMessage);

            // 发送玩家名字
            String playerName = scanner.nextLine();
            writer.println(playerName);

            // 创建消息接收线程
            Thread messageReceiver = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (Exception e) {
                    System.err.println("连接断开：" + e.getMessage());
                }
            });
            messageReceiver.start();

            // 处理用户输入
            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("quit")) {
                    break;
                }
                writer.println(command);
            }

        } catch (Exception e) {
            System.err.println("连接服务器失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
} 