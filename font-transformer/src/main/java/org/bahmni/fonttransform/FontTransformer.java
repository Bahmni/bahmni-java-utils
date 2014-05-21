package org.bahmni.fonttransform;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.exception.MigrationException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FontTransformer {
    private static Logger logger = Logger.getLogger(FontTransformer.class.getName());
    private static Properties KRISHNA_TO_UNICODE = new Properties();
    private static List<String> COMBINATION_CHARACTERS = Arrays.asList("Z");
    private final Class csvEntityClass;
    private final EntityTransformer entityTransformer;

    public FontTransformer(Class csvEntityClass, EntityTransformer entityTransformer) {
        this.csvEntityClass = csvEntityClass;
        this.entityTransformer = entityTransformer;
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

    public boolean transform(File csvFile, List<TransformationMetaDatum> metadata) {
        logger.info("Starting fontTransformation");
        CSVFile inputCsvFile = new CSVFile(csvFile.getAbsolutePath(), csvFile.getName(), csvEntityClass);

        try {
            inputCsvFile.openForRead();
            CSVEntity csvEntity;

            while ((csvEntity = inputCsvFile.readEntity()) != null) {
                entityTransformer.transform(csvEntity, metadata);
            }
        } catch (Exception e) {
            logger.error("Thread interrupted exception. " + e.getStackTrace());
            throw new MigrationException("Could not execute threads", e);
        } finally {
            logger.info("Done font transformation");
            closeResources();
        }
        return true;
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
            int iter;
            for (iter = 0; iter < chars.length; iter++) {
                char token = chars[iter];
                char nextToken = iter < chars.length - 1 ? chars[iter + 1] : ' ';
                char tokenAfterNext = iter < chars.length - 2 ? chars[iter + 2] : ' ';
                String u = KRISHNA_TO_UNICODE.getProperty(String.valueOf(token));

                if(isHalfConsonant(token) && nextToken == 'k'){
                    u = getFullUnicodeFor(token);
                    iter++;
                    nextToken = tokenAfterNext;
                    tokenAfterNext = iter < chars.length - 2 ? chars[iter + 2] : ' ';
                }

                if (is_Big_E(token, nextToken)) {
                    u = "\u0908";
                    iter++;
                }else if (isHalf_R_onTop(nextToken)) {
                    String next = KRISHNA_TO_UNICODE.getProperty(String.valueOf(nextToken));
                    stringBuilderInUnicode.append(next);
                    iter++;
                }else if (isVowel(nextToken) && isHalf_R_onTop(tokenAfterNext)) {
                    String topR = KRISHNA_TO_UNICODE.getProperty(String.valueOf(tokenAfterNext));
                    stringBuilderInUnicode.append(topR);
                    stringBuilderInUnicode.append(u);
                    u = KRISHNA_TO_UNICODE.getProperty(String.valueOf(nextToken));
                    iter = iter + 2;
                }


                if (isSmall_E_Matra(token)) {
                    String next = KRISHNA_TO_UNICODE.getProperty(String.valueOf(nextToken));
                    stringBuilderInUnicode.append(next);
                    iter++;
                }



                stringBuilderInUnicode.append(u);
            }

            transformedStrings.add(stringBuilderInUnicode.toString());
        }
        return transformedStrings;
    }

    private String getFullUnicodeFor(char c) {
        switch(c) {
            case '[': return "\u0916";
            case '{': return "\u0915\u094d\u0937";
            case '\"': return "\u0937";
            case '/': return "\u0927";
            case '.': return "\u0923";
            case 'H' : return "\u092d";
            case 'F' : return "\u0925";
            case '?' : return "\u0918";
        }

        return c+""; //Should never come here!
    }

    private boolean isHalfConsonant(char c) {
        return c == '[' ||
                c == '{' ||
                c == '\"' ||
                c == '/' ||
                c == '.' ||
                c == 'H' ||
                c == 'F' ||
                c == '?';
    }

    private String performHalfConsonantSanitization(String s) {
        return s.replaceAll("Ek", "e")
                           .replaceAll("Rk","r")
                            .replaceAll("Tk","t")
                            .replaceAll("Yk","y")
                            .replaceAll("Uk","u")
                            .replaceAll("Ik","i")
                            .replaceAll("Ok","o")
                            .replaceAll("Pk","p")
                            .replaceAll("Dk","d")
                            .replaceAll("Ck","c")
                            .replaceAll("Xk","x");
    }

    private boolean isVowel(char c) {
        return c == 'k';
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

    private void closeResources() {
        //To change body of created methods use File | Settings | File Templates.
    }
}
