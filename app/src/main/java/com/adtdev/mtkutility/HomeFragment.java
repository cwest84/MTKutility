package com.adtdev.mtkutility;
/**
 * @author Alex Tauber
 *
 * This file is part of the open source Android app myLib2. You can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 3 of the License. This extends to files included that were authored by
 * others and modified to make them suitable for this app. All files included were subject to
 * open source licensing.
 *
 * myLib2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You can review a copy of the
 * GNU General Public License at http://www.gnu.org/licenses.
 *
 * HomeFragment provides connect/disconnect to GPS logger, NMEA sentence display control and
 * displays the NEMA sentences while connected.
 */

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements getGPSid.GPSdialogListener {

    private myLibrary mL;
    private String NL;
    private StringBuilder mText = new StringBuilder();
    private static final int TEXT_MAX_SIZE = 5120;

    //layout inflater values
    private View mV;

    private TextView GPstats;
    private ScrollView mSvMsg;
    private TextView msgFrame;
    private TextView txtGGA;
    private TextView txtGLL;
    private TextView txtGSA;
    private TextView txtGSV;
    private TextView txtRMC;
    private TextView txtVTG;
    private TextView txtZDA;
    public static Spinner GGA;
    public static Spinner GLL;
    public static Spinner GSA;
    public static Spinner GSV;
    public static Spinner RMC;
    public static Spinner VTG;
    public static Spinner ZDA;

    private TextView txNMEAinp;
    private TextView txtChkBox;
    private TextView txtRS;
    private Button btnGetGPS;
    private TextView GPSid;
    private Button btnConnect;
    private Button btnPause;
    private Button btnSvNMEA;
    private Button btnNMEAdflt;
    private Button btnCold;
    private Button btnWarm;
    private Button btnHot;
    private Button btnFactory;
    private ScrollView mSvText;
    private TextView mTvText;
    private TextView mEPOinfo;

    private CheckBox cbxInsecure;
    private Context mContext;
    private int valx;
    private int resetCmd;
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mL = Main.mL;
        mL.mLog(mL.VB0, "HomeFragment.onCreateView()");
        NL = mL.NL;
        mContext = mL.mContext;

        mV = inflater.inflate(R.layout.home, container, false);

        GPstats = mV.findViewById(R.id.GPstats);
        txNMEAinp = mV.findViewById(R.id.txNMEAinp);
        txtChkBox = mV.findViewById(R.id.txtChkBox);

        txtGGA = mV.findViewById(R.id.txtGGA);
        txtGLL = mV.findViewById(R.id.txtGLL);
        txtGSA = mV.findViewById(R.id.txtGSA);
        txtGSV = mV.findViewById(R.id.txtGSV);
        txtRMC = mV.findViewById(R.id.txtRMC);
        txtVTG = mV.findViewById(R.id.txtVTG);
        txtZDA = mV.findViewById(R.id.txtZDA);
        txtRS = mV.findViewById(R.id.txtRS);
        GGA = mV.findViewById(R.id.GGA);
        GLL = mV.findViewById(R.id.GLL);
        GSA = mV.findViewById(R.id.GSA);
        GSV = mV.findViewById(R.id.GSV);
        RMC = mV.findViewById(R.id.RMC);
        VTG = mV.findViewById(R.id.VTG);
        ZDA = mV.findViewById(R.id.ZDA);

        GPSid = mV.findViewById(R.id.txtGPSname);
        mSvMsg = mV.findViewById(R.id.mSvMsg);
        msgFrame = mV.findViewById(R.id.msgFrame);

        //show stored GPS device name
        GPSid = mV.findViewById(R.id.txtGPSname);
        mL.GPSmac = mL.appPrefs.getString("GPSmac", "");
        if (mL.GPSmac.length() > 0 && !(mL.mBluetoothAdapter == null)) {
            mL.GPSdevice = mL.mBluetoothAdapter.getRemoteDevice(mL.GPSmac);
            mL.GPSname = mL.GPSdevice.getName();
            GPSid.setText(mL.GPSname);
        }
        //show stored checkbox state
        cbxInsecure = mV.findViewById(R.id.cbxInsecure);
        Boolean BbB = mL.appPrefs.getBoolean("allowInsecure", false);
        mL.allowInsecure = BbB;
        cbxInsecure.setChecked(BbB);

        mSvText = mV.findViewById(R.id.mSvText);
        mTvText = mV.findViewById(R.id.mTvText);

// --------------------------------------button handlers___________________________________________//
        btnSvNMEA = mV.findViewById(R.id.btnSvNMEA);
        btnSvNMEA.setTransformationMethod(null);
        btnSvNMEA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnSvNMEA.getText() + " pressed");
                btnSvNMEA();
            }
        });

        btnGetGPS = mV.findViewById(R.id.btnGetGPS);
        btnGetGPS.setTransformationMethod(null);
        btnGetGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnGetGPS.getText() + " pressed");
                btnGetGPS();
            }
        });

        cbxInsecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ allow insecure checkbox changed");
                mL.appPrefEditor.putBoolean("allowInsecure", isChecked);
                mL.appPrefEditor.commit();
                mL.allowInsecure = isChecked;
            }
        });

        btnConnect = mV.findViewById(R.id.btnConnect);
        btnConnect.setTransformationMethod(null);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnConnect.getText() + " pressed");
                if (mL.GPSname == null  || mL.GPSname.isEmpty()) {
                    mL.showToast(getString(R.string.noGPSselected));
                } else {
                    btnConnect();
                }
            }
        });

        btnNMEAdflt = mV.findViewById(R.id.btnNMEAdflt);
        btnNMEAdflt.setTransformationMethod(null);
        btnNMEAdflt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnNMEAdflt.getText() + " pressed");
                mL.showNMEA = false;
                new defaultNMEA().execute();
            }
        });

        btnHot = mV.findViewById(R.id.btnHot);
        btnHot.setTransformationMethod(null);
        btnHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnHot.getText() + " pressed");
                doReset(101, getString(R.string.btnHot));
            }
        });

        btnWarm = mV.findViewById(R.id.btnWarm);
        btnWarm.setTransformationMethod(null);
        btnWarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnWarm.getText() + " pressed");
                doReset(102, getString(R.string.btnWarm));
            }
        });

        btnCold = mV.findViewById(R.id.btnCold);
        btnCold.setTransformationMethod(null);
        btnCold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnCold.getText() + " pressed");
                doReset(103, getString(R.string.btnCold));
            }
        });

        btnFactory = mV.findViewById(R.id.btnFactory);
        btnFactory.setTransformationMethod(null);
        btnFactory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB1, "+++ HomeFragment.onCreateView() +++ button " + btnFactory.getText() + " pressed");
                doReset(104, getString(R.string.btnFactory));
            }
        });
        setTextSizes();
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.listNMEAshow, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinnerdropdown);
        GGA.setAdapter(adapter);
        GLL.setAdapter(adapter);
        GSA.setAdapter(adapter);
        GSV.setAdapter(adapter);
        RMC.setAdapter(adapter);
        VTG.setAdapter(adapter);
        ZDA.setAdapter(adapter);

        //show log file info in messages frame
        if (mL.aborting){
            // Get all touchable views
            ArrayList<View> layoutButtons = mV.getTouchables();
            // loop through them, if they are instances of Button, disable them.
            for(View v : layoutButtons){
                if( v instanceof Button ) {
                    v.setEnabled(false);
                }
            }
            mL.showToast(mL.errMsg);
            msgFrame.append(mL.errMsg);
            return mV;
        }
        if (mL.logFileIsOpen) {
            msgFrame.append(getText(R.string.created) + mL.logFile.getPath());
            mL.mLog(mL.VB0, "Main.onCreate()");
            mL.nav_Menu.findItem(R.id.nav_Home).setVisible(true);
            mL.nav_Menu.findItem(R.id.nav_Help).setVisible(true);
            mL.nav_Menu.findItem(R.id.nav_About).setVisible(true);
            mL.nav_Menu.findItem(R.id.nav_MakeGPX).setVisible(true);
            mL.nav_Menu.findItem(R.id.nav_GetEPO).setVisible(true);
            mL.nav_Menu.findItem(R.id.nav_eMail).setVisible(true);
        }

        return mV;
    }//onCreateView(LayoutInflater, ViewGroup, Bundle)

    @Override
    public void onPause() {
        super.onPause();
        // do not log here , causes abort on exit
        //stop the AsyncTask
        mL.showNMEA = false;
    }//onPause()

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        mL.mLog(mL.VB0, "HomeFragment.onResume()");
        if (mL.GPSconnected){
            startNMEA();
            setNMEAfields();
            setTextSizes();
            mL.GPstatsTxt = null;
            if (mL.strAGPS.length() > 0) {
                mL.GPstatsTxt = mL.strAGPS + mL.NL;
            }
            if (mL.strGPS.length() > 0) {
                mL.GPstatsTxt = mL.GPstatsTxt + mL.strGPS;
            }
            GPstats.setText(mL.GPstatsTxt);
            btnConnect.setText(getString(R.string.connected));
        }
    }//onResume

    private void btnSvNMEA() {
        mL.mLog(mL.VB0, "HomeFragment - " + btnSvNMEA.getText() + " pressed");
        if (!mL.GPSconnected) {
            mL.showToast(getString(R.string.noConnect));
            return;
        }
        //send output settings to logger
        String cmd = "PMTK314," + mL.GLL + "," + mL.RMC + "," + mL.VTG + "," + mL.GGA + "," + mL.GSA + "," + mL.GSV + "," + "0,0,0,0,0,0,0,0,0,0,0," + mL.ZDA + "," + "0";
        mL.sendCommand(cmd);

        mL.appPrefEditor.putString("NMEAsettings", cmd);
        mL.appPrefEditor.commit();
        mL.showToast(getString(R.string.saved));
    }//btnSvNMEA()

    private void btnGetGPS() {
        mL.mLog(mL.VB0, "HomeFragment - " + btnGetGPS.getText() + " pressed");
        if (!mL.isBTenabled()) {
            return;
        }

        getGPSid dialog = new getGPSid();
        dialog.setTargetFragment(HomeFragment.this, 1);
        dialog.show(getFragmentManager(), "getGPSid");
    }//btnGetGPS()

    private void btnConnect() {
        mL.mLog(mL.VB0, "HomeFragment.btnConnect()");
        String str;
        if (mL.GPSmac == null) {
            mL.showToast(getString(R.string.noGPSselected));
            mL.mLog(mL.VB0, "+++ GPS logger has not been selected");
            return;
        }

        if (mL.GPSconnected) {
            mL.showNMEA = false;
            new disconnect().execute();
        } else {
            new connect().execute();
        }
    }//btnConnect()

    public void doReset(int code, String name){
        mL.mLog(mL.VB0, "HomeFragment.doReset()");
        if (!mL.GPSconnected) {
            mL.showToast(getString(R.string.noConnect));
            return;
        }
        mL.showNMEA = false;
        resetCmd = code;
        resetCMD doreset = new resetCMD();
        doreset.execute();
    }//btnReset()

    @Override
    public void onClick(android.support.v4.app.DialogFragment dialog) {
        mL.mLog(mL.VB0, "HomeFragment.onClick()");
//        mL.GPSmac = mL.appPrefs.getString("GPSmac", null);
        if (mL.GPSmac == null) {
            return;
        }
//        mL.GPSdevice = mL.mBluetoothAdapter.getRemoteDevice(mL.GPSmac);
//        String name = mL.GPSname = mL.GPSdevice.getName();
        mL.GPSdevice = mL.mBluetoothAdapter.getRemoteDevice(mL.GPSmac);
        GPSid.setText(mL.GPSname);
        mL.showToast(mL.GPSname + " " + getString(R.string.selected));
    }//onClick(android.app.DialogFragment dialog)

    @Override
    public void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog) {
        mL.mLog(mL.VB0, "HomeFragment.onDialogNegativeClick()");
        mL.showToast(getText(android.R.string.cancel) + " pressed");
    }//onDialogNegativeClick(android.app.DialogFragment dialog)

    private void setListeners() {
        mL.mLog(mL.VB1, "HomeFragment.setListeners()");
        //set item selected listeners for the NMEA spinners - not done in onCreateView as
        //setOnItemSelectedListener event is triggered when spiinner value is initialized
        GGA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.mLog(mL.VB1, "+++ HomeFragment.setListeners() +++ GGA.setOnItemSelectedListener");
                mL.GGA = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        GLL.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.mLog(mL.VB1, "+++ HomeFragment.setListeners() +++ GLL.setOnItemSelectedListener");
                mL.GLL = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        GSA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.mLog(mL.VB1, "+++ HomeFragment.setListeners() +++ GSA.setOnItemSelectedListener");
                mL.GSA = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        GSV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.mLog(mL.VB1, "+++ HomeFragment.setListeners() +++ GSV.setOnItemSelectedListener");
                mL.GSV = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        RMC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.RMC = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        VTG.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.mLog(mL.VB1, "+++ HomeFragment.setListeners() +++ VTG.setOnItemSelectedListener");
                mL.VTG = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ZDA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mL.mLog(mL.VB1, "+++ HomeFragment.setListeners() +++ ZDA.setOnItemSelectedListener");
                mL.ZDA = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }//setListeners()

    private void setNMEAfields() {
        mL.mLog(mL.VB1, "HomeFragment.setNMEAfields()");
        GLL.setSelection(Integer.parseInt(mL.GLL));
        RMC.setSelection(Integer.parseInt(mL.RMC));
        VTG.setSelection(Integer.parseInt(mL.VTG));
        GGA.setSelection(Integer.parseInt(mL.GGA));
        GSA.setSelection(Integer.parseInt(mL.GSA));
        GSV.setSelection(Integer.parseInt(mL.GSV));
        ZDA.setSelection(Integer.parseInt(mL.ZDA));
    }//setNMEAfields()

    private void setTextSizes(){
        mL.mLog(mL.VB1, "HomeFragment.setTextSizes()");

        int NMEAfont = mL.homeFont-1;
        int epoMSGfont = mL.homeFont-3;
        int btnfont = mL.homeFont-2;

        txtGGA.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);
        txtGLL.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);
        txtGSA.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);
        txtGSV.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);
        txtRMC.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);
        txtVTG.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);
        txtZDA.setTextSize(TypedValue.COMPLEX_UNIT_SP, NMEAfont);

        txtChkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, epoMSGfont+1);
        txNMEAinp.setTextSize(TypedValue.COMPLEX_UNIT_SP, epoMSGfont);
        btnSvNMEA.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        btnNMEAdflt.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        btnGetGPS.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        btnConnect.setTextSize(TypedValue.COMPLEX_UNIT_SP,btnfont);
        btnCold.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        btnWarm.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        btnHot.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        btnFactory.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont);
        txtRS.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnfont-2);
    }//setTextSizes()

    private void startNMEA() {
        mL.mLog(mL.VB0, "HomeFragment.startNMEA()");
        mL.showNMEA = true;
        showNMEA task = new showNMEA();
        task.execute();
    }//startNMEA()

    class showNMEA extends AsyncTask<Void, String, Void> {
        boolean loop = true;

        protected void onPreExecute() {
            mL.mLog(mL.VB1, "HomeFragment.showNMEA.onPreExecute()");
            mL.showNMEAisRunning = true;
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            mL.mLog(mL.VB1, "HomeFragment.showNMEA.doInBackground()");
            String reply = null;

            while (mL.showNMEA) {
//                if (!mL.showNMEA) {
//                    mL.mLog(mL.NORMAL, "+++ HomeFragment.showNMEA.doInBackground() +++ showNMEA is false");
//                    break;
//                }
                try {
                    reply = mL.readString(10);
                } catch (IOException e) {
                    mL.buildCrashReport(e);
                } catch (InterruptedException e) {
                    mL.buildCrashReport(e);
                }
                if (reply != null) {
                    publishProgress(reply);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (mTvText.length() > TEXT_MAX_SIZE) {
                StringBuilder sb = new StringBuilder();
                sb.append(mTvText.getText());
                sb.delete(0, TEXT_MAX_SIZE / 2);
                mTvText.setText(sb);
            }
            mTvText.append(values[0]);
            mText.setLength(0);
            mSvText.fullScroll(View.FOCUS_DOWN);
        }//onProgressUpdate()

        protected void onPostExecute() {
            mL.mLog(mL.VB1, "HomeFragment.showNMEA.onPostExecute()");
            mL.showNMEAisRunning = false;
        }//onPostExecute()
    }//class showNMEA

    public class connect extends AsyncTask<Void, Void, Void> {
        private String str;

        private ProgressDialog dialog = new ProgressDialog(mL.mContext);
        NavigationView navigationView;
        Menu nav_Menu;

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1, "HomeFragment.connect.onPreExecute()");
            this.dialog.setMessage(getString(R.string.connecting));
            this.dialog.show();
            navigationView = getActivity().findViewById(R.id.nav_view);
            nav_Menu = navigationView.getMenu();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... voids) {
            mL.mLog(mL.VB1, "HomeFragment.connect.doInBackground()");
            mL.connect();
            if (mL.GPSconnected) {
                //send text output reset - precautionary to correct fail during binary mode
                mL.sendPMTK253();
                mL.goSleep(250);
                mL.getGPSid();
                //check for AGPS and show expiry when GPS has AGPS
                mL.strAGPS = mL.hasAGPS();
                //check for logger function and show trackpoint count if has
                mL.strGPS = mL.isGPSlogger();
                //make sure logger can display NMEA output
                mL.NMEAgetSetting();
               mL.mLog(mL.VB0, "+++ HomeFragment.connect.doInBackground() +++ Connected *****");
            }
            return null;
        }//doInBackground()

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB1, "HomeFragment.connect.onPostExecute()");
            if (mL.aborting){
                mL.abortApp("");
                return;
            }
            mL.GPstatsTxt = null;
            GPstats.setText("");
            if (mL.GPSconnected) {
                nav_Menu.findItem(R.id.nav_Settings).setVisible(true);
                if (mL.hasAGPS){
                    mL.mLog(mL.VB1, "+++ HomeFragment.connect.onPostExecute() +++ showing GetEPO, CheckEPO, UpdtAGPS");
                    nav_Menu.findItem(R.id.nav_GetEPO).setVisible(true);
//                    nav_Menu.findItem(R.id.nav_CheckEPO).setVisible(true);
                    nav_Menu.findItem(R.id.nav_UpdtAGPS).setVisible(true);
                } else {
                    msgFrame.append(NL + getText(R.string.noAGPS));
                    mL.showToast(mContext.getString(R.string.noAGPS));
                }
                if (mL.isGPSlogger) {
                    //logging method 1=overlap, 2=stop when full
                    //app can only handle stop when full
                    if (mL.recordingMode.equals("2")) {
                        mL.mLog(mL.VB1, "+++ HomeFragment.connect.onPostExecute() +++ showing GetLog, MakeGPX");
                        nav_Menu.findItem(R.id.nav_GetLog).setVisible(true);
                        nav_Menu.findItem(R.id.nav_MakeGPX).setVisible(true);
                    } else {
                        msgFrame.append(NL + getText(R.string.wrongMode));
                        mL.showToast(mContext.getString(R.string.wrongMode));
                    }
                } else {
                    msgFrame.append(NL + getText(R.string.noLog));
                    mL.showToast(mContext.getString(R.string.noLog));
                }

                //get and show NMEA setting
                setListeners();
                setNMEAfields();
                btnConnect.setText(getString(R.string.connected));
                if (!mL.showNMEA) {
                    startNMEA();
                }
                mL.fillGPSstats();
                GPstats.append(mL.GPstatsTxt);
                str = mL.GPSname + " " + mContext.getString(R.string.GPSconnected);
                mL.showToast(str);
                mL.mLog(mL.VB0, "+++ HomeFragment.connect.onPostExecute() +++ " + str);
            }else{
                mL.showToast(mContext.getString(R.string.noConnect));
            }

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            mSvMsg.post(new Runnable() {
                @Override
                public void run() {
                    mSvMsg.fullScroll(View.FOCUS_DOWN);
                }
            });
        }//onPostExecute()
    }//class connect

    public class defaultNMEA extends AsyncTask<Void, Void, Void> {
        String str, strAGPS, strGPS;

        private ProgressDialog dialog = new ProgressDialog(mL.mContext);

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1, "HomeFragment.defaultNMEA.onPreExecute()");
            if (!mL.GPSconnected) {
                mL.showToast(getString(R.string.noConnect));
                return;
            }
            this.dialog.setMessage(getString(R.string.working));
            this.dialog.show();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... voids) {
            mL.mLog(mL.VB1, "HomeFragment.defaultNMEA.doInBackground()");
            if (mL.GPSconnected) {
                mL.mtkCmd("PMTK314,-1","PMTK001", mL.cmdTimeOut);
                mL.NMEAgetSetting();
            }
            return null;
        }//doInBackground()

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB1, "HomeFragment.defaultNMEA.onPostExecute()");
            if (mL.GPSconnected) {
                setNMEAfields();
                if (!mL.showNMEA) {
                    startNMEA();
                }
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }//onPostExecute()
    }//class defaultNMEA


    public class disconnect extends AsyncTask<Void, Void, Void> {
        String str;
        NavigationView navigationView;
        Menu nav_Menu;

        private ProgressDialog dialog = new ProgressDialog(mL.mContext);

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1, "HomeFragment.disconnect.onPreExecute()");
            this.dialog.setMessage(getString(R.string.working));
            this.dialog.show();
            navigationView = getActivity().findViewById(R.id.nav_view);
            nav_Menu = navigationView.getMenu();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mL.mLog(mL.VB1, "HomeFragment.disconnect.doInBackground()");
            mL.disconnect();
            return null;
        }

        //        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB1, "HomeFragment.disconnect.onPostExecute()");
            btnConnect.setText(getString(R.string.disconnected));
            str = String.format(getString(R.string.GPSdisconnected), mL.GPSname);
            mL.showToast(str);
            mL.mLog(mL.VB0, "+++ HomeFragment.disconnect.onPostExecute() +++ " + str);
            nav_Menu.findItem(R.id.nav_GetLog).setVisible(false);
            nav_Menu.findItem(R.id.nav_UpdtAGPS).setVisible(false);
            nav_Menu.findItem(R.id.nav_Settings).setVisible(false);
