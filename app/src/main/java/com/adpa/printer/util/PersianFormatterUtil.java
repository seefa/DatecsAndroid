package com.adpa.printer.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is PersianFormatter for converting unsupported Windows-1256 unicode to supported characters based Java universal unicode
 *
 * @author Saman Delfani
 * @version 1.0
 * @since 18 Jan 2017
 */
class PersianFormatterUtil implements PersianFormatter {

    /**
     * This method used for convert special Persian number charsets to Latin unicode
     *
     * @param persianNumberText input Persian number charsets
     * @return converted String Persian number charsets to Latin unicode
     */

    private static String convertNumberChars(String persianNumberText) {
        persianNumberText = persianNumberText
                .replace('\u06F0', '\u0030')
                .replace('\u06F1', '\u0031')
                .replace('\u06F2', '\u0032')
                .replace('\u06F3', '\u0033')
                .replace('\u06F4', '\u0034')
                .replace('\u06F5', '\u0035')
                .replace('\u06F6', '\u0036')
                .replace('\u06F7', '\u0037')
                .replace('\u06F8', '\u0038')
                .replace('\u06F9', '\u0039');

        persianNumberText = persianNumberText
                .replace('\u0660', '\u0030')
                .replace('\u0661', '\u0031')
                .replace('\u0662', '\u0032')
                .replace('\u0663', '\u0033')
                .replace('\u0664', '\u0034')
                .replace('\u0665', '\u0035')
                .replace('\u0666', '\u0036')
                .replace('\u0667', '\u0037')
                .replace('\u0668', '\u0038')
                .replace('\u0669', '\u0039');

        return persianNumberText;
    }

    /**
     * This method used for convert special Persian sign and characters charsets to Latin unicode
     *
     * @param persianCharsText Persian characters to supported ones
     * @return converted String after replacing supported characters
     */
    private static String convertSpecialChars(String persianCharsText) {

        persianCharsText = persianCharsText
                .replace('\u061F', '\u003F')    // Question mark
                .replace('\u0626', '\u064A')    // Yeh char
                .replace('\u0649', '\u064A')    // Yeh char
                .replace('\u06CC', '\u064A')    // Yeh char
                .replace('\u0643', '\u06A9')    // Keh char
                .replace('\u06AC', '\u06AF')    // Geh char
                .replace('\u06AD', '\u06AF')    // Geh char
                .replace('\u06AE', '\u06AF')    // Geh char
                .replace('\u0624', '\u0648')    // Veh char
                .replace('\u0629', '\u0647')    // Heh char
                .replace('\u06C0', '\u0647')    // Heh char
                .replace('\u06C1', '\u0647')    // Heh char
                .replace('\u06C2', '\u0647')    // Heh char
                .replace('\u0691', '\u0698')    // Heh char
                .replace('\u0688', '\u0698')   // Heh char
                .replace('،',',');
        return persianCharsText;
    }

    /**
     * This method used to reverse mirror characters like square brackets, curly brackets, parenthesises
     *
     * @param mirrorCharsText input text consists of mirror characters
     * @return result of changing mirror characters
     */
    private static String convertMirrorChars(String mirrorCharsText) {
        Map<Integer, Character> mirrorsMap = new HashMap<Integer, Character>();
        char[] mirrorChars = new char[mirrorCharsText.length()];
        mirrorCharsText.getChars(0, mirrorCharsText.length(), mirrorChars, 0);
        for (int index = 0; index < mirrorCharsText.length(); index++) {
            if (mirrorChars[index] == '(')
                mirrorsMap.put(index, ')');
            else if (mirrorChars[index] == ')')
                mirrorsMap.put(index, '(');
            else if (mirrorChars[index] == '[')
                mirrorsMap.put(index, ']');
            else if (mirrorChars[index] == ']')
                mirrorsMap.put(index, '[');
            else if (mirrorChars[index] == '{')
                mirrorsMap.put(index, '}');
            else if (mirrorChars[index] == '}')
                mirrorsMap.put(index, '{');

        }
        Iterator<Map.Entry<Integer, Character>> iterator = mirrorsMap.entrySet().iterator();
        StringBuilder resultText = new StringBuilder(mirrorCharsText);
        while (iterator.hasNext()) {
            Map.Entry<Integer, Character> entry = iterator.next();
            resultText.replace(entry.getKey(), entry.getKey() + 1, entry.getValue().toString());
        }
        return resultText.toString();
    }

    /***
     * This method used for detecting numbers and non-Persian characters for reversing them, because they print incorrect in RTL printing.
     * @param inputText A Persian text consists of number or non-Persian characters
     * @return result of method after reversing numbers and non-Persian characters
     */
    private static String reverseNumberAndNonPersianCharacters(String inputText) {

        Pattern p = Pattern.compile("(([\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FF]+[\\u0021-\\u007E\\u00A1-\\u05FF\\u0660-\\u066D\\u06F0-\\u06F9\\u0700-\\uFFFF][\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FF]*)|" +
                "([\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FF]+[\\u0000-\\u001F\\u007F-\\u00A0()]*[\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FF]*)|" +
                "([-\\u0000-\\u0020\\u007F-\\u00A0]+[:\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FF]+))");

        Matcher m = p.matcher(inputText);
        StringBuilder sbAfterChange = new StringBuilder();
        int firstIndex = 0;
        while (m.find()) {
            int lastIndex = inputText.indexOf(m.group(), firstIndex);
            if (lastIndex > 0) {
                String nonPersianChars = inputText.substring(firstIndex, lastIndex);
                String noSpaceNonPersianChars = nonPersianChars.trim();
                StringBuilder reverseNonPersianChars = new StringBuilder(noSpaceNonPersianChars);
                nonPersianChars = nonPersianChars.replace(noSpaceNonPersianChars, reverseNonPersianChars.reverse().toString());
                sbAfterChange.append(nonPersianChars);
            }
            sbAfterChange.append(m.group());
            firstIndex = sbAfterChange.length();
        }
        if (firstIndex < inputText.length()) {
            Pattern p2 = Pattern.compile("([\\u0021-\\u007E\\u00A1-\\u05FF\\u0660-\\u066D\\u06F0-\\u06F9\\u0700-" +
                    "\\uFFFF-_+/*|,()]{2,})");
            String lastPartString = inputText.substring(firstIndex, inputText.length());
            Matcher mLast = p2.matcher(lastPartString);
            int index = 0;
            while (mLast.find()) {
                int numberIndex = lastPartString.indexOf(mLast.group(), index);
                lastPartString = lastPartString.substring(0, numberIndex) + new StringBuilder(mLast.group()).reverse().toString() + lastPartString.substring(numberIndex + mLast.group().length(), lastPartString.length());
            }
            lastPartString = convertMirrorChars(lastPartString);
            sbAfterChange.append(lastPartString);
        }
        return sbAfterChange.toString();
    }

    /**
     * This method used as facade method to encapsulate Persian characters formatting
     * @param inputText is input value for applying formatting
     * @return after applying formatting methods, prepared text will turn back
     */
    public String persianFormatter(String inputText) {
        String result = PersianFormatterUtil.convertNumberChars(inputText);
        result = PersianFormatterUtil.convertSpecialChars(result);
        result = PersianFormatterUtil.convertMirrorChars(result);
        result = PersianFormatterUtil.reverseNumberAndNonPersianCharacters(result);
        return result;
    }
}
