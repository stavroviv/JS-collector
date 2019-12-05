package main.java;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class CustomSocketHandler extends WebSocketHandler {
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(CustomSocket.class);
    }
}