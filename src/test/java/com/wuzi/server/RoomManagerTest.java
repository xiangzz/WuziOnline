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
        assertEquals(1, room.getRoomId());
        
        GameRoom room2 = roomManager.createRoom();
        assertNotNull(room2);
        assertEquals(2, room2.getRoomId());
    }

    @Test
    void testGetRoom() {
        GameRoom createdRoom = roomManager.createRoom();
        GameRoom retrievedRoom = roomManager.getRoom(1);
        
        assertNotNull(retrievedRoom);
        assertEquals(createdRoom.getRoomId(), retrievedRoom.getRoomId());
        
        assertNull(roomManager.getRoom(114514)); // 不存在的房间
    }

    @Test
    void testGetAllRooms() {
        roomManager.createRoom();
        roomManager.createRoom();
        
        assertEquals(2, roomManager.getAllRooms().size());
        assertTrue(roomManager.getAllRooms().containsKey(1));
        assertTrue(roomManager.getAllRooms().containsKey(2));
    }

    @Test
    void testRemoveRoom() {
        roomManager.createRoom();
        assertEquals(1, roomManager.getAllRooms().size());
        
        roomManager.removeRoom(1);
        assertEquals(0, roomManager.getAllRooms().size());
        assertNull(roomManager.getRoom(1));
    }
} 