package jballant.CommonJSAutoComplete.completion;

import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import jballant.CommonJSAutoComplete.completion.util.LangUtil;
import jballant.CommonJSAutoComplete.config.JSRequireConfig;
import org.jetbrains.annotations.NotNull;

public class JSRequireLookupElemGenerator {

    private static final String SEMICOLON_STR = ";";

    private Language elemLanguage;
    private String filePath;
    private boolean useDoubleQuotes;

    JSRequireLookupElemGenerator(
            @NotNull Language elemLanguage,
            @NotNull Project currentProject,
            @NotNull String filePath
    ) {
        this.elemLanguage = elemLanguage;
        this.filePath = filePath;
        JSRequireConfig config = JSRequireConfig.getInstanceForProject(currentProject);
        this.useDoubleQuotes = config.getShouldUseDoubleQuotes();
    }

    @NotNull LookupElement generate() {
        boolean isCoffee = LangUtil.isCoffeeScript(this.elemLanguage);
        String completionString = isCoffee ? generateCoffeeRequireString() : generateJSRequireString();
        LookupElementBuilder builder = LookupElementBuilder
                .create(completionString)
                .withBoldness(true)
                .withPresentableText(completionString)
                .withCaseSensitivity(true);
        if (!isCoffee) {
            builder = builder.withTailText(SEMICOLON_STR);
        }
        return builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
    }

    private @NotNull String generateJSRequireString() {
        char quote = this.useDoubleQuotes ? '"' : '\'';
        String open = "(" + quote;
        String close = quote + ")";
        return JSRequireConstants.REQUIRE_FUNC_NAME.concat(open).concat(this.filePath).concat(close);
    }

    private @NotNull String generateCoffeeRequireString() {
        char quote = this.useDoubleQuotes ? '"' : '\'';
        String open = " " + quote;
        String close = quote + "";
        return JSRequireConstants.REQUIRE_FUNC_NAME.concat(open).concat(this.filePath).concat(close);
    }

    public static @NotNull LookupElement generateLookupElement(
            @NotNull Language elemLanguage,
            @NotNull Project currentProject,
            @NotNull String filePath
    ) {
        JSRequireLookupElemGenerator generator = new JSRequireLookupElemGenerator(elemLanguage, currentProject, filePath);
        return generator.generate();
    }

}
