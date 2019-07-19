package com.jiang.baseWebview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.jiang.baseWebview.KitWebviewUtil.exceptionToString;

/**
 * Created by xiyou on 2019/1/15
 * <p>
 * 虽然写的构造方法，但是实际使用时，只通过creatWebView方法创建YtWebView的对象
 */
public class YtWebView extends WebView implements LifecycleObserver {
    private YtJsCallbackHandler mYtJsCallbackHandler;
    private Activity mActivity;
    private boolean mLifeCycleEnable = true;//是否需要监听activity生命周期
    public volatile Map<String, YtJsInterface> mJavascriptInterfaces = new HashMap<>();
    //url是否开始加载
    private boolean onPageStart;

    //webview选择图片
    private final static int FILE_CHOOSER_RESULT_CODE = 1154;
    private final static int REQUEST_CAPTURE = 1155;
    private final static int RC_CAPTURE = 1156;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private Uri cameraPath;

    public YtWebView(Context context) {
        super(context);
    }

    /**
     * 整合了 WebViewClient 和 WebChromeClient 内方法的接口，统一进行设置
     */
    public interface YtWebviewLoadListener {
        //WebViewClient onReceivedSslError
        void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

        //WebViewClient shouldOverrideUrlLoading
        boolean shouldOverrideUrlLoading(WebView webView, String url);

        WebResourceResponse shouldInterceptRequest(WebView view, String url);

        WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest resourceRequest);

        void onPageFinished(WebView view, final String url);

        void onPageStarted(WebView webView, String url, Bitmap favor);

        //WebChromeClient
        void onReceivedTitle(WebView view, String title);

