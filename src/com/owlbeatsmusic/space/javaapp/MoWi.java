package com.owlbeatsmusic.space.javaapp;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class MoWi {

    // Integers
    public final int windowWidth = 900;
    public final int windowHeight = 600;
    public final int windowX = (1920 - windowWidth)/2;
    public final int windowY = (1080 - windowHeight)/2;
    private final int consoleWidth = windowWidth/9-2;
    private final int consoleHeight = windowHeight/19-2;
    private int localX = 0;
    private int localY = 0;
    private int dragX = 0;
    private int dragY = 0;

    // Objects
    private Object[][][] pixels = new Object[consoleHeight][consoleWidth][2];
    private Color bgColor;
    private Color fgColor = new Color ( 255, 255, 255 );
    private Font font = new Font("Consolas", Font.BOLD, 16);
    private JFrame root = new JFrame();
    private JPanel panel = new JPanel();
    private JTextPane textPane = new JTextPane() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isInGui) {
                g.setColor(Color.white);
                g.fillRect(0, 0, windowWidth, windowHeight);

                g.setColor(Color.white);
                g.fillRect(0, 0, windowWidth, windowHeight);
            }
        }
    };
    private StyledDocument doc = textPane.getStyledDocument();
    private final Console console = new Console();
    private Socket s;

    // Colors
    private Style white = textPane.addStyle("", null);
    private Style fadedWhite = textPane.addStyle("", null);
    private Style green = textPane.addStyle("", null);
    private Style fadedGreen = textPane.addStyle("", null);
    private Style red = textPane.addStyle("", null);
    private Style fadedRed = textPane.addStyle("", null);
    private Style blue = textPane.addStyle("", null);
    private Style fadedBlue = textPane.addStyle("", null);

    private Style popColor = textPane.addStyle("", null);
    private Style normalPopColor = textPane.addStyle("", null);
    private Style fadedPopColor = textPane.addStyle("", null);

    private Style defaultColor = textPane.addStyle("", null);
    private Style normalDefaultColor = textPane.addStyle("", null);
    private Style fadedDefaultColor = textPane.addStyle("", null);

    // Booleans
    public boolean isInDebugConsole = false;
    private boolean widgetDebugging = false;
    private boolean printToIDEConsole = false;
    public boolean consoleKeyboardShortcut = true;
    private boolean isInGui = false;



    // <-------| MAIN |------->

    class WindowEventHandler extends WindowAdapter {
        public void windowClosing(WindowEvent evt) {
            try {
                s.close();
            } catch (Exception ignored) {}
        }
    }

    // Setup JFrame and all components and start rendering the window
    public void render() {

        // Colors
        bgColor = new Color ( 0, 0, 0 );
        StyleConstants.setForeground(white, new Color(255, 255, 255));
        StyleConstants.setForeground(fadedWhite, new Color(70, 70, 70));

        StyleConstants.setForeground(green, new Color(30, 255, 30));
        StyleConstants.setForeground(fadedGreen, new Color(0, 70, 0));

        StyleConstants.setForeground(red, new Color(255, 30, 30));
        StyleConstants.setForeground(fadedRed, new Color(70, 0, 0));

        StyleConstants.setForeground(blue, new Color(100, 100, 255));
        StyleConstants.setForeground(fadedBlue, new Color(0, 0, 70));

        defaultColor = white;
        normalDefaultColor = defaultColor;
        fadedDefaultColor = fadedWhite;

        popColor = green;
        normalPopColor = popColor;
        fadedPopColor = fadedGreen;

        // TextPane
        textPane.setBackground(bgColor);
        textPane.setForeground(fgColor);
        textPane.setFont(font);
        textPane.setHighlighter(null);
        textPane.setSelectionColor(bgColor);
        textPane.setEditable(false);
        textPane.setSize(consoleWidth, consoleHeight);
        textPane.setMinimumSize(new Dimension(consoleWidth, consoleHeight));
        textPane.setMaximumSize(new Dimension(consoleWidth, consoleHeight));
        clearPixels();

        // JPanel
        panel.setBackground(Color.black);
        panel.setMaximumSize(new Dimension(consoleWidth, consoleHeight));
        panel.setMinimumSize(new Dimension(consoleWidth, consoleHeight));
        panel.setMaximumSize(new Dimension(consoleWidth, consoleHeight));


        // JFrame
        root.setBackground(Color.white);
        root.setVisible(true);
        root.setResizable(true);
        root.addWindowListener(new WindowEventHandler());
        root.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        root.add(panel);
        panel.setLayout(new BorderLayout(1,1));
        panel.add(textPane);

        root.setResizable(true);

        root.setSize(windowWidth, windowHeight);
        root.setLocation(windowX, windowY);

        // Begin rendering screen
        renderScreen();
        clearPixels();
        renderWindow();
        renderScreen();

        startMouseInput();
    }

    // Simpler print to console
    public void print(String s) {
        console.print(s);
    }

    // Enable debugging for interaction with widgets
    public void setWidgetDebugging(boolean widgetDebugging, boolean printToIDEConsole) {
        this.widgetDebugging = widgetDebugging;
        this.printToIDEConsole = printToIDEConsole;
    }



    // <-------| RENDERER |------->

    // Clear all pixels to a blank screen
    private void clearPixels() {

            // Clear text
            textPane.setText("");

            // Add all characters
            for (int y = 0; y < pixels.length; y++) {
                for (int x = 0; x < pixels[0].length; x++) {
                    pixels[y][x] = new Object[]{" ", defaultColor};
                }
            }

    }

    // Render small changes instead of render full screen || (deleting pixel and inserting another)
    private void renderChange(int x, int y, char letter, Style color) {
        try {
            int position = (x + y*(consoleWidth));
            doc.remove(position, 1);
            doc.insertString(position, String.valueOf(letter), color);
        } catch(BadLocationException ignored) {}
    }

    // Render full screen
    private void renderScreen() {
        try {
            textPane.setText("");
            for (int y = 0; y < pixels.length; y++) {
                for (int x = 0; x < pixels[0].length - 1; x++) {
                    doc.insertString(doc.getLength(), String.valueOf(pixels[y][x][0]), (AttributeSet) pixels[y][x][1]);
                }
                doc.insertString(doc.getLength(), "\n", defaultColor);
            }
        } catch (Exception ignored) {}
    }



    // <-------| INPUT |------->

    private void startMouseInput() {
        root.requestFocus();
        textPane.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                if(!isInDebugConsole) {
                    // Update mouse position
                    localX = ((e.getX() - 30) / 9);
                    localY = ((e.getY()) / 19);

                    for (InputWidget inputWidget : inputWidgets) {
                        inputWidget.drag(localX,localY);
                    }
                }
            }
            @Override public void mouseMoved(MouseEvent e) {

                if(!isInDebugConsole) {
                    // Update mouse position
                    localX = ((e.getX()) / 9);
                    localY = ((e.getY()) / 19);

                    for (InputWidget inputWidget : inputWidgets) {
                        inputWidget.hover();
                    }
                }

            }
        });
        textPane.addMouseListener(new MouseListener() {
            @Override public void mousePressed(MouseEvent e) {
                if(!isInDebugConsole) {
                    // Update mouse position
                    localX = ((e.getX() - 30) / 9) + 3;
                    localY = ((e.getY()) / 19);

                    for (InputWidget inputWidget : inputWidgets) {
                        inputWidget.click();
                    }
                }
            }
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseClicked(MouseEvent e) {}

            @Override public void mouseReleased(MouseEvent e) {
                root.requestFocus();
                if(!isInDebugConsole) {

                    // Update Mouse pos & reset variables
                    localX = ((e.getX() - 30) / 9);
                    localY = ((e.getY()) / 19);

                }
            }
        });
    }
    public void startKeyboardInput() {
        root.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 27) {
                    if (consoleKeyboardShortcut)
                        console.toggleOnOff();
                }
                else if (e.getKeyCode() == 16) {}
                else if (e.getKeyCode() == 8) {
                    if (isInDebugConsole) {
                        console.deleteCharacter();
                    }
                }
                else if (e.getKeyCode() == 10) {
                    if (isInDebugConsole) {
                        console.sendCommand();
                    }
                }
                else if (e.getKeyCode() == 40) {
                    if (isInDebugConsole) {
                        console.startIndex += 1;
                        console.render();
                    }
                }
                else if (e.getKeyCode() == 38) {
                    if (isInDebugConsole) {
                        console.startIndex -= 1;
                        console.render();
                    }
                }
                else {
                    if (isInDebugConsole) {
                        console.typeCharacter(e.getKeyChar());
                    }
                }
            }
        });
    }



    // <-------| WINDOW |------->
    private InputWidget[] inputWidgets = new InputWidget[] {};

    // Render all widgets and window components
    public void renderWindow() {

        for (InputWidget inputWidget : inputWidgets) {
            inputWidget.render();
        }
        root.requestFocus();
        if(isInDebugConsole) console.render();
    }

    public void placeWidget(InputWidget widget, int x, int y) {
        System.out.println(widget.getClass());
        InputWidget[] tempWidgets = new InputWidget[inputWidgets.length+1];
        System.arraycopy(inputWidgets, 0, tempWidgets, 0, inputWidgets.length);
        widget.x = x;
        widget.y = y;
        tempWidgets[tempWidgets.length-1] = widget;
        inputWidgets = tempWidgets;
        System.out.println(Arrays.toString(inputWidgets));
    }
    public void removeWidget(InputWidget widget) {
        InputWidget[] tempInputs = new InputWidget[inputWidgets.length];
        System.arraycopy(inputWidgets, 0, tempInputs, 0, inputWidgets.length);

        int counter1 = 0;
        for (int i = 0; i < tempInputs.length; i++) {
            if (tempInputs[i] == widget) {
                tempInputs[i] = null;
            } else {
                counter1++;
            }
        }
        inputWidgets = new InputWidget[counter1];
        int index = 0;
        for (InputWidget tempInput : tempInputs) {
            if (tempInput != null) {
                inputWidgets[index] = tempInput;
                index += 1;
            }
        }
    }



    // <-------| WIDGETS |------->

    public abstract static class InputWidget {
        public int x;
        public int y;

        public void render() {}
        public void click() {}
        public void hover() {}
        public void drag(int startX, int startY) {}
    }

    public class Text {
        public void render(String title, Style color, int x, int y) {
            if (x + title.length() < consoleWidth-3) {
                for (int i = 0; i < title.length(); i++) {
                    pixels[y][x + i] = new Object[]{title.charAt(i), color};
                    renderChange(x + i, y, title.charAt(i), color);
                }
            }
        }
    }
    public class Rect {
        public int index = 0;
        public void render(int width, int height, int posX, int posY, Style color) {
            index = posX;

            // First Line
            pixels[posY][index] = new Object[]{'╔', color};
            index++;
            for (int x = 0; x < width-2; x++) {
                pixels[posY][index] = new Object[]{'═', color};
                index++;
            }
            pixels[posY][index] = new Object[]{'╗',  color};
            index = posX;

            // All Lines Between
            for (int y = 1; y < height; y++) {
                pixels[posY+y][index] = new Object[]{'║', color};
                index++;
                for (int x = 0; x < width-2; x++) {
                    pixels[posY+y][index] = new Object[]{' ', color};
                    index++;
                }
                pixels[posY+y][index] = new Object[]{'║', color};
                index = posX;
            }

            // Last Line
            pixels[posY + height][index] = new Object[]{'╚', color};
            index++;
            for (int x = 0; x < width-2; x++) {
                pixels[posY + height][index] = new Object[]{'═', color};
                index++;
            }
            pixels[posY + height][index] = new Object[]{'╝', color};
            index = posX;
            renderScreen();
        }
        public void quickRender(int width, int height, int posX, int posY, Style color) {
            index = posX;

            // First Line
            pixels[posY][index] = new Object[]{'╔', color};
            renderChange(index, posY,'╔', color );
            index++;
            for (int x = 0; x < width-2; x++) {
                pixels[posY][index] = new Object[]{'═', color};
                renderChange(index, posY,'═', color );
                index++;
            }
            pixels[posY][index] = new Object[]{'╗',  color};
            renderChange(index, posY,'╗', color );
            index = posX;

            // All Lines Between
            for (int y = 1; y < height; y++) {
                pixels[posY+y][index] = new Object[]{'║', color};
                renderChange(index, posY+y,'║', color );
                index++;
                for (int x = 0; x < width-2; x++) {
                    pixels[posY+y][index] = new Object[]{' ', color};
                    renderChange(index, posY+y,' ', color );
                    index++;
                }
                pixels[posY+y][index] = new Object[]{'║', color};
                renderChange(index, posY+y,'║', color );
                index = posX;
            }

            // Last Line
            pixels[posY + height][index] = new Object[]{'╚', color};
            renderChange(index, posY+height,'╚', color );
            index++;
            for (int x = 0; x < width-2; x++) {
                pixels[posY + height][index] = new Object[]{'═', color};
                renderChange(index, posY+height,'═', color );
                index++;
            }
            pixels[posY + height][index] = new Object[]{'╝', color};
            renderChange(index, posY+height,'╚', color );
            index = posX;
        }
    }
    public class HalfRect {
        public int index = 0;
        public int width = 2;
        public void render(int height, int posX, int posY) {
            index = posX;

            // First Line
            pixels[posY][index] = new Object[]{'╔', defaultColor};
            index++;
            for (int x = 0; x < width-2; x++) {
                pixels[posY][index] = new Object[]{'═', defaultColor};
                index++;
            }
            pixels[posY][index] = new Object[]{'╗',  defaultColor};
            index = posX;

            // All Lines Between
            for (int y = 1; y < height; y++) {
                pixels[posY+y][index] = new Object[]{'║', defaultColor};
                index++;
                for (int x = 0; x < width-2; x++) {
                    pixels[posY+y][index] = new Object[]{' ', defaultColor};
                    index++;
                }
                index = posX;
            }

            // Last Line
            pixels[posY + height][index] = new Object[]{'╚', defaultColor};
            index++;
            for (int x = 0; x < width-2; x++) {
                pixels[posY + height][index] = new Object[]{'═', defaultColor};
                index++;
            }
            index = posX;
            renderScreen();
        }
    }

    // Buttons
    public abstract static class Button extends InputWidget {
        public int length;
        public String title = "none";

        public enum VisualState {
            NORMAL,
            HOVER
        }
        public VisualState visualState = VisualState.NORMAL;
    }
    public class ActionButton extends Button {

        public ActionButton(String title) {
            this.title = title;
            this.length = title.length();
        }

        public void render() {
            switch (visualState) {
                case NORMAL -> {
                    new Text().render(title, defaultColor, x, y);
                }
                case HOVER -> {
                    new Text().render(title, popColor, x, y);
                }
            }
        }

        public void onClick() {}

        public void click() {
            if (localX >= x+windowX && localX < x+windowX+length && localY == y+windowY) {
                visualState = VisualState.HOVER;
                render();
                if (widgetDebugging) console.print("<b>[ ActionButton (Clicked) ]");
                onClick();
            }
        }
        public void hover() {
            if (localX >= x && localX < x+length && localY == y) {
                visualState = VisualState.HOVER;
            } else {
                visualState = VisualState.NORMAL;
            }
            render();
        }
    }
    public class ToggleButton extends Button {
        public ToggleButton(String title) {
            this.length = title.length() + 4;
            this.title = title;
        }
        public boolean isOn = false;

        public void render() {
            switch (visualState) {
                case NORMAL -> new Text().render("[ ] " + title, defaultColor, x, y);
                case HOVER -> new Text().render("[ ] " + title, popColor, x, y);
            }
            if (isOn) {
                new Text().render("•", popColor, x + 1, y);
            }
            else {
                new Text().render("-", defaultColor, x + 1, y);
            }
        }

        public void hover() {
            if (localX >= x && localX < x+length && localY == y) {
                visualState = VisualState.HOVER;
            } else {
                visualState = VisualState.NORMAL;
            }
            render();
        }

        public void click() {
            if (localX >= x && localX < x+length && localY == y) {
                isOn = !isOn;
                visualState = VisualState.HOVER;
                render();
                if (widgetDebugging)
                    print("<b>[ ToggleButton ("+isOn+") ]");
            }
        }

    }

    // Misc
    public class ListBox extends InputWidget {
        public ListBox(String title, String[] inputOptions, boolean showCounter) {
            this.length = inputOptions.length;
            this.title = title;
            this.inputOptions = inputOptions;
            this.showCounter = showCounter;

        }
        private boolean firstTimeRender = true;
        public int length;
        public String title;
        public String[] inputOptions;
        public boolean showCounter;
        private int counter;
        public String[] selectedValues = {};

        private class Option {
            public Option(String title, int index, int x) {
                this.title = title;
                this.index = index;
                this.x = x;
                this.length = x + title.length();
            }
            String title;
            boolean isSelected;
            int index;
            int x;
            int length;
        }
        private Option[] options = {};

        public void render() {
            System.out.println("listbox render");

            if (firstTimeRender) {
                options = new Option[] {};
                for (String inputOption : inputOptions) {
                    addOption(inputOption);
                }
                firstTimeRender = false;
            }

            new HalfRect().render(length+1, x, y);
            new Text().render("["+title+"]", defaultColor, x+1, y);

            for (Option option : options) {
                if (option.isSelected) {
                    new Text().render("∙", defaultColor, x + 1, y + 1 + option.index);
                    new Text().render(option.title, popColor, x + 2, y + 1 + option.index);
                }
                else {
                    new Text().render(" ", popColor, x + 1, y + 1 + option.index);
                    new Text().render(option.title, defaultColor, x + 2, y + 1 + option.index);
                }
            }

            if (showCounter) {
                new Text().render("  ", defaultColor, x + 10, y+length+1);
                new Text().render("[All : ", defaultColor, x+1, y+length+1);
                new Text().render(String.valueOf(counter), popColor, x+8, y+length+1);
                new Text().render("]", defaultColor, x+8+String.valueOf(counter).length(), y+length+1);
            }

        }

        public void addOption(String title) {
            Option[] tempOptions = new Option[options.length+1];
            System.arraycopy(options, 0, tempOptions, 0, options.length);
            tempOptions[tempOptions.length-1] = new Option(title, options.length, x+2);
            options = tempOptions;
        }

        public void addSelectedValue(String string) {
            if (widgetDebugging) console.print("<b>[ ListBox selected ("+string+") ]");
            String[] tempSelectedValues = new String[selectedValues.length+1];
            System.arraycopy(selectedValues, 0, tempSelectedValues, 0, selectedValues.length);
            tempSelectedValues[tempSelectedValues.length-1] = string;
            selectedValues = tempSelectedValues;
        }

        public void deleteSelectedValue(String string) {
            if (widgetDebugging) console.print("<b>[ ListBox unselected ("+string+") ]");
            String[] tempSelectedValues = new String[selectedValues.length];
            System.arraycopy(selectedValues, 0, tempSelectedValues, 0, selectedValues.length);

            int counter1 = 0;
            for (int i = 0; i < tempSelectedValues.length; i++) {
                if (tempSelectedValues[i] == string) {
                    tempSelectedValues[i] = "$none";
                } else {
                    counter1++;
                }
            }
            String[] newArray = new String[counter1];
            int index = 0;
            for (int i = 0; i < tempSelectedValues.length; i++) {
                if (!tempSelectedValues[i].equals("$none")) {
                    newArray[index] = tempSelectedValues[i];
                    index += 1;
                }
            }
            selectedValues = newArray;
        }

        public void click() {
            for (Option option : options) {
                if (localX >= option.x && localX < option.x+option.title.length() && localY == y+1+option.index) {
                    option.isSelected = !option.isSelected;
                    if (option.isSelected) {
                        counter++;
                        addSelectedValue(option.title);
                    }
                    else {
                        counter--;
                        deleteSelectedValue(option.title);
                    }
                    render();
                }
            }
        }

        public void hover() {
            for (Option option : options) {
                if (localX >= option.x && localX < option.x+option.title.length() && localY == y+1+option.index) {
                    if (option.isSelected) {
                        new Text().render("•", defaultColor, x + 1, y + 1 + option.index);
                    }
                    new Text().render(option.title, popColor, option.x, y+1+option.index);
                }
                else {
                    if (!option.isSelected) {
                        new Text().render(option.title, defaultColor, option.x, y + 1 + option.index);
                    }
                    else {
                        new Text().render("∙", defaultColor, x + 1, y + 1 + option.index);
                    }
                }
            }
        }
    }
    public class RadialBox extends InputWidget {
        public RadialBox(String title, String[] inputOptions, boolean showIndex) {
            this.length = inputOptions.length;
            this.title = title;
            this.inputOptions = inputOptions;
            this.showIndex = showIndex;

        }
        private boolean firstTimeRender = true;
        public int length;
        public String title;
        public String[] inputOptions;
        public boolean showIndex;
        public String selectedValue = "";
        public int index;

        private class Option {
            public Option(String title, int index, int x) {
                this.title = title;
                this.index = index;
                this.x = x;
                this.length = x + title.length();
            }
            String title;
            boolean isSelected;
            int index;
            int x;
            int length;
        }
        private Option[] options = {};

        public void render() {

            if (firstTimeRender) {
                for (String inputOption : inputOptions) {
                    addOption(inputOption);
                }
                firstTimeRender = false;
            }

            new HalfRect().render(length+1, x, y);
            new Text().render("("+title+")", defaultColor, x+1, y);

            for (Option option : options) {
                if (option.isSelected) {
                    new Text().render(">", defaultColor, x + 1, y + 1 + option.index);
                    new Text().render(option.title, popColor, x + 2, y + 1 + option.index);
                }
                else {
                    new Text().render(" ", popColor, x + 1, y + 1 + option.index);
                    new Text().render(option.title, defaultColor, x + 2, y + 1 + option.index);
                }
            }


            if (showIndex) {
                new Text().render("  ", defaultColor, x + 12, y+length+1);

                new Text().render("(Index : ", defaultColor, x+1, y+length+1);
                new Text().render(String.valueOf(index), popColor, x+10, y+length+1);
                new Text().render(")", defaultColor, x+10+String.valueOf(index).length(), y+length+1);
            }

        }

        public void addOption(String title) {
            Option[] tempOptions = new Option[options.length+1];
            System.arraycopy(options, 0, tempOptions, 0, options.length);
            tempOptions[tempOptions.length-1] = new Option(title, options.length, x+2);
            options = tempOptions;
        }

        public void click() {
            for (Option option : options) {
                if (localX >= option.x && localX < option.x+option.title.length() && localY == y+1+option.index) {
                    for (Option option1 : options) {
                        if (option1.isSelected) {
                            option1.isSelected = false;
                        }
                    }
                    selectedValue = option.title;
                    index = option.index;
                    option.isSelected = true;
                    render();
                    if (widgetDebugging) console.print("<b>[ RadialBox selected ("+option.title+") ]");
                }
            }
        }

        public void hover() {
            for (Option option : options) {
                if (localX >= option.x && localX < option.x+option.title.length() && localY == y+1+option.index) {
                    if (option.isSelected) {
                        new Text().render(">", defaultColor, x + 1, y + 1 + option.index);
                    }
                    new Text().render(option.title, popColor, option.x, y+1+option.index);
                }
                else {
                    if (!option.isSelected) {
                        new Text().render(option.title, defaultColor, option.x, y + 1 + option.index);
                    }
                    else {
                        new Text().render("-", defaultColor, x + 1, y + 1 + option.index);
                    }
                }
            }
        }
    }
    public class Slider extends InputWidget {
        public Slider(int length, String key, int value, boolean showKey, boolean showValue, boolean showHoverValue) {
            this.length = length;
            this.key = key;
            this.value = value;
            this.showKey = showKey;
            this.showValue = showValue;
            this.showHoverValue = showHoverValue;
        }

        public int length;
        public String key;
        public int value;
        public int hoverValue;
        public boolean showKey;
        public boolean showValue;
        public boolean showHoverValue;
        public int xPadding;

        public void render() {

            if (showKey) {
                xPadding = key.length() + 1;
                int valueOffset = -xPadding-1;
                if (localX >= x && localX < x+length && localY == y) {
                    new Text().render(key + " ", popColor, x + valueOffset, y);
                }
                else {
                    new Text().render(key + " ", defaultColor, x + valueOffset, y);
                }
            }

            if (showHoverValue) {
                int hoverValueOffset = 5;
                new Text().render("  ", defaultColor, x+length+hoverValueOffset, y);
                new Text().render(String.valueOf(hoverValue), popColor, x+length+hoverValueOffset, y);
            }

            if (showValue) {
                int valueOffset = 2;
                new Text().render("  ", defaultColor, x+length+valueOffset, y);
                new Text().render(String.valueOf(value), defaultColor, x+length+valueOffset, y);
            }

            new Text().render("[", defaultColor, x-1, y);
            for (int i = 0; i < length; i++) {
                new Text().render("-", defaultColor, x+i, y);
            }
            new Text().render("]", defaultColor, x+length, y);

            pixels[y][x + value] = new Object[]{'•', popColor};
            renderChange(x + value, y, '•', popColor);


        }

        public void click() {
            if (localX >= x && localX < x+length && localY == y) {
                value = localX - x;
                render();
                if (widgetDebugging) console.print("<b>[ Slider selected ("+value+") ]");
            }
        }

        public void hover() {
            if (localX >= x && localX < x+length && localY == y && localX != value+x) {
                hoverValue = localX - x;
                render();
                pixels[localY][localX] = new Object[]{'∙', popColor};
                renderChange(localX, y, '∙', popColor);
            }
            else {
                hoverValue = 0;
                render();
            }
        }
    }
    public class TextBox extends InputWidget {
        public int width;
        public int height;

        private int startIndex = 0;
        private int oldStartIndex = 0;
        private String[] text;

        public TextBox(int width, int height, String title, String inputText) {
            this.width = width;
            this.height = height;

            inputText = "<g>"+title+"\n"+inputText;

            for (int i = 0; i < inputText.length(); i++) {
                if (i!=0)if(i % (width-2) == 0) {
                    inputText = inputText.substring(0, i) + "\n" + inputText.substring(i);
                }
            }
            text = inputText.split("\n");
        }

        public void render() {

            new Rect().render(width, height, x, y, defaultColor);
            for (int i = 0; i < height - 1; i++) {
                try {
                    if (text[startIndex+i].startsWith("<g>")) {
                        new Text().render(text[startIndex+i].substring(3), popColor, x + 1, y + 1 + i);
                    }
                    else {
                        new Text().render(text[startIndex+i], defaultColor, x + 1, y + 1 + i);
                    }

                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }

            new Text().render("#", defaultColor, x + width - 1, y + 1);
            new Text().render("#", defaultColor, x + width - 1, y + height - 1);


        }
        public void replaceText(String title, String inputText) {
            inputText = "<g>"+title+"\n"+inputText;

            for (int i = 0; i < inputText.length(); i++) {
                if (i!=0)if(i % (width-2) == 0) {
                    inputText = inputText.substring(0, i) + "\n" + inputText.substring(i);
                }
            }
            text = inputText.split("\n");
        }
        public void click() {
            oldStartIndex = startIndex;
            if (localX == x+width-1 && localY == y+1) {
                new Text().render("#", popColor, x + width - 1, y + 1);
                if (startIndex > 0) {
                    startIndex--;
                }
                render();
            }
            if (localX == x+width-1  && localY == y + height - 1) {
                new Text().render("#", popColor, x + width - 1, y + height - 1);
                if (startIndex < text.length-height+2) {
                    startIndex++;
                }
                render();
            }
            hover();
        }
        public void hover() {
            if (localX == x+width-1 && localY == y+1) {
                new Text().render("#", popColor, x + width - 1, y + 1);
            }
            else {
                new Text().render("#", defaultColor, x + width - 1, y + 1);

            }
            if (localX == x+width-1 && localY == y + height - 1) {
                new Text().render("#", popColor, x + width - 1, y + height - 1);
            }
            else {
                new Text().render("#", defaultColor, x + width - 1, y + height - 1);

            }

        }
        /*
        public void drag(int startX, int startY) {
            if (localX > x+1 && localX < x+width-3 && localY > y+1 && localY < y+height-3) {
                startIndex = oldStartIndex + dragY - localY;
                render();
            }
        }
        */
    }

    // Console
    public class Console extends InputWidget {
        public int width;
        public int height;

        private int startIndex;
        private String[] text;
        private String currentCommand = "";

        public Console() {
            this.x = 4;
            this.y = 2;
            this.width = consoleWidth-8;
            //this.height = 8;
            this.height = consoleHeight-5;
            //String startMessage = "Console v0.1";
//
            //for (int i = 0; i < startMessage.length(); i++) {
            //    if (i!=0)
            //        if(i % (width-2) == 0) {
            //        startMessage = startMessage.substring(0, i) + "\n" + startMessage.substring(i);
            //    }
            //}
            //text = startMessage.split("\n");
            text = new String[height];
            Arrays.fill(text, "");
            startKeyboardInput();
        }

        public void render() {
            new Rect().render(width, height, x, y, white);
            new Text().render("DebugConsole", white, x + 1, y);
            for (int i = 1; i < height - 1; i++) {
                try {
                    if (text[startIndex+i].startsWith("<g>")) {
                        new Text().render(text[startIndex+i].substring(3), green, x + 1, y + i);
                    }
                    else if (text[startIndex+i].startsWith("<r>")) {
                        new Text().render(text[startIndex+i].substring(3), red, x + 1, y + i);
                    }
                    else if (text[startIndex+i].startsWith("<b>")) {
                        new Text().render(text[startIndex+i].substring(3), blue, x + 1, y + i);
                    }
                    else if (text[startIndex+i].startsWith("<w>")) {
                        new Text().render(text[startIndex+i].substring(3), white, x + 1, y + i);
                    }
                    else {
                        new Text().render(text[startIndex+i], white, x + 1, y + i);
                    }

                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
            new Text().render(" ".repeat(currentCommand.length()), white, x + 1, y+height-1);
            new Text().render(currentCommand, white, x + 1, y+height-1);

        }
        public void renderText() {
            for (int i = 1; i < height - 1; i++) {
                try {
                    if (text[startIndex+i].startsWith("<g>")) {
                        new Text().render(text[startIndex+i].substring(3), green, x + 1, y + i);
                    }
                    else if (text[startIndex+i].startsWith("<r>")) {
                        new Text().render(text[startIndex+i].substring(3), red, x + 1, y + i);
                    }
                    else if (text[startIndex+i].startsWith("<b>")) {
                        new Text().render(text[startIndex+i].substring(3), blue, x + 1, y + i);
                    }
                    else if (text[startIndex+i].startsWith("<w>")) {
                        new Text().render(text[startIndex+i].substring(3), white, x + 1, y + i);
                    }
                    else {
                        new Text().render(text[startIndex+i], white, x + 1, y + i);
                    }

                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        public void typeCharacter(char c) {

            currentCommand += c;
            new Text().render(" ".repeat(currentCommand.length()+1), defaultColor, x + 1, y+height-1); // Clear line behind
            new Text().render(currentCommand, white, x + 1, y+height-1);

        }
        public void deleteCharacter() {
            try {currentCommand = Str.delLastChars(currentCommand, 1);}catch (Exception ignored) {}
            new Text().render(" ".repeat(currentCommand.length()+1), defaultColor, x + 1, y+height-1); // Clear line behind
            new Text().render(currentCommand, white, x + 1, y+height-1);
        }
        public void sendCommand() {

            // Add command string to all console text
            String[] temp = new String[text.length+1];
            System.arraycopy(text, 0, temp, 0, text.length);
            temp[temp.length-1] = currentCommand;
            text = temp;
            startIndex = text.length-height+1;
            render();

            // Detect all commands

            // Connect to socket
            if ((currentCommand+" ").split(" ")[0].equals("connect") || (currentCommand+" ").split(" ")[0].equals("c")) {
                try {
                    int port = Integer.parseInt(currentCommand.split(" ")[1]);
                    print(connectToServer(port));
                } catch (ArrayIndexOutOfBoundsException e) {
                    print("<r>[ Expected port ]");
                }
            }

            // Set popColor
            else if ((currentCommand+" ").split(" ")[0].equals("select")) {
                try {
                    String selectedColor = currentCommand.split(" ")[1];
                    switch (selectedColor) {
                        case "green" -> {
                            popColor = fadedGreen;
                            fadedPopColor = fadedGreen;
                            normalPopColor = green;
                            print("<g>[ Color is now green ]");
                        }
                        case "red" -> {
                            popColor = fadedRed;
                            fadedPopColor = fadedRed;
                            normalPopColor = red;
                            print("<r>[ Color is now red ]");
                        }
                        case "blue" -> {
                            popColor = fadedBlue;
                            fadedPopColor = fadedBlue;
                            normalPopColor = blue;
                            print("<b>[ Color is now blue ]");
                        }
                        case "white" -> {
                            popColor = fadedWhite;
                            fadedPopColor = fadedWhite;
                            normalPopColor = white;
                            print("<w>[ Color is now white ]");
                        }
                        default -> print("<r>[ No Such Color ]");
                    }
                    renderWindow();
                } catch (ArrayIndexOutOfBoundsException e) {
                    print("<r>[ Expected color name ]");
                }
            }

            // Help command
            else if ((currentCommand+" ").split(" ")[0].equals("help") || (currentCommand+" ").split(" ")[0].equals("?")) {
                print("help                - lists all commands");
                print("default {colorName} - set the default color");
                print("select  {colorName} - set the default color");
                print("print   {message}   - prints command");
            }

            // Set defaultColor
            else if ((currentCommand+" ").split(" ")[0].equals("default")) {
                try {
                    String selectedColor = currentCommand.split(" ")[1];
                    switch (selectedColor) {
                        case "green" -> {
                            defaultColor = fadedGreen;
                            fadedDefaultColor = fadedGreen;
                            normalDefaultColor = green;
                            print("<g>[ Color is now green ]");
                        }
                        case "red" -> {
                            defaultColor = fadedRed;
                            fadedDefaultColor = fadedRed;
                            normalDefaultColor = red;
                            print("<r>[ Color is now red ]");
                        }
                        case "blue" -> {
                            defaultColor = fadedBlue;
                            fadedDefaultColor = fadedBlue;
                            normalDefaultColor = blue;
                            print("<b>[ Color is now blue ]");
                        }
                        case "white" -> {
                            defaultColor = fadedWhite;
                            fadedDefaultColor = fadedWhite;
                            normalDefaultColor = white;
                            print("<w>[ Color is now defaultColor ]");
                        }
                        default -> print("<r>[ No Such Color ]");
                    }
                    renderWindow();
                } catch (ArrayIndexOutOfBoundsException e) {
                    print("<r>[ Expected color name ]");
                }
            }

            // Print text
            else if ((currentCommand+" ").split(" ")[0].equals("print")) {
                try {
                    print(currentCommand.split(" ")[1]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    print("<r>[ Expected message ]");
                }
            }

            // Open the gui
            else if ((currentCommand+" ").split(" ")[0].equals("gui")) {
                currentCommand = "";
                toggleOnOff();
                isInGui = true;
            }

            // Give error if none of the commands work
            else {
                print("<r>[ Unknown command ]");
            }

            currentCommand = "";
            render();
        }
        public  void setText(String[] inputText) {
            text = inputText;
            startIndex = text.length - height + 1;
            if (isInDebugConsole) render();
        }
        public void toggleOnOff() {
            isInDebugConsole = !isInDebugConsole;
            clearPixels();
            if (isInDebugConsole) {
                popColor = fadedPopColor;
                defaultColor = fadedDefaultColor;
                console.render();
            } else {
                popColor = normalPopColor;
                defaultColor = normalDefaultColor;
            }
            renderWindow();
            renderScreen();
        }

        public void print(String message) {
            if (printToIDEConsole) System.out.println(message);
            String[] messageArray = new String[1];
            try {
                for (int i = 0; i < message.length(); i++) {
                    if (i != 0) {
                        if (i % (width - 4) == 0) {
                            message = message.substring(0, i) + "\n" + message.substring(i);
                        }
                    }
                }
                messageArray = message.split("\n");
            } catch (StringIndexOutOfBoundsException ignored) {
                messageArray[0] = message;
            }
            String[] temp = new String[text.length + messageArray.length];
            System.arraycopy(text, 0, temp, 0, text.length);
            for (int i = 0; i < messageArray.length; i++) {
                temp[text.length+i] = messageArray[i];
            }
            text = temp;
            startIndex = text.length - height + 1;
            if (isInDebugConsole) render();
        }
    }



    // <-------| EXPERIMENTAL |------->

    PrintWriter pr;
    InputStreamReader in;
    BufferedReader bf;
    private String connectToServer(int port) {
        try {
            // First time running
            if (s == null) {
                s = new Socket("localhost", port);
                pr = new PrintWriter(s.getOutputStream());
                in  = new InputStreamReader(s.getInputStream());
                bf = new BufferedReader(in);
                pr.println("Server");
                pr.flush();
            }

            // Always when run
            pr.println(".get");
            pr.flush();
            System.out.println("1");
            String message = bf.readLine();
            System.out.println("2 : " + message);
            console.setText(message.split("<space>"));
            console.render();
        } catch (IOException ignored) {
            return "";
        }
        return "";
    }
}


