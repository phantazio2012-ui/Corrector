package ci.cgsi.correcteur;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Mise à jour automatique : télécharge l'APK de la dernière Release puis lance l'installation
 * via PackageInstaller (sans passer par le navigateur). Android 12+ : installation sans
 * confirmation quand le système l'autorise ; sinon une seule confirmation « Mettre à jour ».
 */
@CapacitorPlugin(name = "ApkUpdater")
public class ApkUpdaterPlugin extends Plugin {

    private static final String ACTION = "ci.cgsi.correcteur.INSTALL_RESULT";
    private BroadcastReceiver receiver;

    @PluginMethod
    public void canInstall(PluginCall call) {
        boolean allowed = Build.VERSION.SDK_INT < 26
                || getContext().getPackageManager().canRequestPackageInstalls();
        JSObject r = new JSObject();
        r.put("allowed", allowed);
        call.resolve(r);
    }

    @PluginMethod
    public void openInstallSettings(PluginCall call) {
        Intent i = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + getContext().getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
        call.resolve();
    }

    @PluginMethod
    public void downloadAndInstall(final PluginCall call) {
        final String url = call.getString("url");
        if (url == null) {
            call.reject("url manquante");
            return;
        }
        new Thread(() -> {
            try {
                File apk = new File(getContext().getCacheDir(), "update.apk");
                download(url, apk);
                install(apk);
                call.resolve();
            } catch (Exception e) {
                call.reject(String.valueOf(e.getMessage()));
            }
        }).start();
    }

    private void download(String address, File dest) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        c.setConnectTimeout(20000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "CorrecteurCopies");
        int code = c.getResponseCode();
        if (code != 200) throw new Exception("Téléchargement impossible (HTTP " + code + ")");
        long total = c.getContentLengthLong();
        long got = 0;
        int lastPct = -1;
        try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                got += n;
                if (total > 0) {
                    int pct = (int) (got * 100 / total);
                    if (pct != lastPct) {
                        lastPct = pct;
                        JSObject o = new JSObject();
                        o.put("percent", pct);
                        notifyListeners("progress", o);
                    }
                }
            }
        } finally {
            c.disconnect();
        }
        if (dest.length() < 1024 * 1024) throw new Exception("Fichier de mise à jour invalide");
    }

    private void install(File apk) throws Exception {
        final Context ctx = getContext();
        registerReceiverOnce(ctx);

        PackageInstaller installer = ctx.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (Build.VERSION.SDK_INT >= 31) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }
        int id = installer.createSession(params);
        PackageInstaller.Session session = installer.openSession(id);
        try (InputStream in = new FileInputStream(apk);
             OutputStream out = session.openWrite("update", 0, apk.length())) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            session.fsync(out);
        }
        Intent cb = new Intent(ACTION).setPackage(ctx.getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 31) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(ctx, id, cb, flags);
        session.commit(pi.getIntentSender());
        session.close();
    }

    private void registerReceiverOnce(Context ctx) {
        if (receiver != null) return;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle ex = intent.getExtras();
                if (ex == null) return;
                int status = ex.getInt(PackageInstaller.EXTRA_STATUS);
                if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                    if (confirm != null) {
                        confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(confirm);
                    }
                } else if (status != PackageInstaller.STATUS_SUCCESS) {
                    JSObject o = new JSObject();
                    o.put("status", status);
                    o.put("message", ex.getString(PackageInstaller.EXTRA_STATUS_MESSAGE));
                    notifyListeners("error", o);
                }
            }
        };
        ContextCompat.registerReceiver(ctx, receiver, new IntentFilter(ACTION),
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }
}
