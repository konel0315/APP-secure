package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    // 사용자 세션 관리 (username -> session)
    private static ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // 방 관리 (roomId -> username 리스트)
    private static ConcurrentHashMap<String, List<String>> roomMap = new ConcurrentHashMap<>();

    private static Random random = new Random();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String clientMessage = message.getPayload();
        JSONObject jsonMessage = new JSONObject(clientMessage);
        String type = jsonMessage.getString("type");

        if ("check".equals(type)) { // 사용자 등록 및 방 배정
            String username = jsonMessage.getString("message");
            sessionMap.put(username, session);
            assignUserToRoom(username);
        } else if ("chat".equals(type)) { // 메시지 전송
            String username = jsonMessage.getString("username");
            String chatMessage = jsonMessage.getString("message");
            sendMessageToRoom(username, chatMessage);
        } else if ("out".equals(type)) { // 사용자 연결 해제
            String username = jsonMessage.getString("message");
            removeUserFromRoom(username);
            sessionMap.remove(username);
        }
    }

    /**
     * 사용자를 방에 랜덤 배정
     */
    private void assignUserToRoom(String username) {
        // 현재 방에 여유가 있는지 확인
        for (String roomId : roomMap.keySet()) {
            List<String> users = roomMap.get(roomId);
            if (users.size() < 2) { // 방에 빈자리가 있으면 추가
                users.add(username);
                System.out.println("User " + username + " joined room " + roomId);
                return;
            }
        }

        // 여유 있는 방이 없으면 새 방 생성
        String newRoomId = "room" + (roomMap.size() + 1);
        List<String> newRoom = new ArrayList<>();

        // 랜덤으로 사용자 추가
        List<String> availableUsers = new ArrayList<>(sessionMap.keySet());
        availableUsers.remove(username); // 자신 제외

        if (!availableUsers.isEmpty()) {
            String randomUser = availableUsers.get(random.nextInt(availableUsers.size()));
            newRoom.add(randomUser);
            sessionMap.remove(randomUser); // 다른 방에 추가되었으니 제외
            System.out.println("User " + randomUser + " added to room " + newRoomId);
        }

        newRoom.add(username); // 현재 유저도 추가
        roomMap.put(newRoomId, newRoom); // 새로운 방 추가
        System.out.println("Created new room: " + newRoomId + " with users: " + newRoom);
    }

    /**
     * 방의 모든 사용자에게 메시지 전송
     */
    private void sendMessageToRoom(String username, String message) {
        String userRoomId = getUserRoomId(username);
        if (userRoomId == null) {
            System.out.println("User " + username + " is not in any room.");
            return;
        }

        List<String> usersInRoom = roomMap.get(userRoomId);
        for (String user : usersInRoom) {
            WebSocketSession session = sessionMap.get(user);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(username + " : " + message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 사용자가 속한 방 ID를 반환
     */
    private String getUserRoomId(String username) {
        for (String roomId : roomMap.keySet()) {
            if (roomMap.get(roomId).contains(username)) {
                return roomId;
            }
        }
        return null;
    }

    /**
     * 사용자를 방에서 제거
     */
    private void removeUserFromRoom(String username) {
        String userRoomId = getUserRoomId(username);
        if (userRoomId != null) {
            List<String> usersInRoom = roomMap.get(userRoomId);
            usersInRoom.remove(username);
            if (usersInRoom.isEmpty()) {
                roomMap.remove(userRoomId);
                System.out.println("Room " + userRoomId + " is empty and removed.");
            } else {
                System.out.println("User " + username + " left room " + userRoomId);
            }
        }
    }
}
