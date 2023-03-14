package com.owlbeatsmusic.space.javaapp;

/**
 *Shortcuts for modifying strings quicker.
 */
public class Str {

    /**
     *Delete a specified amount of characters from the first character.
     * @param str Original String.
     * @param length Numbers of characters to remove from start.
     * @return Changed string.
     */
    public static String delFirstChars(String str, int length) {
        for (int i = 0; i < length; i++) {
            str = String.valueOf(new StringBuilder(str).deleteCharAt(0));
        }
        return str;
    }
  
    /**
     *Delete a specified amount of characters from the last character.
     * @param str Original String.
     * @param length Numbers of characters to remove from end.
     * @return Changed string.
     */
    public static String delLastChars(String str, int length) {
        for (int i = 0; i < length; i++) {
            str = String.valueOf(new StringBuilder(str).deleteCharAt(str.length() - 1));
        }
        return str;
    }

    /**
     *Deletes a specified amount of characters from first and last at the same time.
     * @param str Original String.
     * @param length Numbers of characters to remove from start and end.
     * @return Changed string.
     */
    public static String delFirstLastChars(String str, int length) {
        for (int i = 0; i < length; i++) {
            str = String.valueOf(new StringBuilder(str).deleteCharAt(str.length() - 1).deleteCharAt(0));
        }
        return str;
    }

    /**
     * Gets a specified amount of characters from the first character.
     * @param str Original String.
     * @param length How many characters to get from the start.
     * @return Characters from start.
     */
    public static String getFirstChars(String str, int length) {
        StringBuilder tempStr = new StringBuilder();
        for (int i = 0; i < length; i++) {
            tempStr.append(str.charAt(i));
        }
        return String.valueOf(tempStr);
    }

    /**
     *Gets the specified amount of characters from the last character.
     * @param str Original String.
     * @param length How many characters to get from the end.
     * @return Characters from end.
     */
    public static String getLastChars(String str, int length) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            strBuilder.insert(0, str.charAt(str.length() - 1 - i));
        }
        return strBuilder.toString();
    }

    /**
     *Counts the number of occurrences of a character in a string.
     * @param str Original String.
     * @param chr Character to search for.
     * @return Number of occurrences.
     */
    public static int countChars(String str, char chr) {
        int occurrences = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == chr)
                occurrences++;
        }
        return occurrences;
    }

    /**
     *Counts the number of occurrences of a string in a string.
     * @param str Original String.
     * @param lookupStr String to search for.
     * @return Number of occurrences.
     */
    public static int countStrings(String str, String lookupStr) {
        int occurrences = 0;
        for (int i = 0; i < str.length(); i++) {
            String tempStr = "";
            for (int chr = 0; chr < lookupStr.length(); chr++) {
                if (i + lookupStr.length() - 1 != (str.length())) {
                    if (str.charAt(i + chr) == lookupStr.charAt(chr)) {
                        tempStr = tempStr + str.charAt(i + chr);
                    }
                }
            }
            if (tempStr.equals(lookupStr))
                occurrences++;
        }
        return occurrences;
    }

    /**
     *Converts an array previously converted to string to an array.
     * @param str Original String.
     * @return String converted to an Array.
     */
    public static String[] strToStrArray(String str) {
        String[] tempArray;
        tempArray = Str.delFirstLastChars(str, 1).split(", ");
        return tempArray;
    }

    /**
     *Creates an array of integers with all integers found in string.
     * @param str Original String. Numbers in the string can max be 10.
     * @return Array of ints from string.
     */
    public static int[] allIntsInString(String str) {
        int[] ints = {};
        int index = 0;
        int amount = 0;
        int length = 0;
        int i = 0;
        while (i < str.length()) {
            try { Integer.parseInt(String.valueOf(str.charAt(i)));
                String tempInt = "";
                int o = 0;
                while (true) {
                    try {
                        Integer.parseInt(String.valueOf(str.charAt(i+o)));
                        tempInt = tempInt + str.charAt(i+o);
                        length += 1;
                    } catch (Exception e) {
                        i += length;
                        amount++;
                        break;
                    }
                    o++;
                }
                int[] tempArray = new int[amount];
                for (int n = 0; n < ints.length; n++) {
                    tempArray[n] = ints[n];
                }
                tempArray[index] = Integer.parseInt(tempInt);
                index++;
                ints = tempArray;
            } catch (Exception ignored) {}
            i++;
        }
        return ints;
    }

    /**
     *Deletes all integers from a string.
     * @param str Original String.
     * @return Changed string.
     */
    public static String delInts(String str) {
        StringBuilder newStr = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            try { Integer.parseInt(String.valueOf(str.charAt(i)));
            } catch (Exception ignored) {
                newStr.append(str.charAt(i));
            }
        }
        return String.valueOf(newStr);
    }

    /**
     *Deletes all characters from a string.
     * @param str Original String.
     * @return Changed string.
     */
    public static String delChars(String str) {
        StringBuilder newStr = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            try { Integer.parseInt(String.valueOf(str.charAt(i)));
                newStr.append(str.charAt(i));
            } catch (Exception ignored) {}
        }
        return String.valueOf(newStr);
    }

}
