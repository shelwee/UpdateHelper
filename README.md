#UpdateHelper

##UpdateHelper是什么？
UpdateHelper是一个Android Library项目。
##UpdateHelper是干什么的？
这个项目是为了简化Android App的迭代升级开发，任何一个项目只要引入这个library便集成了在线检查新版本的功能以及下载APK功能。
##UpdateHelper要怎么使用？
**1.首先服务器端需要提供一个接口,返回json数据格式如下：**
```Java
    {   
        "appName": "TestUpdate", 
        "versionCode": "1", 
        "versionName": "1.0", 
        "apkUrl": "http://java.linuxlearn.net/shelwee/Finances.apk", 
        "changeLog":"1.修复xxx Bug;\n2.更新UI界面.", 
        "updateTips": "更新提示" 
    }
```   
**2.客户端操作如下：**
```Java
    UpdateHelper updateHelper = new UpdateHelper.Builder(this)
				.checkUrl("http://localhost/check.jsp")
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
###UpdateHelper需要哪些权限？
```java
    <uses-permission android:name="android.permission.INTERNET"/>
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
###UpdateHelper原理是什么？
UpdateHelper通过接口返回的数据versionCode，与当前app的versionCode匹配，如果版本号比当前app的versionCode大，则存在新版本，弹出更新提示对话框；否则提示当前版本是最新版。
Note:当遇到网络中断或阻塞等问题时，处理方式也为提示当前版本是最新版。
	
###截图
![](https://github.com/shelwee/ImageStorage/raw/master/UpdateHelper/UpdateDialog.png)

![](https://github.com/shelwee/ImageStorage/raw/master/UpdateHelper/Downloading.png)

![](https://github.com/shelwee/ImageStorage/raw/master/UpdateHelper/Downloaded.png)

###License
>Copyright 2014 ShelWee(http://www.shelwee.com)

>Licensed under the Apache License, Version 2.0 (the "License");
>you may not use this file except in compliance with the License.
>You may obtain a copy of the License at

>    http://www.apache.org/licenses/LICENSE-2.0

>Unless required by applicable law or agreed to in writing, software
>distributed under the License is distributed on an "AS IS" BASIS,
>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>See the License for the specific language governing permissions and
>limitations under the License.
