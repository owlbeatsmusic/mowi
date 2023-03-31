package com.owlbeatsmusic.space.javaapp;

public class Main {

    /*
        ATT GÖRA:
         - Fixa actionbutton Click
         - Fixa slidern
     */

    public static void main(String[] args) {
        MoWi moWi = new MoWi();


        MoWi.ActionButton actionButton = moWi.new ActionButton("ActionButton") {
            @Override
            public void click() {
                System.out.println("ActionButton Pressed!");
            }
        };
        moWi.placeWidget(actionButton, 1, 1);


        MoWi.ToggleButton toggleButton1 = moWi.new ToggleButton("ToggleButton");
        moWi.placeWidget(toggleButton1, 1, 3);
        MoWi.ToggleButton toggleButton2 = moWi.new ToggleButton("ToggleButton");
        moWi.placeWidget(toggleButton2, 1, 4);
        MoWi.ToggleButton toggleButton3 = moWi.new ToggleButton("ToggleButton");
        moWi.placeWidget(toggleButton3, 1, 5);


        MoWi.ListBox listBox = moWi.new ListBox("ListBox", new String[]{"Option 1", "Option 2"}, true);
        moWi.placeWidget(listBox, 20, 1);


        MoWi.RadialBox radialBox = moWi.new RadialBox("RadialBox", new String[]{"Option 1", "Option 2"}, true);
        moWi.placeWidget(radialBox, 20, 7);

        //MoWi.Slider slider = moWi.new Slider(7, "Slider", 3, true, true, true);
        //moWi.placeWidget(slider, 1, 7);

        MoWi.TextBox textBox = moWi.new TextBox(24, 13, "TextBox", "contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents contents ");
        moWi.placeWidget(textBox, 35, 1);


        moWi.render();
    }
}
