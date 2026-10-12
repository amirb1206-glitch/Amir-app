package ir.amirb1206.daftarmelk;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String BASE = "https://amirb1206.ir";
    // Plugin's own consultant login page, redirecting into the consultant dashboard.
    private static final String START = BASE + "/ورود-پنل/?redirect_to=" + Uri.encode(BASE + "/پنل-مدیریت/");
    private WebView webView;
    private ProgressBar progress;
    private ValueCallback<Uri[]> uploadCallback;
    private static final int FILE_PICKER = 7412;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setStatusBarColor(Color.rgb(65, 99, 245));
        w.setNavigationBarColor(Color.rgb(248, 248, 252));
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(65,99,245)));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1, dp(3));
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        root.addView(progress, pp);
        setContentView(root);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setAllowFileAccess(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setBackgroundColor(Color.rgb(248,248,252));
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int p) {
                progress.setProgress(p); progress.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
            }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = callback;
                Intent i = params.createIntent();
                try { startActivityForResult(i, FILE_PICKER); }
                catch (Exception e) { uploadCallback = null; Toast.makeText(MainActivity.this, "انتخاب فایل ممکن نیست", Toast.LENGTH_SHORT).show(); return false; }
                return true;
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) { callback.invoke(origin, false, false); }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && (host.equals("amirb1206.ir") || host.equals("www.amirb1206.ir"))) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Metro-inspired styling: remove underlines, use blue accents and square property thumbnails.
                String css = "(function(){var s=document.getElementById('dm-android-css');if(!s){s=document.createElement('style');s.id='dm-android-css';document.head.appendChild(s);}s.innerHTML=`html,body{background:#f7f8fc!important}a,a:visited{text-decoration:none!important}button,input[type=submit],.button{border-radius:12px!important} .pam-action-card,.pam-home-panel-button,.pam-card,.pam-latest-property{border-radius:20px!important;box-shadow:0 8px 26px rgba(31,50,90,.07)!important}img,.pam-property-thumb,.pam-latest-property img{object-fit:cover} .pam-latest-property img,.pam-property-thumb{aspect-ratio:1/1!important} .pam-action-card--match{border-color:#4163f5!important} .pam-bottom-nav a{ text-decoration:none!important} `;})();";
                view.evaluateJavascript(css, null);
            }
        });
        if (savedInstanceState != null) webView.restoreState(savedInstanceState); else webView.loadUrl(START);
    }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER && uploadCallback != null) {
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            uploadCallback.onReceiveValue(results); uploadCallback = null;
        }
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); if (webView != null) webView.saveState(out); }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { if (webView != null) { webView.stopLoading(); webView.destroy(); } super.onDestroy(); }
}
