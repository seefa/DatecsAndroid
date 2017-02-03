package com.adpa.printer.util;

/**
 * This interface is Persian proxy interface
 * @author Saman Delfani
 * @version 1.0
 * @since 2 Feb 2017
 */
public interface PersianFormatter {
    /**
     * This method used as facade method to encapsulate Persian characters formatting
     * @param inputText is input value for applying formatting
     * @return after applying formatting methods, prepared text will turn back
     */
    String persianFormatter(String inputText);
}
