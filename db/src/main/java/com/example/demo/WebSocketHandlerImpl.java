package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    // 클라이언트와 세션을 매핑하기 위한 맵
    private static ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    
    private final QuestionRepository questionRepository;  // QuestionRepository를 final로 선언

    @Autowired  // 생성자 주입
    public WebSocketHandlerImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 받은 메시지 내용 (JSON 형식)
        String clientMessage = message.getPayload();
        JSONObject jsonMessage = new JSONObject(clientMessage);
        String type = jsonMessage.getString("type");

        if ("check".equals(type)) {
            String username = jsonMessage.getString("message"); 
            sessionMap.put(username, session);
            System.out.println("Session for user " + username + " saved.");
        } else if ("chat".equals(type)) {
           
            String username = jsonMessage.getString("message"); 
            String chatMessage = jsonMessage.getString("message");  

            System.out.println("Received chat message from " + username + ": " + chatMessage);
            String randomUsername = questionRepository.findRandomUsername(username);
            sendMessageToUser(randomUsername, chatMessage);
        }
        else if("out".equals(type)) {
        	String username = jsonMessage.getString("message"); 
            sessionMap.remove(username, session);
            System.out.println("Session for user " + username + " removed.");
        }
    }


    private void sendMessageToUser(String username, String message) {
        WebSocketSession session = sessionMap.get(username); 
       
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("Message from server: " + message));
                System.out.println("Sent message to " + username + ": " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No active session for user " + username);
        }
    }
}
