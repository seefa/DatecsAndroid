package com.adpa.printer.util;

/**
 * This class used for Persian formatting characters as Proxy pattern for Datecs printers
 * @author Saman Delfani
 * @version 1.0
 * @since 3 Feb 2017
 */
public class DatecPersianFormatterUtil implements PersianFormatter {

    public String persianFormatter(String inputText) {
        PersianFormatter formatter = new PersianFormatterUtil();
        return formatter.persianFormatter(inputText);
    }
}