//            nav_Menu.findItem(R.id.nav_eMail).setVisible(false);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private class resetCMD extends AsyncTask<Void, Void, Integer> {
        private ProgressDialog dialog = new ProgressDialog(mL.mContext);
        String msg;

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1,"HomeFragment.resetCmd.onPreExecute()");
            this.dialog.setMessage(getString(R.string.working));
            this.dialog.show();
        }//onPreExecute()

        @Override
        protected Integer doInBackground(Void... params) {
            mL.mLog(mL.VB1, "HomeFragment.resetCmd.doInBackground()");
            switch(resetCmd){
                case 101:
                    mL.mLog(mL.VB1, "+++ HomeFragment.resetCMD.doInBackground() +++ sending mtkCmd(PMTK101, PMTK010,001)");
                    mL.mtkCmd("PMTK101", "PMTK010,001", mL.cmdTimeOut*2);
                    msg = getString(R.string.btnHot) + " " + getString(R.string.txtRSdone);
                    return resetCmd;
                case 102:
                    mL.mLog(mL.VB1, "+++ HomeFragment.resetCMD.doInBackground() +++ sending mtkCmd(PMTK102, PMTK010,001)");
                    mL.mtkCmd("PMTK102", "PMTK010,001", mL.cmdTimeOut*2);
                    msg = getString(R.string.btnWarm) + " " + getString(R.string.txtRSdone);
                    return resetCmd;
                case 103:
                    mL.mLog(mL.VB1, "+++ HomeFragment.resetCMD.doInBackground() +++ sending mtkCmd(PMTK103, PMTK010,001)");
                    mL.mtkCmd("PMTK103", "PMTK010,001", mL.cmdTimeOut*2);
                    msg = getString(R.string.btnCold) + " " + getString(R.string.txtRSdone);
                    return resetCmd;
                case 104:
                    mL.mLog(mL.VB1, "+++ HomeFragment.resetCMD.doInBackground() +++ sending mtkCmd(PMTK104, PMTK010,001)");
                    mL.mtkCmd("PMTK104", "PMTK010,001", mL.cmdTimeOut*2);
                    msg = getString(R.string.btnFactory) + " " + getString(R.string.txtRSdone);
                    return resetCmd;
            }
            return resetCmd;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mL.mLog(mL.VB1, "HomeFragment.resetCmd.onPostExecute()");
            if (!mL.showNMEA) {
                startNMEA();
            }
            mL.showToast(msg);
            mL.mLog(mL.VB0, "+++ " + msg);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

}
