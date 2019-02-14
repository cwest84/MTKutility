package com.adtdev.fileChooser;

import android.Manifest;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.io.FileUtils;

public class FileChooser extends ListActivity {

    private static final int REQUEST_READ = 0;
    private static CustomAdapter adapter;
    private ProgressDialog dialog;
    private File currentDir;
    private int method;
    private String rootPath;
    private String strtPath;
    private String ftpFile;
    private String lclFile;
    private String ftpURL;
    private String ftpName;
    private String ftpPswd;
    private int ftpPort;
    private boolean ftpAdd;
    public boolean nofolders = false;
    public boolean showhidden = false;
    private String sTitle;
    private String[] sArr;
    private boolean oK;
    private Context context;
    private  Intent intent;

    List<DataModel> dir = new ArrayList<DataModel>();
    List<DataModel> fls = new ArrayList<DataModel>();
    String bacK = "";
    String fn;
    File fN, fff;

    public FTPClient mFTPClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        //check for write to external storage permission and request if not present

        boolean hasWrite = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasWrite) {
            // ask the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        // Get the transferred data from source activity.
        intent = getIntent();
        method = intent.getIntExtra("method", 0);
        switch (method) {
            case 0:
                runLocal();
                break;
            case 1:
                runFTPsel();
                break;
            case 2:
                runFTPdl();
                break;
        }
    }//onCreate()

    private void runLocal() {
        rootPath = intent.getStringExtra("root");
        strtPath = intent.getStringExtra("start");
        nofolders = intent.getBooleanExtra("nofolders", false);
        showhidden = intent.getBooleanExtra("showhidden", false);
        currentDir = new File(strtPath);
        fill(currentDir);
    }//runLocal()

    private void runFTPsel() {
        ftpURL = intent.getStringExtra("ftpURL").trim();
        ftpName = intent.getStringExtra("ftpName").trim();
        ftpPswd = intent.getStringExtra("ftpPswd").trim();
        ftpPort = Integer.parseInt(intent.getStringExtra("ftpPort").trim());
        this.setTitle(ftpURL);
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.working));
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                oK = ftpConnect();
                if (oK) {
                    handler.sendEmptyMessage(0);
                } else {
                    handler.sendEmptyMessage(-1);
                }
            }
        }).start();
    }//runFTPsel()

    private void runFTPdl() {
        ftpURL = intent.getStringExtra("ftpURL").trim();
        ftpName = intent.getStringExtra("ftpName").trim();
        ftpPswd = intent.getStringExtra("ftpPswd").trim();
        ftpPort = Integer.parseInt(intent.getStringExtra("ftpPort").trim());
        ftpFile = intent.getStringExtra("srceFN").trim();
        lclFile = intent.getStringExtra("destFN").trim();
        ftpAdd = intent.getBooleanExtra("append", false);
        this.setTitle("FTP Download in Progress");
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.working));
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                if (ftpConnect()) {
                    if (ftpDownload()) {
                        handler.sendEmptyMessage(2);
                    }else {
                        handler.sendEmptyMessage(-2);
                    }
                } else {
                    handler.sendEmptyMessage(-1);
                }
            }
        }).start();
    }//runFTPdl()

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

