package main.java;


import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.event.WindowFocusListener;
import java.io.PrintWriter;
import java.io.StringWriter;


public class SuperKod2 extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);

        try {

            WindowFocusListener[] focusListeners = WindowManager.getInstance().suggestParentWindow(project).getWindowFocusListeners();

            WindowFocusListener ifMy = null;
            for (WindowFocusListener focusListener : focusListeners) {
                if (focusListener instanceof MyWindowFocusListener) {
                    ifMy = focusListener;
                    break;
                }
            }

            String message;
            if (ifMy == null) {
                WindowManager.getInstance().suggestParentWindow(project).addWindowFocusListener(new MyWindowFocusListener());
                message = "auto collection is ON";
                anActionEvent.getPresentation().setIcon(IconLoader.getIcon("/icons/sencha_color_smal.png"));
            } else {
                WindowManager.getInstance().suggestParentWindow(project).removeWindowFocusListener(ifMy);
                message = "auto collection is OFF";
                anActionEvent.getPresentation().setIcon(IconLoader.getIcon("/icons/sencha_gray_smal.png"));
            }

            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(message, MessageType.INFO, null)
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
    public boolean isDumbAware() {
        return false;
    }
}
