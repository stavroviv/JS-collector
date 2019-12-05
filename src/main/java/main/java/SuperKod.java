package main.java;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.*;


class MyWindowFocusListener implements WindowFocusListener {

    private AnActionEvent anActionEvent;

    public MyWindowFocusListener(AnActionEvent anActionEvent) {
        super();
        this.anActionEvent = anActionEvent;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        SuperKod.execute(anActionEvent);
    }
}


class WrongApplicationException extends Exception { }


public class SuperKod extends AnAction {

    static ChatWebSocketHandler chatWebSocketHandler;

    static {
//        new Thread(() -> {
//            EventServer.main(null);
//        }).start();

        try {
            // Создание сервера Jetty на 8081 порту
            Server server = new Server(12345);

            // Регистрируем ChatWebSocketHandler в сервере Jetty
            chatWebSocketHandler = new ChatWebSocketHandler();
            // Это вариант хэндлера для WebSocketHandlerContainer
            chatWebSocketHandler.setHandler(new DefaultHandler());

            // Вставляем наш хэндлер слушаться jetty
            server.setHandler(chatWebSocketHandler);


//            server.getSessionIdManager().getId()

            // Запускаем Jetty
            server.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static String getTargetDirectoryName(String pomPath) throws WrongApplicationException {

        try {

            BufferedReader reader = new BufferedReader(new FileReader(pomPath));
            String line = reader.readLine();

            String appName = "";
            while (line != null) {

                int ind1 = line.indexOf("<name>");
                if (ind1 > 0) {

                    int ind2 = line.indexOf("</name>");

                    ind1 = ind1 + 6;

                    appName = line.substring(ind1, ind2).trim();
                }

                int index1 = line.indexOf("<version>");
                if (index1 > 0) {

                    if (!appName.equals("CRM")) {
                        throw new WrongApplicationException();
                    }

                    int index2 = line.indexOf("</version>");

                    index1 = index1 + 9;

                    return line.substring(index1, index2).trim();
                }
                line = reader.readLine();
            }
            reader.close();

            throw new WrongApplicationException();

        } catch (Exception ex) {
            throw new WrongApplicationException();
        }
    }

    public static void showMessage(IdeFrame ideFrame, String message, MessageType messageType) {

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(message, messageType, null)
                .setFadeoutTime(10_000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(ideFrame.getStatusBar().getComponent()), Balloon.Position.above);
    }

    public static void execute(AnActionEvent e) {

        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);

        FileDocumentManager.getInstance().saveAllDocuments();

        try {

            String basedir = project.getPresentableUrl();

            String targetDirectoryName = "CRM-" + getTargetDirectoryName(basedir + "/pom.xml");

            boolean fileChanges = JsFilesCollector.main(basedir, targetDirectoryName);

            if (fileChanges) {
                showMessage(ideFrame, "Файлы собраны.", MessageType.INFO);
            } else {

                showMessage(ideFrame, "Файлы собраны. ЕСТЬ Изменения.", MessageType.INFO);

                if (SuperKod3.isStatus()) {

                    for (Session s : MySocket.ss) {
                        try {
                            s.getRemote().sendString("RELOAD");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

        } catch (WrongApplicationException ex) {

            showMessage(ideFrame, "Не то приложение!", MessageType.ERROR);

        } catch (Exception ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));

            showMessage(ideFrame, errors.toString(), MessageType.ERROR);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        execute(e);
    }
}
