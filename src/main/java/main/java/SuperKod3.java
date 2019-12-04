package main.java;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;


public class SuperKod3 extends AnAction {

    private static boolean status = false;

    public static boolean isStatus() {
        return status;
    }

    public static void setStatus(boolean statusIn) {
        status = statusIn;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);

        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);

        Presentation presentation = anActionEvent.getPresentation();

        String message;
        if (!isStatus()) {
            presentation.setIcon(IconLoader.getIcon("/icons/chrome2_small.png"));
            setStatus(true);
            message = "Автообновление браузера ВКЛЮЧЕНО";
            System.out.println("on");
        } else {
            presentation.setIcon(IconLoader.getIcon("/icons/chrome2_small_grey.png"));
            setStatus(false);
            message = "Автообновление браузера ВЫКЛЮЧЕНА";
            System.out.println("off");
        }
        SuperKod.showMessage(ideFrame, message, MessageType.INFO);
    }
}
