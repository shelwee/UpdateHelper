package com.shelwee.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.shelwee.update.listener.OnUpdateListener;
import com.shelwee.update.pojo.UpdateInfo;
import com.shelwee.update.utils.HttpRequest;
import com.shelwee.update.utils.JSONHandler;
import com.shelwee.update.utils.NetWorkUtils;
import com.shelwee.update.utils.URLUtils;

/**
 * Created by ShelWee on 14-5-8.<br/>
 * Usage:
 * 
 * <pre>
 * UpdateManager updateManager = new UpdateManager.Builder(this)
 * 		.checkUrl(&quot;http://localhost/examples/version.jsp&quot;)
 * 		.isAutoInstall(false)
 * 		.build();
 * updateManager.check();
 * </pre>
 * 
 * @author ShelWee(<a href="http://www.shelwee.com">http://www.shelwee.com</a>)
 * @version 0.1 beta
 */
public class UpdateHelper {

	private Context mContext;
	private String checkUrl;
	private boolean isAutoInstall;
	private boolean isHintVersion;
	private OnUpdateListener updateListener;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder ntfBuilder;

	private static final int UPDATE_NOTIFICATION_PROGRESS = 0x1;
	private static final int COMPLETE_DOWNLOAD_APK = 0x2;
	private static final int DOWNLOAD_NOTIFICATION_ID = 0x3;
	private static final String PATH = Environment
			.getExternalStorageDirectory().getPath();
	private static final String SUFFIX = ".apk";
	private static final String APK_PATH = "APK_PATH";
	private static final String APP_NAME = "APP_NAME";
	private SharedPreferences preferences_update;

