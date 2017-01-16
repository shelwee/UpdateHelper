# UpdateHelper
[![Travis](https://img.shields.io/badge/build-passing-blue.svg)](h) [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)]() [![Hex.pm](https://img.shields.io/badge/Release-v1.0-blue.svg)]()


## UpdateHelper 是什么？
UpdateHelper 是一个只需一行代码即可实现APP在线升级的 Android Library。
## UpdateHelper 是干什么的？
这个项目是为了简化 Android APP 的迭代升级开发，任何一个项目只要引入这个 library 便集成了在线检查新版本的功能以及下载 APK 功能。
## UpdateHelper 要怎么使用？
**1.首先服务器端需要提供一个接口,返回 JSON 数据格式如下：**
```Java
    {   
        "appName": "TestUpdate", 
        "versionCode": "1", 
        "versionName": "1.0", 
        "apkUrl": "http://www.shelwee.com/404.apk", 
        "changeLog":"1.修复xxx Bug;\n2.更新UI界面.", 
        "updateTips": "更新提示" ,
        "forceUpgrade": true    //当有新版本时，APP 将会自动下载安装包
    }
```   
**2.客户端操作如下：**
```Java
    UpdateHelper updateHelper = new UpdateHelper.Builder(this)
				.checkUrl("http://www.shelwee.com/check.jsp")
				.isAutoInstall(false) //设置为false需在下载完手动点击安装;默认值为true，下载后自动安装。
		        .build();
	updateHelper.check(); 
	/**
	或者使用下面的方式，针对复杂需求的可重写回调方法
	updateHelper.check(new OnUpdateListener() {
			
			@Override
			public void onStartDownload() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartCheck() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinshDownload() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinishCheck(UpdateInfo info) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDownloading(int progress) {
				// TODO Auto-generated method stub
				
			}
		});
    */
```
### UpdateHelper 需要哪些权限？
```java
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
### UpdateHelper 原理是什么？
UpdateHelper 通过接口返回的数据 versionCode，与当前 APP 的 versionCode 匹配，如果版本号比当前 APP 的 versionCode 大，则存在新版本，弹出更新提示对话框；否则提示当前版本是最新版。

**注：当遇到网络中断或阻塞等问题时，处理方式也为提示当前版本是最新版。**
	
### UpdateHelper 运行截图
![](https://github.com/shelwee/ImageStorage/raw/master/UpdateHelper/UpdateDialog.png)

![](https://github.com/shelwee/ImageStorage/raw/master/UpdateHelper/Downloading.png)

![](https://github.com/shelwee/ImageStorage/raw/master/UpdateHelper/Downloaded.png)

### Change Logs
---
Version 1.0 - Released date: 1-8-2017
```
- 从 ADT 转换成 Android Studio 项目
- 优化部分代码实现逻辑
- 网络部分全部改为 HttpURLConnection 实现
- 修复下载过程与通知栏进度条卡顿情况
- 提示文字全部提取到 strings.xml，方便国际化操作
- 新增强制升级功能，通过后台接口 JSON 返回 forceUpgrade：true/false
```
Version 0.2 - Released date: 3-26-2016
```
- 新增校验文件是否已下载
```

Version 0.1 - Released date: 5-8-2014
```
- 发布UpdateHelper第一个版本

```
