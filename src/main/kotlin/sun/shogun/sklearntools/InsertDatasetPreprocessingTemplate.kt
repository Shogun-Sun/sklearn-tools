package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class InsertDatasetPreprocessingTemplate : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        // --- импорты для шаблона ---
        val imports = listOf(
            "import pandas as pd",
            "import matplotlib.pyplot as plt",
            "from sklearn.preprocessing import StandardScaler, LabelEncoder",
            "from sklearn.model_selection import train_test_split"
        )

        // --- шаблон Python-кода с комментариями на русском ---
        val code = """
            |# --- Шаблон предобработки данных ---
            |
            |# --- УДАЛЕНИЕ СТОЛБЦОВ ---
            |# Пример: удалить ненужные столбцы
            |# df_train = df_train.drop(columns=["column1", "column2"])
            |
            |# --- ОБРАБОТКА ПРОПУСКОВ ---
            |# Удаление строк с любыми пропущенными значениями по всему датасету
            |# df_train = df_train.dropna()
            |
            |# Удаление строк с пропусками только в определённом столбце
            |# df_train = df_train.dropna(subset=["column_name"])
            |
            |# Заполнение пропусков средним значением по столбцу
            |# df_train["column_name"] = df_train["column_name"].fillna(df_train["column_name"].mean())
            |
            |# Заполнение пропусков медианой по столбцу
            |# df_train["column_name"] = df_train["column_name"].fillna(df_train["column_name"].median())
            |
            |# Заполнение пропусков модой (наиболее частым значением)
            |# df_train["column_name"] = df_train["column_name"].fillna(df_train["column_name"].mode()[0])
            |
            |# --- КОДИРОВАНИЕ КАТЕГОРИАЛЬНЫХ ПРИЗНАКОВ ---
            |# One-hot кодирование (для категорий с большим количеством уникальных значений)
            |# df_train = pd.get_dummies(df_train, columns=["categorical_column"])
            |
            |# Label Encoding (для категорий с небольшим количеством уникальных значений)
            |# le = LabelEncoder()
            |# df_train["categorical_column"] = le.fit_transform(df_train["categorical_column"])
            |
            |# --- МАСШТАБИРОВАНИЕ ЧИСЛОВЫХ ПРИЗНАКОВ ---
            |# Пример стандартизации
            |# scaler = StandardScaler()
            |# df_train[["num_col1", "num_col2"]] = scaler.fit_transform(df_train[["num_col1", "num_col2"]])
            |
            |# --- РАЗДЕЛЕНИЕ НА ПРИЗНАКИ И ЦЕЛЕВУЮ ПЕРЕМЕННУЮ ---
            |# X = df_train.drop(columns=["target_column"])
            |# Y = df_train["target_column"]
            |
            |# --- РАЗДЕЛЕНИЕ НА TRAIN/TEST ---
            |# X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.2, random_state=42)
        """.trimMargin()

        // --- вставка в код ---
        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            importsInserter.insertImports(document, imports)
            document.setText(document.text + "\n\n" + code)
        }
    }
}