//            if (pd != null && pd.isShowing()) {
//                pd.dismiss();
//            }
            if (msg.what == -1) {
                if (dialog.isShowing()) dialog.dismiss();
                Toast.makeText(context, "Error: connect failed - " + ftpURL, Toast.LENGTH_LONG).show();
                finish();
            } else if (msg.what == -2) {
                if (dialog.isShowing()) dialog.dismiss();
                Toast.makeText(context, "Error: download failed - " + ftpURL, Toast.LENGTH_LONG).show();
                finish();
            } else if (msg.what == 0) {
                if (dialog.isShowing()) dialog.dismiss();
                Toast.makeText(context, "connected to " + ftpURL, Toast.LENGTH_SHORT).show();
                getFTPfiles();
            } else if (msg.what == 1) {
                if (dialog.isShowing()) dialog.dismiss();
                adapter = new CustomAdapter(FileChooser.this, R.layout.file_view, dir);
                FileChooser.this.setListAdapter(adapter);
            } else if (msg.what == 2) {
                if (dialog.isShowing()) dialog.dismiss();
                Toast.makeText(context, ftpFile + " download completed", Toast.LENGTH_LONG).show();
                finish();
            }

        }

    };

    private void getFTPfiles() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    FTPFile[] files = mFTPClient.listFiles("/");
                    for (FTPFile file : files) {
                        buildFTPlist(file);
                    }
                    Collections.sort(dir);
                    Collections.sort(fls);
                    dir.addAll(fls);
                    handler.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }//getFTPfiles()

    private void buildFTPlist(FTPFile ff){
        fn = ff.getName();
//            if (fn.startsWith(".") && !showhidden) continue;

//                String s = fff.getAbsolutePath();
//                Date lastModDate = new Date(ff.getTimestamp());
        DateFormat formater = DateFormat.getDateTimeInstance();
        String date_modify = formater.format(ff.getTimestamp().getTime());
        if (ff.isDirectory()) {
//                    //String formated = lastModDate.toString();
//                    dir.add(new DataModel(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), "folder"));
        } else {
            fls.add(new DataModel(ff.getName(), ff.getSize() + " Byte", date_modify, ff.getName(), "file"));
        }

    }

    private boolean ftpConnect() {
        try {
            mFTPClient = new FTPClient();
            mFTPClient.setConnectTimeout(5000);
            // connecting to the host
            mFTPClient.connect(ftpURL, ftpPort);
            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                // login using username & password
                boolean isOK = mFTPClient.login(ftpName, ftpPswd);
                /*
                 * Set File Transfer Mode
                 *
                 * To avoid corruption you must specifie a correct
                 * transfer method, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
                 * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
                 * transferring text, image, and compressed files.
                 */
                mFTPClient.setDataTimeout(20000);
                mFTPClient.setConnectTimeout(20000);
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.enterLocalPassiveMode();
                return isOK;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }//ftpConnect()

    private boolean ftpDownload() {
        boolean isOK = false;
        OutputStream outputStream = null;
        FileOutputStream out = null;
        File localPath = new File(lclFile);

        try {
            out = FileUtils.openOutputStream(localPath, ftpAdd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream = new BufferedOutputStream(out);
            isOK = mFTPClient.retrieveFile(ftpFile, outputStream);
            if (outputStream != null) outputStream.close();
        } catch (FileNotFoundException e) {
            isOK = false;
            e.printStackTrace();
        } catch (IOException e) {
            isOK = false;
            e.printStackTrace();
        }

        if (mFTPClient != null) {
            try {
                mFTPClient.logout();
                mFTPClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isOK;
    }//ftpDownload()

    private void fill(File f) {
        if (f.getName().equalsIgnoreCase("emulated")) {
            f = new File("/storage/emulated/0");
        }
        sTitle = f.getPath();
        sArr = sTitle.split("/");
        if (sArr.length > 4)
            sTitle = "/" + sArr[1] + "/" + sArr[2] + "/.../" + sArr[sArr.length - 1];
        this.setTitle(sTitle);
        File[] dirs = f.listFiles();
        List<DataModel> dir = new ArrayList<DataModel>();
        List<DataModel> fls = new ArrayList<DataModel>();
        String bacK = "";
        String fn;
        File fN, fff;
        try {
            for (File ff : dirs) {
                fn = ff.getName();
                if (fn.startsWith(".") && !showhidden) continue;
                if (fn.equalsIgnoreCase("emulated")) {
                    fff = new File("/storage/emulated/0");
                } else {
                    fff = ff;
                }
                if (fff.canRead()) {
                    String s = fff.getAbsolutePath();
                    Date lastModDate = new Date(fff.lastModified());
                    DateFormat formater = DateFormat.getDateTimeInstance();
                    String date_modify = formater.format(lastModDate);
                    if (ff.isDirectory()) {
                        File[] fbuf = fff.listFiles();
                        int buf = 0;
                        if (fbuf != null) {
                            buf = fbuf.length;
                        } else buf = 0;
                        String num_item = String.valueOf(buf);
                        if (buf == 0) num_item = num_item + " item";
                        else num_item = num_item + " items";

                        //String formated = lastModDate.toString();
                        dir.add(new DataModel(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), "folder"));
                    } else {
                        fls.add(new DataModel(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "file"));
                    }
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        String ff = f.getAbsolutePath();
        if (!f.getAbsolutePath().equalsIgnoreCase(rootPath)) {
            bacK = f.getParent();
            fN = new File(bacK);
            bacK = fN.getAbsolutePath();
            if (bacK.equalsIgnoreCase("/storage/emulated")) bacK = "/storage";
            dir.add(0, new DataModel("..", bacK, "", f.getParent(), "directory_up"));
        }
        adapter = new CustomAdapter(FileChooser.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }//fill()

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        DataModel object = adapter.getItem(position);

        if (object.getimage().equalsIgnoreCase("folder") || object.getimage().equalsIgnoreCase("directory_up")) {
//            String path = o.getPath();
            if (object.getpath().equalsIgnoreCase("/storage/emulated") && object.getimage().equalsIgnoreCase("directory_up")) {
                currentDir = new File("/storage");
            } else {
                currentDir = new File(object.getpath());
            }
            fill(currentDir);
        } else {
//            sendBack(object);
        }
    }

    public void startsendBack(DataModel object) {
        String ss = object.getimage();
        if (object.getimage().equalsIgnoreCase("file")) {
            sendBack(object);
        } else {
            if (!nofolders) sendBack(object);
        }
    }

    public void sendBack(DataModel object) {
        Intent intent = new Intent();
        intent.putExtra("GetPath", object.getpath());
        intent.putExtra("GetFileName", object.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
