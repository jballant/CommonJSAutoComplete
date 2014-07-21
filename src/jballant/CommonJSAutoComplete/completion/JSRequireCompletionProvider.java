package completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSVariableImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.apache.tools.ant.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import config.JSRequireConfig;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by james on 7/15/14.
 */
public class JSRequireCompletionProvider extends CompletionProvider<CompletionParameters> {

    static final String JS_REF_CLASS = JSReferenceExpressionImpl.class.getName();
    static final String REQUIRE_FUNC_NAME = "require";

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters completionParameters,
            ProcessingContext processingContext,
            @NotNull CompletionResultSet completionResultSet) {

        PsiElement origPsiElement = completionParameters.getOriginalPosition();
        if (origPsiElement == null) {
            return;
        }

        PsiElement psiElement = completionParameters.getPosition();
        if (!psiElement.getLanguage().is(JavascriptLanguage.INSTANCE)) {
            return;
        }

        if (!isJSRefExpression(psiElement)) {
            return;
        }

        PsiElement rawJsVar = origPsiElement.getParent().getParent();
        JSVariable jsVar;
        // Make sure that the current element is a JS Variable
        if (isJSVar(rawJsVar)) {
            jsVar = (JSVariableImpl)rawJsVar;
        } else {
            return;
        }

        PsiElement varStatement = jsVar.getParent();
        if (!isJSVarStatement(varStatement) || !isPotentialRequireStatement(origPsiElement)) {
            return;
        }

        String varName = jsVar.getName();
        PsiFile origFile = completionParameters.getOriginalFile();

        // Find all the corresponding require statement file paths
        // given the var name
        ArrayList<String> paths = findFilePathsForVarName(varName, origFile);
        for (String path : paths) {
            completionResultSet.addElement(buildLookupElementWithPath(path));
        }
    }

    public static LookupElement buildLookupElementWithPath(String path) {
        String completionString = "require('".concat(path).concat("')");
        return LookupElementBuilder
                .create(completionString.concat(";"))
                .withBoldness(true)
                .withPresentableText(completionString)
                .withTailText(";")
                .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
    }

    public static boolean isJSRefExpression (PsiElement element) {
        PsiElement context = element.getContext();
        return context != null && context instanceof JSReferenceExpression;
    }

    public static boolean isJSVarStatement (PsiElement element) {
        return element instanceof JSVarStatement;
    }

    public static boolean isJSVar (PsiElement element) {
        return element instanceof JSVariable;
    }

    // TODO: use relativise with current file to create relative path
    public static ArrayList<String> findFilePathsForVarName(String varName, PsiFile currentPsiFile) {

        Project project = currentPsiFile.getProject();
        JSRequireConfig config = JSRequireConfig.getInstanceForProject(project);
        boolean shouldMakePathsRelative = config.getUseRelativePathsForMain();

        VirtualFile[] rootJSDirs = new VirtualFile[2];
        rootJSDirs[0] = config.getMainJSRootDir();
        rootJSDirs[1] = config.getNodeModulesRootDir();

        ArrayList<String> paths = new ArrayList<String>();
        ArrayList<VirtualFile> rootDirFiles;
        String filePath;
        for (VirtualFile rootDir : rootJSDirs) {

            if (rootDir == null) {
                continue;
            }

            rootDirFiles = new ArrayList<VirtualFile>();

            // Populates rootDirFiles with VirtualFiles that match the varName
            // searching throught the root directory
            findFilePathForNameHelper(rootDir, varName, rootDirFiles, config);

            for (VirtualFile file : rootDirFiles) {
                if (file != null) {
                    if (!shouldMakePathsRelative || rootDir != config.getMainJSRootDir()) {
                        filePath = getRequirePathFromRootDir(file, rootDir);
                    } else {
                        filePath = getRequirePathRelativeToCurrentFile(currentPsiFile.getVirtualFile(), file);
                    }
                    if (filePath != null) {
                        paths.add(filePath);
                    }
                }
            }
        }

        return paths;
    }

    private static String getRequirePathFromRootDir(VirtualFile file, VirtualFile rootDir) {
        String ext = file.getExtension();
        String filePath = file.getPath();
        return filePath.substring(
                rootDir.getPath().length() + 1,
                (filePath.length() - (ext != null ? ext.length() + 1 : 0))
        );
    }

    private static String getRequirePathRelativeToCurrentFile(VirtualFile currentFile, VirtualFile relativeFile) {
        String ext = relativeFile.getExtension();
        String filePath = relativizePaths(currentFile.getPath(), relativeFile.getPath());
        if (filePath != null) {
            return filePath.substring(0, filePath.length() - (ext != null ? ext.length() + 1 : 0));
        }
        return null;
    }

    private static String relativizePaths(String aAbsolutePath, String bAbsolutePath) {
        String relPath;
        try {
             relPath = FileUtils.getRelativePath(new File(aAbsolutePath), new File(bAbsolutePath));
        } catch (Exception e) {
            return null;
        }
        return relPath;
    }

    private static void findFilePathForNameHelper(
            final VirtualFile searchDir,
            final String fileNameWithoutExt,
            final ArrayList<VirtualFile> files,
            final JSRequireConfig config
    ) {

        final ArrayList<VirtualFile> deepIncludedNodeModules = config.getDeepIncludedNodeModules();
        final VirtualFile ownNodeModulesDir = config.getNodeModulesRootDir();
        final boolean withinOwnNodeModules = ownNodeModulesDir == searchDir;

        VfsUtilCore.visitChildrenRecursively(searchDir, new VirtualFileVisitor() {
            private boolean isDeepIncludedNodeModule = false;
            final char DOT_CHAR = '.';
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {

                // Return true (that we want to search this directory)
                // if the current file is the search directory
                if (file == searchDir) {
                    return true;
                }

                String curFileName = file.getName();
                String ext = file.getExtension(); // TODO: TEST APPROVED EXTENSIONS!
                boolean hasAllowedExtension = config.hasAllowedExtension(ext);
                ext = ext != null ? ".".concat(ext) : "";
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
                if (withinOwnNodeModules && !isOwnNodeModules(file) && isANodeModulesDir(file)) {
                    return false;
                }
                // camelcase the current file name if it has dashes and
                // then test to see if the varName + extension is the same
                if (hasAllowedExtension && camelCaseString(curFileName).equals(fileNameWithoutExt.concat(ext))) {
                    files.add(file);
                }
                // If we are withinOwnNodeModules and our deepIncludedNodeModules contains
                // the current file, then we can search that folder
                if (withinOwnNodeModules && !isDeepIncludedNodeModule && deepIncludedNodeModules.contains(file)) {
                    isDeepIncludedNodeModule = true;
                }
                return (file.isDirectory() && (!withinOwnNodeModules || isDeepIncludedNodeModule));
            }
            private boolean isANodeModulesDir(VirtualFile file) {
                return file.isDirectory() && file.getName().equals(JSRequireConfig.NODE_MODULES_DIR_NAME);
            }
            private boolean isOwnNodeModules(VirtualFile file) {
                return file == ownNodeModulesDir;
            }
        });

    }

    public static boolean isPotentialRequireStatement (PsiElement element) {
        String text = element.getText();
        if (text == null) {
            return false;
        }

        int len = text.length();
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) != REQUIRE_FUNC_NAME.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static String camelCaseString (String input) {
        if (!input.contains("-")) {
            return input;
        }
        String[] splitInput = input.split("-");
        String str;
        String assembledStr = "";
        for (int i = 0; i < splitInput.length; i++) {
            str = splitInput[i];
            if (i > 0) {
                str = str.substring(0, 1).toUpperCase().concat(str.substring(1));
            }
            assembledStr = assembledStr.concat(str);
        }
        return assembledStr;
    }

}
