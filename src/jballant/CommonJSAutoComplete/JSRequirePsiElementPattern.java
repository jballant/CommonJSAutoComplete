import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.patterns.InitialPatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by james on 7/19/14.
 */
public class JSRequirePsiElementPattern<T extends PsiElement> extends PsiElementPattern {

    public JSRequirePsiElementPattern(Class aClass) {
        super(aClass);
    }

    public JSRequirePsiElementPattern(@NotNull InitialPatternCondition condition) {
        super(condition);
    }

    @Override
    public boolean accepts(@Nullable Object o, ProcessingContext context) {
        return super.accepts(o, context);
    }
}
