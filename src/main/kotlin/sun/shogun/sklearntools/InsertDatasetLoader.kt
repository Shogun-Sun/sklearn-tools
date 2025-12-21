package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

class InsertDatasetLoader : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        // Диалог выбора одного или двух файлов
        val descriptor = FileChooserDescriptor(true, false, false, false, false, true).apply {
            title = "Select dataset file(s) (CSV or ZIP)"
            withShowHiddenFiles(true)
        }
        val selectedFiles: Array<VirtualFile> = FileChooser.chooseFiles(descriptor, project, null)

        if (selectedFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No file selected", "Info")
            return
        }

        val (imports, code) = generateLoaderCodeAndImports(selectedFiles)

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document

            // Вставляем необходимые импорты перед кодом
            importsInserter.insertImports(document, imports)

            // Добавляем шаблон кода после импортов
            val fullText = document.text
            document.setText(fullText + "\n\n" + code)
        }
    }

    /**
     * Генерирует код загрузки данных и список импортов, необходимых для работы этого кода
     */
    private fun generateLoaderCodeAndImports(files: Array<VirtualFile>): Pair<List<String>, String> {
        val imports = mutableSetOf(
            "import pandas as pd"
        )
        val codeBuilder = StringBuilder()

        files.forEachIndexed { index, file ->
            when (file.extension?.lowercase()) {
                "zip" -> {
                    imports.add("import zipfile")
                    imports.add("import os")

                    codeBuilder.append("""
                        zip_path_${index + 1} = "${file.path}"
                        extract_dir_${index + 1} = "data_${index + 1}"

                        with zipfile.ZipFile(zip_path_${index + 1}, 'r') as zip_ref:
                            zip_ref.extractall(extract_dir_${index + 1})

                        csv_files_${index + 1} = [f for f in os.listdir(extract_dir_${index + 1}) if f.endswith('.csv')]
                        if not csv_files_${index + 1}:
                            raise FileNotFoundError("No CSV file found in the zip archive")
                        ${if (files.size == 1) "df" else "df_${if (index == 0) "train" else "test"}"} = pd.read_csv(os.path.join(extract_dir_${index + 1}, csv_files_${index + 1}[0]))
                        
                    """.trimIndent())
                }

                "csv" -> {
                    codeBuilder.append("${if (files.size == 1) "df" else "df_${if (index == 0) "train" else "test"}"} = pd.read_csv(\"${file.path}\")\n")
                }

                else -> {
                    codeBuilder.append("# Unsupported file type: ${file.name}\n")
                }
            }
            codeBuilder.append("\n")
        }

        return imports.toList() to codeBuilder.toString()
    }
}
