package com.wuzi.server;

import com.wuzi.common.AnsiColor;
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
            writer.println(AnsiColor.info("请输入你的名字："));
            String playerName = reader.readLine();
            player = new Player(playerName, clientSocket, reader, writer);
            ServerLogger.info("玩家 " + playerName + " (" + clientSocket.getInetAddress() + ") 已登录");

            // 发送欢迎消息和帮助
            player.sendMessage(AnsiColor.success("欢迎 " + playerName + "！") + AnsiColor.info("输入 'ls rooms' 查看房间列表，或 'help' 查看帮助。"));

            // 处理玩家命令
            boolean running = true;
            while (running) {
                String command = reader.readLine();
                if (command == null) break;

                ServerLogger.info("收到玩家 " + playerName + " 命令: " + command);
                running = handleCommand(command);
            }
        } catch (Exception e) {
            ServerLogger.error("客户端处理错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (player != null) {
                ServerLogger.info("玩家 " + player.getName() + " 断开连接");
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
                    player.sendMessage(AnsiColor.info("再见！"));
                    return false;
                case "again":
                    startGame();
                    break;
                case "leave":
                    leaveRoom();
                    break;
                case "help":
                    printHelp();
                    break;
                default:
                    player.sendMessage(AnsiColor.error("未知命令") + "，输入 " + AnsiColor.bold("help") + " 查看帮助");
            }
            return true;
        } catch (Exception e) {
            player.sendMessage(AnsiColor.error("命令格式错误：" + e.getMessage()));
            return true;
        }
    }

    private void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(AnsiColor.color("\n================ 五子棋命令帮助 ================\n", AnsiColor.CYAN));
        
        sb.append(AnsiColor.bold("\n[ 房间管理 ]\n"));
        sb.append("  " + AnsiColor.color("ls rooms", AnsiColor.YELLOW) + "          - 查看房间列表\n");
        sb.append("  " + AnsiColor.color("enter room <id>", AnsiColor.YELLOW) + "   - 进入房间 (例如: enter room 1)\n");
        sb.append("  " + AnsiColor.color("leave", AnsiColor.YELLOW) + "             - 离开当前房间\n");
        
        sb.append(AnsiColor.bold("\n[ 游戏操作 ]\n"));
        sb.append("  " + AnsiColor.color("start", AnsiColor.YELLOW) + "             - 准备/开始游戏\n");
        sb.append("  " + AnsiColor.color("put <x> <y>", AnsiColor.YELLOW) + "       - 落子 (坐标 0-E, 例如: put 7 7)\n");
        sb.append("  " + AnsiColor.color("again", AnsiColor.YELLOW) + "             - 重新开始一局\n");
        
        sb.append(AnsiColor.bold("\n[ 系统 ]\n"));
        sb.append("  " + AnsiColor.color("help", AnsiColor.YELLOW) + "              - 显示此帮助\n");
        sb.append("  " + AnsiColor.color("quit", AnsiColor.YELLOW) + "              - 退出游戏\n");
        
        sb.append(AnsiColor.color("\n================================================\n", AnsiColor.CYAN));
        player.sendMessage(sb.toString());
    }

    private void listRooms() {
        StringBuilder sb = new StringBuilder();
        sb.append(AnsiColor.color("\n==== 房间列表 ====\n", AnsiColor.CYAN));
        
        roomManager.getAllRooms().forEach((id, room) -> {
            int count = room.getPlayerCount();
            String status;
            String statusColor;
            
            if (room.isGameStarted()) {
                status = "🔴 游戏中";
                statusColor = AnsiColor.RED;
            } else if (count == 2) {
                status = "🔴 已满员";
                statusColor = AnsiColor.RED;
            } else if (count == 1) {
                status = "🟡 等待中";
                statusColor = AnsiColor.YELLOW;
            } else {
                status = "🟢 空闲  ";
                statusColor = AnsiColor.GREEN;
            }
            
            String roomId = String.format("[%02d]", id);
            String playerCount = String.format("(%d/2)", count);
            
            sb.append(AnsiColor.color(roomId, AnsiColor.BOLD))
              .append(" ")
              .append(AnsiColor.color(status, statusColor))
              .append(" ")
              .append(AnsiColor.color(playerCount, AnsiColor.WHITE))
              .append("\n");
        });
        sb.append(AnsiColor.color("==================\n", AnsiColor.CYAN));
        player.sendMessage(sb.toString());
    }

    private void enterRoom(int roomId) {
        GameRoom room = roomManager.getRoom(roomId);
        if (room == null) {
            player.sendMessage(AnsiColor.error("房间不存在"));
            return;
        }

        if (room.addPlayer(player)) {
            player.setCurrentRoom(room);
            player.sendMessage(AnsiColor.success("成功进入房间 " + roomId));
            ServerLogger.info("玩家 " + player.getName() + " 进入房间 " + roomId);
            if (room.getPlayerCount() == 2) {
                room.getPlayer1().sendMessage(AnsiColor.info("对手已加入，请输入 start 开始游戏"));
                room.getPlayer2().sendMessage(AnsiColor.info("对手已加入，请输入 start 开始游戏"));
            }
        } else {
            player.sendMessage(AnsiColor.error("房间已满"));
        }
    }

    private void leaveRoom() {
        GameRoom room = player.getCurrentRoom();
        if (room == null) {
            player.sendMessage(AnsiColor.error("你不在任何房间中"));
            return;
        }

        // Notify other player before removing
        Player other = (room.getPlayer1() == player) ? room.getPlayer2() : room.getPlayer1();
        if (other != null) {
            other.sendMessage(AnsiColor.info(player.getName() + " 离开了房间"));
            other.sendMessage(AnsiColor.info("等待其他玩家加入..."));
        }

        room.removePlayer(player);
        player.setCurrentRoom(null);
        player.sendMessage(AnsiColor.success("已离开房间 " + room.getRoomId()));
        ServerLogger.info("玩家 " + player.getName() + " 离开房间 " + room.getRoomId());
    }

    private void startGame() {
        GameRoom room = player.getCurrentRoom();
        if (room == null) {
            player.sendMessage(AnsiColor.error("你不在任何房间中"));
            return;
        }

        if (room.isGameStarted() && !room.isGameOver()) {
            player.sendMessage(AnsiColor.error("游戏已经开始了"));
            return;
        }

        if (room.setPlayerReady(player)) {
            Player p1 = room.getPlayer1();
            Player p2 = room.getPlayer2();

            ServerLogger.success("房间 " + room.getRoomId() + " 游戏开始 (" + p1.getName() + " vs " + p2.getName() + ")");

            String msg1 = AnsiColor.success(String.format("游戏开始！你是%s方", p1.getColor().equals("black") ? "黑" : "白"));
            String msg2 = AnsiColor.success(String.format("游戏开始！你是%s方", p2.getColor().equals("black") ? "黑" : "白"));

            p1.sendMessage(msg1);
            p2.sendMessage(msg2);
            p1.sendMessage(room.getBoardString());
            p2.sendMessage(room.getBoardString());
        } else {
            player.sendMessage(AnsiColor.info("等待对手准备..."));
        }
    }

    private void makeMove(int x, int y) {
        GameRoom room = player.getCurrentRoom();
        if (room == null) {
            player.sendMessage(AnsiColor.error("你不在任何房间中"));
            return;
        }

        if (!room.isGameStarted()) {
            player.sendMessage(AnsiColor.error("游戏还未开始"));
            return;
        }

        if (room.isGameOver()) {
            player.sendMessage(AnsiColor.error("游戏已结束"));
            return;
        }

        if (!room.getCurrentTurnColor().equals(player.getColor())) {
            player.sendMessage(AnsiColor.error("并不是你的回合，请等待对手落子"));
            return;
        }

        if (room.makeMove(x, y, player.getColor())) {
            ServerLogger.info("房间 " + room.getRoomId() + ": " + player.getName() + " 落子 (" + x + ", " + y + ")");
            String boardString = room.getBoardString();
            room.getPlayer1().sendMessage(boardString);
            room.getPlayer2().sendMessage(boardString);

            if (room.checkWin(x, y)) {
                room.setGameOver(true);
                ServerLogger.success("房间 " + room.getRoomId() + " 游戏结束，获胜者: " + player.getName());
                String winMessage = AnsiColor.success(AnsiColor.bold(String.format("游戏结束！%s 获胜！\n输入 leave 离开房间，或者输入 again 再来一局\n", player.getName())));
                room.getPlayer1().sendMessage(winMessage);
                room.getPlayer2().sendMessage(winMessage);
            } else {
                // Notify the next player
                Player nextPlayer = room.getCurrentTurnColor().equals(room.getPlayer1().getColor()) ? room.getPlayer1() : room.getPlayer2();
                nextPlayer.sendMessage(AnsiColor.info("轮到你了"));
            }
        } else {
            player.sendMessage(AnsiColor.error("无效的落子位置"));
        }
    }
} 