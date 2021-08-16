package com.andy.chatserver.websock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service
public class ChatMessageHandler  extends TextWebSocketHandler{
	
	private static final Logger logger = LoggerFactory.getLogger(ChatMessageHandler.class);

	/*map key is the chat room id, and the value is the list
	 of all session of that room.
	 For the purpose of this app, the room id is equals to the sender's name
	 */
	private final Map<String, List<WebSocketSession>> chatRooms;
	
	private final Map<String, String> inverseChatRooms;
	
	
	public ChatMessageHandler() {
		chatRooms = new ConcurrentHashMap<>();
		inverseChatRooms = new ConcurrentHashMap<>();
	}
	

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		
		TextMessage textMessage = new TextMessage("Welcome to chat room !");
		try {
			session.sendMessage(textMessage);
		} catch (IOException e) {
			logger.error("Error when sending welcome message", e);
		}
	}

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        
    	
    	String payload = message.getPayload();
    	
    	if(payload.trim().isEmpty())
    		return;
    	
    	JSONObject jsonObject = null;
    			
    	try {
    		jsonObject = new JSONObject(payload);
    		String roomID = jsonObject.getString("roomID");
    		payload = jsonObject.getString("message");
    		
    		if(!inverseChatRooms.containsKey(session.getId()))
    			inverseChatRooms.put(session.getId(), roomID);
    		
    		if(!chatRooms.containsKey(roomID)) {
    			chatRooms.put(roomID, new ArrayList<>());
    		}
    		
    		if(!chatRooms.get(roomID).contains(session))
    			chatRooms.get(roomID).add(session);
    		
    		multicast(roomID, payload, session);
    	}
    	catch(Exception ex) {
    		TextMessage msg = new TextMessage("Invalid message format !");
    		session.sendMessage(msg);
    		return;
    	}

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    	String roomId = inverseChatRooms.get(session.getId());
    	
    	// Remove this session from the chat room
    	if(roomId != null) {
    		chatRooms.get(roomId).remove(session);
    	}
    	
    	inverseChatRooms.remove(session.getId());
    }
    
    
    /**
     * Broadcast a given message to all sessions of the chat room.
     * @param roomId
     * @param message
     */
    private void multicast(String roomId, String message, WebSocketSession senderSession) {
    	
    	TextMessage textMessage = new TextMessage(message);
    	
    	if(chatRooms.get(roomId) == null)
    		return;
    	
    	chatRooms.get(roomId).forEach(session->{
    		if(session != senderSession) {
	    		try {
					session.sendMessage(textMessage);
				} catch (IOException e) {
					logger.error("Error when sending message to session "+session.getId(), e);
				}
    		}
    	});
    }
}

