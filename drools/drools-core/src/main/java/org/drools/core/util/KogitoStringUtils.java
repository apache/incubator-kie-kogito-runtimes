package org.drools.core.util;

public class KogitoStringUtils {

    public static String capitalize(String string) {
        if (string == null) {
            return null;
        }

        if (string.length() == 1) {
            return string.toUpperCase();
        }

        return Character.toString(string.charAt(0)).toUpperCase() + string.substring(1);
    }
}
