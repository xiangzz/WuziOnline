package com.wuzi.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RoomManager roomManager;
    private Player player;

    public ClientHandler(Socket socket, RoomManager roomManager) {
        this.clientSocket = socket;
        this.roomManager = roomManager;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // 等待玩家输入名字
            writer.println("请输入你的名字：");
            String playerName = reader.readLine();
            player = new Player(playerName, clientSocket, reader, writer);

            // 发送欢迎消息和帮助
            player.sendMessage("欢迎 " + playerName + "！输入 'ls rooms' 查看房间列表，或 'help' 查看帮助。");

            // 处理玩家命令
            boolean running = true;
            while (running) {
                String command = reader.readLine();
                if (command == null) break;

                running = handleCommand(command);
            }
        } catch (Exception e) {
            System.err.println("客户端处理错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (player != null) {
                if (player.getCurrentRoom() != null) {
                    player.getCurrentRoom().removePlayer(player);
                }
                player.close();
            }
        }
    }

    private boolean handleCommand(String command) {
        try {
            String[] parts = command.split(" ");
            switch (parts[0].toLowerCase()) {
                case "ls":
                    if (parts.length > 1 && parts[1].equals("rooms")) {
                        listRooms();
                    }
                    break;
                case "enter":
                    if (parts.length > 2 && parts[1].equals("room")) {
                        enterRoom(Integer.parseInt(parts[2]));
                    }
                    break;
                case "start":
                    startGame();
                    break;
                case "put":
                    if (parts.length > 2) {
                        // 使用16进制解析坐标
                        makeMove(Integer.parseInt(parts[1], 16), Integer.parseInt(parts[2], 16));
                    }
                    break;
                case "quit":
                    player.sendMessage("再见！");
                    return false;
                case "help":
                    printHelp();
                    break;
                default:
                    player.sendMessage("未知命令，输入 'help' 查看帮助");
            }
            return true;
        } catch (Exception e) {
            player.sendMessage("命令格式错误：" + e.getMessage());
            return true;
        }
    }

    private void printHelp() {
        player.sendMessage("可用命令：");
        player.sendMessage("  ls rooms        - 查看所有房间列表");
        player.sendMessage("  enter room <id> - 进入指定房间");
        player.sendMessage("  start           - 准备开始游戏");
        player.sendMessage("  put <x> <y>     - 在指定位置落子 (坐标 0-E)");
        player.sendMessage("  quit            - 退出游戏");
    }

    private void listRooms() {
        StringBuilder sb = new StringBuilder("房间列表：\n");
        roomManager.getAllRooms().forEach((id, room) -> {
            sb.append(String.format("房间 %d: %d/2 玩家", id, room.getPlayerCount()));
            if (room.isGameStarted()) {
                sb.append(" (游戏中)");
            }
            sb.append("\n");
        });
        player.sendMessage(sb.toString());
    }

    private void enterRoom(int roomId) {
        GameRoom room = roomManager.getRoom(roomId);
        if (room == null) {
            player.sendMessage("房间不存在");
            return;
        }

        if (room.addPlayer(player)) {
            player.setCurrentRoom(room);
            player.sendMessage("成功进入房间 " + roomId);
            if (room.getPlayerCount() == 2) {
                room.getPlayer1().sendMessage("对手已加入，请输入 start 开始游戏");
                room.getPlayer2().sendMessage("对手已加入，请输入 start 开始游戏");
            }
        } else {
            player.sendMessage("房间已满");
        }
    }

    private void startGame() {
        GameRoom room = player.getCurrentRoom();
        if (room == null) {
            player.sendMessage("你不在任何房间中");
            return;
        }

        if (room.startGame()) {
            String message = String.format("游戏开始！你是%s方", player.getColor().equals("black") ? "黑" : "白");
            room.getPlayer1().sendMessage(message);
            room.getPlayer2().sendMessage(message);
            room.getPlayer1().sendMessage(room.getBoardString());
            room.getPlayer2().sendMessage(room.getBoardString());
        } else {
            player.sendMessage("等待对手准备...");
        }
    }

    private void makeMove(int x, int y) {
        GameRoom room = player.getCurrentRoom();
        if (room == null) {
            player.sendMessage("你不在任何房间中");
            return;
        }

        if (!room.isGameStarted()) {
            player.sendMessage("游戏还未开始");
            return;
        }

        if (room.isGameOver()) {
            player.sendMessage("游戏已结束");
            return;
        }

        if (room.makeMove(x, y, player.getColor())) {
            String boardString = room.getBoardString();
            room.getPlayer1().sendMessage(boardString);
            room.getPlayer2().sendMessage(boardString);

            if (room.checkWin(x, y)) {
                room.setGameOver(true);
                String winMessage = String.format("游戏结束！%s 获胜！", player.getName());
                room.getPlayer1().sendMessage(winMessage);
                room.getPlayer2().sendMessage(winMessage);
            }
        } else {
            player.sendMessage("无效的落子位置");
        }
    }
} 