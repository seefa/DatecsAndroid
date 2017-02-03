package com.adpa.printer.wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davoud on 2/3/2017.
 */

public enum DatecsStyle {

    BREAK,
    RESET,
    CENTER,
    SMALL,
    ITALIC,
    BOLD,
    RIGHT,
    LEFT,
    HIGH,
    WIDE,
    UNDERLINE;

    public List<DatecsStyle> getDatecsStyleList(){
        List<DatecsStyle> datecsStyles=new ArrayList<>();

        datecsStyles.add(DatecsStyle.BOLD);
        datecsStyles.add(DatecsStyle.BREAK);
        datecsStyles.add(DatecsStyle.CENTER);
        datecsStyles.add(DatecsStyle.HIGH);
        datecsStyles.add(DatecsStyle.ITALIC);
        datecsStyles.add(DatecsStyle.LEFT);
        datecsStyles.add(DatecsStyle.RESET);
        datecsStyles.add(DatecsStyle.RIGHT);
        datecsStyles.add(DatecsStyle.SMALL);
        datecsStyles.add(DatecsStyle.UNDERLINE);
        datecsStyles.add(DatecsStyle.WIDE);

        return datecsStyles;
    }
}
