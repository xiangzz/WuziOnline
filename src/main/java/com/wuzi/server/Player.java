package com.wuzi.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private final String name;
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private String color;
    private GameRoom currentRoom;

    public Player(String name, Socket socket, BufferedReader reader, PrintWriter writer) {
        this.name = name;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.color = null;
        this.currentRoom = null;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
    }

    public void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    public String readMessage() throws Exception {
        return reader.readLine();
    }

    public void close() {
        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 