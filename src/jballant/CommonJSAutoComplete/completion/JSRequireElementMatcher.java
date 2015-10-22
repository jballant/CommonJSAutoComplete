package completion;

import com.intellij.lang.Language;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.psi.PsiElement;
import completion.util.LangUtil;
import completion.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSRequireElementMatcher {

    private PsiElement psiElement;
    private PsiElement origPsiElement;
    private Language language;
    private boolean isES6Import = false;

    JSRequireElementMatcher(@NotNull PsiElement psiElement, @NotNull PsiElement origPsiElement) {
        this.psiElement = psiElement;
        this.origPsiElement = origPsiElement;
        this.language = psiElement.getLanguage();
    }

    public boolean isES6Import() {
        return isES6Import;
    }

    protected @Nullable String getVariableName() {
        if (origPsiElement.getParent() instanceof ES6ImportedBinding) {
            this.isES6Import = true;
            return getImportVarName(origPsiElement);
        }
        JSVariable var = this.getVariable();
        return var != null ? var.getName() : null;
    }

    protected @Nullable JSVariable getVariable() {

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

    protected String getImportVarName (@NotNull PsiElement element) {
        return element.getText();
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

}
