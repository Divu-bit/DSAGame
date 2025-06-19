package com.example.dsagame.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class AceEditor extends WebView {

    private String currentText = "";

    public AceEditor(Context context) {
        super(context);
        init();
    }

    public AceEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        setWebChromeClient(new WebChromeClient());
        addJavascriptInterface(this, "Android");
        loadUrl("file:///android_asset/ace/ace.html");
    }

    public void setText(String code) {
        currentText = code;
        post(() -> evaluateJavascript("editor.setValue(`" + escape(code) + "`); editor.clearSelection();", null));
    }

    public String getText() {
        return currentText;
    }

    @JavascriptInterface
    public void updateText(String code) {
        this.currentText = code;
    }

    private String escape(String str) {
        return str.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$");
    }
}
