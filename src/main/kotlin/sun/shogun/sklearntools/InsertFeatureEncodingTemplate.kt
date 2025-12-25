package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class InsertFeatureEncodingTemplate : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val imports = listOf(
            "import pandas as pd",
            "from sklearn.preprocessing import LabelEncoder"
        )

        val code = """
    |# --- КОДИРОВАНИЕ КАТЕГОРИАЛЬНЫХ ПРИЗНАКОВ ---
    |
    |# Укажите список категориальных столбцов
    |cat_cols = ["cat_col1", "cat_col2", "cat_col3"]
    |
    |for col in cat_cols:
    |    print("\n==============================")
    |    print(f"Обработка столбца: {col}")
    |
    |    # --- 1. Проверки ---
    |    if col not in df_train.columns:
    |        print(f"Столбец {col} не найден, пропускаем")
    |        continue
    |
    |    # Приводим к строке и заполняем пропуски
    |    df_train[col] = df_train[col].astype(str).fillna("MISSING")
    |
    |    unique_vals = df_train[col].nunique()
    |    print(f"Уникальных значений: {unique_vals}")
    |
    |    print("Примеры значений:")
    |    print(df_train[col].value_counts().head())
    |
    |    # --- 2. Выбор способа кодирования ---
    |    # <= 10  → Label Encoding
    |    # <= 50  → One-Hot Encoding
    |    # > 50   → удалить
    |
    |    # ===== ВАРИАНТ A: Label Encoding =====
    |    if unique_vals <= 10:
    |        print("→ Используем Label Encoding")
    |        le = LabelEncoder()
    |        df_train[col] = le.fit_transform(df_train[col])
    |
    |    # ===== ВАРИАНТ B: One-Hot Encoding =====
    |    elif unique_vals <= 50:
    |        print("→ Используем One-Hot Encoding")
    |        df_train = pd.get_dummies(df_train, columns=[col], drop_first=True)
    |
    |    # ===== ВАРИАНТ C: слишком много категорий =====
    |    else:
    |        print("→ Слишком много уникальных значений, удаляем столбец")
    |        df_train.drop(columns=[col], inplace=True)
    |
    |# --- 3. Проверка после кодирования ---
    |print("\nИтоговые столбцы:")
    |print(df_train.columns)
    |
    |print("\nПримеры данных после кодирования:")
    |print(df_train.head())
""".trimMargin()

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            importsInserter.insertImports(document, imports)
            document.setText(document.text + "\n\n" + code)
        }
    }
}
