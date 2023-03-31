package com.owlbeatsmusic.space.javaapp;

public class Main {



    public static void main(String[] args) {

        MoWi moWi = new MoWi();

        MoWi.Slider slider = moWi.new Slider(10, "Ange",2, true, true, true);
        moWi.placeWidget(slider, 10, 10);

        MoWi.ToggleButton t1 = moWi.new ToggleButton("Toggle 1");
        moWi.placeWidget(t1, 1, 1);
        MoWi.ToggleButton t2 = moWi.new ToggleButton("Toggle 2");
        moWi.placeWidget(t2, 1, 2);
        MoWi.ToggleButton t3 = moWi.new ToggleButton("Toggle 3");
        moWi.placeWidget(t3, 1, 3);

        MoWi.ActionButton a1 = moWi.new ActionButton("ActionButton") {
            @Override
            public void click() {
                System.out.println("Action Button Clicked!");
            }
        };



        moWi.render();

        moWi.renderWindow();

    }
}
