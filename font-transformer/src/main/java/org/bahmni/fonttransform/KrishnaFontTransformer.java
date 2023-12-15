package org.bahmni.fonttransform;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * This class performs Krishna Font to Unicode Transformation. It does this via:
 * a) A mapping file which contains Krishna character to Unicode character mapping (created by Pankaj)
 * b) Some rules which it applies to ensure the hindi word is actually correctly mapped in unicode.
 *
 * @see #krishnaToUnicode(java.util.List)
 */
public class KrishnaFontTransformer {
    private static Logger logger = LoggerFactory.getLogger(KrishnaFontTransformer.class.getName());
    private static Properties KRISHNA_TO_UNICODE = new Properties();

    public KrishnaFontTransformer() {
        initializeUnicodeMapperFromProperties();
    }

    private void initializeUnicodeMapperFromProperties() {
        try {
            KRISHNA_TO_UNICODE.load(getClass().getClassLoader().getResourceAsStream("krishnaToUnicode.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printAllProperties() {
        Enumeration enumeration = KRISHNA_TO_UNICODE.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            System.out.println(key + " = " + KRISHNA_TO_UNICODE.getProperty(key));
        }
    }

    public String krishnaToUnicode(String s) {
        return krishnaToUnicode(Arrays.asList(s)).get(0);
    }

    public ArrayList<String> krishnaToUnicode(List<String> stringList) {
        ArrayList<String> transformedStrings = new ArrayList<>();

        StringBuilder stringBuilderInUnicode = null;
        for (String s : stringList) {

            s = performHalfConsonantSanitization(s);

            stringBuilderInUnicode = new StringBuilder();
            char[] chars = s.toCharArray();

            for (int iter = 0; iter < chars.length; iter++) {
                char token = chars[iter];
                char nextToken = iter < chars.length - 1 ? chars[iter + 1] : ' ';
                char tokenAfterNext = iter < chars.length - 2 ? chars[iter + 2] : ' ';
                String u;
                if(Character.isSpaceChar(token)){
                    u = String.valueOf(token);
                }
                else{
                    u = KRISHNA_TO_UNICODE.getProperty(String.valueOf(token));

                    if (isHalfConsonant(token) && nextToken == 'k') {
                        u = getFullUnicodeFor(token);
                        iter++;
                        nextToken = tokenAfterNext;
                        tokenAfterNext = iter < chars.length - 2 ? chars[iter + 2] : ' ';
                    }
                    if (is_Big_E(token, nextToken)) {
                        u = "\u0908";
                        iter++;
                    } else if (isHalf_R_onTop(nextToken)) {
                        String next = KRISHNA_TO_UNICODE.getProperty(String.valueOf(nextToken));
                        stringBuilderInUnicode.append(next);
                        iter++;
                    } else if (isVowel(nextToken) && isHalf_R_onTop(tokenAfterNext)) {
                        String topR = KRISHNA_TO_UNICODE.getProperty(String.valueOf(tokenAfterNext));
                        stringBuilderInUnicode.append(topR);
                        stringBuilderInUnicode.append(u);
                        u = KRISHNA_TO_UNICODE.getProperty(String.valueOf(nextToken));
                        iter = iter + 2;
                    }
                    if (isSmall_E_Matra(token)) {
                        iter = handleSmall_E_Matra(stringBuilderInUnicode, iter, nextToken, tokenAfterNext);
                    }
                }
                stringBuilderInUnicode.append(u);
            }

            String transformedString = stringBuilderInUnicode.toString();
            transformedString = replace_O_MatraUnicode(transformedString);
            transformedStrings.add(transformedString);
        }
        return transformedStrings;
    }

    private String replace_O_MatraUnicode(String transformedString) {
        return transformedString.replaceAll("\u093e\u0947", "\u094b");
    }

    private int handleSmall_E_Matra(StringBuilder stringBuilderInUnicode, int iter, char nextToken, char tokenAfterNext) {
        String next;
        if (isHalfConsonant(nextToken)) {
            next = krishnaToUnicode(new String(new char[]{nextToken, tokenAfterNext}));
            iter = iter + 2;
        } else {
            next = KRISHNA_TO_UNICODE.getProperty(String.valueOf(nextToken));
            iter++;
        }
        stringBuilderInUnicode.append(next);
        return iter;
    }

    private boolean isModifierVowel(char c) {
        return c == 's'
                || c == 'f';
    }

    private String getFullUnicodeFor(char c) {
        switch (c) {
            case '[':
                return "\u0916";
            case '{':
                return "\u0915\u094d\u0937";
            case '\"':
                return "\u0937";
            case '/':
                return "\u0927";
            case '.':
                return "\u0923";
            case 'H':
                return "\u092d";
            case 'F':
                return "\u0925";
            case '?':
                return "\u0918";
            case '\'':
                return "\u0936";
        }

        return c + ""; //Should never come here!
    }

    private boolean isHalfConsonant(char c) {
//        fHkokth
        return c == '[' ||
                c == '{' ||
                c == '\"' ||
                c == '/' ||
                c == '.' ||
                c == 'H' ||
                c == 'F' ||
                c == '?' ||
                c == '\'';
    }

    private String performHalfConsonantSanitization(String s) {
        return s.replaceAll("Ek", "e")
                .replaceAll("Rk", "r")
                .replaceAll("Tk", "t")
                .replaceAll("Yk", "y")
                .replaceAll("Uk", "u")
                .replaceAll("Ik", "i")
                .replaceAll("Ok", "o")
                .replaceAll("Pk", "p")
                .replaceAll("Dk", "d")
                .replaceAll("Ck", "c")
                .replaceAll("Xk", "x");
    }

    private boolean isVowel(char c) {
        return c == 'k' || c == 'h' || c == 'f' || c == 'S' || c == 's';
    }

    private boolean isHalf_R_onTop(char c) {
        return c == 'Z';
    }

    private boolean isSmall_E_Matra(char c) {
        return c == 'f';
    }

    private boolean is_Big_E(char token, char nextToken) {
        return token == 'b' && nextToken == 'Z';
    }

}
