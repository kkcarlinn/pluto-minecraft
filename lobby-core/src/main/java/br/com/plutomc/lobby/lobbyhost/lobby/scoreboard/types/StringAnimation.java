package br.com.plutomc.lobby.lobbyhost.lobby.scoreboard.types;

import java.util.ArrayList;
import java.util.List;

public class StringAnimation {

    private String string;
    private List<String> strings;
    private int index;
    private boolean bools;

    public StringAnimation(String title, String color1, String color2, String color3) {
        this(title, color1, color2, color3, 12);
    }

    public StringAnimation(String title, String color1, String color2, String color3, int value) {
        this.string = title;
        this.strings = new ArrayList<>();
        create(color1, color2, color3, value);
    }

    public void create(String color1, String color2, String color3, int value) {
        if (string != null && !string.isEmpty()) {
            for (int i = 0; i < string.length(); i++)
                if (string.charAt(i) != ' ')
                    strings.add(color1 + string.substring(0, i) + color2 + string.charAt(i) + color3 + string.substring(i + 1));

            for (int i = 0; i < value;  i++)
                strings.add(color1 + string);

            for (int i = 0; i < string.length(); i++)
                if(string.charAt(i) != ' ')
                    strings.add(color3 + string.substring(0, i) + color2 + string.charAt(i) + color1 + string.substring(i + 1));

            for (int i = 0; i < value; i++)
                strings.add(color3 + string);
        }
    }

    public String next() {
        if (strings.isEmpty())
            return "";

        if (bools) {
            index--;
            if (index <= 0)
                bools = false;
        }
        else
        {
            index++;
            if (index >= strings.size()) {
                bools = true;
                return next();
            }
        }
        return strings.get(index);
    }

}