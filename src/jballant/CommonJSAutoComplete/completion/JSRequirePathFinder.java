package completion;

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

    private boolean shouldMakePathsRelative;
    private VirtualFile mainJsDir = null;
    private VirtualFile nodeModulesDir = null;
    private VirtualFile[] rootJSDirs = null;
    private boolean matchAllCases = false;
    private boolean ignoreCapitalization = false;
    private PsiFile currentPsiFile = null;
    private JSRequireConfig config = null;
    private ArrayList<VirtualFile> deepIncludedNodeModules = null;

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

        VfsUtilCore.visitChildrenRecursively(searchDir, new VirtualFileVisitor() {
            private boolean isDeepIncludedNodeModule = false;
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
                boolean hasAllowedExtension = config.hasAllowedExtension(ext);
                ext = ext != null ? DOT_STRING.concat(ext) : "";
                // skip dir if current file or folder is hidden
                if (curFileName.charAt(0) == DOT_CHAR) {
                    return false;
                }
                // skip dir if we are not searching within our node modules
                // and this file is our node modules directory
                if (!withinOwnNodeModules && isOwnNodeModules(file)) {
                    return false;
                }
                // skip dir if the current directory is a node_modules
                // directory that isn't our own
                if (!isOwnNodeModules(file) && isANodeModulesDir(file)) {
                    return false;
                }
                // if the file is an allowed extension then
                // then test to see if the varName + extension is the same
                if (hasAllowedExtension && varNameMatchesFileString(fileNameWithoutExt, curFileName, ext)) {
                    files.add(file);
                }
                // If we are withinOwnNodeModules and our deepIncludedNodeModules contains
                // the current file, then we can search that folder
                if (withinOwnNodeModules && !isDeepIncludedNodeModule && deepIncludedNodeModules.contains(file)) {
                    isDeepIncludedNodeModule = true;
                }
                return (file.isDirectory() && (!withinOwnNodeModules || isDeepIncludedNodeModule));
            }

            private boolean isANodeModulesDir(@NotNull VirtualFile file) {
                return file.isDirectory() && file.getName().equals(JSRequireConfig.NODE_MODULES_DIR_NAME);
            }

            private boolean isOwnNodeModules(@NotNull VirtualFile file) {
                return file.equals(ownNodeModulesDir);
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
                (camelCaseFile.equals(varName.concat(jsCamelCaseModuleSuffix).concat(ext)));
    }


}
