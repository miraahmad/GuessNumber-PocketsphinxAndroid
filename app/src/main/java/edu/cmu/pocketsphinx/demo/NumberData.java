package edu.cmu.pocketsphinx.demo;

import java.util.StringTokenizer;

/**
 * Created by miraahmad on 7/9/2016.
 */
public class NumberData {

    public static final String[] DIGITS = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    public static final String[] TENS = {null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    public static final String[] TEENS = {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    public static final String[] ZERO = {"zero", "oh"};

    public static String replaceNumbers(String input) {
        String result = "";
        String[] tendig = input.split(ZERO[0]);


        for (int j = 0; j < tendig.length; j++) {
            StringTokenizer set = new StringTokenizer(tendig[j]);
            int[] num = {0, 0};

            if (set.countTokens() == 2) {
                String uno = set.nextToken();
                String dos = set.nextToken();

                num[0] = 0;
                for (int k = 0; k < DIGITS.length; k++) {
                    if (uno.equals(TENS[k])) {
                        num[0] = k + 1;
                    }
                    if (dos.equals(DIGITS[k])) {
                        num[1] = k + 1;
                    }

                }

                result = result + Integer.toString(num[0]) + Integer.toString(num[1]);

            } else {

                String uno = set.nextToken();
                num[0] = 0;

                for (int k = 0; k < DIGITS.length; k++) {
                    if (uno.equals(DIGITS[k])) {
                        num[1] = k + 1;
                        result = result + Integer.toString(num[1]);
                    }
                }
                for (int k = 0; k < TENS.length; k++) {
                    if (uno.equals(TENS[k])) {
                        num[0] = k + 1;
                        result = result + Integer.toString(num[0]) + Integer.toString(num[1]);
                    }
                }
                for (int k = 0; k < TEENS.length; k++) {
                    if (uno.equals(TEENS[k])) {
                        num[1] = k + 10;
                        result = result + Integer.toString(num[1]);
                    }
                }

            }

        }

        return result;
    }
}
