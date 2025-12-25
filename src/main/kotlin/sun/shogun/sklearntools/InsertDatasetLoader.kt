package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.util.zip.ZipFile

class InsertDatasetLoader : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val descriptor = FileChooserDescriptor(true, false, false, false, false, false).apply {
            title = "Select dataset (CSV or ZIP)"
        }
        val file = FileChooser.chooseFile(descriptor, project, null) ?: return

        val (imports, code) = when (file.extension?.lowercase()) {
            "csv" -> handleCsv(file, project)
            "zip" -> handleZip(file, project)
            else -> {
                Messages.showErrorDialog(project, "Unsupported file type", "Error")
                return
            }
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            importsInserter.insertImports(document, imports)
            document.setText(document.text + "\n\n" + code)
        }
    }

    // ===== CSV =====
    private fun handleCsv(trainFile: VirtualFile, project: com.intellij.openapi.project.Project): Pair<List<String>, String> {
        val hasTest = Messages.showYesNoDialog(
            project,
            "Do you have a separate test dataset?",
            "Test dataset",
            Messages.getQuestionIcon()
        ) == Messages.YES

        var testFile: VirtualFile? = null
        if (hasTest) {
            val testDescriptor = FileChooserDescriptor(true, false, false, false, false, false).apply {
                title = "Select test dataset (CSV)"
            }
            testFile = FileChooser.chooseFile(testDescriptor, project, null)
        }

        val imports = mutableSetOf("import pandas as pd")
        val code = buildString {
            append("""df_train = pd.read_csv("${trainFile.path}")""")
            if (testFile != null) {
                append("\n\ndf_test = pd.read_csv(\"${testFile.path}\")")
            }
        }

        return imports.toList() to code
    }

    // ===== ZIP =====
    private fun handleZip(zipFile: VirtualFile, project: com.intellij.openapi.project.Project): Pair<List<String>, String> {
        val csvNames = getCsvFromZip(zipFile.path)
        if (csvNames.isEmpty()) {
            Messages.showErrorDialog(project, "No CSV files in archive", "Error")
            return emptyList<String>() to ""
        }

        val imports = mutableSetOf("import pandas as pd", "import zipfile")

        // --- пользователь выбирает TRAIN и TEST ---
        val trainCsv: String
        val testCsv: String?

        if (csvNames.size == 1) {
            trainCsv = csvNames.first()
            testCsv = null
        } else {
            Messages.showInfoMessage(
                project,
                "Archive contains ${csvNames.size} CSV files.\nPlease choose train and test datasets.",
                "Select datasets"
            )

            val trainIndex = Messages.showChooseDialog(
                project,
                "Select TRAIN dataset",
                "Train CSV",
                null,
                csvNames.toTypedArray(),
                csvNames.first()
            ) ?: 0  // если отменили диалог, берем первый элемент

            trainCsv = csvNames[trainIndex]

            val remaining = csvNames.filter { it != trainCsv }
            testCsv = if (remaining.isNotEmpty()) {
                val testIndex = Messages.showChooseDialog(
                    project,
                    "Select TEST dataset",
                    "Test CSV",
                    null,
                    remaining.toTypedArray(),
                    remaining.first()
                ) ?: 0
                remaining[testIndex]
            } else null

        }

        // --- генерируем Python-код ---
        val code = buildString {
            append(
                """
zip_path = "${zipFile.path}"

with zipfile.ZipFile(zip_path, 'r') as zip_ref:
    with zip_ref.open("$trainCsv") as f:
        df_train = pd.read_csv(f)
""".trimIndent()
            )

            if (testCsv != null) {
                append("\n") // перенос строки между блоками
                append(
                    "    with zip_ref.open(\"$testCsv\") as f:\n" +
                            "        df_test = pd.read_csv(f)\n"
                )
            }
        }



        return imports.toList() to code
    }

    private fun getCsvFromZip(zipPath: String): List<String> =
        ZipFile(zipPath).use { zip ->
            zip.entries().toList()
                .filter { !it.isDirectory && it.name.endsWith(".csv") }
                .map { it.name }
        }
}
