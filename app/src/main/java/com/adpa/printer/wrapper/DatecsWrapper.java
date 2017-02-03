package com.adpa.printer.wrapper;

import com.adpa.printer.util.DatecPersianFormatterUtil;
import com.adpa.printer.util.PersianFormatter;
import com.datecs.api.printer.Printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Davoud on 1/25/2017.
 */

public class DatecsWrapper {

    private List<DatecsStyle> stringStyle;

    public enum PrinterType {
        DPP250(24), DPP450(60), DPP350(50), DLP621(50), DK2300(50);

        private int length;

        PrinterType(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }

    public static PrinterType printerType;

    public static PrinterType getPrinterType() {
        return printerType;
    }

    public static void setPrinterType(PrinterType printerType) {
        DatecsWrapper.printerType = printerType;
    }

    public static Printer getPrinter() {
        return printer;
    }

    public static void setPrinter(Printer printer) {
        DatecsWrapper.printer = printer;
    }

    private static Printer printer;

    public DatecsWrapper(Printer p, PrinterType printerType) {
        printer = p;
        this.printerType = printerType;
        stringStyle=new ArrayList<>();
        stringStyle.add(DatecsStyle.RESET);
    }

    public void printPersianText(String text) throws IOException {
        PersianFormatter formatter = new DatecPersianFormatterUtil();
        String result = formatter.persianFormatter(text);
        result = wrapString(result, printerType);
        printer.printArabicText(24, result);
    }

    public void printPersianText(String text, List<DatecsStyle> styles) throws IOException {
        setPrinterStyle(styles);

        PersianFormatter formatter = new DatecPersianFormatterUtil();
        String result = formatter.persianFormatter(text);
        result = wrapString(result, printerType);
        printer.printArabicText(24, result);
    }


    public String wrapString(String string, PrinterType printerType) {
        int charWrap = 0;
        switch (printerType) {
            case DPP250:
                charWrap = PrinterType.DPP250.getLength();
                break;
            case DPP450:
                charWrap = PrinterType.DPP450.getLength();
                break;
            case DPP350:
                charWrap = PrinterType.DPP350.getLength();
                break;
            case DLP621:
                charWrap = PrinterType.DLP621.getLength();
                break;
            case DK2300:
                charWrap = PrinterType.DK2300.getLength();
                break;
        }
        charWrap = charWrap + getWrapperStringStyle();

        int lastBreak = 0;
        int nextBreak = charWrap;
        if (string.length() > charWrap) {
            String setString = "";
            do {
                while (string.charAt(nextBreak) != ' ' && nextBreak > lastBreak) {
                    nextBreak--;
                }
                if (nextBreak == lastBreak) {
                    nextBreak = lastBreak + charWrap;
                }
                setString += string.substring(lastBreak, nextBreak).trim() + "\n";
                lastBreak = nextBreak;
                nextBreak += charWrap;

            } while (nextBreak < string.length());
            setString += string.substring(lastBreak).trim();
            return setString;
        } else {
            return string;
        }
    }


    public void setPrinterStyle(List<DatecsStyle> styles) throws IOException {
        stringStyle.addAll(styles);
        StringBuffer sb = new StringBuffer();
        for (DatecsStyle s : styles) {
            switch (s) {
                case BOLD:
                    sb.append("{b}");
                    break;
                case BREAK:
                    sb.append("{br}");
                    break;
                case SMALL:
                    sb.append("{s}");
                    break;
                case CENTER:
                    sb.append("{center}");
                    break;
                case HIGH:
                    sb.append("{h}");
                    break;
                case RIGHT:
                    sb.append("{right}");
                    break;
                case LEFT:
                    sb.append("{left}");
                    break;
                case ITALIC:
                    sb.append("{i}");
                    break;
                case WIDE:
                    sb.append("{w}");
                    break;
                case UNDERLINE:
                    sb.append("{u}");
                    break;
                case RESET:
                    sb.append("{reset}");
                    break;
            }
            printer.printTaggedText(sb.toString());
        }
    }

    public void setPrinterStyle(String styles) throws IOException {
        List<DatecsStyle> list = new ArrayList<>();
        Pattern p = Pattern.compile("[{][b][}]|[{][s][}]|[{][u][}]|[{][w][}]|[{][h][}]|[{][i][}]|[{](br)[}]|[{](reset)[}]|[{](left)[}]|[{](center)[}]|[{](right)[}]");
        Matcher m = p.matcher(styles);
        while (m.find()) {
            switch (m.group()) {
                case "{b}":
                    list.add(DatecsStyle.BOLD);
                    break;
                case "{br}":
                    list.add(DatecsStyle.BREAK);
                    break;
                case "{s}":
                    list.add(DatecsStyle.SMALL);
                    break;
                case "{center}":
                    list.add(DatecsStyle.CENTER);
                    break;
                case "{h}":
                    list.add(DatecsStyle.HIGH);
                    break;
                case "{right}":
                    list.add(DatecsStyle.RIGHT);
                    break;
                case "{left}":
                    list.add(DatecsStyle.LEFT);
                    break;
                case "{i}":
                    list.add(DatecsStyle.ITALIC);
                    break;
                case "{w}":
                    list.add(DatecsStyle.WIDE);
                    break;
                case "{u}":
                    list.add(DatecsStyle.UNDERLINE);
                    break;
                case "{reset}":
                    list.add(DatecsStyle.RESET);
                    break;
            }
        }
        stringStyle.addAll(list);
        printer.printTaggedText(styles);
    }

    public int getWrapperStringStyle() {
        int styleWrap = 0;
        if (stringStyle != null)
            for (DatecsStyle s : stringStyle) {
                switch (s) {
                    case BOLD:
                        styleWrap = 0;
                        break;
                    case SMALL:
                        styleWrap = 7;
                        break;
                    case HIGH:
                        styleWrap = 0;
                        break;
                    case WIDE:
                        styleWrap = -11;
                        break;
                    case RESET:
                        styleWrap = 0;
                        break;
                }
            }

        return styleWrap;
    }

}
