import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by james on 7/19/14.
 */
public class JSRequireConfig {

    private static HashMap<String, JSRequireConfig> instances = null;

    static final String NODE_MODULES_DIR_NAME = "node_modules";

    private VirtualFile mainJSRootDir = null;
    private VirtualFile nodeModulesRootDir = null;
    private ArrayList<VirtualFile> includedNodeModules = null;

    public static JSRequireConfig getInstanceForProject (Project project) {
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
        // TODO: add users main path from ui
        if (mainJSRootDir == null) {
            VirtualFile main = project.getBaseDir().findFileByRelativePath("");
            if (main != null) {
                mainJSRootDir = main;
            }
        }

        // TODO: make path to node_modules configurable
        // TODO: make a configurable list of included directories!!!!
        if (nodeModulesRootDir == null) {
            VirtualFile nodeModules = project.getBaseDir().findChild(NODE_MODULES_DIR_NAME);
            if (nodeModules != null) {
                nodeModulesRootDir = nodeModules; // node modules are only shallow includes by default
                determineDeepIncludedNodeModules();
            }
        }

    }

    public VirtualFile getNodeModulesRootDir () {
        return nodeModulesRootDir;
    }

    public VirtualFile getMainJSRootDir () {
        return mainJSRootDir;
    }

    private void determineDeepIncludedNodeModules() {

        String includedModulesString = "bloop"; // TODO: get this from config
        String[] splitStrings = includedModulesString.split(",");
        includedNodeModules = new ArrayList<VirtualFile>();
        for (String moduleName : splitStrings) {
            VirtualFile child = getNodeModulesRootDir().findChild(moduleName);
            if (child != null && child.isDirectory()) {
                includedNodeModules.add(child);
            }
        }
    }

    public ArrayList<VirtualFile> getDeepIncludedNodeModules() {
        return includedNodeModules;
    }

}
