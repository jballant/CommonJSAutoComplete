package config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectImpl;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;


public class JSRequireConfig {

    private static HashMap<String, JSRequireConfig> instances = null;

    public static final String NODE_MODULES_DIR_NAME = "node_modules";
    private static final String COMMA_STR = ",";
    private static final String JS_EXT = "js";
    private static final String COFFEE_EXT = "coffee";

    private static final String MAIN_JS_DIR_KEY = "jballant.CommonJSAutoComplete.mainJSDirKey";
    private static final String NODE_MODULES_DIR_KEY = "jballant.CommonJSAutoComplete.nodeModulesDir";
    private static final String DEEP_INCLUDE_MODULES_DIR_KEY = "jballant.CommonJSAutoComplete.deepIncludeNodeModulesKey";
    private static final String USE_RELATIVE_PATHS_FOR_MAIN_KEY = "jballant.CommonJSAutoComplete.useRelativePathsForMainKey";

    private static final String TRUE_STRING = "Y";
    private static final String FALSE_STRING = "N";

    private static String[] allowedExtensions = null;

    private VirtualFile mainJSRootDir = null;
    private VirtualFile nodeModulesRootDir = null;
    private ArrayList<VirtualFile> includedNodeModules = null;

    private Project myProject = null;

    public static @NotNull JSRequireConfig getInstanceForProject (@NotNull Project project) {
        if (instances == null) {
            instances = new HashMap<String, JSRequireConfig>();
        }

        JSRequireConfig inst = instances.get(project.getName());
        if (inst == null) {
            inst = new JSRequireConfig();
            instances.put(project.getName(), inst);
        }
        inst.setup(project);
        return inst;
    }

    private void setup (@NotNull Project project) {

        if (myProject == null) {
            myProject = project;
        }

        if (allowedExtensions == null) {
            allowedExtensions = new String[2];
            allowedExtensions[0] = JS_EXT;
            allowedExtensions[1] = COFFEE_EXT;
        }

        if (mainJSRootDir == null) {
            String mainJSDirString = getMainJSDirString();
            mainJSDirString = mainJSDirString != null ? mainJSDirString : "";
            VirtualFile main = project.getBaseDir().findFileByRelativePath(mainJSDirString);
            if (main != null) {
                mainJSRootDir = main;
            }
        }

        if (nodeModulesRootDir == null) {
            String nodeModulesDirString = getNodeModulesDirString();
            nodeModulesDirString = nodeModulesDirString != null ? nodeModulesDirString : NODE_MODULES_DIR_NAME;
            VirtualFile nodeModules = project.getBaseDir().findFileByRelativePath(nodeModulesDirString);
            if (nodeModules != null) {
                nodeModulesRootDir = nodeModules; // node modules are only shallow includes by default
                determineDeepIncludedNodeModules(getDeepIncludeModulesDirString());
            }
        }

    }

    private void setMainJSDirWithString (@NotNull String relativePath) {
        mainJSRootDir = myProject.getBaseDir().findFileByRelativePath(relativePath);
    }

    private void setNodeModulesDirWithString (@NotNull String relativePath) {
        if (relativePath.trim().equals("")) {
            relativePath = NODE_MODULES_DIR_NAME;
        }
        nodeModulesRootDir = myProject.getBaseDir().findFileByRelativePath(relativePath);
    }

    public @Nullable VirtualFile getNodeModulesRootDir () {
        return nodeModulesRootDir;
    }

    public @Nullable VirtualFile getMainJSRootDir () {
        return mainJSRootDir;
    }

    private void determineDeepIncludedNodeModules(@Nullable String includedModulesString) {

        // Make included included node_modules at least an empty
        // array list, even if the string value is null. If the
        // string value is not null, then it will be populated
        // with virtual files
        includedNodeModules = new ArrayList<VirtualFile>();

        if (includedModulesString == null) {
            return;
        }

        String[] splitStrings = includedModulesString.split(COMMA_STR);
        for (String moduleName : splitStrings) {
            VirtualFile nmd = getNodeModulesRootDir();
            if (nmd == null) {
                return;
            }
            VirtualFile child = nmd.findChild(moduleName.trim());
            if (child != null && child.isDirectory()) {
                includedNodeModules.add(child);
            }
        }
    }

    public boolean hasAllowedExtension(@Nullable String ext) {
        // Null could mean it is a directory, which we allow because it could be a node_module
        if (ext == null) {
            return true;
        }
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<VirtualFile> getDeepIncludedNodeModules() {
        return includedNodeModules;
    }

    private void setPersistVal(@NotNull String key, @NotNull String value) {
        PropertiesComponent.getInstance(myProject).setValue(formatKey(key), value.trim());
    }

    public @NotNull String getPersistVal(@NotNull String key) {
        return PropertiesComponent.getInstance(myProject).getValue(formatKey(key), "");
    }

    public void setMainJSDirString(@NotNull String value) {
        setPersistVal(MAIN_JS_DIR_KEY, value);
        setMainJSDirWithString(value);
    }

    public void setNodeModulesDirString(@NotNull String value) {
        setPersistVal(NODE_MODULES_DIR_KEY, value);
        setNodeModulesDirWithString(value);
    }

    public void setDeepIncludeModulesDirString(@NotNull String value) {
        setPersistVal(DEEP_INCLUDE_MODULES_DIR_KEY, value);
        determineDeepIncludedNodeModules(value);
    }

    public void setUseRelativePathsForMain(boolean value) {
        setPersistVal(USE_RELATIVE_PATHS_FOR_MAIN_KEY, value ? TRUE_STRING : FALSE_STRING);
    }

    public @NotNull String getMainJSDirString() {
        return getPersistVal(MAIN_JS_DIR_KEY);
    }

    public @NotNull String getNodeModulesDirString() {
        return getPersistVal(NODE_MODULES_DIR_KEY);
    }

    public @NotNull String getDeepIncludeModulesDirString() {
        return getPersistVal(DEEP_INCLUDE_MODULES_DIR_KEY);
    }

    public boolean getUseRelativePathsForMain() {
        return getPersistVal(USE_RELATIVE_PATHS_FOR_MAIN_KEY).equals(TRUE_STRING);
    }

    private String formatKey(@NotNull String key) {
        ProjectImpl myProjectImpl = (ProjectImpl) myProject;
        return key . concat("-") . concat(myProjectImpl.getDefaultName());
    }

}
