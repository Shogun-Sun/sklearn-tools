package sun.shogun.sklearntools

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class InsertModelTrainingTemplate : AnAction() {

    private val importsInserter = InsertImportsBeforeCode()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val imports = listOf(
            "import pandas as pd",
            "from sklearn.model_selection import train_test_split",
            "from sklearn.metrics import accuracy_score, classification_report, mean_squared_error, mean_absolute_error, r2_score",
            "from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor",
            "from sklearn.linear_model import LogisticRegression, LinearRegression",
            "from xgboost import XGBClassifier, XGBRegressor"
        )

        val code = """
            |# --- ОБУЧЕНИЕ МОДЕЛЕЙ ---
            |
            |# Передача данных
            |# X_train, X_test, y_train, y_test должны быть определены
            |
            |# Укажите тип задачи: "classification" или "regression"
            |task_type = "classification"
            |
            |# Список моделей для обучения
            |models = []
            |
            |if task_type == "classification":
            |    models = [
            |        ("RandomForest", RandomForestClassifier(random_state=42)),
            |        ("LogisticRegression", LogisticRegression(max_iter=1000)),
            |        ("XGBoost", XGBClassifier(use_label_encoder=False, eval_metric='logloss'))
            |    ]
            |elif task_type == "regression":
            |    models = [
            |        ("RandomForest", RandomForestRegressor(random_state=42)),
            |        ("LinearRegression", LinearRegression()),
            |        ("XGBoost", XGBRegressor())
            |    ]
            |
            |# Обучение и оценка моделей
            |for name, model in models:
            |    print("\\n==============================")
            |    print(f"Обучение модели: {name}")
            |    model.fit(X_train, y_train)
            |
            |    y_pred = model.predict(X_test)
            |
            |    if task_type == "classification":
            |        acc = accuracy_score(y_test, y_pred)
            |        print(f"Accuracy: {acc:.4f}")
            |        print("Classification report:")
            |        print(classification_report(y_test, y_pred))
            |    else:
            |        mse = mean_squared_error(y_test, y_pred)
            |        mae = mean_absolute_error(y_test, y_pred)
            |        r2 = r2_score(y_test, y_pred)
            |        print(f"MSE: {mse:.4f}, MAE: {mae:.4f}, R2: {r2:.4f}")
        """.trimMargin()

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            importsInserter.insertImports(document, imports)
            document.setText(document.text + "\n\n" + code)
        }
    }
}