        void onProgressChanged(WebView view, int newProgress);
    }

    private YtWebviewLoadListener mYtWebviewLoadListener;

    public void setYtWebviewLoadListener(YtWebviewLoadListener listener) {
        mYtWebviewLoadListener = listener;
    }

    /**
     * 直接调用该方法获取新建对象，禁止使用构造方法创建
     *
     * @param activity
     * @return
     */
    public static YtWebView creatWebView(YtJsCallbackHandler activity) {
        return creatWebView(activity, 0, false);
    }

    public static YtWebView creatWebView(YtJsCallbackHandler activity, int topMargin) {
        return creatWebView(activity, topMargin, false);
    }

    public static YtWebView creatWebView(YtJsCallbackHandler activity, boolean debugEnable) {
        return creatWebView(activity, 0, debugEnable);
    }

    /**
     * @param handler
     * @param topMargin   为进度条展示预留的 topMargin
     * @param debugEnable 是否可debug
     * @return
     */
    public static YtWebView creatWebView(YtJsCallbackHandler handler, int topMargin, boolean debugEnable) {
        YtWebView webView = null;
        try {
            webView = new YtWebView(new MutableContextWrapper(handler.getActivity()));
        } catch (Exception e) {
            /**
             * 主要针对某些机子定制了自己的webview，干掉了系统的那一套
             * 目前遇到 小米 REDMI NOTE 5
             * 直接finish处理，不然一个个配置，。。。。。。。
             */
            handler.getActivity().finish();
            return null;
        }
        webView.init(handler);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.topMargin = topMargin;
        webView.setLayoutParams(layoutParams);
        webView.setBackgroundColor(Color.WHITE);

        webView.clearJavascriptInterface();
        webView.addJavascriptInterface(new YtWebViewJsInterface(webView.getYtJsCallbackHandler()), YtWebViewJsInterface.JS_BRIDGE);

        webView.setDebuggingEnabled(debugEnable);
        return webView;
    }

    //webview初始化设置
    public void init(YtJsCallbackHandler handler) {
        initYtJsCallbackHandler(handler);
        initWebSettings();
        initBaseYtJsInterface();
        initWebClient();
    }

    public void initYtJsCallbackHandler(YtJsCallbackHandler handler) {
        mYtJsCallbackHandler = handler;
        //添加生命周期绑定监听
        if (mYtJsCallbackHandler instanceof LifecycleOwner) {
            ((LifecycleOwner) mYtJsCallbackHandler).getLifecycle().addObserver(this);
        }
        mActivity = mYtJsCallbackHandler.getActivity();
    }

    public YtJsCallbackHandler getYtJsCallbackHandler() {
        return mYtJsCallbackHandler;
    }

    /**
     * 设置是否需要监听生命周期，默认true
     *
     * @param enable
     */
    public void setLifeCycleEnable(boolean enable) {
        mLifeCycleEnable = enable;
    }

    private void initWebSettings() {
        setBackgroundColor(Color.WHITE);
        // 构建WebView配置WebSetting
        final WebSettings webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);
        /*混合加载*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        /**缩放*/
        webSettings.setSupportZoom(true);//是否支持使用屏幕控件或手势进行缩放，默认是true，支持缩放。
        webSettings.setTextZoom(100);//加载页面字体变焦百分比，默认100，整型数。
        webSettings.setBuiltInZoomControls(true);//是否使用其内置的变焦机制，该机制集合屏幕缩放控件使用，默认是false
        webSettings.setDisplayZoomControls(false);//使用内置缩放机制时，是否展现在屏幕缩放控件上，默认true
        /*webview内容可超出屏幕*/
        webSettings.setLoadWithOverviewMode(true);//是否使用预览模式加载界面
        webSettings.setUseWideViewPort(true);//是否使用viewport，当该属性被设置为false时，
        // 加载页面的宽度总是适应WebView控件宽度；当被设置为true，当前页面包含viewport属性标签，
        // 在标签中指定宽度值生效，如果页面不包含viewport标签，无法提供一个宽度值，这个时候该方法将被使用。

        webSettings.setDomStorageEnabled(true);//设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM storage API
        webSettings.setDatabaseEnabled(true);//设置是否开启数据库存储API权限，默认false，未开启

        /* 防止其他应用隐藏执行脚本 */
        webSettings.setAllowFileAccess(false);//是否允许访问文件，默认允许访问。
        webSettings.setAllowContentAccess(false);//是否使用其内置的变焦机制，该机制结合屏幕缩放控件使用，默认是false，不使用内置变焦机制。
        webSettings.setAllowFileAccessFromFileURLs(false);//设置WebView运行中的一个文件方案被允许访问其他文件方案中的内容，默认值true
        webSettings.setAllowUniversalAccessFromFileURLs(false);//设置WebView运行中的脚本可以是否访问任何原始起点内容，默认true
        /* 缓存  */
        File cacheFile;
        cacheFile = new File(KitWebviewUtil.getExternalFilesDir(mActivity, null), "cache");
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(cacheFile.getPath());
        webSettings.setAppCacheMaxSize(1024 * 1024 * 50); //50M
        if (Build.VERSION.SDK_INT >= 19) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        /*不支持多窗口*/
        webSettings.setSupportMultipleWindows(false);

        //mWebView.setLayerType(View.LAYER_TYPE_NONE, null);

        // 配置WebView
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setWillNotCacheDrawing(false);
    }

    public void setDebuggingEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(enabled);
        }
    }

    /**
     * 基础JS接口注入初始化
     */
    public void initBaseYtJsInterface() {
        clearJavascriptInterface();
        addJavascriptInterface(YtWebViewJsInterface.class, YtWebViewJsInterface.JS_BRIDGE);
    }

    protected void initWebClient() {
        setWebViewClient(
                new WebViewClient() {
                    /***
                     * 防止中间人攻击，遇到错误及时cancel
                     * @param view
                     * @param handler
                     * @param error
                     */
                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        if (mYtWebviewLoadListener != null) {
                            mYtWebviewLoadListener.onReceivedSslError(view, handler, error);
                        } else {
                            super.onReceivedSslError(view, handler, error);
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                        HitTestResult hitTestResult = webView.getHitTestResult();
                        if (hitTestResult != null && hitTestResult.getType() == HitTestResult.PHONE_TYPE) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                            mActivity.startActivity(intent);
                            return true;
                        }
                        if (mYtWebviewLoadListener != null) {
                            //若接口 YtWebviewLoadListener 未作处理返回false，则调用 super 处理
                            return mYtWebviewLoadListener.shouldOverrideUrlLoading(webView, url) || super.shouldOverrideUrlLoading(webView, url);
                        }
                        return super.shouldOverrideUrlLoading(webView, url);
                    }

                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                        if (mYtWebviewLoadListener != null) {
                            //若接口 YtWebviewLoadListener 未作处理返回 null，则调用 super 处理
                            WebResourceResponse response = mYtWebviewLoadListener.shouldInterceptRequest(view, url);
                            if (response != null) {
                                return response;
                            }
                        }
                        return super.shouldInterceptRequest(view, url);
                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest resourceRequest) {
                        if (mYtWebviewLoadListener != null) {
                            //若接口 YtWebviewLoadListener 未作处理返回 null，则调用 super 处理
                            WebResourceResponse response = mYtWebviewLoadListener.shouldInterceptRequest(view, resourceRequest);
                            if (response != null) {
                                return response;
                            }
                        }
                        return super.shouldInterceptRequest(view, resourceRequest);
                    }

                    @Override
                    public void onPageFinished(WebView view, final String url) {
                        onPageStart = false;
                        if ("about:blank".equals(url)) {
                            mActivity.finish();
                            return;
                        }
                        view.getSettings().setBlockNetworkImage(false);
                        super.onPageFinished(view, url);
                        if (mYtWebviewLoadListener != null) {
                            mYtWebviewLoadListener.onPageFinished(view, url);
                        }
                        super.onPageFinished(view, url);
                    }

                    @Override
                    public void onPageStarted(WebView webView, String url, Bitmap favor) {
                        if (mYtWebviewLoadListener != null) {
                            mYtWebviewLoadListener.onPageStarted(webView, url, favor);
                        }
                        super.onPageStarted(webView, url, favor);
                        onPageStart = true;
                    }
                }
        );
        setWebChromeClient(
                new WebChromeClient() {
                    @Override
                    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                        callback.invoke(origin, true, false);
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        if (mYtWebviewLoadListener != null) {
                            mYtWebviewLoadListener.onReceivedTitle(view, title);
                        }
                        super.onReceivedTitle(view, title);
                    }

                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        if (mYtWebviewLoadListener != null) {
                            mYtWebviewLoadListener.onProgressChanged(view, newProgress);
                        }
                        super.onProgressChanged(view, newProgress);
                    }

                    //For Android API >= 11 (3.0 OS)
                    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                        uploadMessage = valueCallback;
                        if ("camera".equals(capture)) {
                            openCamera();
                        } else {
                            openImageChooserActivity();
                        }
                    }

                    //For Android API >= 21 (5.0 OS)
                    @Override
                    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                        boolean isCapture = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            isCapture = fileChooserParams.isCaptureEnabled();
                        }
                        uploadMessageAboveL = filePathCallback;
                        if (isCapture) {
                            openCamera();
                        } else {
                            openImageChooserActivity();
                        }
                        return true;
                    }
                }
        );
        setDownloadListener(
                new DownloadListener() {
                    @Override
                    public void onDownloadStart(String url, String userAgent, String contentDisposition, String
                            mimetype, long contentLength) {
                        if (!TextUtils.isEmpty(url)) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            mActivity.startActivity(intent);
                        }
                    }
                }
        );
    }

    /**
     * 通过类反射机制创建js对象，
     * 此js类必须实现 YtJsInterface 接口才会注入到webview中
     *
     * @param clazz
     * @param name
     */
    public void addJavascriptInterface(@NonNull Class clazz, String name) {
        try {
            Object object = clazz.newInstance();
            if (object instanceof YtJsInterface) {
                ((YtJsInterface) object).setYtJsCallbackHandler(mYtJsCallbackHandler);
                addJavascriptInterface(object, name);
            } else {
                object = null;
            }
        } catch (Exception e) {
            Log.e("addJavascriptInterface", exceptionToString(e));
        }
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void addJavascriptInterface(Object object, String name) {
        if (object instanceof YtJsInterface) {
            //若已存在相同name的js设置，则将原先js对象置空
            YtJsInterface value = mJavascriptInterfaces.get(name);
            if (value != null) {
                value.resetContext();
                value = null;
            }
            if (!name.equals(YtWebViewJsInterface.JS_BRIDGE)) {
                //当添加的 YtJsInterface 非基础设置 YtWebViewJsInterface.JS_BRIDGE 时，
                // 需要校验 YtWebViewJsInterface.JS_BRIDGE 的注入情况，若未注入则先注入基础设置
                YtWebViewJsInterface ytWebViewJsInterface = (YtWebViewJsInterface) mJavascriptInterfaces.get(YtWebViewJsInterface.JS_BRIDGE);
                if (ytWebViewJsInterface == null) {
                    ytWebViewJsInterface = new YtWebViewJsInterface(mYtJsCallbackHandler);
                    mJavascriptInterfaces.put(YtWebViewJsInterface.JS_BRIDGE, ytWebViewJsInterface);
                    super.addJavascriptInterface(ytWebViewJsInterface, YtWebViewJsInterface.JS_BRIDGE);
                }
            }
            mJavascriptInterfaces.put(name, (YtJsInterface) object);
            super.addJavascriptInterface(object, name);
        }
    }

    /**
     * 清除所有js接口注入
     */
    public void clearJavascriptInterface() {
        if (mJavascriptInterfaces != null && mJavascriptInterfaces.size() > 0) {
            Iterator<Map.Entry<String, YtJsInterface>> iterator = mJavascriptInterfaces.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<String, YtJsInterface> entry = iterator.next();
                Object jsInterface = mJavascriptInterfaces.get(entry.getKey());
                if (jsInterface != null && jsInterface instanceof YtJsInterface) {
                    //移除对应js时需要重置context，并置空
                    ((YtJsInterface) jsInterface).resetContext();
                    jsInterface = null;
                }
                iterator.remove();
                removeJavascriptInterface(entry.getKey());
            }
        }
    }

    @Override
    public void destroy() {
        mYtJsCallbackHandler = null;
        mActivity = null;
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
        clearJavascriptInterface();
        setVisibility(View.VISIBLE);
        clearFormData();
        clearSslPreferences();
        stopLoading();
        removeAllViews();
        setWebChromeClient(null);
        setWebViewClient(null);
        setDownloadListener(null);
        clearHistory();
        super.destroy();
    }

    /**
     * 调用H5 js 接口
     *
     * @param script
     */
    public void invokeJsCallback(final String script) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invokeJs(script);
        } else {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invokeJs(script);
                }
            });
        }
    }

    private void invokeJs(String script) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                evaluateJavascript(script, null);
            } else {
                loadUrl("javascript:" + script);
            }
        } catch (Exception e) {
            Log.e("YtWebView invokeJs", exceptionToString(e));
        }
    }

    public void setCookies(String host, String cookieHost, @NonNull List<Cookie> cookies) throws Throwable {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(this, true);
        }
        if (!TextUtils.isEmpty(cookieHost) && host.contains(cookieHost)) {
            //只针对传入的匹配的host传输cookie
            for (Cookie cookie : cookies) {
                cookieManager.setCookie(cookieHost, cookie.name() + "=" + cookie.value() +
                        "; domain=" + cookieHost + "; path=/");
            }
        }
    }

    public boolean isPageStartIng() {
        return onPageStart;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resume() {
        if (mLifeCycleEnable) {
            onResume();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause() {
        if (mLifeCycleEnable) {
            onPause();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (mLifeCycleEnable) {
            destroy();
        }
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        mActivity.startActivityForResult(Intent.createChooser(i, "选择图片"), FILE_CHOOSER_RESULT_CODE);
    }


    @AfterPermissionGranted(RC_CAPTURE)
    private void openCamera() {  //调用相机拍照
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //申请权限 WRITE_EXTERNAL_STORAGE
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            cameraPath = mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPath);
            mActivity.startActivityForResult(intent, REQUEST_CAPTURE);
        } else {
            EasyPermissions.requestPermissions(mActivity, "为保证App正常使用请授予相机权限",
                    RC_CAPTURE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * H5选择图片回调监听，需要在activity的onActivityResult方法中调用
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE || requestCode == REQUEST_CAPTURE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            if (cameraPath != null) {
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{cameraPath});
                } else if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(cameraPath);
                }
                cameraPath = null;
            } else {
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                if (uploadMessageAboveL != null) {
                    onActivityResultAboveL(requestCode, resultCode, data);
                } else if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(result);
                }
            }
        }
        //必须要加的，避免重复调起相册，没反应的问题
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }
        if (uploadMessageAboveL != null) {
            uploadMessageAboveL.onReceiveValue(null);
            uploadMessageAboveL = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }
}
