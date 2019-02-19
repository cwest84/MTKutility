package com.adtdev.mtkutility;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Arrays;

public class myLibrary {
    public Activity mContext;
    public ActionBar actionbar;
    public int cmdTimeOut;
    public int cmdRetries;
    public boolean stopNMEA;
    public boolean stopLOG;
	public int downBlockSize;
    public boolean initStart = true;
    public String NL = System.getProperty("line.separator");
    public char CR = 0x0d;
    public char LF = 0x0a;
    SimpleDateFormat SDF = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.CANADA);
    SimpleDateFormat FDF = new SimpleDateFormat("yyyy.MM.dd.HHmmss", Locale.CANADA);
    public String asterisks = "************************************************************";
    public Menu nav_Menu;
    public static final int doLOCAL = 0;
    public static final int doFTPselect = 1;
    public static final int doFTPdownld = 2;
    public static final String urlKey = "urlKey";


    public SharedPreferences publicPrefs;
    public SharedPreferences.Editor publicPrefEditor;
    public SharedPreferences appPrefs;
    public SharedPreferences.Editor appPrefEditor;

    // binary command to switch to NMEA mode
    final static byte[] binPMTK253 = new byte[]{(byte) 0x04, 0x24, 0x0E, 0x00, (byte) 0xFD, 0x00, 0x00, 0x00, (byte) 0xC2, 0x01, 0x00, 0x30, 0x0D, 0x0A};
    final static String defaultNMEA = "PMTK314,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0";
    public String gpsNMEA;
    public boolean isGPSlogger = true;
    public boolean hasAGPS = true;
    public boolean hasWrite = false;
    public String recordingMode = null;
    public int logRecCount = 0;
    public String[] parms;
    public int flashSize;

    public boolean OK;
    public boolean failed = false;
    public boolean aborting = false;
    public String errMsg;
    public int activeFragment;

    //mLog constants
    public final int VB0 = 0;
    public final int VB1 = 1;
    public final int VB2 = 2;
    public final int VB3 = 3;
    public final int ABORT = 9;
    public int AGPSsize;
    public int debugLVL;
    public int homeFont;
    public int setngFontBTNS;
    public int setngFontHTML;

    public int screenWidth;
    public int screenHeight;
    public int screenDPI;

    public boolean logFileIsOpen = false;
    public boolean binFileIsOpen = false;
    public File basePath;
    public File logPath;
    public File logFile;
    public File errFile;
    public File eFile;
    public File binPath;
    public File binFile;
    public File epoPath;
    public File epoFile;
    public File gpxPath;
    public File gpxFile;
    public String basePathName = "mtkutility";
    public String binPathName = "mtkutility/bin";
    public String gpxPathName = "mtkutility/gpx";
    public String epoPathName = "mtkutility/epo";
    private String logFileName = "MTKutilityLog.txt";
    private String errFileName = "MTKutilityErr.txt";
    public String binFileName = "temp.bin";
    private static OutputStreamWriter logWriter;
    public FileOutputStream lOut;
    public FileOutputStream bOut;
    public FileOutputStream gOut;
    public PrintStream ps;

    public static int REQUEST_BLUETOOTH = 1;
    public boolean GPSconnected = false;
    public String GPSname = null;
    public String GPSmac = null;
    public String GPstatsTxt = null;
    public String strAGPS;
    public String strGPS;
    public boolean allowInsecure;
    public BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothDevice GPSdevice = null;
    public BluetoothSocket GPSsocket = null;

    public InputStream GPSin = null;
    public OutputStream GPSout = null;

    public String GGA;
    public String GLL;
    public String GSA;
    public String GSV;
    public String RMC;
    public String VTG;
    public String ZDA;

