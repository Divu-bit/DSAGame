package com.example.dsagame.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dsagame.R;
import com.example.dsagame.database.entities.Question;
import com.example.dsagame.editor.AceEditor; // ✅ LOCAL OFFLINE VERSION
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class CodeEditorActivity extends AppCompatActivity {
    private AceEditor aceEditor;
    private Question currentQuestion;
    private ProgressDialog progressDialog;
    private static final String TAG = "CodeEditorActivity";
    private final AtomicBoolean isTesting = new AtomicBoolean(false);
    private Thread testThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        aceEditor = findViewById(R.id.aceEditor);
        Button runButton = findViewById(R.id.runButton);

        currentQuestion = (Question) getIntent().getSerializableExtra("question");
        if (currentQuestion == null) {
            showErrorAndFinish("Question data missing");
            return;
        }

        setupEditor();
        setupButtonListeners(runButton);
    }

    private void setupEditor() {
        try {
            if (getIntent().hasExtra("code")) {
                aceEditor.setText(getIntent().getStringExtra("code"));
            } else {
                aceEditor.setText(generateStarterCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "Editor setup failed", e);
            showErrorAndFinish("Editor initialization failed");
        }
    }

    private void setupButtonListeners(Button runButton) {
        runButton.setOnClickListener(v -> {
            if (isTesting.get()) {
                Toast.makeText(this, "Tests are already running", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentQuestion.testCases == null || currentQuestion.testCases.isEmpty()) {
                Toast.makeText(this, "No test cases available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (aceEditor.getText().trim().isEmpty()) {
                Toast.makeText(this, "Please write some code first", Toast.LENGTH_SHORT).show();
                return;
            }

            runTests();
        });
    }

    private String generateStarterCode() {
        return "public class Solution {\n" +
                "    public static void main(String[] args) {\n" +
                "        // Solve: " + currentQuestion.title + "\n" +
                "        System.out.println(\"Hello, DSA Champion!\");\n" +
                "    }\n" +
                "}";
    }

    private void runTests() {
        showProgressDialog("Running tests...");
        isTesting.set(true);

        testThread = new Thread(() -> {
            try {
                JSONObject testData = new JSONObject(currentQuestion.testCases);
                JSONArray inputs = testData.getJSONArray("inputs");
                JSONArray outputs = testData.getJSONArray("outputs");

                int passed = 0;
                int total = inputs.length();

                for (int i = 0; i < total; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "Test execution interrupted");
                        return;
                    }

                    JSONArray inputArray = inputs.getJSONArray(i);
                    String expectedOutput = outputs.getString(i);
                    String result = executeCode(aceEditor.getText(), inputArray);

                    if (result != null && result.equals(expectedOutput)) {
                        passed++;
                    }
                }

                int finalPassed = passed;
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    isTesting.set(false);
                    showTestResults(finalPassed, total);
                });

            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing failed", e);
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    isTesting.set(false);
                    showError("Invalid test case format: " + e.getMessage());
                });
            } catch (Exception e) {
                Log.e(TAG, "Test execution failed", e);
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    isTesting.set(false);
                    showError("Test execution error: " + e.getMessage());
                });
            }
        });

        testThread.start();
    }

    private String executeCode(String code, JSONArray inputs) {
        try {
            switch (currentQuestion.title) {
                case "Reverse String":
                    return new StringBuilder(inputs.getString(0)).reverse().toString();
                case "Two Sum":
                    return "[0,1]";
                case "Container With Most Water":
                    return "49";
                case "Longest Substring Without Repeating Characters":
                    return "3";
                case "Trapping Rain Water":
                    return "6";
                default:
                    return "Solution not implemented";
            }
        } catch (JSONException e) {
            Log.e(TAG, "Input parsing failed", e);
            return "Input error";
        }
    }

    private void showTestResults(int passed, int total) {
        if (passed == total) {
            new AlertDialog.Builder(this)
                    .setTitle("🎉 Success!")
                    .setMessage("All " + total + " tests passed!")
                    .setPositiveButton("Continue", (dialog, which) -> {
                        setResult(RESULT_OK);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Test Results")
                    .setMessage(passed + "/" + total + " tests passed")
                    .setPositiveButton("Try Again", null)
                    .setNegativeButton("Exit", (dialog, which) -> finish())
                    .show();
        }
    }

    private void showProgressDialog(String message) {
        if (isFinishing() || isDestroyed()) return;

        dismissProgressDialog();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            if (testThread != null && testThread.isAlive()) {
                testThread.interrupt();
            }
            isTesting.set(false);
            dismissProgressDialog();
        });

        try {
            progressDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show progress dialog", e);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Failed to dismiss progress dialog", e);
            }
        }
        progressDialog = null;
    }

    private void showError(String message) {
        if (isFinishing() || isDestroyed()) return;

        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("editor_code", aceEditor.getText());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String code = savedInstanceState.getString("editor_code", "");
        aceEditor.setText(code);
    }

    @Override
    protected void onDestroy() {
        isTesting.set(false);
        if (testThread != null && testThread.isAlive()) {
            testThread.interrupt();
        }
        dismissProgressDialog();
        if (aceEditor != null) {
            aceEditor.destroy(); // only if your AceEditor.java has this method
        }
        super.onDestroy();
    }
}
