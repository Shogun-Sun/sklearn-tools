package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class InsertFeatureSelectionTemplate : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val imports = listOf(
            "import pandas as pd",
            "from sklearn.model_selection import train_test_split"
        )

        val code = """
            |# --- ОТБОР ПРИЗНАКОВ ---
            |
            |# Укажите целевой столбец
            |target_col = "target"
            |
            |# Укажите список признаков
            |feature_cols = ["feature1", "feature2", "feature3"]
            |
            |# Отбор признаков и целевой переменной
            |X = df[feature_cols]
            |y = df[target_col]
            |
            |# Разделение на обучающую и тестовую выборки
            |X_train, X_test, y_train, y_test = train_test_split(
            |    X, y, test_size=0.2, random_state=42
            |)
            |
            |print("Размер обучающей выборки:", X_train.shape)
            |print("Размер тестовой выборки:", X_test.shape)
            |
            |print("Примеры признаков:")
            |print(X_train.head())
        """.trimMargin()

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            importsInserter.insertImports(document, imports)
            document.setText(document.text + "\n\n" + code)
        }
    }
}