//    public boolean bakgrndOK = false;
    public boolean bkGroundOK = false;
    public boolean showNMEAisRunning = false;
    public Handler handler = new Handler();

    private byte[] bytBuf;
    private StringBuilder newBuf = new StringBuilder();
    private StringBuilder oldBuf = new StringBuilder();
    private StringBuilder parBuf = new StringBuilder();
    private StringBuilder sndBuf = new StringBuilder();
    private boolean doAppend = false;

    public myLibrary(Activity context) {
        mContext = context;
    }//myLibrary()

    public void abortApp(String str) {
        final String emsg;
        if (str.isEmpty()) {
            emsg = mContext.getString(R.string.crashLog);
        } else {
            emsg = str;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog
            .setTitle("FATAL ERROR")
            .setMessage(emsg)
            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                throw new RuntimeException(emsg);
            }
        }).show();
    }//abortApp()

    public void askForEmail() {
        mLog(VB0, "myLibrary.askForEmail()");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        // set dialog message
        alertDialogBuilder.setMessage(mContext.getString(R.string.crashLog)).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                sendEmail(1);
            }
        });
        //show alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    } //askForEmail()

    public boolean BINopen(){
        mLog(VB0, "myLibrary.BINopen()");
        // Create bin file object log download
        OK = true;
        binPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), binPathName);
        // make sure mtkutility/bin directory exists - create if it is missing
        if (!binPath.exists()) {OK = binPath.mkdirs();}
        if (!OK) {return false;}
        binFile = new File(binPath, binFileName);
        if (binFile.exists()) {binFile.delete();}

        try {
            binFile.createNewFile();
            bOut = new FileOutputStream(binFile);
            return true;
        } catch (IOException e) {
            binFileIsOpen = false;
            buildCrashReport(e);
        }
        return false;
    }//BINopen()

    public void BINclose(){
        mLog(VB0, "myLibrary.BINclose()");
        if (binFileIsOpen) {
            try {
                bOut.flush();
                bOut.close();
            } catch (IOException e) {
                buildCrashReport(e);
            }
            binFileIsOpen = false;
        }
    }

    public void buildCrashReport(Throwable ex) {
        mLog(VB0, "myLibrary.buildCrashReport()");
        appPrefEditor.putBoolean("appFailed", true);
        appPrefEditor.commit();
//        closeActivities();
//        mLog(VB0, "myLibrary.buildCrashReport()");
        mLog(VB0, "********** Stack **********\n");
        mLog(VB0, ex.getMessage());
        mLog(VB0, ex.getCause().toString());
        final Writer stringwriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringwriter);
        ex.printStackTrace(printWriter);
        printWriter.close();
        mLog(VB0, stringwriter.toString());
        mLog(VB0, "****** End of Stack ******");
