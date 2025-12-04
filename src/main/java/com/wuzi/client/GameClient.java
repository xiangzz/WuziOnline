package com.wuzi.client;

import com.wuzi.common.AnsiColor;
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
            System.out.print("> ");
            String playerName = scanner.nextLine();
            writer.println(playerName);

            // 创建消息接收线程
            Thread messageReceiver = new Thread(() -> {
                boolean firstBoard = true;
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        // 去除ANSI颜色代码以便进行逻辑判断
                        String cleanMessage = message.replaceAll("\u001B\\[[;\\d]*m", "");

                        if (cleanMessage.startsWith("游戏开始！")) {
                            firstBoard = true;
                        }
                        
                        // 匹配棋盘头部 (允许少量空格差异)
                        if (cleanMessage.trim().startsWith("0 1 2 3 4 5")) {
                            if (!firstBoard) {
                                // 清屏代码：移动光标到左上角并清除屏幕
                                System.out.print("\033[H\033[2J");
                                System.out.flush();
                            }
                            firstBoard = false;
                        }
                        
                        // 清除当前行（可能是提示符），打印消息，然后重打印提示符
                        System.out.print("\033[2K\r");
                        System.out.println(message);
                        System.out.print("> ");
                        System.out.flush();
                    }
                } catch (Exception e) {
                    System.out.println(AnsiColor.error("连接断开：" + e.getMessage()));
                }
            });
            messageReceiver.start();

            // 处理用户输入
            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();
                writer.println(command);
                if (command.equalsIgnoreCase("quit")) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println(AnsiColor.error("连接服务器失败：" + e.getMessage()));
            e.printStackTrace();
        }
    }
} 