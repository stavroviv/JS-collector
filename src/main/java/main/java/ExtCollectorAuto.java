package main.java;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.event.WindowFocusListener;
import java.io.PrintWriter;
import java.io.StringWriter;


public class ExtCollectorAuto extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);

        try {

            WindowFocusListener[] focusListeners = WindowManager.getInstance().suggestParentWindow(project).getWindowFocusListeners();

            WindowFocusListener ifMy = null;
            for (WindowFocusListener focusListener : focusListeners) {
                if (focusListener instanceof CustomWindowFocusListener) {
                    ifMy = focusListener;
                    break;
                }
            }

            String message;
            if (ifMy == null) {
                WindowManager.getInstance().suggestParentWindow(project).addWindowFocusListener(new CustomWindowFocusListener(anActionEvent));
                message = "Автосборка ВКЛЮЧЕНА";
                anActionEvent.getPresentation().setIcon(IconLoader.getIcon("/icons/sencha_color_smal.png"));
            } else {
                WindowManager.getInstance().suggestParentWindow(project).removeWindowFocusListener(ifMy);
                message = "Автосборка ВЫКЛЮЧЕНА";
                anActionEvent.getPresentation().setIcon(IconLoader.getIcon("/icons/sencha_gray_smal.png"));
            }
            ExtCollector.showMessage(ideFrame, message, MessageType.INFO);
        } catch (Exception ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            ExtCollector.showMessage(ideFrame, errors.toString(), MessageType.ERROR);
        }
    }

//    @Override
//    public boolean isDumbAware() {
//        return false;
//    }
}
