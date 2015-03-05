package completion;

// import com.intellij.json.JsonFileType;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.json.JSONFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import completion.util.StringUtil;
import config.JSRequireConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;

/**
 * Class to help find require paths given a var name
 * a current psi file and some other settings
 */
public class JSRequirePathFinder {

    static final char REQUIRE_PATH_SEPARATOR = '/';
    static final String JS_MODULE_SUFFIX = "js";
    static final String JS_CAMELCASE_MODULE_SUFFIX = "Js";
    static final String NODE_PREFIX = "node";

    private boolean shouldMakePathsRelative;
    private VirtualFile mainJsDir = null;
    private VirtualFile nodeModulesDir = null;
    private VirtualFile[] rootJSDirs = null;
    private boolean matchAllCases = false;
    private boolean ignoreCapitalization = false;
    private PsiFile currentPsiFile = null;
    private JSRequireConfig config = null;

    public JSRequirePathFinder(@NotNull PsiFile currentFile) {
        currentPsiFile = currentFile;
        config = JSRequireConfig.getInstanceForProject(currentFile.getProject());
        shouldMakePathsRelative = config.getUseRelativePathsForMain();
        mainJsDir = config.getMainJSRootDir();
        nodeModulesDir = config.getNodeModulesRootDir();
        matchAllCases = config.getShouldIgnoreCase();
        rootJSDirs = new VirtualFile[2];
        rootJSDirs[0] = mainJsDir;
        rootJSDirs[1] = nodeModulesDir;

    }

    public @NotNull JSRequirePathFinder setIgnoreCapitalization(boolean ignoreCapitalization) {
        this.ignoreCapitalization = ignoreCapitalization;
        return this;
    }

    public @NotNull ArrayList<String> findPathsForVarName (@NotNull String varName) {

        ArrayList<String> paths = new ArrayList<String>();
        ArrayList<VirtualFile> rootDirFiles;
        String filePath;
        for (VirtualFile rootDir : rootJSDirs) {

            if (rootDir == null) {
                continue;
            }
            boolean isMainRootDir = mainJsDir != null && mainJsDir.equals(rootDir);
            boolean isNodeModulesDir = nodeModulesDir != null && nodeModulesDir.equals(rootDir);

            if (!isMainRootDir && !isNodeModulesDir) {
                continue;
            }

            rootDirFiles = new ArrayList<VirtualFile>();

            // Populates rootDirFiles with VirtualFiles that match the varName
            // searching through the root directory
            findFilePathForNameHelper(rootDir, varName, rootDirFiles);

            for (VirtualFile file : rootDirFiles) {
                if (file != null) {
                    if (isNodeModulesDir || (!shouldMakePathsRelative && isMainRootDir)) {
                        filePath = getRequirePathFromRootDir(file, rootDir);
                    } else {
                        filePath = getRequirePathRelativeToCurrentFile(currentPsiFile.getVirtualFile(), file);
                    }
                    if (filePath != null) {
                        filePath = filePath.replace(File.separatorChar, REQUIRE_PATH_SEPARATOR);
                        paths.add(filePath);
                    }
                }
            }
        }

        return paths;
    }

    public static @NotNull String getRequirePathFromRootDir(@NotNull VirtualFile file, @NotNull VirtualFile rootDir) {
        String ext = file.getExtension();
        String filePath = file.getPath();
        return filePath.substring(
                rootDir.getPath().length() + 1,
                (filePath.length() - (ext != null ? ext.length() + 1 : 0))
        );
    }

    public static @Nullable String getRequirePathRelativeToCurrentFile(
            @NotNull VirtualFile currentFile,
            @NotNull VirtualFile relativeFile
    ) {
        String ext = relativeFile.getExtension();
        String filePath = StringUtil.relativizePaths(currentFile.getParent().getPath(), relativeFile.getPath());
        if (filePath != null) {
            return filePath.substring(0, filePath.length() - (ext != null ? ext.length() + 1 : 0));
        }
        return null;
    }

