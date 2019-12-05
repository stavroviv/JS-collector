package main.java;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebSocket
public class MySocket {

    private Session session;
    public static List<Session> ss = new ArrayList<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
//        try {
        this.session = session;
        //session.getRemote().sendString("Got your connect message");
        ss.add(session);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @OnWebSocketMessage
    public void onText(String message) {
//        System.out.println("text: " + message);
//        try {
//            this.session.getRemote().sendString(message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
        ss.remove(this.session);
    }

//    public void ddd(int statusCode, String reason) throws IOException {
//        session.getRemote().sendString("Got your connect message");
//    }
}

