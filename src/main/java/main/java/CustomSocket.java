package main.java;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.ArrayList;
import java.util.List;

@WebSocket
public class CustomSocket {
    static List<Session> sessions = new ArrayList<>();
    private Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        sessions.add(session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
       sessions.remove(this.session);
    }

    @OnWebSocketMessage
    public void onText(Session session, String message) {
        //System.out.printf("IN");
        if (session.isOpen()) {
            System.out.printf(message);
        }
    }
}