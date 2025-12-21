package sun.shogun.sklearntools

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document

class InsertImportsBeforeCode {

    /**
     * Вставляет переданный список импортов в документ.
     * Дублирующие импорты игнорируются.
     */
    fun insertImports(editorDocument: Document, imports: List<String>) {
        val fullText = editorDocument.text
        val missingImports = imports.filter { !fullText.contains(it) }
        if (missingImports.isNotEmpty()) {
            val importText = missingImports.joinToString(separator = "\n") + "\n\n"
            WriteCommandAction.runWriteCommandAction(null) {
                editorDocument.setText(importText + fullText)
            }
        }
    }
}
