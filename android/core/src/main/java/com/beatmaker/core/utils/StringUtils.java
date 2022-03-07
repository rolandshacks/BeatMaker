package com.beatmaker.core.utils;

public class StringUtils {

    private static final String ZEROS = "0000000000000000000000000000000000000000";
    private static final String HEXCHARS = "0123456789abcdef";

    public static String toHex(int i) {
        return toHex((byte) (i&0xff));
    }

    public static String toHex(byte b) {
        int num = (0xff & b);
        int high = ((num&0xf0) >> 4);
        int low = (num&0x0f);
        return "" + HEXCHARS.charAt(high) + HEXCHARS.charAt(low);
    }

    public static String hexformat(byte[] data, int ofs, int len) {

        StringBuilder sb = new StringBuilder();

        for (int i=0; i<len; i++) {
            byte b = data[ofs+i];
            if (i>0) {
                sb.append(' ');
            }
            sb.append(toHex(b));
        }

        return sb.toString();
    }

    public static String getSymbolName(String str) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasChar = false;
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
                lastWasChar = true;
            } else if (lastWasChar) {
                sb.append('_');
                lastWasChar = false;
            }
        }
        return sb.toString();
    }

    public static String formatNumber(long number, int len) {
        String numberString = String.valueOf(number);
        int zerosNeeded = len - numberString.length();

        if (zerosNeeded < 1) return numberString;
        if (zerosNeeded > ZEROS.length()) zerosNeeded = ZEROS.length();

        return ZEROS.substring(0, zerosNeeded) + numberString;
    }

}
