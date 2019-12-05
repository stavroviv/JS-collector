package main.java;

import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

class CustomWindowFocusListener implements WindowFocusListener {
    private AnActionEvent anActionEvent;

    CustomWindowFocusListener(AnActionEvent anActionEvent) {
        super();
        this.anActionEvent = anActionEvent;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        ExtCollector.execute(anActionEvent);
    }
}