//        logClose();

        //create restart intent
        Intent intent = new Intent(mContext, Main.class);
        mContext.startActivity(intent);

        // make sure we die, otherwise the app will hang ...
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(2);
    }//buildCrashReport()

    public String concatSarray(String[] Sa, int bgn) {
        mLog(VB0, "myLibrary.concatSarray()");
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String s : Sa) {
            if (i >= bgn) {
                builder.append(s);
                if (i < Sa.length - 1) {
                    builder.append(",");
                }
            }
            i++;
        }
        return builder.toString();
    }//concatSarray()

    public void closeActivities() {
        mLog(VB0, "myLibrary.closeActivities()");
        //stop NMEA AsyncTask
        bkGroundOK = false;
        goSleep(3000);
        // close navigation drawer
        DrawerLayout drawer = mContext.findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            mLog(VB1, "+++ myLibrary.closeActivities() +++ closing navigation drawer");
            drawer.closeDrawer(GravityCompat.START);
        }
        //close logger input/output and disconnectGPS
        if (GPSconnected) {
            mLog(VB1, "+++ myLibrary.closeActivities() +++ disconnecting GPS");
            disconnect();
        }
    }//closeActivities()

    public boolean connect() {
        mLog(VB0, "myLibrary.connect()");
        if (GPSmac == null) {
            showToast(mContext.getString(R.string.noGPSselected));
            return false;
        }
//        String str = appPrefs.getString("GPSmac", "");
//        BluetoothDevice zee = GPSdevice;
        Method m = null;
        String methodName;

        if (allowInsecure) {
            methodName = "createInsecureRfcommSocket";
        } else {
            methodName = "createRfcommSocket";
        }
        try {
            m = GPSdevice.getClass().getMethod(methodName, int.class);
        } catch (SecurityException e) {
            buildCrashReport(e);
        } catch (NoSuchMethodException e) {
            {
                buildCrashReport(e);
            }
        }

        try {
            GPSsocket = (BluetoothSocket) m.invoke(GPSdevice, Integer.valueOf(1));
        } catch (IllegalArgumentException e) {
            buildCrashReport(e);
        } catch (IllegalAccessException e) {
            buildCrashReport(e);
        } catch (InvocationTargetException e) {
            buildCrashReport(e);
        }

        try {
            GPSsocket.connect();
        } catch (IOException e) {
//            GPSconnected = false;
            return GPSconnected = false;
        }

        GPSconnected = GPSsocket.isConnected();

        try {
            GPSin = GPSsocket.getInputStream();
        } catch (IOException e) {
            buildCrashReport(e);
        }

        try {
            GPSout = GPSsocket.getOutputStream();
        } catch (IOException e) {
            buildCrashReport(e);
        }
        return GPSconnected;
    }//connect()

    public boolean disconnect() {
        mLog(VB0, "myLibrary.disconnect()");
        //stop Async tasks
        bkGroundOK = false;
        goSleep(3000);

//        try {GPSin.close();
//        } catch (IOException e) {
//            buildCrashReport(e);
//            return false;
//        }
        try {
            mLog(VB2, "+++ myLibrary.disconnect() +++ executing GPSout.flush()");
            GPSout.flush();
        } catch (IOException e) {
            buildCrashReport(e);
            return false;
        }
        try {
            mLog(VB2, "+++ myLibrary.disconnect() +++ executing GPSout.close()");
            GPSout.close();
        } catch (IOException e) {
            buildCrashReport(e);
            return false;
        }
        try {
            mLog(VB2, "+++ myLibrary.disconnect() +++ executing GPSsocket.close()");
            GPSsocket.close();
        } catch (IOException e) {
            buildCrashReport(e);
            return false;
        }
        GPSconnected = false;
        mLog(VB0, "+++ myLibrary.disconnect() +++ GPS is disconnected *****");
        return true;
    }//disconnect()

    public boolean isBTenabled() {
        mLog(VB0, "myLibrary.isBTenabled()");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            return false;
        }
        return true;
    }//isBTenabled()

    public Date dateCalc(int weeks, int secs) {
        mLog(VB0, "myLibrary.dateCalc()");
        Calendar wrkCal = new GregorianCalendar(1980, 0, 6, 0, 0, 0);
        wrkCal.add(Calendar.DATE, weeks * 7);
        wrkCal.add(Calendar.SECOND, secs);
        return wrkCal.getTime();
    }//dateCalc()

    public void exit_app() {
        mLog(VB0, "myLibrary.exit_app()");
        // close navigation drawer
        DrawerLayout drawer = mContext.findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        //close logger input/output and disconnect
        goSleep(500);
        if (GPSconnected) {
            disconnect();
        }
    }//exit_app()

    public void fillGPSstats(){
        if (strAGPS.length() > 0) {
            GPstatsTxt = strAGPS + NL;
			        } else {
            GPstatsTxt = "";
        }
        if (strGPS.length() > 0) {
            GPstatsTxt = GPstatsTxt + strGPS;
        }

    }//fillGPSstats()


    public int getFlashSize (int model) {
        mLog(VB0, "myLibrary.getFlashSize()");
        // 8 Mbit = 1 Mb
        if (model == 0x1388) return( 8 * 1024 * 1024 / 8); // 757/ZI v1
        if (model == 0x5202) return( 8 * 1024 * 1024 / 8); // 757/ZI v2
        // 32 Mbit = 4 Mb
        if (model == 0x0000) return(32 * 1024 * 1024 / 8); // Holux M-1200E
        if (model == 0x0001) return(32 * 1024 * 1024 / 8); // Qstarz BT-Q1000X
        if (model == 0x0004) return(32 * 1024 * 1024 / 8); // 747 A+ GPS Trip Recorder
        if (model == 0x0005) return(32 * 1024 * 1024 / 8); // Qstarz BT-Q1000P
        if (model == 0x0006) return(32 * 1024 * 1024 / 8); // 747 A+ GPS Trip Recorder
        if (model == 0x0008) return(32 * 1024 * 1024 / 8); // Pentagram PathFinder P 3106
        if (model == 0x000F) return(32 * 1024 * 1024 / 8); // 747 A+ GPS Trip Recorder
        if (model == 0x005C) return(32 * 1024 * 1024 / 8); // Holux M-1000C
        if (model == 0x8300) return(32 * 1024 * 1024 / 8); // Qstarz BT-1200
        // 16Mbit -> 2Mb
        // 0x0051    i-Blue 737, Qstarz 810, Polaris iBT-GPS, Holux M1000
        // 0x0002    Qstarz 815
        // 0x001B    i-Blue 747
        // 0x001d    BT-Q1000 / BGL-32
        // 0x0131    EB-85A
        return(16 * 1024 * 1024 / 8);
    }


    public void getGPSid(){
        mLog(VB0, "myLibrary.getGPSid()");
        flashSize = 32 * 1024 * 1024 / 8; //a safe default flash size
        parms = mtkCmd("PMTK605", "PMTK705", cmdTimeOut);
        if (parms != null) {
            flashSize = getFlashSize((int) Long.parseLong(parms[2],16));
            String ss = concatSarray(parms, 1);
            mLog(VB0, asterisks);
            mLog(VB0, ss);
            mLog(VB0, asterisks);
        }
    }//getGPSid()

    public void getSharedPreference(){
        String curFunc = "myLibrary.getSharedPreference()";

        AGPSsize = Integer.parseInt(publicPrefs.getString("AGPSsize", "7"));
        debugLVL = Integer.parseInt(publicPrefs.getString("debugPref", "0"));
        cmdTimeOut = Integer.parseInt(publicPrefs.getString("cmdTimeOut", "30"));
        cmdRetries = Integer.parseInt(publicPrefs.getString("cmdRetries", "2"));
        stopNMEA = publicPrefs.getBoolean("stopNMEA", true);
        stopLOG = publicPrefs.getBoolean("stopLOG", false);
        homeFont = Integer.parseInt(publicPrefs.getString("homeFont", "13"));
        setngFontBTNS = Integer.parseInt(publicPrefs.getString("setBtns", "12"));
        setngFontHTML = Integer.parseInt(publicPrefs.getString("setHtml", "15"));
        downBlockSize = Integer.parseInt(publicPrefs.getString("downBlockSize", "2048"));
        mLog(VB0, String.format("%1$s --- debugLVL=%2$d cmdTimeOut=%3$d cmdRetries=%4$d",curFunc, debugLVL, cmdTimeOut, cmdRetries));
        mLog(VB0, String.format("%1$s --- AGPSsize=%2$d stopNMEA=%3$b stopLOG=%4$b",curFunc, AGPSsize, stopNMEA, stopLOG));
    }//getSharedPreference()

    public void goSleep(int mSec) {
        mLog(VB3, String.format("myLibrary.goSleep(%d)", mSec));
        try {
            Thread.sleep(mSec);
        } catch (InterruptedException e) {
            buildCrashReport(e);
        }
    }//goSleep()

    public String hasAGPS() {
        mLog(VB0, "myLibrary.hasAGPS()");
        // check for AGPS
        hasAGPS = true;
        StringBuilder txtOut = new StringBuilder();
        parms = mtkCmd("PMTK607", "PMTK707", "PMTK001,607", cmdTimeOut);
        if (parms == null) {
            hasAGPS = false;
            mLog(VB1, "+++ myLibrary.hasAGPS() +++ PMTK607 returned null");
            //showToast(mContext.getString(R.string.noAGPS));
        } else {
            int EPOblks = 0;
            int L = parms.length;
            if ((parms[0].equals("PMTK001")) && (parms[L - 1].equals("1"))) {
                hasAGPS = false;
                //showToast(mContext.getString(R.string.noAGPS));
            } else {
                EPOblks = Integer.valueOf(parms[1]);
                txtOut.append(parms[1] + " EPO sets");
                if (EPOblks > 0) {
                    Date dd = dateCalc(Integer.valueOf(parms[4]), Integer.valueOf(parms[5]));
                    txtOut.append(" expires " + SDF.format(dd));
                }
                mLog(VB0, "+++ myLibrary.hasAGPS() +++ GPS has AGPS ******");
            }
        }
        return txtOut.toString();
    }//hasAGPS()

    public String isGPSlogger() {
        String curFunc = "myLibrary.isGPSlogger()";
        mLog(VB0, curFunc);
        curFunc = String.format("+++ %1$s +++",curFunc);
        StringBuilder txtOut = new StringBuilder();
        //PMTK182,2,9 requests the GPS flash id
        parms = mtkCmd("PMTK182,2,9", "PMTK182,3,9,", "PMTK001,182,2,1", cmdTimeOut);
        if (parms == null) {
            isGPSlogger = false;
            mLog(VB1, String.format("%1$s PMTK182,2,9 returned null", curFunc));
        } else {
            int L = parms.length;
            if ((parms[0].equals("PMTK001")) && (parms[L - 1].equals("1"))) {
                isGPSlogger = false;
                mLog(VB1, String.format("%1$s %2$ss returned", curFunc, concatSarray(parms, 0)));
            } else {
                OK = false;
                int ix = 0;
                do {//get log record count
                    parms = mtkCmd("PMTK182,2,10", "PMTK182,3,10", cmdTimeOut);
                    if (parms != null && parms[0].contains("PMTK182") && parms[1].contains("3")) {
                        logRecCount = Integer.parseInt(parms[3], 16);
                        txtOut.append(Integer.toString(logRecCount) + " log records");
                        isGPSlogger = true;
                        OK = true;
                        mLog(VB0, String.format("%1$s GPS is data logger ******", curFunc));
                        mLog(VB0, String.format("%1$s log has %2$d records ******", curFunc, logRecCount));                    }
                } while (!OK && ix <= 10);
                OK = false;
                ix = 0;
                recordingMode = null;
                do {//query logging method 1=overlap, 2=stop when full
                    parms = mtkCmd("PMTK182,2,6", "PMTK182,3,6", cmdTimeOut);
                    if (parms == null) {
                        goSleep(150);
                        continue;
                    }
                    OK = true;
                    recordingMode = parms[3];
                    mLog(VB0, String.format("%1$s record mode is %2$s ******", curFunc, recordingMode));
                } while (!OK && ix <= 10);
                if (!OK){
                    mLog(VB0, String.format("%1$s PMTK182,2,6 failed", curFunc));
                    aborting = true;
                }
            }
        }
        return txtOut.toString();
    }//isGPSlogger()

    public void logClose() {
        mLog(VB0, "myLibrary.logClose()\n");
        if (logFileIsOpen) {
            try {
                logWriter.flush();
                logWriter.close();
                lOut.close();
            } catch (IOException e) {
                buildCrashReport(e);
            }
            logFileIsOpen = false;
        }
    } //logClose()

    private void logPhoneInfo() {
        mLog(132, "myLibrary.logPhoneInfo()");
        mLog(132, String.format("%s************ DEVICE INFO ************", NL));
        mLog(132, String.format("--------Brand: %s", Build.BRAND));
        mLog(132, String.format("-------Device: %s", Build.DEVICE));
        mLog(132, String.format("--------Model: %s", Build.MODEL));
        mLog(132, String.format("------------ID: %s", Build.ID));
        mLog(132, String.format("-------Product: %s", Build.PRODUCT));
        mLog(132, String.format("----Screen DPI: %d", screenDPI));
        mLog(132, String.format("---------width: %d", screenWidth));
        mLog(132, String.format("--------height: %d", screenHeight));
        mLog(132, String.format("%s************ FIRMWARE ************", NL));
        mLog(132, String .format("---Android Version: %s", Build.VERSION.RELEASE));
        mLog(132, String.format("-Android Increment: %s", Build.VERSION.INCREMENTAL));
        mLog(132, String .format("-------------Board: %s %s", Build.BOARD , NL));
    }//logPhoneInfo()

    public void mLog(int mode, String msg) {
        if (!logFileIsOpen) {
            return;
        }
        switch (mode) {
            case VB0:
                if (msg.length() > 127) {msg = msg.substring(0, 127) + " ...";}
                break;
            case VB1:
                if (mode > debugLVL) {return;}
                break;
            case VB2:
                if (mode > debugLVL) {return;}
                break;
            case VB3:
                if (mode > debugLVL) {return;}
                break;
            case ABORT:
                throw new RuntimeException(msg);
        }

        try {
            logWriter.append(msg + NL);
            logWriter.flush();
        } catch (IOException e) {
            buildCrashReport(e);
        }
    }//mLog()

    public String[] mtkCmd(String mtkCmd, String mtkReply, int timeout) {
        return mtkCmd(mtkCmd, new String[]{mtkReply}, timeout, cmdRetries);
    }

    public String[] mtkCmd(String mtkCmd, String mtkReply1, String mtkReply2, int timeout) {
        return mtkCmd(mtkCmd, new String[]{mtkReply1, mtkReply2}, timeout, cmdRetries);
    }

    public String[] mtkCmd(String mtkCmd, String[] mtkReplies, int timeout, int retries) {
        for (int i=0; i <= retries; i++) {
            mLog(VB1, "myLibrary.mtkCmd(" + mtkCmd + "|" + Arrays.toString(mtkReplies) + "|"
                    + Double.toString(timeout) + "|" + Integer.toString(i) + ")");
            OK = true;
            String[] sArray = new String[99];
            String reply;

//          mLog(VB1, "+++ myLibrary.mtkCmd() +++ sending: " + mtkCmd);
            sendCommand(mtkCmd);

            if (OK) {
//              mLog(VB1, "+++ myLibrary.mtkCmd() +++ waiting for: " + mtkReply);
                reply = waitForReply(mtkReplies, timeout);
                if (reply == null) {
                    OK = false;
                }

                if (OK) {
                    for (String mtkReply : mtkReplies) {
                        int bgn = reply.indexOf(mtkReply);
                        if (bgn > 0) {
                            int end = reply.indexOf("*", bgn);
                            reply = reply.substring(bgn, end);
                            mLog(VB1, "+++ myLibrary.mtkCmd() +++ received: " + reply);
                            sArray = reply.split(",");
                            return sArray;
                        }
                    }
                } else {
                    mLog(VB1, "+++ myLibrary.mtkCmd() +++ no response for: " + Arrays.toString(mtkReplies));
                    return null;
                }
            }
        }
        return null;
    }//mtkCmd(mtkCmd, mtkReplies, timeout, retries)

    public void NMEAgetSetting() {
        String curFunc = "myLibrary.NMEAgetSetting()";
        mLog(VB0, curFunc);
        curFunc = String.format("+++ %1$s +++",curFunc);

        //get saved PMTK314 and make sure it is not all 0
        mLog(VB1, String.format("%1$s retreiving saved preference", curFunc));
        String cmd = appPrefs.getString("saveMNEA", "PMTK314,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0");
        String[] sArray = cmd.split(",");
        //check settings string to make sure we have at least 1 output
        OK = false;
        for (int ix = 1; ix <= 19; ix++) {
            if (!sArray[ix].contains("0")) {
                OK = true;
            }
        }
        if (!OK) {
            mLog(VB2, String.format("%1$s resetting saved to default", curFunc));
            cmd = defaultNMEA;
        }
        int retry = 10;
        do {
            //get NMEA output setting from GPS
            parms = mtkCmd("PMTK414", "PMTK514", cmdTimeOut);
            if (parms == null) {
                continue;
            }
            if (!parms[0].contains("PMTK514")) {
                continue;
            }
            //check settings string to make sure we have at least 1 output
            OK = false;
            for (int ix = 1; ix <= 19; ix++) {
                if (!parms[ix].contains("0")) {
                    OK = true;
                }
            }
            if (!OK){
                //reset GPS to default output if all 0 in settings
                mLog(VB1, String.format("%1$s invalid prefernce stored - resetting",curFunc));
                sendCommand(cmd);
                goSleep(200);
                //save to private preferences
                appPrefEditor.putString("saveMNEA", cmd);
                appPrefEditor.commit();

            }
            retry--;
        }
        while (!OK && retry > 0);

        if (OK) {
            gpsNMEA = concatSarray(parms, 1);
            mLog(VB1, String.format("%1$s saving UI settings:%2$s ", curFunc, gpsNMEA));
            GLL = parms[1];
            RMC = parms[2];
            VTG = parms[3];
            GGA = parms[4];
            GSA = parms[5];
            GSV = parms[6];
            ZDA = parms[18];
        }
    }//NMEAgetSetting()

    public void NMEAstart(){
        String curFunc = "myLibrary.NMEAstart()";
        mLog(VB0, curFunc);
        curFunc = String.format("+++ %1$s +++",curFunc);

        mLog(VB1, String.format("%1$s retreiving saved preference", curFunc));
        String cmd = appPrefs.getString("saveMNEA", "PMTK314,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0");
        String[] sArray = cmd.split(",");
        OK = false;
        for (int ix = 1; ix <= 19; ix++) {
            if (!sArray[ix].contains("0")) {
                OK = true;
            }
        }
        if (!OK) {
            mLog(VB2, String.format("%1$s resetting saved to default", curFunc));
            cmd = defaultNMEA;
        }

        sendCommand(cmd);
        goSleep(2000);
        int retry = 10;
        do {
            //get NMEA output setting from GPS
            parms = mtkCmd("PMTK414", "PMTK514", cmdTimeOut);
            if (parms == null) {
                continue;
            }
            if (!parms[0].contains("PMTK514")) {
                continue;
            }
            //check settings string to make sure we have at least 1 output
            OK = false;
            for (int ix = 1; ix <= 19; ix++) {
                if (!parms[ix].contains("0")) {
                    OK = true;
                    retry = 0;
                }
            }
            retry--;
        }
        while (!OK && retry > 0);
    }//NMEAstart()

    public void NMEAstop() {
        String curFunc = "myLibrary.NMEAstop()";
        mLog(VB0, curFunc);
        curFunc = String.format("+++ %1$s +++", curFunc);

        sendCommand("PMTK314,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
        waitForReply("PMTK001,314", cmdTimeOut);
//        goSleep(2000);
    }//NMEAstop()

    public void LOGstop() {
        String curFunc = "myLibrary.LOGstop()";
        mLog(VB0, curFunc);
        curFunc = String.format("+++ %1$s +++", curFunc);

        sendCommand("PMTK182,5");
        waitForReply("PMTK001,182,5,3", cmdTimeOut);
//        goSleep(2000);
    }//NMEAstop()

    public void LOGstart() {
        String curFunc = "myLibrary.LOGstart()";
        mLog(VB0, curFunc);
        curFunc = String.format("+++ %1$s +++", curFunc);

        sendCommand("PMTK182,4");
        waitForReply("PMTK001,182,4,3", cmdTimeOut);
//        goSleep(2000);
    }//NMEAstop()

    public boolean openGPX(){
        // Create bin file object log download
        OK = true;
        gpxPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), gpxPathName);
        // make sure mtkutility/gpx directory exists - create if it is missing
        if (!gpxPath.exists()) {OK = gpxPath.mkdirs();}
        if (!OK) {return false;}
        String fname = "need file name";
        gpxFile = new File(gpxPath, fname);
        if (gpxFile.exists()) {gpxFile.delete();}

        try {
            gpxFile.createNewFile();
            gOut = new FileOutputStream(gpxFile);
            return true;
        } catch (IOException e) {
            buildCrashReport(e);
        }
        return false;
    }//openGPX()

    public boolean openLog() {
        // Create log file objects for the the email method
        OK = true;
        logFileIsOpen = false;
        logPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), basePathName);
        // make sure mtkutility directory exists - create if it is missing
        if (!logPath.exists()) {OK = logPath.mkdirs();}
        if (!OK) {return false;}

        logFile = new File(logPath, logFileName);
        // did previous app execute fail - ask user to send an email
        failed = appPrefs.getBoolean("appFailed", false);
        if (failed) {
            if (logFile.exists()) {
                errFile = new File(logPath, errFileName);
                // rename log file to preserve error log for email
                logFile.renameTo(errFile);
                askForEmail();
                setAppFailed(false);
            }
        }

        if (logFile.exists()) {logFile.delete();}

        try {
            logFile.createNewFile();
            lOut = new FileOutputStream(logFile);
            ps = new PrintStream(logFile);
            logFileIsOpen = true;
        } catch (IOException e) {
            buildCrashReport(e);
        }

