/*
 * SPDX-FileCopyrightText: 2014 Albert Vaca Cintora <albertvaka@gmail.com>
 * SPDX-FileCopyrightText: 2021 Ilmaz Gumerov <ilmaz1309@gmail.com>
 *
 * SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL
 */

package org.kde.kdeconnect.Plugins.ClibpoardPlugin;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.kde.kdeconnect.Helpers.ThreadHelper;
import org.kde.kdeconnect_tp.BuildConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import rikka.shizuku.Shizuku;

public class ClipboardListener {

    public interface ClipboardObserver {
        void clipboardChanged(String content);
    }

    private final HashSet<ClipboardObserver> observers = new HashSet<>();

    private final Context context;
    private String currentContent;
    private long updateTimestamp;

    private ClipboardManager cm = null;

    private static ClipboardListener _instance = null;

    public static ClipboardListener instance(Context context) {
        if (_instance == null) {
            _instance = new ClipboardListener(context);
            // FIXME: The _instance we return won't be completely initialized yet since initialization happens on a new thread (why?)
        }
        return _instance;
    }

    public void registerObserver(ClipboardObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ClipboardObserver observer) {
        observers.remove(observer);
    }

    private ClipboardListener(final Context ctx) {
        context = ctx;

        new Handler(Looper.getMainLooper()).post(() -> {
            cm = ContextCompat.getSystemService(context, ClipboardManager.class);
            cm.addPrimaryClipChangedListener(this::onClipboardChanged);
        });

        // 优先尝试使用 Shizuku
        if (isShizukuAvailableAndAuthorized()) {
            startShizukuLogcatListener();
        } else if (Shizuku.pingBinder()) {
            requestShizukuPermission();
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_LOGS) == PackageManager.PERMISSION_GRANTED) {
            // 回退到原有的 READ_LOGS 方案
            startLogcatListener();
        } else {
            // 提示用户需要 Shizuku 或 ADB 权限
            Toast.makeText(context, "剪贴板同步需要 Shizuku 或 ADB 权限", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isShizukuAvailableAndAuthorized() {
        try {
            return Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }

    private void requestShizukuPermission() {
        Shizuku.OnRequestPermissionResultListener listener = (requestCode, grantResult) -> {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                startShizukuLogcatListener();
            } else {
                Toast.makeText(context, "Shizuku 权限被拒绝", Toast.LENGTH_LONG).show();
            }
        };
        Shizuku.addRequestPermissionResultListener(listener);
        try {
            Shizuku.requestPermission(0);
        } catch (Exception e) {
            Toast.makeText(context, "Shizuku 服务不可用", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("deprecation")
    private void startShizukuLogcatListener() {
        ThreadHelper.execute(() -> {
            try {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                String logcatFilter = Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM
                        ? "E ClipboardService"
                        : "ClipboardService:E";
                Process process = Shizuku.newProcess(new String[]{"logcat", "-T", timeStamp, logcatFilter, "*:S"}, null, null);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(BuildConfig.APPLICATION_ID)) {
                        context.startActivity(ClipboardFloatingActivity.getIntent(context, false));
                    }
                }
                bufferedReader.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

     private void startLogcatListener() {
        ThreadHelper.execute(() -> {
            try {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                String logcatFilter = Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM
                        ? "E ClipboardService"
                        : "ClipboardService:E";
                Process process = Runtime.getRuntime().exec(new String[]{"logcat", "-T", timeStamp, logcatFilter, "*:S"});
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(BuildConfig.APPLICATION_ID)) {
                        context.startActivity(ClipboardFloatingActivity.getIntent(context, false));
                    }
                }
                bufferedReader.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    //原来的资源占用有点高，试试定期重启服务能不能改善一点 ps:二次备注实测下面那个东西效果并不明显

    /*private void startLogcatListener() {
        ThreadHelper.execute(() -> {
            while (true) { // 持续运行，定期重启
                try {
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                    String logcatFilter = Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM
                            ? "E ClipboardService"
                            : "ClipboardService:E";
                    Process process = Runtime.getRuntime().exec(new String[]{"logcat", "-T", timeStamp, logcatFilter, "*:S"});
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    long startTime = System.currentTimeMillis();
                    long maxRuntime = 300000; // 5分钟持续阈值

                    while ((line = bufferedReader.readLine()) != null) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime > maxRuntime) {
                            break; // 退出当前循环
                        }
                        if (line.contains(BuildConfig.APPLICATION_ID)) {
                            context.startActivity(ClipboardFloatingActivity.getIntent(context, false));
                        }
                    }
                    bufferedReader.close();
                    process.destroy(); // 资源释放
                    Thread.sleep(10000); // 休眠10秒后重启
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000); // 异常时间
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
    }*/

    public void onClipboardChanged() {
        try {
            ClipData.Item item = cm.getPrimaryClip().getItemAt(0);
            String content = item.coerceToText(context).toString();

            if (content.equals(currentContent)) {
                return;
            }
            updateTimestamp = System.currentTimeMillis();
            currentContent = content;

            for (ClipboardObserver observer : observers) {
                observer.clipboardChanged(content);
            }
        } catch (Exception e) {
            // Probably clipboard was not text
        }
    }

    public String getCurrentContent() {
        return currentContent;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    @SuppressWarnings("deprecation")
    public void setText(String text) {
        if (cm != null) {
            updateTimestamp = System.currentTimeMillis();
            currentContent = text;
            cm.setText(text);
        }
    }
}