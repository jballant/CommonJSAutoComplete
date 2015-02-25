package config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;


public class JSRequireConfig {

    public static boolean DEBUG = false;

    private static HashMap<String, JSRequireConfig> instances = null;

    public static final String NODE_MODULES_DIR_NAME = "node_modules";
    private static final String COMMA_STR = ",";
    private static final String JS_EXT = "js";
    private static final String COFFEE_EXT = "coffee";

    private static final String MAIN_JS_DIR_KEY = "jballant.CommonJSAutoComplete.mainJSDirKey";
    private static final String NODE_MODULES_DIR_KEY = "jballant.CommonJSAutoComplete.nodeModulesDir";
    private static final String DEEP_INCLUDE_MODULES_DIR_KEY = "jballant.CommonJSAutoComplete.deepIncludeNodeModulesKey";
    private static final String USE_RELATIVE_PATHS_FOR_MAIN_KEY = "jballant.CommonJSAutoComplete.useRelativePathsForMainKey";
    private static final String SHOULD_IGNORE_CASE_KEY = "jballant.CommonJSAutoComplete.shouldIgnoreCaseKey";

    private static final String TRUE_STRING = "Y";
    private static final String FALSE_STRING = "N";

    private static String[] allowedExtensions = null;

    private VirtualFile mainJSRootDir = null;
    private VirtualFile nodeModulesRootDir = null;
    private ArrayList<VirtualFile> includedNodeModules = null;

    private Project myProject = null;

    private String mainJSDirString = null;
    private String nodeModulesDirString = null;
    private String deepIncludedNodeModulesString = null;

    private boolean useRelativePathsForMain = false;
    private boolean hasRetrievedUseRelativePathsForMainVal = false;

    private boolean shouldIgnoreCase = false;
    private boolean hasRetrievedShouldIgnoreCase = false;

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
            VirtualFile main = project.getBaseDir().findFileByRelativePath(getMainJSDirString());
            if (main != null) {
                mainJSRootDir = main;
            }
        }

        if (nodeModulesRootDir == null) {
            String nodeModulesDirString = getNodeModulesDirString();
            nodeModulesDirString = !nodeModulesDirString.equals("") ? nodeModulesDirString : NODE_MODULES_DIR_NAME;
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
        try {
            if (!myProject.isDisposed()) {
                PropertiesComponent.getInstance(myProject).setValue(formatKey(key), value.trim());
            }
        } catch (AssertionError error) {
            if (DEBUG) {
                error.printStackTrace();
            }
        }
    }

    public @NotNull String getPersistVal(@NotNull String key) {
        String val = "";
        try {
            if (!myProject.isDisposed()) {
                val = PropertiesComponent.getInstance(myProject).getValue(formatKey(key), "");
            }
        } catch (AssertionError error) {
            if (DEBUG) {
                error.printStackTrace();
            }
        }
        return val;
    }

    public void setMainJSDirString(@NotNull String value) {
        mainJSDirString = value;
        setPersistVal(MAIN_JS_DIR_KEY, value);
        setMainJSDirWithString(value);
    }

    public void setNodeModulesDirString(@NotNull String value) {
        nodeModulesDirString = value;
        setPersistVal(NODE_MODULES_DIR_KEY, value);
        setNodeModulesDirWithString(value);
    }

    public void setDeepIncludeModulesDirString(@NotNull String value) {
        setPersistVal(DEEP_INCLUDE_MODULES_DIR_KEY, value);
        deepIncludedNodeModulesString = value;
        determineDeepIncludedNodeModules(value);
    }

    public void setUseRelativePathsForMain(boolean value) {
        hasRetrievedUseRelativePathsForMainVal = true;
        useRelativePathsForMain = value;
        setPersistVal(USE_RELATIVE_PATHS_FOR_MAIN_KEY, value ? TRUE_STRING : FALSE_STRING);
    }

    public void setShouldIgnoreCase(boolean value) {
        hasRetrievedShouldIgnoreCase = true;
        shouldIgnoreCase = value;
        setPersistVal(SHOULD_IGNORE_CASE_KEY, value ? TRUE_STRING : FALSE_STRING);
    }

    public @NotNull String getMainJSDirString() {
        if (mainJSDirString == null) {
            mainJSDirString = getPersistVal(MAIN_JS_DIR_KEY);
        }
        return mainJSDirString;
    }

    public @NotNull String getNodeModulesDirString() {
        if (nodeModulesDirString == null) {
            nodeModulesDirString = getPersistVal(NODE_MODULES_DIR_KEY);
        }
        return getPersistVal(NODE_MODULES_DIR_KEY);
    }

    public @NotNull String getDeepIncludeModulesDirString() {
        if (deepIncludedNodeModulesString == null) {
            deepIncludedNodeModulesString = getPersistVal(DEEP_INCLUDE_MODULES_DIR_KEY);
        }
        return deepIncludedNodeModulesString;
    }

    public boolean getUseRelativePathsForMain() {
        if (!hasRetrievedUseRelativePathsForMainVal) {
            String stringVal = getPersistVal(USE_RELATIVE_PATHS_FOR_MAIN_KEY);
            useRelativePathsForMain = stringVal.equals("") || stringVal.equals(TRUE_STRING); // default to true
            hasRetrievedUseRelativePathsForMainVal = true;
        }
        return useRelativePathsForMain;
    }

    public boolean getShouldIgnoreCase() {
        if (!hasRetrievedShouldIgnoreCase) {
            String stringVal = getPersistVal(SHOULD_IGNORE_CASE_KEY);
            shouldIgnoreCase = stringVal.equals(TRUE_STRING); // default to false
            hasRetrievedShouldIgnoreCase = true;
        }
        return shouldIgnoreCase;
    }

    private String formatKey(@NotNull String key) {
        return key . concat("-") . concat(myProject.getBasePath());
    }

}
