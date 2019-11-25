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
import org.jetbrains.annotations.NotNull;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;


class MyWindowFocusListener implements WindowFocusListener {

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        SuperKod.execute();
    }
}


class WrongApplicationException extends Exception { }


public class SuperKod extends AnAction {

//    static {
//
//        DataContext dataContext = DataManager.getInstance().getDataContext();
//        Project project = (Project) dataContext.getData(DataConstants.PROJECT);
//
//        WindowManager.getInstance().suggestParentWindow(project).addWindowFocusListener(new MyWindowFocusListener());
//    }

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

    public static void execute() {

        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);

        FileDocumentManager.getInstance().saveAllDocuments();

        try {

            String basedir = project.getPresentableUrl();

            String targetDirectoryName = "CRM-" + getTargetDirectoryName(basedir + "/pom.xml");

            JsFilesCollector.main(basedir, targetDirectoryName);

            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("JS files collected successfully", MessageType.INFO, null)
                    .setFadeoutTime(15000)
                    .createBalloon()
                    .show(RelativePoint.getSouthEastOf(ideFrame.getComponent()), Balloon.Position.above);

        } catch (WrongApplicationException ex) {

            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("Wrong Application!", MessageType.ERROR, null)
                    .setFadeoutTime(15000)
                    .createBalloon()
                    .show(RelativePoint.getSouthEastOf(ideFrame.getComponent()), Balloon.Position.above);

        } catch (Exception ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(errors.toString(), MessageType.ERROR, null)
                    .setFadeoutTime(15000)
                    .createBalloon()
                    .show(RelativePoint.getSouthEastOf(ideFrame.getComponent()), Balloon.Position.above);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        execute();
    }
}