	private HashMap<String, String> cache = new HashMap<String, String>();

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_NOTIFICATION_PROGRESS:
				showDownloadNotificationUI((UpdateInfo) msg.obj, msg.arg1);
				break;
			case COMPLETE_DOWNLOAD_APK:
				if (UpdateHelper.this.isAutoInstall) {
					installApk(Uri.parse("file://" + cache.get(APK_PATH)));
				} else {
					if (ntfBuilder == null) {
						ntfBuilder = new NotificationCompat.Builder(mContext);
					}
					ntfBuilder.setSmallIcon(mContext.getApplicationInfo().icon)
							.setContentTitle(cache.get(APP_NAME))
							.setContentText("下载完成，点击安装").setTicker("任务下载完成");
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.parse("file://" + cache.get(APK_PATH)),
							"application/vnd.android.package-archive");
					PendingIntent pendingIntent = PendingIntent.getActivity(
							mContext, 0, intent, 0);
					ntfBuilder.setContentIntent(pendingIntent);
					if (notificationManager == null) {
						notificationManager = (NotificationManager) mContext
								.getSystemService(Context.NOTIFICATION_SERVICE);
					}
					notificationManager.notify(DOWNLOAD_NOTIFICATION_ID,
							ntfBuilder.build());
				}
				break;
			}
		}

	};

	private UpdateHelper(Builder builder) {
		this.mContext = builder.context;
		this.checkUrl = builder.checkUrl;
		this.isAutoInstall = builder.isAutoInstall;
		this.isHintVersion = builder.isHintNewVersion;
		preferences_update = mContext.getSharedPreferences("Updater",
				Context.MODE_PRIVATE);
	}

	/**
	 * 检查app是否有新版本，check之前先Builer所需参数
	 */
	public void check() {
		check(null);
	}

	public void check(OnUpdateListener listener) {
		if (listener != null) {
			this.updateListener = listener;
		}
		if (mContext == null) {
			Log.e("NullPointerException", "The context must not be null.");
			return;
		}
		AsyncCheck asyncCheck = new AsyncCheck();
		asyncCheck.execute(checkUrl);
	}
	
	/**
	 * 2014-10-27新增流量提示框，当网络为数据流量方式时，下载就会弹出此对话框提示
	 * @param updateInfo
	 */
	private void showNetDialog(final UpdateInfo updateInfo){
		AlertDialog.Builder netBuilder = new AlertDialog.Builder(mContext);
		netBuilder.setTitle("下载提示");
		netBuilder.setMessage("您在目前的网络环境下继续下载将可能会消耗手机流量，请确认是否继续下载？");
		netBuilder.setNegativeButton("取消下载",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		netBuilder.setPositiveButton("继续下载",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						AsyncDownLoad asyncDownLoad = new AsyncDownLoad();
						asyncDownLoad.execute(updateInfo);
					}
				});
		AlertDialog netDialog = netBuilder.create();
		netDialog.setCanceledOnTouchOutside(false);
		netDialog.show();
	}

	/**
	 * 弹出提示更新窗口
	 * 
	 * @param updateInfo
	 */
	private void showUpdateUI(final UpdateInfo updateInfo) {
		AlertDialog.Builder upDialogBuilder = new AlertDialog.Builder(mContext);
		upDialogBuilder.setTitle(updateInfo.getUpdateTips());
		upDialogBuilder.setMessage(updateInfo.getChangeLog());
		upDialogBuilder.setNegativeButton("下次再说",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		upDialogBuilder.setPositiveButton("下载",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						NetWorkUtils netWorkUtils = new NetWorkUtils(mContext);
						int type = netWorkUtils.getNetType();
						if (type != 1) {
							showNetDialog(updateInfo);
						}else {
							AsyncDownLoad asyncDownLoad = new AsyncDownLoad();
							asyncDownLoad.execute(updateInfo);
						}
					}
				});
		AlertDialog updateDialog = upDialogBuilder.create();
		updateDialog.setCanceledOnTouchOutside(false);
		updateDialog.show();
	}

	/**
	 * 通知栏弹出下载提示进度
	 * 
	 * @param updateInfo
	 * @param progress
	 */
	private void showDownloadNotificationUI(UpdateInfo updateInfo,
			final int progress) {
		if (mContext != null) {
			String contentText = new StringBuffer().append(progress)
					.append("%").toString();
			PendingIntent contentIntent = PendingIntent.getActivity(mContext,
					0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
			if (notificationManager == null) {
				notificationManager = (NotificationManager) mContext
						.getSystemService(Context.NOTIFICATION_SERVICE);
			}
			if (ntfBuilder == null) {
				ntfBuilder = new NotificationCompat.Builder(mContext)
						.setSmallIcon(mContext.getApplicationInfo().icon)
						.setTicker("开始下载...")
						.setContentTitle(updateInfo.getAppName())
						.setContentIntent(contentIntent);
			}
			ntfBuilder.setContentText(contentText);
			ntfBuilder.setProgress(100, progress, false);
			notificationManager.notify(DOWNLOAD_NOTIFICATION_ID,
					ntfBuilder.build());
		}
	}

	/**
	 * 获取当前app版本
	 * 
	 * @return
	 * @throws android.content.pm.PackageManager.NameNotFoundException
	 */
	private PackageInfo getPackageInfo() {
		PackageInfo pinfo = null;
		if (mContext != null) {
			try {
				pinfo = mContext.getPackageManager().getPackageInfo(
						mContext.getPackageName(), 0);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return pinfo;
	}

	/**
	 * 检查更新任务
	 */
	private class AsyncCheck extends AsyncTask<String, Integer, UpdateInfo> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (UpdateHelper.this.updateListener != null) {
				UpdateHelper.this.updateListener.onStartCheck();
			}
		}

		@Override
		protected UpdateInfo doInBackground(String... params) {
			UpdateInfo updateInfo = null;
			if (params.length == 0) {
				Log.e("NullPointerException",
						" Url parameter must not be null.");
				return null;
			}
			String url = params[0];
			if (!URLUtils.isNetworkUrl(url)) {
				Log.e("IllegalArgumentException", "The URL is invalid.");
				return null;
			}
			try {
				updateInfo = JSONHandler.toUpdateInfo(HttpRequest.get(url));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return updateInfo;
		}

		@Override
		protected void onPostExecute(UpdateInfo updateInfo) {
			super.onPostExecute(updateInfo);
			SharedPreferences.Editor editor = preferences_update.edit();
			if (mContext != null && updateInfo != null) {
				if (Integer.parseInt(updateInfo.getVersionCode()) > getPackageInfo().versionCode) {
					showUpdateUI(updateInfo);
					editor.putBoolean("hasNewVersion", true);
					editor.putString("lastestVersionCode",
							updateInfo.getVersionCode());
					editor.putString("lastestVersionName",
							updateInfo.getVersionName());
				} else {
					if (isHintVersion) {
						Toast.makeText(mContext, "当前已是最新版", Toast.LENGTH_LONG).show();
					}
					editor.putBoolean("hasNewVersion", false);
				}
			} else {
				if (isHintVersion) {
					Toast.makeText(mContext, "当前已是最新版", Toast.LENGTH_LONG).show();
				}
			}
			editor.putString("currentVersionCode", getPackageInfo().versionCode
					+ "");
			editor.putString("currentVersionName", getPackageInfo().versionName);
			editor.commit();
			if (UpdateHelper.this.updateListener != null) {
				UpdateHelper.this.updateListener.onFinishCheck(updateInfo);
			}
		}
	}

	/**
	 * 异步下载app任务
	 */
	private class AsyncDownLoad extends AsyncTask<UpdateInfo, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(UpdateInfo... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(params[0].getApkUrl());
			try {
				HttpResponse response = httpClient.execute(httpGet);
				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					Log.e("IllegalArgumentException", "APK路径出错，请检查服务端配置接口。");
					return false;
				} else {
					HttpEntity entity = response.getEntity();
					InputStream inputStream = entity.getContent();
					long total = entity.getContentLength();
					String apkName = params[0].getAppName()
							+ params[0].getVersionName() + SUFFIX;
					cache.put(APP_NAME, params[0].getAppName());
					cache.put(APK_PATH,
							PATH + File.separator + params[0].getAppName()
									+ File.separator + apkName);
					File savePath = new File(PATH + File.separator
							+ params[0].getAppName());
					if (!savePath.exists())
						savePath.mkdirs();
					File apkFile = new File(savePath, apkName);
					if (apkFile.exists()) {
						return true;
					}
					FileOutputStream fos = new FileOutputStream(apkFile);
					byte[] buf = new byte[1024];
					int count = 0;
					int length = -1;
					while ((length = inputStream.read(buf)) != -1) {
						fos.write(buf, 0, length);
						count += length;
						int progress = (int) ((count / (float) total) * 100);
						if (progress % 5 == 0) {
							handler.obtainMessage(UPDATE_NOTIFICATION_PROGRESS,
									progress, -1, params[0]).sendToTarget();
						}
						if (UpdateHelper.this.updateListener != null) {
							UpdateHelper.this.updateListener
									.onDownloading(progress);
						}
					}
					inputStream.close();
					fos.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean flag) {
			if (flag) {
				handler.obtainMessage(COMPLETE_DOWNLOAD_APK).sendToTarget();
				if (UpdateHelper.this.updateListener != null) {
					UpdateHelper.this.updateListener.onFinshDownload();
				}
			} else {
				Log.e("Error", "下载失败。");
			}
		}
	}

	public static class Builder {
		private Context context;
		private String checkUrl;
		private boolean isAutoInstall = true;
		private boolean isHintNewVersion = true;

		public Builder(Context ctx) {
			this.context = ctx;
		}

		/**
		 * 检查是否有新版本App的URL接口路径
		 * 
		 * @param checkUrl
		 * @return
		 */
		public Builder checkUrl(String checkUrl) {
			this.checkUrl = checkUrl;
			return this;
		}

		/**
		 * 是否需要自动安装, 不设置默认自动安装
		 * 
		 * @param isAuto
		 *            true下载完成后自动安装，false下载完成后需在通知栏手动点击安装
		 * @return
		 */
		public Builder isAutoInstall(boolean isAuto) {
			this.isAutoInstall = isAuto;
			return this;
		}
		
		/**
		 * 当没有新版本时，是否Toast提示
		 * @param isHint 
		 * @return true提示，false不提示
		 */
		public Builder isHintNewVersion(boolean isHint){
			this.isHintNewVersion = isHint;
			return this;
		}

		/**
		 * 构造UpdateManager对象
		 * 
		 * @return
		 */
		public UpdateHelper build() {
			return new UpdateHelper(this);
		}
	}

	private void installApk(Uri data) {
		if (mContext != null) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(data, "application/vnd.android.package-archive");
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(i);
			if (notificationManager != null) {
				notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);
			}
		} else {
			Log.e("NullPointerException", "The context must not be null.");
		}

	}

}