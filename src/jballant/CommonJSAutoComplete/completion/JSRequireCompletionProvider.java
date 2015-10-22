package completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class JSRequireCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters completionParameters,
            ProcessingContext processingContext,
            @NotNull CompletionResultSet completionResultSet) {

        PsiElement psiElement = completionParameters.getPosition();
        PsiElement origPosition = completionParameters.getOriginalPosition();

        if (origPosition == null) {
            return;
        }

        JSRequireElementMatcher elementMatcher = new JSRequireElementMatcher(psiElement, origPosition);

        String varName = elementMatcher.getVariableName();

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
            if (elementMatcher.isES6Import()) {
                completionResultSet.addElement(ES6ImportLookupElemGenerator.generateLookupElement(varName, currentProject, path));
            } else {
                completionResultSet.addElement(JSRequireLookupElemGenerator.generateLookupElement(origFile.getLanguage(), currentProject, path));
            }
        }
    }
}
