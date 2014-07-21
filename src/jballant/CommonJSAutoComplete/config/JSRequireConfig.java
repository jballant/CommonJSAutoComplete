package config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by james on 7/19/14.
 */
public class JSRequireConfig {

    private static HashMap<String, JSRequireConfig> instances = null;

    public static final String NODE_MODULES_DIR_NAME = "node_modules";

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

    public static JSRequireConfig getInstanceForProject (@NotNull Project project) {
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

    private void setup (Project project) {

        if (myProject == null) {
            myProject = project;
        }

        if (allowedExtensions == null) {
            allowedExtensions = new String[2];
            allowedExtensions[0] = "js";
            allowedExtensions[1] = "coffee";
        }

        // TODO: add users main path from ui
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

    private void setMainJSDirWithString (String relativePath) {
        mainJSRootDir = myProject.getBaseDir().findFileByRelativePath(relativePath);
    }

    private void setNodeModulesDirWithString (String relativePath) {
        nodeModulesRootDir = myProject.getBaseDir().findFileByRelativePath(relativePath);
    }

    public VirtualFile getNodeModulesRootDir () {
        return nodeModulesRootDir;
    }

    public VirtualFile getMainJSRootDir () {
        return mainJSRootDir;
    }

    private void determineDeepIncludedNodeModules(String includedModulesString) {

        includedNodeModules = new ArrayList<VirtualFile>();

        if (includedModulesString == null) {
            return;
        }

        String[] splitStrings = includedModulesString.split(",");
        for (String moduleName : splitStrings) {
            VirtualFile child = getNodeModulesRootDir().findChild(moduleName.trim());
            if (child != null && child.isDirectory()) {
                includedNodeModules.add(child);
            }
        }
    }

    public boolean hasAllowedExtension(String ext) {
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

    private void setPersistVal(String key, String value) {
        PropertiesComponent.getInstance(myProject).setValue(formatKey(key), value);
    }

    public String getPersistVal(String key) {
        return PropertiesComponent.getInstance(myProject).getValue(formatKey(key), "");
    }

    public void setMainJSDirString(String value) {
        setPersistVal(MAIN_JS_DIR_KEY, value);
        setMainJSDirWithString(value);
    }

    public void setNodeModulesDirString(String value) {
        setPersistVal(NODE_MODULES_DIR_KEY, value);
        setNodeModulesDirWithString(value);
    }

    public void setDeepIncludeModulesDirString(String value) {
        setPersistVal(DEEP_INCLUDE_MODULES_DIR_KEY, value);
        determineDeepIncludedNodeModules(value);
    }

    public void setUseRelativePathsForMain(boolean value) {
        setPersistVal(USE_RELATIVE_PATHS_FOR_MAIN_KEY, value ? TRUE_STRING : FALSE_STRING);
    }

    public String getMainJSDirString() {
        return getPersistVal(MAIN_JS_DIR_KEY);
    }

    public String getNodeModulesDirString() {
        return getPersistVal(NODE_MODULES_DIR_KEY);
    }

    public String getDeepIncludeModulesDirString() {
        return getPersistVal(DEEP_INCLUDE_MODULES_DIR_KEY);
    }

    public boolean getUseRelativePathsForMain() {
        return getPersistVal(USE_RELATIVE_PATHS_FOR_MAIN_KEY).equals(TRUE_STRING);
    }

    private String formatKey(String key) {
        return key . concat("-") . concat(myProject.getName());
    }

}
