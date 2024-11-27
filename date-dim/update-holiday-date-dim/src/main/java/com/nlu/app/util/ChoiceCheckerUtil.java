package com.nlu.app.util;

public class ChoiceCheckerUtil {
    public static boolean isValidChoice(String choice) {
        return choice.equals("1") || choice.equals("2");
    }
}
