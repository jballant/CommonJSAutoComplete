package forms;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import config.JSRequireConfig;


/**
 * Created by james on 7/20/14.
 */
public class CommonJSAutoCompleteConfigurable implements Configurable {
    private JTextField mainJSRootDirTextField;
    private JPanel myPanel;
    private JLabel mainJSRootDirLabel;
    private JTextField deepIncludeNodeModulesField;
    private JCheckBox useRelativePathsCheckBox;
    private JLabel nodeModulesDirLabel;
    private JTextField nodeModulesDirField;
    private JLabel deepIncludeNodeModulesLabel;

    private Project myProject;

    private JSRequireConfig myConfig;

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

        myProject = ProjectUtil.guessCurrentProject(myPanel);

        myConfig = JSRequireConfig.getInstanceForProject(myProject);

        mainJSRootDirTextField.setText(myConfig.getMainJSDirString());
        nodeModulesDirField.setText(myConfig.getNodeModulesDirString());
        deepIncludeNodeModulesField.setText(myConfig.getDeepIncludeModulesDirString());
        useRelativePathsCheckBox.setSelected(myConfig.getUseRelativePathsForMain());

        return myPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {

        String mainJSRootDirString = mainJSRootDirTextField.getText();
        String nodeModulesDirString = nodeModulesDirField.getText();
        String deepIncludeNodeModulesString = deepIncludeNodeModulesField.getText();
        boolean useRelativePathsForMainJsDir = useRelativePathsCheckBox.isSelected();

        myConfig.setMainJSDirString(mainJSRootDirString.trim());
        myConfig.setNodeModulesDirString(nodeModulesDirString.trim());
        myConfig.setDeepIncludeModulesDirString(deepIncludeNodeModulesString.trim());
        myConfig.setUseRelativePathsForMain(useRelativePathsForMainJsDir);

    }

    @Override
    public void reset() {
        myConfig.setUseRelativePathsForMain(false);
        myConfig.setNodeModulesDirString("");
        myConfig.setMainJSDirString("");
        myConfig.setDeepIncludeModulesDirString("");
    }

    @Override
    public void disposeUIResources() {

    }
}
