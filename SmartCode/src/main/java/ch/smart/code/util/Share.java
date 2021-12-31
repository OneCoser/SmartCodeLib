package ch.smart.code.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles share intent.
 */
public class Share {

    private final Context context;
    private Activity activity;

    private final String providerAuthority;

    /**
     * Constructs a Share object. The {@code context} and {@code activity} are used to start the share
     * intent. The {@code activity} might be null when constructing the {@link Share} object and set
     * to non-null when an activity is available using {@link #setActivity(Activity)}.
     */
    public Share(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        this.providerAuthority = getContext().getPackageName() + ".share_provider";
    }

    /**
     * Sets the activity when an activity is available. When the activity becomes unavailable, use
     * this method to set it to null.
     */
    void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void share(String text, String subject) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Non-empty text expected");
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.setType("text/plain");
        Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);
        startActivity(chooserIntent);
    }

    public void shareFiles(Map<String, String> paths, List<String> mimeTypes, String text, String subject)
            throws IOException {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("Non-empty path expected");
        }

        clearShareCacheFolder();
        ArrayList<Uri> fileUris = getUrisForPaths(paths);

        Intent shareIntent = new Intent();
        if (fileUris.isEmpty()) {
            share(text, subject);
            return;
        } else if (fileUris.size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUris.get(0));
            shareIntent.setType(
                    !mimeTypes.isEmpty() && mimeTypes.get(0) != null ? mimeTypes.get(0) : "*/*");
        } else {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
            shareIntent.setType(reduceMimeTypes(mimeTypes));
        }
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        if (subject != null) {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);

        List<ResolveInfo> resInfoList =
                getContext()
                        .getPackageManager()
                        .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            for (Uri fileUri : fileUris) {
                getContext()
                        .grantUriPermission(
                                packageName,
                                fileUri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        startActivity(chooserIntent);
    }

    private void startActivity(Intent intent) {
        if (activity != null) {
            activity.startActivity(intent);
        } else if (context != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            throw new IllegalStateException("Both context and activity are null");
        }
    }

    private ArrayList<Uri> getUrisForPaths(Map<String, String> paths) throws IOException {
        ArrayList<Uri> uris = new ArrayList<>(paths.size());

        for (String path : paths.keySet()) {
            File file = new File(path);
            if (fileIsInShareCache(file)) {
                // If file was saved in '.../caches/share_plus' it will have been erased by 'clearShareCacheFolder();'
                throw new IOException(
                        "File to share not allowed to be located in '"
                                + getShareCacheFolder().getCanonicalPath()
                                + "'");
            }
            file = copyToShareCacheFolder(file, paths.get(path));

            uris.add(FileProvider.getUriForFile(getContext(), providerAuthority, file));
        }

        return uris;
    }

    private String reduceMimeTypes(List<String> mimeTypes) {
        if (mimeTypes.size() > 1) {
            String reducedMimeType = mimeTypes.get(0);
            for (int i = 1; i < mimeTypes.size(); i++) {
                String mimeType = mimeTypes.get(i);
                if (!reducedMimeType.equals(mimeType)) {
                    if (getMimeTypeBase(mimeType).equals(getMimeTypeBase(reducedMimeType))) {
                        reducedMimeType = getMimeTypeBase(mimeType) + "/*";
                    } else {
                        reducedMimeType = "*/*";
                        break;
                    }
                }
            }
            return reducedMimeType;
        } else if (mimeTypes.size() == 1) {
            return mimeTypes.get(0);
        } else {
            return "*/*";
        }
    }

    @NonNull
    private String getMimeTypeBase(String mimeType) {
        if (mimeType == null || !mimeType.contains("/")) {
            return "*";
        }

        return mimeType.substring(0, mimeType.indexOf("/"));
    }

    private boolean fileIsInShareCache(File file) {
        try {
            String filePath = file.getCanonicalPath();
            return filePath.startsWith(getShareCacheFolder().getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clearShareCacheFolder() {
        File folder = getShareCacheFolder();
        final File[] files = folder.listFiles();
        if (folder.exists() && files != null) {
            for (File file : files) {
                file.delete();
            }
            folder.delete();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File copyToShareCacheFolder(File file, String holdName) throws IOException {
        File folder = getShareCacheFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = file.getName();
        if (holdName != null && !holdName.isEmpty()) {
            fileName = holdName + fileName.substring(Math.max(0, fileName.lastIndexOf(".")));
        }
        File newFile = new File(folder, fileName);
        copy(file, newFile);
        return newFile;
    }

    @NonNull
    private File getShareCacheFolder() {
        return new File(getContext().getCacheDir(), "share_plus");
    }

    private Context getContext() {
        if (activity != null) {
            return activity;
        }
        if (context != null) {
            return context;
        }

        throw new IllegalStateException("Both context and activity are null");
    }

    private static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