//        logFileIsOpen = false; //use to test log file open failure
        if (logFileIsOpen) {
            logWriter = new OutputStreamWriter(lOut);
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            mLog(VB0, "+++++++ log file opened " + currentDateTimeString + NL);
            mLog(VB0, "myLibrary.openLog()");
            mLog(VB0, "debug level is " + debugLVL);
            logPhoneInfo();
            return true;
        }
        return false;
    }//openLog()

    public byte[] readBytes(int timeout) {
        int bytes_available = 0;
        long retry = timeout;
        byte[] buf = null;
        mLog(VB2, String.format("+++ readBytes timeout: %1$d", retry));

        while (bytes_available == 0 && retry > 0) {
            try {
                bytes_available = GPSin.available();
            } catch (IOException e) {
                buildCrashReport(e);
            }
            goSleep(250);
            timeout -= .25;
        }
        mLog(VB2, String.format("+++ readBytes timeout: %1$d - read: %2$d bytes", retry, bytes_available));
        if (bytes_available > 0) {
            buf = new byte[bytes_available];
            try {
                GPSin.read(buf);
            } catch (IOException e) {
                buildCrashReport(e);
            }
        }
        return buf;
    }//readBytes(timeout)

    public String readString(int timeout) throws IOException, InterruptedException {
        int bytes_available = 0;
        StringBuilder buffer = new StringBuilder();
        buffer.setLength(0);

        do {
            Thread.sleep(250);
            timeout -= 0.25;
        } while (GPSin.available() == 0 && timeout > 0);

        if (GPSin.available() > 0) {
            byte[] buf = new byte[GPSin.available()];
            GPSin.read(buf);
            for (int k = 0; k < buf.length; k++) {
                char c = (char) (buf[k] & 0xff);
                buffer.append(c);
            }
        }
        return buffer.toString();
    }//readString()

    public void resetBuffers() {
        mLog(VB0, "myLibrary.resetBuffers");
        newBuf = new StringBuilder();
        oldBuf = new StringBuilder();
        parBuf = new StringBuilder();
        sndBuf = new StringBuilder();
    }//resetBuffers()

    public void sendBytes(byte[] byteArray) {
        mLog(VB1, "myLibrary.sendBytes");
        try {
            GPSout.write(byteArray);
        } catch (Exception e) {
            mLog(VB0, "+++ myLibrary.sendBytes() +++ failed ");
            buildCrashReport(e);
        }
    }//sendBytes()

    public static byte calculateChecksum(String command) {
        byte checksum = 0;
        int startPoint = command.indexOf("$") + 1;
        int endPoint = command.indexOf("*",startPoint);
        if (endPoint == -1) {
            endPoint = command.length();
        }
        for (int i=startPoint; i<endPoint; i++) {
            checksum ^= (byte) command.charAt(i);
        }

        return (checksum);
    }

    public static boolean checkChecksum(String command) {
        byte calcChecksum = calculateChecksum(command);
        int checkPosition = command.indexOf("*") + 1;
        if (checkPosition != 0) {
            if (command.length() < (checkPosition+2)) {
                return(false);
            }
            byte sentChecksum = (byte) ((Character.digit(command.charAt(checkPosition), 16) << 4)
                    + Character.digit(command.charAt(checkPosition + 1), 16));
            return(calcChecksum == sentChecksum);
        }
        return (false);
    }

    public void sendCommand(String command) {
        mLog(VB1, "myLibrary.sendCommand");
        byte checksum = calculateChecksum(command);
        StringBuilder rec = new StringBuilder(256);
        rec.setLength(0);
        rec.append('$');
        rec.append(command);
        rec.append('*');
        rec.append(String.format("%02X", checksum));
        rec.append("\r\n");
        mLog(VB1, "+++ myLibrary.sendCommand() +++ sending: " + rec.toString().substring(0, rec.length() - 2));

        try {
            GPSout.write(rec.toString().getBytes());
            sndBuf = new StringBuilder();
        } catch (Exception e) {
            mLog(VB0, String.format("+++ myLibrary.sendCommand() +++ %s failed-", rec.toString()));
            OK = false;
            buildCrashReport(e);
        }
    }//sendCommand()

    public boolean sendPMTK253() {
        mLog(VB0, "myLibrary.sendPMTK253()");
        //send reset to normal text output - precautionary to coorect fail during
        //during binary communication mode
        sendBytes(binPMTK253);
        mLog(VB1, "+++ myLibrary.sendPMTK253() +++ sendPMTK253 suceeded");
        return true;
    }//sendPMTK253()

    public void sendEmail(int idx) {
        mLog(VB0, "myLibrary.sendEmail()");
        switch (idx){
            case 0:
                if (!eFile.exists() || !eFile.canRead()) return;
                break;
            case 1:
                if (!errFile.exists() || !errFile.canRead()) return;
                break;
        }
        //create lookup table for email body text
        final int[] LOOKUP_TABLE = new int[]{R.string.emailtext, R.string.errortext};
        Uri uri;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getString(R.string.myEmail)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "mtkutility log file");
        emailIntent.putExtra(Intent.EXTRA_TEXT, mContext.getString(LOOKUP_TABLE[idx]));
        if (idx == 0) {
            uri = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".provider", logFile);
        } else {
            uri = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".provider", errFile);
        }
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        mContext.startActivity(Intent.createChooser(emailIntent, mContext.getResources().getString(R.string.emailMsg)));
    }//sendEmail()

    public void setAppFailed(Boolean bb) {
        mLog(VB0, "myLibrary.setAppFailed()");
        appPrefEditor.putBoolean("appFailed", bb);
        appPrefEditor.commit();
    }//setAppFailed()

    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    } //showToast()

    public void throwException() {
        //use this routine to test uncaught exception handler
        mLog(VB0, "myLibrary.throwException()");
        //divide by 0 error
        int zz = 0;
        int res = 1 / zz;
    }//testException(int Int)

    public String waitForReply(String reply, int timeout) {
        return waitForReply(new String[]{reply}, timeout);
    }

    public String waitForReply(String reply1, String reply2, int timeout) {
        return waitForReply(new String[]{reply1, reply2}, timeout);
    }

    public String waitForReply(String[] replies, int timeout) {
        String curFunc = "myLibrary.waitForReply()";
        mLog(VB1, String.format("%1$s waiting for:%2$s timeout: %3$d",curFunc, Arrays.toString(replies), timeout));

        char b;
        int retries = timeout;
        int loop = 0;

        // Read from the device until we get the reply we are looking for
        while (loop < retries) {
            loop++;
            bytBuf = readBytes(timeout);
            if (bytBuf == null){
                continue;
            }
            //convert the byte array to char
            newBuf = new StringBuilder();
            for (int j = 0; j < bytBuf.length; j++) {
                b = (char) (bytBuf[j] & 0xff);
                newBuf.append(b);
            }
            parBuf = new StringBuilder(oldBuf.length() + newBuf.length());
            parBuf.append(oldBuf);
            parBuf.append(newBuf);
            mLog(VB1, String.format(" %1$s have %2$d bytes read %3$d parsing %4$d", curFunc, oldBuf.length(), newBuf.length(), parBuf.length()));
            mLog(VB3, String.format(" %1$s oldBuf <%2$s>", curFunc, oldBuf.toString()));
            mLog(VB3, String.format(" %1$s newBuf <%2$s>", curFunc, newBuf.toString()));
            mLog(VB3, String.format(" %1$s parBuf <%2$s>", curFunc, parBuf.toString()));
            mLog(VB3, String.format(" %1$s sndBuf <%2$s>", curFunc, sndBuf.toString()));
            //extract NMEA and PMTK responses - strings that start with $ and end with *..\r\n
            if (parBuf.length() > 0) {
                for (int j = 0; j < parBuf.length(); j++) {
                    b = parBuf.charAt(j);
                    if (b == ' ') {
                        continue;
                    }
                    if (b == '$') {
                        doAppend = true;
                        sndBuf = new StringBuilder();
                        mLog(VB2, String.format(" %1$s $ found at index %2$d", curFunc, j));
                    }
                    if (b == CR) {
                        doAppend = false;
                        mLog(VB2, String.format(" %1$s * found j=%2$d sndBuf length=%3$d", curFunc, j, sndBuf.length()));
                        mLog(VB3, String.format(" %1$s sndBuf <%2$s>", curFunc, sndBuf.toString()));
                        j += 1;
                        //save the unrocessed part of the array read
                        oldBuf = new StringBuilder();
                        if (j+1 < parBuf.length()) {
                            oldBuf.append(parBuf.substring(j+1));
                        }
                        mLog(VB3, String.format(" %1$s oldBuf <%2$s>", curFunc, oldBuf.toString()));
                        for (String reply : replies) {
                            if (sndBuf.indexOf(reply, 0) > 0) {
                                if (checkChecksum(sndBuf.toString())) {
//                                    result = a > b ? x : y;
                                    int end = (sndBuf.indexOf("*") > 30) ? 30 : sndBuf.indexOf("*");
                                    mLog(VB1, String.format(" %1$s returning <%2$s>...", curFunc, sndBuf.toString().substring(0, end)));
                                    return sndBuf.toString();
                                } else {
                                    mLog(VB1, String.format(" %1$s NOT returning due to wrong Checksum <%2$s>...", curFunc, sndBuf.toString().substring(0, 15)));
                                    byte checksum = calculateChecksum(sndBuf.toString());
                                    String calcCHK = String.format("%02X", checksum);
                                    mLog(VB3, String.format(" %1$s The expected calculated Checksum is <%2$s>", curFunc, calcCHK));
                                    return "";
                                }
                            }
                        }
                    }
                    if (doAppend) {
                        sndBuf.append(b);
                    }
                }
				// if the parBuf doesn't contain CR, the newBuf is never saved into oldBuf
                if (parBuf.indexOf(String.valueOf(CR)) == -1){
                    oldBuf.append(newBuf);
                }
            }
//            mLog(VB1, String.format(" %1$s sndBuf.length=%2$d", curFunc, sndBuf.length()));
        }
        // We did not receive the message we where waiting for after 100 messages! Return empty string.
        mLog(VB1, String.format(" %1$s did not receive %2$s after %3$d reads", curFunc, Arrays.toString(replies), retries));
        OK = false;
        return "";
    }//waitForReply()
}


