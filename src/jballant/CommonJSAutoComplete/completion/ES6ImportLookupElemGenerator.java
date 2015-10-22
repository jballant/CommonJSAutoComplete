package completion;

import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import completion.util.LangUtil;
import config.JSRequireConfig;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class ES6ImportLookupElemGenerator {

    private static final String SEMICOLON_STR = ";";

    private String varName;
    private String filePath;
    private boolean useDoubleQuotes;

    ES6ImportLookupElemGenerator(
            @NotNull String varName,
            @NotNull Project currentProject,
            @NotNull String filePath
    ) {
        this.varName = varName;
        this.filePath = filePath;
        JSRequireConfig config = JSRequireConfig.getInstanceForProject(currentProject);
        this.useDoubleQuotes = config.getShouldUseDoubleQuotes();
    }

    @NotNull LookupElement generate() {
        String completionString = generateCompletionString();
        LookupElementBuilder builder = LookupElementBuilder
                .create(completionString)
                .withBoldness(true)
                .withPresentableText(completionString)
                .withCaseSensitivity(true)
                .withTailText(SEMICOLON_STR);
        return builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
    }

    private @NotNull String generateCompletionString() {
        char quote = this.useDoubleQuotes ? '"' : '\'';
        return this.varName + " from " + quote + (this.filePath) + quote;
    }

    public static @NotNull LookupElement generateLookupElement(
            @NotNull String varName,
            @NotNull Project currentProject,
            @NotNull String filePath
    ) {
        ES6ImportLookupElemGenerator generator = new ES6ImportLookupElemGenerator(varName, currentProject, filePath);
        return generator.generate();
    }

}
