package jballant.CommonJSAutoComplete.completion;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.psi.PsiElement;
import jballant.CommonJSAutoComplete.completion.util.LangUtil;
import jballant.CommonJSAutoComplete.completion.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSRequireElementMatcher {

    private PsiElement psiElement;
    private PsiElement origPsiElement;
    private Language language;

    JSRequireElementMatcher(@NotNull PsiElement psiElement, @NotNull PsiElement origPsiElement) {
        this.psiElement = psiElement;
        this.origPsiElement = origPsiElement;
        this.language = psiElement.getLanguage();
    }

    protected @Nullable String getVariableName() {
        JSVariable var = this.getVariable();
        return var != null ? var.getName() : null;
    }

    protected JSVariable getVariable() {

        if (!isJSRefExpression(LangUtil.isCoffeeScript(language) ? origPsiElement :psiElement)) {
            return null;
        }

        PsiElement rawJsVar = origPsiElement.getParent().getParent();
        JSVariable jsVar;
        // Make sure that the current element is a JS Variable
        if (isJSVar(rawJsVar)) {
            jsVar = (JSVariable)rawJsVar;
        } else {
            return null;
        }

        PsiElement varStatement = jsVar.getParent();
        if (!isJSVarStatement(varStatement) || !isPotentialRequireStatement(origPsiElement)) {
            return null;
        }

        return jsVar;
    }


    protected static boolean isJSRefExpression (@NotNull PsiElement element) {
        PsiElement context = element.getContext();
        return context != null && context instanceof JSReferenceExpression;
    }

    protected static boolean isJSVarStatement (@NotNull PsiElement element) {
        return element instanceof JSVarStatement;
    }

    protected static boolean isJSVar (@Nullable PsiElement element) {
        return element instanceof JSVariable;
    }

    protected static boolean isPotentialRequireStatement (@NotNull PsiElement element) {
        String text = element.getText();
        return text != null && StringUtil.stringIsPotentialSubString(text, JSRequireConstants.REQUIRE_FUNC_NAME);
    }

    public static @Nullable String getJSVarNameForElement(
            @Nullable PsiElement origPsiElement,
            @Nullable PsiElement psiElement
    ) {
        if (origPsiElement == null || psiElement == null) {
            return null;
        }
        return new JSRequireElementMatcher(psiElement, origPsiElement).getVariableName();
    }
}
