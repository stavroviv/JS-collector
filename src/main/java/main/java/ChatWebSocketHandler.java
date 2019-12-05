package main.java;

//import java.io.IOException;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import javax.servlet.http.HttpServletRequest;
//import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServletRequest;


public class ChatWebSocketHandler extends WebSocketHandler {
    @Override
    public void configure(WebSocketServletFactory factory) {


        factory.register(MySocket.class);
    }

//    @Override
//    public WebSocket doWebSocketConnect(HttpServletRequest request,
//                                        String protocol) {
//
//        // У нас есть два варианта
//        // Либо мы не пускаем клиента и вызываем исключение
//        //    throw new Exception();
//        // Либо возвращаем объект, который будет соединять сервер с клиентом
//        //   и обрабатывать запросы от клиента
//        return new ChatWebSocket();
//    }

}
