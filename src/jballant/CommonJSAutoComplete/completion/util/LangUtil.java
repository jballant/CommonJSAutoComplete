package jballant.CommonJSAutoComplete.completion.util;

import com.intellij.json.JsonFileType;
//import com.intellij.lang.javascript.json.JSONFileType;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.fileTypes.FileType;
import org.coffeescript.CoffeeScriptLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LangUtil {

    final static String JS_ID = "JavaScript";
    final static String COFFEESCRIPT_ID = "CoffeeScript";

    public static @NotNull JavascriptLanguage getJSLang () {
        Language js = Language.findLanguageByID(JS_ID);
        if (js == null) {
            return JavascriptLanguage.INSTANCE;
        }
        return (JavascriptLanguage) js;
    }

    public static @NotNull CoffeeScriptLanguage getCoffeeScriptLang() {
        Language coffee = Language.findLanguageByID(COFFEESCRIPT_ID);
        if (coffee == null) {
            return CoffeeScriptLanguage.INSTANCE;
        }
        return (CoffeeScriptLanguage) coffee;
    }

    public static boolean isJSFileType(@Nullable FileType fileType) {
        return fileType != null && fileType.equals(JavaScriptFileType.INSTANCE);
    }

    public static boolean isCoffeeScriptFileType(@Nullable FileType fileType) {
        return fileType != null && fileType.getName().equals(COFFEESCRIPT_ID);
    }

    public static boolean isJSONFileType(@Nullable FileType fileType) {
        return fileType != null && fileType.equals(JsonFileType.INSTANCE);
//        return fileType != null && fileType.equals(JSONFileType.JSON);
    }

    public static boolean isCoffeeScript (@NotNull Language lang) {
        return lang.getID().equals(COFFEESCRIPT_ID);
    }

}
