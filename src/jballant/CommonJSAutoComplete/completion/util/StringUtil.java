package completion.util;

import org.apache.tools.ant.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * String utility methods for completion
 */
public class StringUtil {

    static final String DASH_STRING = "-";

    public static boolean isEmptyString (@NotNull String str) {
        return str.length() == 0;
    }

    public static @NotNull String capitalize (@NotNull String str) {
        if (str.length() == 0) {
            return str;
        }
        String capital = str.substring(0, 1).toUpperCase();
        if (str.length() == 1) {
            return capital;
        }
        return capital.concat(str.substring(1));
    }

    public static @NotNull String camelCaseString (@NotNull String input) {
        if (!input.contains(DASH_STRING)) {
            return input;
        }
        String[] splitInput = input.split(DASH_STRING);
        String str;
        String assembledStr = "";
        for (int i = 0; i < splitInput.length; i++) {
            str = splitInput[i];
            if (i > 0) {
                str = str.substring(0, 1).toUpperCase().concat(str.substring(1));
            }
            assembledStr = assembledStr.concat(str);
        }
        return assembledStr;
    }

    public static boolean stringIsPotentialSubString (@NotNull String subString, @NotNull String string) {
        int len = subString.length();

        if (len > string.length()) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (subString.charAt(i) != string.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static @Nullable String relativizePaths(
            @NotNull String aAbsolutePath,
            @NotNull String bAbsolutePath
    ) {
        String relPath;
        try {
            relPath = FileUtils.getRelativePath(new File(aAbsolutePath), new File(bAbsolutePath));
            // If the first character is not a '.' we add a ./ to indicate that
            // the file is in the same directory and not a node module
            if (relPath.charAt(0) != '.') {
                relPath = "./".concat(relPath);
            }
        } catch (Exception e) {
            return null;
        }
        return relPath;
    }


}
