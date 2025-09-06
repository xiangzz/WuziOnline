package com.wuzi.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RoomManager roomManager;
    private final MatchMaker matchMaker;
    private Player player;

    public ClientHandler(Socket socket, RoomManager roomManager, MatchMaker matchMaker) {
        this.clientSocket = socket;
        this.roomManager = roomManager;
        this.matchMaker = matchMaker;
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
            
            // 发送欢迎信息
            player.sendMessage("\n=== 欢迎来到五子棋在线游戏 ===");
            player.sendMessage("你好, " + playerName + "！");
            player.sendMessage("输入 'help' 查看命令帮助");
            player.sendMessage("输入 'match' 开始匹配对战");
            player.sendMessage("输入 'ls rooms' 查看现有房间");
            player.sendMessage("================================\n");

            // 处理玩家命令
            while (true) {
                String command = reader.readLine();
                if (command == null) break;

                handleCommand(command);
            }
        } catch (Exception e) {
            System.err.println("客户端处理错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (player != null) {
                // 从匹配队列中移除玩家
                matchMaker.leaveMatchQueue(player);
                // 从房间中移除玩家
                if (player.getCurrentRoom() != null) {
                    player.getCurrentRoom().removePlayer(player);
                }
                player.close();
            }
        }
    }

    private void handleCommand(String command) {
        try {
            String[] parts = command.split(" ");
            switch (parts[0].toLowerCase()) {
                case "ls":
                    if (parts.length > 1 && parts[1].equals("rooms")) {
                        listRooms();
                    }
                    break;
                case "help":
                    showHelp();
                    break;
                case "leave":
                    leaveRoom();
                    break;
                case "create":
                    if (parts.length > 1 && parts[1].equals("room")) {
                        String roomName = parts.length > 2 ? 
                            String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length)) : 
                            null;
                        createRoom(roomName);
                    }
                    break;
                case "enter":
                    if (parts.length > 2 && parts[1].equals("room")) {
                        enterRoom(Integer.parseInt(parts[2]));
                    }
                    break;
                case "match":
                    joinMatch();
                    break;
                case "cancel":
                    if (parts.length > 1 && parts[1].equals("match")) {
                        cancelMatch();
                    }
                    break;
                case "start":
                    startGame();
                    break;
                case "put":
                    if (parts.length > 2) {
                        try {
                            int[] coords = CommandParser.parseMoveCommand(command);
                            makeMove(coords[0], coords[1]);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage("坐标解析错误: " + e.getMessage());
                        }
                    } else {
                        player.sendMessage("落子命令格式错误。" + CommandParser.getCoordinateHelp());
                    }
                    break;
                default:
                    player.sendMessage("未知命令: " + command);
                    player.sendMessage("输入 'help' 查看可用命令列表");
                    break;
            }
        } catch (Exception e) {
            player.sendMessage("坐标格式错误！");
            player.sendMessage("正确格式: x,y (支持0-9和A-E)");
            player.sendMessage("示例: 7,8 或 A,B 或 0xA,0xE");
        }
    }

    private void listRooms() {
        StringBuilder sb = new StringBuilder("房间列表：\n");
        roomManager.getAllRooms().forEach((id, room) -> {
            sb.append(String.format("房间 %d [%s]: %d/2 玩家", id, room.getRoomName(), room.getPlayerCount()));
            if (room.isGameStarted()) {
                sb.append(" (游戏中)");
            }
            sb.append("\n");
        });
        player.sendMessage(sb.toString());
    }

    private void createRoom(String roomName) {
        GameRoom room;
        if (roomName != null && !roomName.trim().isEmpty()) {
            room = roomManager.createRoom(roomName.trim());
        } else {
            room = roomManager.createRoom();
        }
        player.sendMessage(String.format("成功创建房间 %d [%s]", room.getRoomId(), room.getRoomName()));
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
            player.sendMessage("你不在任何房间中！");
            player.sendMessage("请先使用 'enter <房间号>' 进入房间或 'match' 开始匹配");
            return;
        }

        if (!room.isGameStarted()) {
            player.sendMessage("游戏还未开始！");
            player.sendMessage("等待另一位玩家加入或使用 'start' 开始游戏");
            return;
        }

        if (room.isGameOver()) {
            player.sendMessage("游戏已结束！");
            player.sendMessage("使用 'start' 开始新游戏");
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
    
    private void joinMatch() {
        if (player.getCurrentRoom() != null) {
            player.sendMessage("你已经在房间中，无法加入匹配队列！");
            player.sendMessage("请先使用 'leave' 离开当前房间");
            return;
        }
        
        if (matchMaker.joinMatchQueue(player)) {
            player.sendMessage("\n🔍 正在为你寻找对手...");
            player.sendMessage("请耐心等待，匹配成功后将自动开始游戏");
            player.sendMessage("使用 'cancel match' 可以取消匹配\n");
        } else {
            player.sendMessage("加入匹配队列失败！");
            player.sendMessage("你可能已经在队列中，使用 'cancel match' 先取消当前匹配");
        }
    }
    
    private void cancelMatch() {
        if (matchMaker.leaveMatchQueue(player)) {
            player.sendMessage("\n❌ 已取消匹配");
            player.sendMessage("你可以随时使用 'match' 重新开始匹配\n");
        } else {
            player.sendMessage("你不在匹配队列中！");
            player.sendMessage("使用 'match' 加入匹配队列");
        }
    }
    
    private void showHelp() {
        StringBuilder help = new StringBuilder();
        help.append("\n=== 五子棋游戏命令帮助 ===\n");
        help.append("\n【房间管理】\n");
        help.append("  ls                    - 查看所有房间状态\n");
        help.append("  enter <房间号>        - 进入指定房间 (例: enter 1)\n");
        help.append("  create [房间名]       - 创建新房间 (例: create 我的房间)\n");
        help.append("  leave                 - 离开当前房间\n");
        help.append("\n【游戏控制】\n");
        help.append("  start                 - 开始游戏 (需要2名玩家)\n");
        help.append("  match                 - 加入自动匹配队列\n");
        help.append("  cancel match          - 取消匹配\n");
        help.append("\n【游戏操作】\n");
        help.append("  x,y                   - 在坐标(x,y)落子\n");
        help.append("  坐标格式: 支持0-9和A-E (A=10, B=11, C=12, D=13, E=14)\n");
        help.append("  示例: 7,8 或 A,B 或 0xA,0xE\n");
        help.append("\n【其他命令】\n");
        help.append("  help                  - 显示此帮助信息\n");
        help.append("  quit                  - 退出游戏\n");
        help.append("\n【游戏规则】\n");
        help.append("  - 黑子先手，白子后手\n");
        help.append("  - 率先连成5子的一方获胜\n");
        help.append("  - 棋盘大小: 15x15\n");
        help.append("================================\n");
        player.sendMessage(help.toString());
    }
    
    private void leaveRoom() {
        GameRoom currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            player.sendMessage("你不在任何房间中！");
            player.sendMessage("使用 'ls rooms' 查看可用房间或 'match' 开始匹配");
            return;
        }
        
        String roomName = currentRoom.getRoomName();
        currentRoom.removePlayer(player);
        player.sendMessage("\n👋 已离开房间: " + roomName);
        player.sendMessage("感谢你的参与！");
        player.sendMessage("使用 'match' 快速匹配或 'ls rooms' 查看其他房间\n");
        
        // 如果房间为空，可以考虑移除房间
        if (currentRoom.getPlayerCount() == 0) {
            roomManager.removeRoom(currentRoom.getRoomId());
        }
    }
}