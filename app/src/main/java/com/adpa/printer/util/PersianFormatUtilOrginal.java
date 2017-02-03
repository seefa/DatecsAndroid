package com.adpa.printer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sami
 * @version 1.0
 * @since 1/18/2017
 */
public class PersianFormatUtilOrginal {

    public static String convertNumbersInTextToEnglish(String value) {

        value = value
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

        value = value
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

        return value;
    }
    public static String convertSpecialPersianLetters(String value) {

        value = value
                .replace('\u003F', '\u061F') //?
                .replace('\u0626', '\u064A') //ي --->ی
                .replace('\u0649', '\u064A')  //ي --->ی
                .replace('\u06CC', '\u064A')  //ی --->ی
                .replace('\u0643', '\u06A9') //ک
                .replace('\u06AC', '\u06AF') //گ
                .replace('\u06AD', '\u06AF') //گ
                .replace('\u06AE', '\u06AF') //گ
                .replace('\u0624', '\u0648') //و
                .replace('\u0629', '\u0647') //ه
                .replace('\u06C0', '\u0647') //ه
                .replace('\u06C1', '\u0647') //ه
                .replace('\u06C2', '\u0647') //ه
                .replace('\u0691', '\u0698') //ژ
                .replace('\u0688', '\u0698') //ژ
        ;

        return value;
    }

    public static String containerConvertor(String input) {
        char[] l = new char[input.length()];
        input.getChars(0, input.length(), l, 0);

        for (int i = 0; i < input.length(); i++) {
            int j = 0;
            if (l[i] == '(') {
                j = i;
                while (l[j] == '(' && l[j] != ')') {
                    l[j] = ')';
                    j = j + 1;
                }

            } else if (l[j] == ')') {
                j = i;
                while (l[j] == ')' && l[j] != '(') {
                    l[j] = '(';
                    j = j + 1;
                }
                i = j;
            }
            /*if (l[i] == '[') {
                j = i;
                while (l[j] == '[' && l[j] != ']') {
                    l[j] = ']';
                    j = j + 1;
                }

            } else if (l[j] == ']') {
                j = i;
                while (l[j] == ']' && l[j] != '[') {
                    l[j] = '[';
                    j = j + 1;
                }
                i = j;
            }
            if (l[i] == '{') {
                j = i;
                while (l[j] == '{' && l[j] != '}') {
                    l[j] = '}';
                    j = j + 1;
                }

            } else if (l[j] == '}') {
                j = i;
                while (l[j] == '}' && l[j] != '{') {
                    l[j] = '{';
                    j = j + 1;
                }
                i = j;
            }*/
        }
        return new String(l);
    }

    public static String reverseNumbers(String input) {
        Pattern p = Pattern.compile("(([^\\u0600-\\u065F\\u066E-\\u06EF\\u06FA-\\u06FF\\u0020\\[\\](){}]*[\\u0660-\\u0669\\u06F0-\\u06F9]+)|" +
                "([^\\u0600-\\u065F\\u066E-\\u06EF\\u06FA-\\u06FF\\u0020\\[\\](){}]*[0-9]+)|" +
                "([^\\u0600-\\u065F\\u066E-\\u06EF\\u06FA-\\u06FF\\u0020\\[\\](){}]*[\\u0021-\\u0027\\u002A-\\u00FF]+[^-_;:'\"|]+))");

        Matcher m = p.matcher(input);
        StringBuffer sbAfterChange = new StringBuffer();
        int findex = 0;
        // TODO: need to resolve for character more than once and we have many some of them
        while (m.find()) {
            int lindex = input.indexOf(m.group());
            sbAfterChange.append(input.substring(findex, lindex));
            sbAfterChange.append(new StringBuilder(m.group()).reverse().toString());
            findex = sbAfterChange.length();
        }
        if (findex < input.length())
            sbAfterChange.append(input.substring(findex, input.length()));
        return sbAfterChange.toString();
    }




    //Should be deleted
    public static List<String> printWindows1256Characters() {
        List<String> stringBuffers = new ArrayList<>();
        List<byte[]> stringBufferUTF8Bytes = new ArrayList<>();
        List<byte[]> stringBufferWindows1256Bytes = new ArrayList<>();
//        for (char start = 0x0000; start < 0xFFFF; start++) {
        for (char start = 0x0625; start < 0x070E; start++) {
//        for (char start = 0x0625; start < 0x0626; start++) {
            int intChar = (int) start;
            String hex = String.format("%04X", intChar);
//            System.out.print(intChar + "(" + hex + ") : " + start + "\t\t");
//            String character = intChar + "(" + hex + ") : " + start + "\t";
            String character = String.valueOf(start);
            String numberIndex = intChar + "(" + hex + "):";
           // stringBuffers.add(PersianFormatUtilOrginal.datecsPersianConvertor1(numberIndex) + start);
//

        }
        /*System.out.println("----------------------------------------------------");

        StringBuffer persianChars = new StringBuffer();
        for (int index = 0; index < stringBuffers.size(); index++) {
            byte[] utf8Charset = stringBuffers.get(index).getBytes(); // UTF-8 Charset
            stringBufferUTF8Bytes.add(utf8Charset);
            try {
                byte[] windows1256Charset = stringBuffers.get(index).getBytes("Windows-1256"); // Windows-1256 Charset
                stringBufferWindows1256Bytes.add(windows1256Charset);
                if (index >= 1575 && index <= 1800) {
                    String ch = stringBuffers.get(index);

                    System.out.print(index +"("+ch.getBytes()+")"+ ":" + ch + "\t");


                    *//*persianChars.append(ch);
                    if(index%10 == 0)
                        System.out.println();*//*
                }
                // Numbers
//
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/
        return stringBuffers;

        //System.out.println("Persian Chars : "+persianChars);
    }


}
