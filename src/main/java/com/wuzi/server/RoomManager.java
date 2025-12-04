package com.wuzi.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager {
    private final Map<Integer, GameRoom> rooms;
    private final AtomicInteger roomIdGenerator;

    public RoomManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.roomIdGenerator = new AtomicInteger(1);
        // 初始化10个房间
        for (int i = 0; i < 10; i++) {
            createRoom();
        }
    }

    public GameRoom createRoom() {
        int roomId = roomIdGenerator.getAndIncrement();
        GameRoom room = new GameRoom(roomId);
        rooms.put(roomId, room);
        ServerLogger.info("创建房间: " + roomId);
        return room;
    }

    public GameRoom getRoom(int roomId) {
        return rooms.get(roomId);
    }

    public Map<Integer, GameRoom> getAllRooms() {
        return rooms;
    }

    public void removeRoom(int roomId) {
        rooms.remove(roomId);
        ServerLogger.info("销毁房间: " + roomId);
    }
} 