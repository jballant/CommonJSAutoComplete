package jballant.CommonJSAutoComplete.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import jballant.CommonJSAutoComplete.completion.util.LangUtil;
import jballant.CommonJSAutoComplete.completion.util.StringUtil;
import jballant.CommonJSAutoComplete.config.JSRequireConfig;
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

        PsiElement psiElement = completionParameters.getPosition();
        String varName = JSRequireElementMatcher.getJSVarNameForElement(
                completionParameters.getOriginalPosition(),
                psiElement
        );
        if (varName == null) {
            return;
        }

        PsiFile origFile = completionParameters.getOriginalFile();
        Project currentProject = origFile.getProject();

        JSRequirePathFinder pathFinder = new JSRequirePathFinder(origFile);

        // Find all the corresponding require statement file paths
        // given the var name
        ArrayList<String> paths = pathFinder.findPathsForVarName(varName);
        for (String path : paths) {
            completionResultSet.addElement(JSRequireLookupElemGenerator.generateLookupElement(origFile.getLanguage(), currentProject, path));
        }
    }
}
