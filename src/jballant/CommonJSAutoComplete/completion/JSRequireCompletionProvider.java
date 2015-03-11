package completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import completion.util.StringUtil;
import config.JSRequireConfig;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class JSRequireCompletionProvider extends CompletionProvider<CompletionParameters> {

    static final String REQUIRE_FUNC_NAME = "require";
    static final String DASH_STRING = "-";
    static final String SEMICOLON_STR = ";";
    static final String JS_MODULE_SUFFIX = "js";
    static final String JS_CAMELCASE_MODULE_SUFFIX = "Js";
    static final char REQUIRE_PATH_SEPARATOR = '/';

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters completionParameters,
            ProcessingContext processingContext,
            @NotNull CompletionResultSet completionResultSet) {

        PsiElement origPsiElement = completionParameters.getOriginalPosition();
        if (origPsiElement == null) {
            return;
        }

        PsiElement psiElement = completionParameters.getPosition();
        if (!psiElement.getLanguage().is(JavascriptLanguage.INSTANCE)) {
            return;
        }

        if (!isJSRefExpression(psiElement)) {
            return;
        }

        PsiElement rawJsVar = origPsiElement.getParent().getParent();
        JSVariable jsVar;
        // Make sure that the current element is a JS Variable
        if (isJSVar(rawJsVar)) {
            jsVar = (JSVariable)rawJsVar;
        } else {
            return;
        }

        PsiElement varStatement = jsVar.getParent();
        if (!isJSVarStatement(varStatement) || !isPotentialRequireStatement(origPsiElement)) {
            return;
        }

        String varName = jsVar.getName();
        if (varName == null) {
            return;
        }

        PsiFile origFile = completionParameters.getOriginalFile();
        Project currentProject = origFile.getProject();
        boolean useDoubleQuotes = JSRequireConfig.getInstanceForProject(currentProject).getShouldUseDoubleQuotes();

        JSRequirePathFinder pathFinder = new JSRequirePathFinder(origFile);

        // Find all the corresponding require statement file paths
        // given the var name
        ArrayList<String> paths = pathFinder.findPathsForVarName(varName);
        for (String path : paths) {
            completionResultSet.addElement(buildLookupElementWithPath(path, useDoubleQuotes));
        }
    }

    public static @NotNull LookupElement buildLookupElementWithPath(@NotNull String path, boolean useDoubleQuotes) {
        String completionString = generateRequireStringForPath(path, useDoubleQuotes);
        return LookupElementBuilder
                .create(completionString)
                .withBoldness(true)
                .withPresentableText(completionString)
                .withCaseSensitivity(true)
                .withTailText(SEMICOLON_STR, true)
                .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
    }

    public static @NotNull String generateRequireStringForPath(@NotNull String path, boolean useDoubleQuotes) {
        char quote = useDoubleQuotes ? '"' : '\'';
        String open = "(" + quote;
        String close = quote + ")";
        return REQUIRE_FUNC_NAME.concat(open).concat(path).concat(close);
    }

    public static boolean isJSRefExpression (@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        PsiElement context = element.getContext();
        return context != null && context instanceof JSReferenceExpression;
    }

    public static boolean isJSVarStatement (@Nullable PsiElement element) {
        return element instanceof JSVarStatement;
    }

    public static boolean isJSVar (@Nullable PsiElement element) {
        return element instanceof JSVariable;
    }

    public static boolean isPotentialRequireStatement (@NotNull PsiElement element) {
        String text = element.getText();
        return text != null && StringUtil.stringIsPotentialSubString(text, REQUIRE_FUNC_NAME);
    }

}
