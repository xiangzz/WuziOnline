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
        initializeDefaultRooms();
    }

    private void initializeDefaultRooms() {
        // 创建3个默认房间
        createRoom("新手房间");
        createRoom("进阶房间");
        createRoom("高手房间");
    }

    public GameRoom createRoom() {
        int roomId = roomIdGenerator.getAndIncrement();
        GameRoom room = new GameRoom(roomId);
        rooms.put(roomId, room);
        return room;
    }

    public GameRoom createRoom(String roomName) {
        int roomId = roomIdGenerator.getAndIncrement();
        GameRoom room = new GameRoom(roomId, roomName);
        rooms.put(roomId, room);
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
    }
}