package com.module.common.net.download;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.module.common.BaseLib;
import com.module.common.log.LogUtil;
import com.module.common.net.FileUtil;
import com.module.common.net.callback.IDownLoadCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;

/**
 * 下载保存文件
 */

public final class SaveFileTask extends AsyncTask<Object, Void, File> {
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private final IDownLoadCallback iRequestCallback;

    public SaveFileTask(IDownLoadCallback iRequestCallback) {
        this.iRequestCallback = iRequestCallback;
    }

    @Override
    protected File doInBackground(Object... params) {
        String downloadDir = (String) params[0];
        String extension = (String) params[1];
        final ResponseBody body = (ResponseBody) params[2];
        final String name = (String) params[3];

        if (downloadDir == null || downloadDir.equals("")) {
            downloadDir = "down_loads";
        }
        if (extension == null || extension.equals("")) {
            extension = "";
        }

        File file = null;
        try {
            if (name == null) {
                file =  writeToDisk(body, downloadDir, extension.toUpperCase(), extension);
            }else {
                file =  writeToDisk(body, downloadDir, name);
            }

        } catch (final Exception e) {
            file = null;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (iRequestCallback != null) {
                        iRequestCallback.onFailure("saveFile fail." + e.toString());
                    }
                }
            });
        }
        return file;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (file != null) {
            if (iRequestCallback != null) {
                iRequestCallback.onFinish(file);
            }
//            autoInstallApk(file);
        }
    }


    public  File writeToDisk(ResponseBody body, String dir, String name) throws Exception {

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        final InputStream is = body.byteStream();
        final File file = new File(dir, name);
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(file,false);
            bos = new BufferedOutputStream(fos);

            byte data[] = new byte[1024 * 4];
            int count;
            while ((count = bis.read(data)) != -1) {
                bos.write(data, 0, count);
            }
            bos.flush();
            fos.flush();
        }finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                is.close();
            } catch (IOException e) {
                LogUtil.e("zh","download writeToDisk e11 " + e.toString() );
                e.printStackTrace();
            }
        }

        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createFile(String sdcardDirName, String fileName) {
        return new File(createDir(sdcardDirName), fileName);
    }

    private static File createFileByTime(String sdcardDirName, String timeFormatHeader, String extension) {
        final String fileName = getFileNameByTime(timeFormatHeader, extension);
        return createFile(sdcardDirName, fileName);
    }

    /**
     * @param timeFormatHeader 格式化的头(除去时间部分)
     * @param extension        后缀名
     * @return 返回时间格式化后的文件名
     */
    public static String getFileNameByTime(String timeFormatHeader, String extension) {
        return getTimeFormatName(timeFormatHeader) + "." + extension;
    }

    private static String getTimeFormatName(String timeFormatHeader) {
        final Date date = new Date(System.currentTimeMillis());
        //必须要加上单引号
        final SimpleDateFormat dateFormat = new SimpleDateFormat("'" + timeFormatHeader + "'" + TIME_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    //格式化的模板
    private static final String TIME_FORMAT = "_yyyyMMdd_HHmmss";

    private static final String SDCARD_DIR =
            Environment.getExternalStorageDirectory().getPath();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File createDir(String sdcardDirName) {
        //拼接成SD卡中完整的dir
        final String dir = SDCARD_DIR + "/" + sdcardDirName + "/";
        final File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    public File writeToDisk(ResponseBody body, String dir, String prefix, String extension) throws Exception {
        final InputStream is = body.byteStream();
        final File file = createFileByTime(dir, prefix, extension);
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            byte data[] = new byte[1024 * 4];
            long complete_len = 0;
            final long total_len = body.contentLength();
            int count;
            while ((count = bis.read(data)) != -1) {
                bos.write(data, 0, count);
                complete_len += count;
                final long finalComplete_len = complete_len;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(iRequestCallback != null) {
                            iRequestCallback.onProgress(finalComplete_len, total_len);
                        }
                    }
                });
            }

            bos.flush();
            fos.flush();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void autoInstallApk(File file) {
        if (FileUtil.getExtension(file.getPath()).equals("apk")) {
            final Intent install = new Intent();
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setAction(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            BaseLib.getContext().startActivity(install);
        }
    }
}
