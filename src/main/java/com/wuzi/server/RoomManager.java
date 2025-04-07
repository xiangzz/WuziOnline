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
    }

    public GameRoom createRoom() {
        int roomId = roomIdGenerator.getAndIncrement();
        GameRoom room = new GameRoom(roomId);
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