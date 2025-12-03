package com.wuzi.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomManagerTest {
    private RoomManager roomManager;

    @BeforeEach
    void setUp() {
        roomManager = new RoomManager();
    }

    @Test
    void testCreateRoom() {
        GameRoom room = roomManager.createRoom();
        assertNotNull(room);
        assertEquals(11, room.getRoomId());
        
        GameRoom room2 = roomManager.createRoom();
        assertNotNull(room2);
        assertEquals(12, room2.getRoomId());
    }

    @Test
    void testGetRoom() {
        GameRoom createdRoom = roomManager.createRoom();
        GameRoom retrievedRoom = roomManager.getRoom(createdRoom.getRoomId());
        
        assertNotNull(retrievedRoom);
        assertEquals(createdRoom.getRoomId(), retrievedRoom.getRoomId());
        
        assertNull(roomManager.getRoom(114514)); // 不存在的房间
    }

    @Test
    void testGetAllRooms() {
        roomManager.createRoom();
        roomManager.createRoom();
        
        // 10 initial rooms + 2 created = 12
        assertEquals(12, roomManager.getAllRooms().size());
        assertTrue(roomManager.getAllRooms().containsKey(11));
        assertTrue(roomManager.getAllRooms().containsKey(12));
    }

    @Test
    void testRemoveRoom() {
        GameRoom room = roomManager.createRoom();
        // 10 initial + 1 created = 11
        assertEquals(11, roomManager.getAllRooms().size());
        
        roomManager.removeRoom(room.getRoomId());
        assertEquals(10, roomManager.getAllRooms().size());
        assertNull(roomManager.getRoom(room.getRoomId()));
    }
} 