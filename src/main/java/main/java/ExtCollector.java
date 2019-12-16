package main.java;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
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

import java.io.*;

import static main.java.Constants.JETTY_PORT;


public class ExtCollector extends AnAction {

    static CustomSocketHandler chatWebSocketHandler;

    static {
        try {
            Server server = new Server(JETTY_PORT);
            chatWebSocketHandler = new CustomSocketHandler();
            chatWebSocketHandler.setHandler(new DefaultHandler());
            server.setHandler(chatWebSocketHandler);
            server.start();

            System.out.println("server start on port: " + JETTY_PORT);

        } catch (Exception e) {

            try {

                Server server = new Server(0);

                chatWebSocketHandler = new CustomSocketHandler();
                chatWebSocketHandler.setHandler(new DefaultHandler());
                server.setHandler(chatWebSocketHandler);
                server.start();

                JETTY_PORT = server.getURI().getPort();
                System.out.println("server start on random port: " + JETTY_PORT);

            } catch (Exception e2) {
                e2.printStackTrace();
            }

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

    static void showMessage(IdeFrame ideFrame, String message, MessageType messageType) {
        String html = "<html><body>" + message + "</body></html>";
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(html, messageType, null)
                .setFadeoutTime(10_000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(ideFrame.getStatusBar().getComponent()), Balloon.Position.above);
    }

    static void execute(AnActionEvent e) {
        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
        FileDocumentManager.getInstance().saveAllDocuments();

        try {
            String basedir = project.getPresentableUrl();
            String targetDirectoryName = "CRM-" + getTargetDirectoryName(basedir + "/pom.xml");
            boolean fileChanges = JsFilesCollector.runCollector(basedir, targetDirectoryName);

            if (fileChanges) {
                showMessage(ideFrame, "Изменений не обнаружено", MessageType.INFO);
            } else {
                showMessage(ideFrame, "Файлы собраны. Есть изменения", MessageType.INFO);
                if (ChromeUpdate.isStatus()) {
                    for (Session s : CustomSocket.sessions) {
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
//            StringWriter errors = new StringWriter();
//            ex.printStackTrace(new PrintWriter(errors));
            String errorMessage = "Произошла ошибка";
            showMessage(ideFrame, errorMessage, MessageType.ERROR);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        execute(e);
    }
}
