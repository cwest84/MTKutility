package com.adtdev.mtkutility;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import com.google.gson.Gson;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static myLibrary mL;

    private boolean OK;
    private Thread.UncaughtExceptionHandler appUEH;
    private static final int REQUEST_WRITE_STORAGE = 0;
    private FragmentManager fragmentManager;
    NavigationView navigationView;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mL = new myLibrary(this);
        mL.mContext = this;
        //turn off screen rotation - force portrate mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        //set public and private preference handlers
        mL.publicPrefs = PreferenceManager.getDefaultSharedPreferences(mL.mContext);
        mL.publicPrefEditor = mL.publicPrefs.edit();
        mL.appPrefs = mL.mContext.getSharedPreferences("otherprefs", Context.MODE_PRIVATE);
        mL.appPrefEditor = mL.appPrefs.edit();

        //set custom exception handler as default handler
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        //check for file write permission - twice
        if (!checkFileWritePermisssion()) requestFileWritePermisssion();
        if (!checkFileWritePermisssion()) requestFileWritePermisssion();
        if (!mL.hasWrite) {
            mL.aborting = true;
            mL.showToast("FATAL ERROR - mtkutility needs write permission");
            finish();
        }

        //is this the first app execute? - execute startup routine
        mL.initStart = mL.appPrefs.getBoolean("iniTStarT", true);
        if (mL.initStart) initialRun();

        //get screen size for initial log entries
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        mL.screenDPI = metrics.densityDpi;
        mL.screenWidth = metrics.widthPixels;
        mL.screenHeight = metrics.heightPixels;

        //open activity log file
        if (mL.hasWrite) {
            boolean ok = mL.openLog();
            if (!ok) {
                mL.aborting = true;
                mL.showToast(String.format(getString(R.string.logFatal), mL.logFile));
                finish();
            }
        }

        //make sure phone has Bluetooth
        hasBluetooth();

        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mL.actionbar = getSupportActionBar();

        // create the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        mL.nav_Menu = navigationView.getMenu();

        // set the toolbar title
        String title = getResources().getString(R.string.app_name) + " - " + getString(R.string.nav_home);
        mL.actionbar.setTitle(title);

        //set the startup screen from the navigation drawer
        mL.getSharedPreference();
        Fragment fragment;
        fragmentManager = getSupportFragmentManager();
        fragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        if (mL.initStart) {
            fragment = new AboutFragment();
            mL.activeFragment = R.id.nav_About;
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
            mL.appPrefEditor.putBoolean("iniTStarT", false).commit();
        }
    }//onCreate()

    @Override
    public void onBackPressed() {
        mL.mLog(mL.VB0, "Main.onBackPressed()");
        if (back_pressed + 2000 > System.currentTimeMillis()){
            mL.closeActivities();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            finish();
            super.onBackPressed();
        }else{
            Toast.makeText(mL.mContext, "press back again to exit mtkutility",Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
        // back is disabled to ensure bluetooth connection with GPS is closed properly
//        mL.showToast(this.getString(R.string.noback));
    }//onBackPressed()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mL.mLog(mL.VB0, "Main.onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }//onCreateOptionsMenu(Menu menu)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mL.mLog(mL.VB0, "Main.onOptionsItemSelected()");
        Intent prefIntent = new Intent(this, PreferencesActivity.class);
        startActivity(prefIntent);
        return true;
    }//onOptionsItemSelected(MenuItem item)

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mL.mLog(mL.VB0, "Main.onNavigationItemSelected()");
        // Handle navigation view item clicks here.
        mL.mLog(mL.VB0, String.format("+++ Main.onNavigationItemSelected() +++ %s selected", item.getTitle()));
        mL.activeFragment = item.getItemId();
        Fragment fragment = null;
        if (mL.showNMEAisRunning){
            mL.showNMEA = false;
        }

        switch (mL.activeFragment) {
            case R.id.nav_Home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_GetLog:
                if (mL.GPSconnected) {
                    fragment = new GetLogFragment();
                } else {
                    mL.showToast(this.getString(R.string.noConnect));
                }
                break;
            case R.id.nav_MakeGPX:
                fragment = new MakeGPXFragment();
                break;
            case R.id.nav_GetEPO:
                fragment = new GetEPOFragment();
                break;
            case R.id.nav_CheckEPO:
                fragment = new CheckEPOFragment();
                break;
            case R.id.nav_UpdtAGPS:
                fragment = new UpdtAGPSFragment();
                break;
            case R.id.nav_Settings:
                fragment = new SettingsFragment();
                break;
            case R.id.nav_eMail:
                fragment = new eMailFragment();
                break;
            case R.id.nav_Help:
                fragment = new HelpFragment();
                break;
            case R.id.nav_About:
                fragment = new AboutFragment();
                break;
            case R.id.nav_Exit:
                mL.closeActivities();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                mL.logClose();
                finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        if (fragment != null) {
//            while (mL.backgroundRunning){
//                mL.bakgrndOK = false;
//                mL.goSleep(500);
//            }
//            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            // set the toolbar title
            if (getSupportActionBar() != null) {
                String title = getResources().getString(R.string.app_name) + " - " + item.toString();
                getSupportActionBar().setTitle(title);
            }
        }

        return true;
    }//onNavigationItemSelected(MenuItem item)

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mL.mLog(mL.VB0, "Main.onRequestPermissionsResult()");
        // The result of the popup opened with the requestPermissions() method
        // is in that method, you need to check that your application comes here
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mL.hasWrite = true;
                boolean ok = mL.openLog();
                if (!ok) {
                    mL.aborting = true;
                    mL.errMsg = getString(R.string.logFatal);
                }
            }else{
                mL.aborting = true;
            }
        }
    }//onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)

    private boolean checkFileWritePermisssion() {
        mL.hasWrite = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        return mL.hasWrite;
    }//checkFileWritePermisssion()

    private void requestFileWritePermisssion() {
        if (!mL.hasWrite) {
            // ask the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            mL.goSleep(5000);
        }
    }//checkFileWritePermisssion()


    public void hasBluetooth() {
        mL.mLog(mL.VB0, "Main.hasBluetooth()");
        mL.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mL.mBluetoothAdapter == null) {
            mL.mLog(mL.VB0, "+++ myLibrary.hasBluetooth() +++ phone does not support Bluetooth");
            mL.showToast(mL.mContext.getString(R.string.noBluetooth));
            mL.aborting = true;
            finish();
        }
    }//hasBluetooth()

    private void initialRun() {
        mL.appPrefs.edit().clear().commit();
        mL.publicPrefs.edit().clear().commit();
        mL.goSleep(250);

        //build FTP site array
        ArrayList<urlModel> sitesList = new ArrayList<>();
        String F60[] = getResources().getStringArray(R.array.F60);
        sitesList.add(new urlModel(F60[0], F60[1], F60[2], F60[3]));
        String F72[] = getResources().getStringArray(R.array.F72);
        sitesList.add(new urlModel(F72[0], F72[1], F72[2], F72[3]));

        //store FTP site array in private preferences
        Gson gson = new Gson();
        String json = gson.toJson(sitesList);
        mL.appPrefEditor.putString(myLibrary.urlKey, json);
        mL.appPrefEditor.commit();

        //store defaults for public preferences
        mL.publicPrefEditor.putString("homeFont", "13");
        mL.publicPrefEditor.putString("setBtns", "12");
        mL.publicPrefEditor.putString("setHtml", "15");
        mL.publicPrefEditor.putString("AGPSsize", "7");
        mL.publicPrefEditor.putString("debugPref", "0");
        mL.publicPrefEditor.putString("cmdTimeOut", "30");
        mL.publicPrefEditor.putString("cmdRetries", "2");
        mL.publicPrefEditor.putBoolean("stopNMEA", true);
        mL.publicPrefEditor.putBoolean("stopLOG", false);
        mL.publicPrefEditor.putString("downBlockSize", "2048");
        mL.publicPrefEditor.commit();
        mL.goSleep(250);

        //create app fo;ders in Downlaod folder
        OK = true;
        mL.basePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mL.basePathName);
        if (!mL.basePath.exists())
            OK = mL.basePath.mkdir();
        if (!OK) {
            mL.showToast(String.format("ERROR - create %1$s failed", mL.basePathName));
        }

        mL.gpxPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mL.gpxPathName);
        if (!mL.gpxPath.exists())
            OK = mL.gpxPath.mkdir();
        if (!OK) {
            mL.showToast(String.format("ERROR - create %1$s failed", mL.gpxPathName));
        }

        mL.binPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mL.binPathName);
        if (!mL.binPath.exists())
            OK = mL.binPath.mkdir();
        if (!OK) {
            mL.showToast(String.format("ERROR - create %1$s failed", mL.binPathName));
        }

        mL.epoPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mL.epoPathName);
        // make sure mtkutility/bin directory exists - create if it is missing
        if (!mL.epoPath.exists())
            OK = mL.epoPath.mkdir();
        if (!OK) {
            mL.showToast(String.format("ERROR - create %1$s failed", mL.epoPathName));
        }
    }//initialRun()

    public void testException(int Int) {
        // call with Int = 0
        int res = 1 / Int;
    }//testException()

    public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
        private Activity mContext;
        private myLibrary mL;

        public CustomExceptionHandler(Activity context) {
            mContext = context;
        }

        public void uncaughtException(Thread t, Throwable ex) {
            mL = Main.mL;
            OK = true;
            if (!mL.logFileIsOpen) {
                OK = mL.openLog();
            }
            if (OK) {
                mL.mLog(mL.VB0, String.format("%s**** CustomExceptionHandler.uncaughtException()%s", mL.NL, mL.NL));
                mL.buildCrashReport(ex);
            }
        }
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mL.mLog(mL.VB0, "Main.SpinnerActivity.onItemSelected()");
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
        }

        public void onNothingSelected(AdapterView<?> parent) {
            mL.mLog(mL.VB0, "Main.SpinnerActivity.onNothingSelected()");
            // Another interface callback
        }
    }
}
