package forms;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by james on 7/20/14.
 */
public class CommonJSAutoCompleteConfigurable implements Configurable {
    private JTextField textField1;
    private JPanel myPanel;

    @Nls
    @Override
    public String getDisplayName() {
        return "Jamie's test config";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Jamie's test config";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return myPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
