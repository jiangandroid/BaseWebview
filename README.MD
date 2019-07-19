# kit_webview
**webview-Version = "1.0.3"**
注解与注解处理器版本尽量保持一致
**processor-Version = "1.0.0"**
**annotations-Version = "1.0.0"**

## gradle 配置

### 基础组建依赖
```Java
api 'com.hipac.webview:webview:$webview-Version'
annotationProcessor "com.hipac.webview:processor:$processor-Version"
//如果未依赖webview但需要使用注解，则添加依赖
implementation 'com.hipac.webview:webview:$webview-Version'
```
### 第三方依赖库支持
```Java
   compileOnly 'com.google.code.gson:gson:2.8.5'
    compileOnly 'com.squareup.okhttp3:okhttp:3.12.1'
    compileOnly 'com.android.support:support-annotations:28.0.0'
    compileOnly 'android.arch.lifecycle:runtime:1.1.1'
    compileOnly 'pub.devrel:easypermissions:1.1.0'
    compileOnly 'com.android.support:appcompat-v7:28.0.0'
```
## 基本使用
### 1、YtWebview实例化
通过YtWebView.creatWebView(this, 1, SysConfig.LOGGING_ENABLED)获取对象实例，在parentView中addView(mYtWebView)
```Java
 mYtWebView=YtWebView.creatWebView(this,1,SysConfig.LOGGING_ENABLED);
webviewContainer.addView(mYtWebView, 1);
```
### 2、JsInterface绑定
 注：通过jsMethod进行分发，此乃约定，Android会将解析获得的 func进行首字母的小写，以便后续的分发。jsMethod参数结构
```{
    "func":"js方法名",
    "jsCallbackFunName":"h5回调方法名",
    "params":{//json格式具体参数}
}
```
(1)、使用 YtWebViewJsInterface，默认YtWebview实例化时已进行绑定，jsName默认为 MallJsBridge ，可在在Application中调用YtWebViewJsInterface.initJsName("MallJsBridge");进行修改。
YtWebViewJsInterface中提供 jsMethod 分发及 nativeFunNames获取所有支持js接口列表
(2)、继承YtWebViewJsInterface或者自定义实现YtJsInterface接口，mYtWebView.addJavascriptInterface(WebViewJsInterfaceTest.class, WebViewJsInterfaceTest.JS_BRIDGE);
若name重复，则会进行覆盖
### 3、YtWebviewLoadListener接口实现
YtWebView.YtWebviewLoadListener接口整合了WebViewClient 和 WebChromeClient 内方法的接口，可统一进行设置
### 4、是否需要支持H5调用系统选择图片
在YtWebview中已经添加了H5选择图片的支持设置，若有改需求，则需要在Activity载体的onActivityResult中实现
mYtWebView.onActivityResult(requestCode, resultCode, data);
### 5、YtJsCallbackHandler接口实现
实现YtJsCallbackHandler接口，其中handleJsFunctionSelf方法用于js方法分发时进行前置判断，若存在非全局js接口，则可在重写该方法时进行逻辑处理，返回true则不会进行后续全局js的分发；
### 6、使用注解定义js接口
示例：
```
public class YtBaseJsHandler {
		/**
			 * @param activity
			 * @param funcParams
			 * @param jsCallbackFunName
			 */
			@JsRegister
			public static void callPhone(Activity activity, String funcParams, String jsCallbackFunName) {

			}
	}
```
定义公开的静态方法，方法名需要和js接口的func一致，必须带有示例中三个参数，使用  @JsRegister 注解标记该静态方法

因为需要注解处理器的支持，所以定义js接口所在的module需要添加依赖
```
annotationProcessor "com.hipac.webview:processor:$processor-Version"
```

kit_webview库中已自动注册 callPhone 电话及 copyToPasteboard 复制剪切板

### 7、js接口注册初始化
在Application中调用register注册方法，传入定义具体js方法所在class
```
YtJsRegistor.register(YtBaseJsHandler.class);
```

### 其他
1、YtWebview与载体进行生命周期绑定，所以需要Fragement或者AppCompatActivity。已进行RESUME、RESUME、DESTROY状态的监听及部分操作。
2、YtWebview提供invokeJsCallback(final String script)方法调用JavaScript