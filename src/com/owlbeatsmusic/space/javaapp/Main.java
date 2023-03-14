package com.owlbeatsmusic.space.javaapp;

public class Main {



    public static void main(String[] args) {

        MoWi moWi = new MoWi();
        moWi.setWidgetDebugging(true, true);
        moWi.isInDebugConsole = false; // Open console on start
        moWi.consoleKeyboardShortcut = true; // Disable escape for console

        MoWi.ToggleButton t = moWi.new ToggleButton("Hejsan");
        moWi.placeWidget(t, 1, 1);

        moWi.render();

    }
}
