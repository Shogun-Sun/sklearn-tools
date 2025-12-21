package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil

class CreateRequirementsTxt : AnAction() {

    private val libraries = listOf(
        "numpy",
        "pandas",
        "pandas-stubs",
        "scikit-learn",
        "matplotlib"
    )

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        createOrUpdateRequirementsFile(project)
    }

    fun createOrUpdateRequirementsFile(project: com.intellij.openapi.project.Project) {
        val basePath = project.basePath ?: return
        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath) ?: return

        ApplicationManager.getApplication().runWriteAction {
            var file = vFile.findChild("requirements.txt")
            if (file == null) {
                file = vFile.createChildData(this, "requirements.txt")
            }
            VfsUtil.saveText(file, libraries.joinToString(separator = "\n"))
        }
    }
}