    private void findFilePathForNameHelper(
            @NotNull final VirtualFile searchDir,
            @NotNull final String fileNameWithoutExt,
            @NotNull final ArrayList<VirtualFile> files
    ) {

        final ArrayList<VirtualFile> deepIncludedNodeModules = config.getDeepIncludedNodeModules();
        final VirtualFile ownNodeModulesDir = config.getNodeModulesRootDir();
        final boolean withinOwnNodeModules = ownNodeModulesDir != null && ownNodeModulesDir.equals(searchDir);
        final VirtualFile projectRoot = config.getMainJSRootDir();

        VfsUtilCore.visitChildrenRecursively(searchDir, new VirtualFileVisitor() {

            final char DOT_CHAR = '.';
            final String DOT_STRING = ".";

            @Override
            public boolean visitFile(@NotNull VirtualFile file) {

                // Return true (that we want to search this directory)
                // if the current file is the search directory
                if (file.equals(searchDir)) {
                    return true;
                }

                String curFileName = file.getName();
                String ext = file.getExtension();
                ext = ext != null ? DOT_STRING.concat(ext) : "";
                // skip dir if current file or folder is hidden
                if (curFileName.charAt(0) == DOT_CHAR) {
                    return false;
                }
                boolean isOwnNodeModDir = isOwnNodeModules(file);
                // skip dir if we are not searching within our node modules
                // and this file is our node modules directory
                if (!withinOwnNodeModules && isOwnNodeModDir) {
                    return false;
                }

                // skip dir if the current directory is a node_modules
                // directory that isn't our own
                if (!isOwnNodeModDir && isANodeModulesDir(file)) {
                    return false;
                }
                boolean isOwnNodeModule = isNodeModule(file);
                // if the file is an allowed extension or a node_module or is then test to see
                // if the varName + extension is the same
                if ((isOwnNodeModule || isJSFile(file)) && varNameMatchesFileString(fileNameWithoutExt, curFileName, ext)) {
                    files.add(file);
                }

                return (
                    file.isDirectory() &&
                    !isANodeModulesDir(file) && (
                            isWithinMainJS() ||
                            deepIncludedNodeModules.contains(file) ||
                            hasDeepIncludedNoduleModuleAncestor(file)
                    )
                );
            }

            private boolean isWithinMainJS() {
                return searchDir.equals(mainJsDir);
            }

            private boolean isJSFile(@NotNull VirtualFile file) {
                FileType fileType = file.getFileType();
//                return fileType.equals(JavaScriptFileType.INSTANCE) || fileType.equals(JsonFileType.INSTANCE);
                return fileType.equals(JavaScriptFileType.INSTANCE) || fileType.equals(JSONFileType.JSON);
            }

            private boolean isNodeModule(@NotNull VirtualFile file) {
                VirtualFile parent = file.getParent();
                return parent != null && isOwnNodeModules(parent) && (file.isDirectory() || isJSFile(file));
            }

            private boolean isANodeModulesDir(@NotNull VirtualFile file) {
                return file.isDirectory() && file.getName().equals(JSRequireConfig.NODE_MODULES_DIR_NAME);
            }

            private boolean isOwnNodeModules(@NotNull VirtualFile file) {
                return file.equals(ownNodeModulesDir);
            }

            private boolean hasDeepIncludedNoduleModuleAncestor(@NotNull VirtualFile dir) {
                if (deepIncludedNodeModules.size() == 0) {
                    return false;
                }
                VirtualFile parent = dir.getParent();
                while (parent != null) {
                    if (deepIncludedNodeModules.contains(parent)) {
                        return true;
                    }
                    if (mainJsDir.equals(parent) || isOwnNodeModules(parent)) {
                        return false;
                    }
                    parent = parent.getParent();
                }
                return false;
            }
        });

    }

    private boolean varNameMatchesFileString(
            @NotNull String varName,
            @NotNull String fileName,
            @NotNull String ext
    ) {

        String camelCaseFile = StringUtil.camelCaseString(fileName);
        String jsModuleSuffix = JS_MODULE_SUFFIX;
        String jsCamelCaseModuleSuffix = JS_CAMELCASE_MODULE_SUFFIX;

        if (ignoreCapitalization && !matchAllCases) {
            camelCaseFile = StringUtil.capitalize(camelCaseFile);
            varName = StringUtil.capitalize(varName);
        } else if (matchAllCases) {
            camelCaseFile = camelCaseFile.toLowerCase();
            varName = varName.toLowerCase();
            ext = ext.toLowerCase();
            jsCamelCaseModuleSuffix = jsCamelCaseModuleSuffix.toLowerCase();
            jsModuleSuffix = jsModuleSuffix.toLowerCase();
        }

        return
                (camelCaseFile.equals(varName.concat(ext))) ||
                (camelCaseFile.equals(varName.concat(jsModuleSuffix).concat(ext))) ||
                (camelCaseFile.equals(varName.concat(jsCamelCaseModuleSuffix).concat(ext))) ||
                (camelCaseFile.equals(varName.concat(jsCamelCaseModuleSuffix).concat(ext))) ||
                (camelCaseFile.equals(NODE_PREFIX.concat(StringUtil.capitalize(varName))));
    }


}
