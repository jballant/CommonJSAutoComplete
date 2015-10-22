package completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import completion.util.LangUtil;
import org.jetbrains.annotations.NotNull;

public class JSRequireCompletionContributer extends CompletionContributor {

    public JSRequireCompletionContributer() {

        super();

        PsiElementPattern.Capture<PsiElement> pattern =
                PlatformPatterns.psiElement()
                        .withLanguage(LangUtil.getJSLang());

        JSRequireCompletionProvider completionProvider = new JSRequireCompletionProvider();

        extend(CompletionType.BASIC, pattern, completionProvider);
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);
    }

}
