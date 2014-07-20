import com.intellij.codeInsight.completion.*;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by james on 7/14/14.
 */
public class JSRequireCompletionContributer extends CompletionContributor {

//    protected PsiElementPattern pattern;

    public JSRequireCompletionContributer() {

        super();

        PsiElementPattern.Capture<PsiElement> pattern =
                PlatformPatterns.psiElement()
                    .withParent(PlatformPatterns.psiElement(JSReferenceExpression.class))
                    .withLanguage(JavascriptLanguage.INSTANCE);
//                    .withParent(PlatformPatterns.psiElement(JSElementTypes.VAR_STATEMENT));
//        PsiElementPattern.Capture<PsiElement> pattern = PlatformPatterns.psiElement();

        JSRequireCompletionProvider completionProvider = new JSRequireCompletionProvider();

        extend(
                CompletionType.BASIC,
                pattern,
                completionProvider
        );

    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
//        result.addElement(LookupElementBuilder.create("BLAH BLAH!"));
        super.fillCompletionVariants(parameters, result);
    }

    //
//    PsiElementPattern.Capture<PsiElement> stringInFuncCall = PlatformPatterns.psiElement()
//            .withParent(PlatformPatterns.psiElement(PhpElementTypes.PARAMETER_LIST)
//                            .withParent(
//                                    PlatformPatterns.or(
//                                            PlatformPatterns.psiElement(PhpElementTypes.FUNCTION_CALL),
//                                            PlatformPatterns.psiElement(PhpElementTypes.METHOD_REFERENCE),
//                                            PlatformPatterns.psiElement(PhpElementTypes.NEW_EXPRESSION)
//                                    )
//                            )
//            );

}
