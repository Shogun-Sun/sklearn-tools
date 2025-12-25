package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class InsertOutlierDetectionTemplate : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val imports = listOf(
            "import pandas as pd",
            "import matplotlib.pyplot as plt",
            "import numpy as np"
        )

        val code = """
            |# --- ОБНАРУЖЕНИЕ И ОБРАБОТКА ВЫБРОСОВ (метод IQR) ---
            |
            |col = "num_col1"  # название числового столбца для анализа
            |
            |# Приводим столбец к числовому типу:
            |# errors='coerce' -> все нечисловые значения станут NaN
            |df_train[col] = pd.to_numeric(df_train[col], errors='coerce')
            |
            |# Q1 — 25-й перцентиль (нижний квартиль)
            |# Q3 — 75-й перцентиль (верхний квартиль)
            |Q1 = df_train[col].quantile(0.25)
            |Q3 = df_train[col].quantile(0.75)
            |
            |# IQR (Interquartile Range) — межквартильный размах:
            |# показывает разброс "центральных" 50% данных
            |IQR = Q3 - Q1
            |
            |# Границы выбросов:
            |# 1.5 — классический коэффициент Тьюки
            |# все значения за этими границами считаются выбросами
            |lower_bound = Q1 - 1.5 * IQR
            |upper_bound = Q3 + 1.5 * IQR
            |
            |# --- Вариант 1: удаление выбросов ---
            |# Оставляем только те строки, где значение внутри границ
            |df_removed = df_train[~((df_train[col] < lower_bound) | (df_train[col] > upper_bound))].copy()
            |
            |# --- Вариант 2: замена выбросов медианой ---
            |# median_val = df_train[col].median()  # медиана — устойчива к выбросам
            |# df_median = df_train.copy()
            |# df_median[col] = df_median[col].apply(
            |#     lambda x: median_val if x < lower_bound or x > upper_bound else x
            |# )
            |
            |# --- Вариант 3: замена выбросов средним значением ---
            |# mean_val = df_train[col].mean()  # среднее чувствительно к выбросам
            |# df_mean = df_train.copy()
            |# df_mean[col] = df_mean[col].apply(
            |#     lambda x: mean_val if x < lower_bound or x > upper_bound else x
            |# )
            |
            |# --- Визуализация: Boxplot после удаления выбросов ---
            |plt.figure(figsize=(10,5))  # размер графика (ширина, высота)
            |
            |# vert=False -> горизонтальный ящик с усами (слева направо)
            |# dropna() -> убираем NaN, чтобы matplotlib не ломался
            |plt.boxplot([df_removed[col].dropna()],
            |            labels=["Удаление"],
            |            vert=False)
            |
            |plt.title(f"Boxplot для {col} после удаления выбросов")
            |plt.show()
        """.trimMargin()

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            importsInserter.insertImports(document, imports)
            document.setText(document.text + "\n\n" + code)
        }
    }
}
