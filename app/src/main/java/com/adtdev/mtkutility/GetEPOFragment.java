package com.adtdev.mtkutility;
/**
 * @author Alex Tauber
 * <p>
 * This file is part of the open source Android app MTKutility2. You can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 3 of the License. This extends to files included that were authored by
 * others and modified to make them suitable for this app. All files included were subject to
 * open source licensing.
 * <p>
 * MTKutility2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You can review a copy of the
 * GNU General Public License at http://www.gnu.org/licenses.
 * <p>
 * GetEPOFragment
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.adtdev.fileChooser.FileChooser;

import static android.app.Activity.RESULT_OK;

public class GetEPOFragment extends Fragment {

    private myLibrary mL;

    private static final int BYPASS_SELECT = 0;
    private static final int LOCAL_FILE = 1;
    private static final int FTP_FILE = 2;
    private TextView FTPurl;
    private TextView FTPuser;
    private TextView FTPpswd;
    private Button btnFTPsave;
    private Button btnFTPsel;
    private Button btnFTPadd;
    private Button btnFTPfile;
    private Button btnLCLfile;
    private TextView FTPfile;
    private TextView LCLfile;
    private Button btnFTPapnd;
    private Button btnFTPdnld;
    private TextView HTPurl;
    private Button btnHTPsave;
    private Button btnHTPdnld;
    private Button btnChkDl;
    private EditText aFTPdesc;
    private EditText aFTPip;
    private EditText aFTPuser;
    private EditText aFTPpswd;

    private Intent intent;
    private boolean FTPappend;
    private String startPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/mtkutility/epo";
    private String localFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mL = Main.mL;
        mL.mLog(mL.VB0, "GetEPOFragment.onCreateView()");

        View rootView = inflater.inflate(R.layout.getepo, container, false);
        FTPurl = rootView.findViewById(R.id.FTPip);
        FTPuser = rootView.findViewById(R.id.FTPuser);
        FTPpswd = rootView.findViewById(R.id.FTPpswd);
        FTPfile = rootView.findViewById(R.id.FTPfile);
        LCLfile = rootView.findViewById(R.id.LCLfile);
        HTPurl = rootView.findViewById(R.id.HTPurl);

        btnFTPsel = rootView.findViewById(R.id.btnFTPsel);
        btnFTPsel.setTransformationMethod(null);
        btnFTPsel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnFTPsel.getText() + " pressed");
                selectFTPurl();
            }
        });

        btnFTPsave = rootView.findViewById(R.id.btnFTPsave);
        btnFTPsave.setTransformationMethod(null);
        btnFTPsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnFTPsave.getText() + " pressed");
                saveFTPpreferences();
            }
        });

        btnFTPadd = rootView.findViewById(R.id.btnFTPadd);
        btnFTPadd.setTransformationMethod(null);
        btnFTPadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnFTPadd.getText() + " pressed");
                addFTPurl();
            }
        });

//        private Button btnFTPfile;
        btnFTPfile = rootView.findViewById(R.id.btnFTPfile);
        btnFTPfile.setTransformationMethod(null);
        btnFTPfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnFTPfile.getText() + " pressed");
                doFTPfileSelect();
            }
        });

//        private Button btnLCLfile;
        btnLCLfile = rootView.findViewById(R.id.btnLCLfile);
        btnLCLfile.setTransformationMethod(null);
        btnLCLfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnLCLfile.getText() + " pressed");
                doLCLfileSelect();
            }
        });

//        private Button btnFTPapnd;
        btnFTPapnd = rootView.findViewById(R.id.btnFTPapnd);
        btnFTPapnd.setTransformationMethod(null);
        btnFTPapnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnFTPapnd.getText() + " pressed");
                doFTPappend();
            }
        });

//        private Button btnFTPdnld;
        btnFTPdnld = rootView.findViewById(R.id.btnFTPdnld);
        btnFTPdnld.setTransformationMethod(null);
        btnFTPdnld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnFTPdnld.getText() + " pressed");
                doFTPdownload();
            }
        });


//        private Button btnHTPsave;
        btnHTPsave = rootView.findViewById(R.id.btnHTPsave);
        btnHTPsave.setTransformationMethod(null);
        btnHTPsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnHTPsave.getText() + " pressed");
                saveHTPpreferences();
            }
        });

        btnHTPdnld = rootView.findViewById(R.id.btnHTPdnld);
        btnHTPdnld.setTransformationMethod(null);
        btnHTPdnld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnHTPdnld.getText() + " pressed");
                new HTPdownld(getActivity()).execute();
            }
        });

//        private Button btnChkDl;
        btnChkDl = rootView.findViewById(R.id.btnChkDl);
        btnChkDl.setTransformationMethod(null);
        btnChkDl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetEPOFragment - button " + btnChkDl.getText() + " pressed");
                doLOCALview();
            }
        });
        btnFTPapnd.setEnabled(false);
        btnFTPdnld.setEnabled(false);
//        btnHTPdnld.setEnabled(false);
        return rootView;
    }//onCreateView()

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mL.mLog(mL.VB0, "GetEPOFragment.onViewCreated()");
        String FTPipD = "60.248.237.25";
        String FTPuserD = "tsi0001";
        String FTPpswdD = "tweyet";
        String FTPfileD = "MTK7d.EPO";
        String HTPurlD = "http://epodownload.mediatek.com/EPO.DAT"; // http://epodownload.mediatek.com/EPO.MD5
        FTPurl.setText(mL.appPrefs.getString("FTPurl", FTPipD), TextView.BufferType.NORMAL);
        FTPuser.setText(mL.appPrefs.getString("FTPuser", FTPuserD), TextView.BufferType.NORMAL);
        FTPpswd.setText(mL.appPrefs.getString("FTPpswd", FTPpswdD), TextView.BufferType.NORMAL);
        HTPurl.setText(mL.appPrefs.getString("HTPurl", HTPurlD));
    }//onViewCreated()

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // determine which child activity is calling us back.
        //               String Name = data.getStringExtra("GetFileName");
//               String Path = data.getStringExtra("GetPath");

        switch (requestCode) {
            case LOCAL_FILE:
                if (resultCode == RESULT_OK) {
                    localFile = data.getStringExtra("GetPath");
                    LCLfile.setText(data.getStringExtra("GetFileName"));
                    if (!FTPfile.getText().toString().matches("EPO file name"))
                        btnFTPapnd.setEnabled(true);
                }
                break;
            case FTP_FILE:
                if (resultCode == RESULT_OK) {
                    FTPfile.setText(data.getStringExtra("GetFileName"));
                    btnFTPdnld.setEnabled(true);
                    if (!LCLfile.getText().toString().matches("local file name"))
                        btnFTPapnd.setEnabled(true);
                }
                break;
            default:
                break;
        }
        if (requestCode == LOCAL_FILE) {
        }
    }//onActivityResult()

    private void addFTPurl() {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.addurl, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mL.mContext);
        dialog.setView(dialogView)
                .setTitle("Enter FTP site data")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Gson gson = new Gson();
                        String response = mL.appPrefs.getString(myLibrary.urlKey, "");
                        ArrayList<urlModel> sitesList = gson.fromJson(response, new TypeToken<ArrayList<urlModel>>() {
                        }.getType());
                        aFTPdesc = dialogView.findViewById(R.id.aFTPdesc);
                        aFTPip = dialogView.findViewById(R.id.aFTPip);
                        aFTPuser = dialogView.findViewById(R.id.aFTPuser);
                        aFTPpswd = dialogView.findViewById(R.id.aFTPpswd);
                        String ipn = aFTPdesc.getText().toString();
                        String url = aFTPip.getText().toString();
                        String usr = aFTPuser.getText().toString();
                        String psw = aFTPpswd.getText().toString();
                        sitesList.add(new urlModel(ipn, url, usr, psw));
                        gson = new Gson();
                        String json = gson.toJson(sitesList);
                        mL.appPrefEditor.putString(myLibrary.urlKey, json);
                        mL.appPrefEditor.commit();
                    }
                }).show();

    }//addFTPurl()

    private void doFTPappend(){
        FTPappend = true;
        intent = new Intent(getActivity(), FileChooser.class);
        intent.putExtra("method", myLibrary.doFTPdownld);
        intent.putExtra("ftpURL", FTPurl.getText().toString());
        intent.putExtra("ftpName", FTPuser.getText().toString());
        intent.putExtra("ftpPswd", FTPpswd.getText().toString());
        intent.putExtra("ftpPort", "21");
        intent.putExtra("srceFN", FTPfile.getText().toString());
        intent.putExtra("destFN", localFile);
        intent.putExtra("append", FTPappend);
        new FTPdownld(getActivity()).execute();

    }//doFTPappend()

    private void doFTPdownload(){
        String curFunc = "GetEPOFragment.doFTPdownload()";
        mL.mLog(mL.VB0, curFunc);
        curFunc = String.format("+++ %1$s +++",curFunc);

        boolean OK = true;
        mL.epoPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mL.epoPathName);
        // make sure mtkutility/bin directory exists - create if it is missing
        if (!mL.epoPath.exists()) {
            OK = mL.epoPath.mkdirs();
        }
        if (!OK) {
            mL.mLog(mL.ABORT, String.format("%1$s aborting - create %2$s failed +++", curFunc, mL.epoPathName));
            return;
        }
        mL.epoPath = new File(mL.epoPath.toString(), FTPfile.getText().toString());
        if (!FTPappend && mL.epoPath.exists()) {
            mL.epoPath.delete();
        }

        FTPappend = false;
        intent = new Intent(getActivity(), FileChooser.class);
        intent.putExtra("method", myLibrary.doFTPdownld);
        intent.putExtra("ftpURL", FTPurl.getText().toString());
        intent.putExtra("ftpName", FTPuser.getText().toString());
        intent.putExtra("ftpPswd", FTPpswd.getText().toString());
        intent.putExtra("ftpPort", "21");
        intent.putExtra("srceFN", FTPfile.getText().toString());
        intent.putExtra("destFN", mL.epoPath.toString());
        intent.putExtra("append", FTPappend);
        new FTPdownld(getActivity()).execute();
    }//doFTPdownload()

    private void doFTPfileSelect(){
        mL.mLog(mL.VB0, "GetEPOFragment.doFTPfileSelect()");
        intent = new Intent(getActivity(), FileChooser.class);
        intent.putExtra("method", myLibrary.doFTPselect);
        intent.putExtra("ftpURL", FTPurl.getText().toString());
        intent.putExtra("ftpName", FTPuser.getText().toString());
        intent.putExtra("ftpPswd", FTPpswd.getText().toString());
        intent.putExtra("ftpPort", "21");
        startActivityForResult(intent, FTP_FILE);
    }//doFTPfileSelect()

    private void doLCLfileSelect(){
        intent = new Intent(getActivity(), FileChooser.class);
        intent.putExtra("method", myLibrary.doLOCAL);
        intent.putExtra("root", "/storage");
        intent.putExtra("start", startPath);
        intent.putExtra("nofolders", false);
        intent.putExtra("showhidden", false);
        startActivityForResult(intent, LOCAL_FILE);
    }//doLCLfileSelect()

    private void doLOCALview() {
        mL.mLog(mL.VB0, "GetEPOFragment.doLOCALview()");
        intent = new Intent(getActivity(), FileChooser.class);
        intent.putExtra("method", myLibrary.doLOCAL);
//        intent.putExtra("root", "/storage");
        intent.putExtra("root", startPath);
        intent.putExtra("start", startPath);
        intent.putExtra("nofolders", false);
        intent.putExtra("showhidden", false);
        startActivityForResult(intent, BYPASS_SELECT);
    }//doLOCALview()

    private void saveFTPpreferences() {
        mL.mLog(mL.VB0, "GetEPOFragment.saveFTPpreferences()");
        String tmp;
        tmp = FTPurl.getText().toString();
        mL.appPrefEditor.putString("FTPurl", tmp);
        tmp = FTPuser.getText().toString();
        mL.appPrefEditor.putString("FTPuser", tmp);
        tmp = FTPpswd.getText().toString();
        mL.appPrefEditor.putString("FTPpswd", tmp);
        mL.appPrefEditor.commit();
        mL.showToast(getString(R.string.saved));
    }//saveFTPpreferences()

    private void saveHTPpreferences() {
        mL.mLog(mL.VB0, "GetEPOFragment.saveHTPpreferences()");
        mL.appPrefEditor.putString("HTPurl", HTPurl.getText().toString());
        mL.appPrefEditor.commit();
        mL.showToast(getString(R.string.saved));
    }//saveHTPpreferences()

    private void selectFTPurl() {
//        ArrayList<urlModel> sitesList = new ArrayList();

        final ArrayList<String> selList = new ArrayList<String>();
        Gson gson = new Gson();
        String response = mL.appPrefs.getString(myLibrary.urlKey, "");
        final ArrayList<urlModel> sitesList = gson.fromJson(response,
                new TypeToken<ArrayList<urlModel>>(){}.getType());

        for (urlModel site : sitesList) {
            selList.add(site.getDesc());
        }
        CharSequence[] cs = selList.toArray(new CharSequence[selList.size()]);
        int setChk = -1;
        AlertDialog.Builder dialog = new AlertDialog.Builder(mL.mContext);
        dialog.setTitle("Select Site")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setSingleChoiceItems(cs,setChk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int idx) {
                        urlModel obj = sitesList.get(idx);
                        FTPurl.setText(obj.getURL());
                        FTPuser.setText(obj.getUSER());
                        FTPpswd.setText(obj.getPSWD());
                        dialog.dismiss();
                    }
                })
                .show();
    }//selectFTPurl()

    private class FTPdownld extends AsyncTask<Void, String, Void> {
        private ProgressDialog dialog = new ProgressDialog(mL.mContext);
        private Context mContext;
        private String Surl, epoFN, tmp;
        private String[] parms;
        private boolean OK;
        private int ix;
        private File epoPath;


        public FTPdownld(Context context) {
            mL.mLog(mL.VB1, "GetEPOFragment.FTPdownld.HTPdownld()");
            mContext = context;
        }//HTPdownld()

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1, "GetEPOFragment.FTPdownld.onPreExecute()");
//            this.dialog.setMessage(getString(R.string.working));
//            this.dialog.show();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            mL.mLog(mL.VB1, "GetEPOFragment.FTPdownld.doInBackground()");

            startActivityForResult(intent, BYPASS_SELECT);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB1, "GetEPOFragment.FTPdownld.onPostExecute()");
            if (dialog.isShowing()) dialog.dismiss();
        }//onPostExecute()

    }//class FTPdownld

    private class HTPdownld extends AsyncTask<Void, String, Void> {
        private ProgressDialog dialog = new ProgressDialog(mL.mContext);
        private Context mContext;
        private String Surl, epoFN, tmp;
        private String[] parms;
        private boolean OK;
        private int ix;
        private File epoPath;
        public String epoPathName = "mtkutility/epo";

        public HTPdownld(Context context) {
            mL.mLog(mL.VB1, "GetEPOFragment.HTPdownld.HTPdownld()");
            mContext = context;
        }//HTPdownld()

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1, "GetEPOFragment.HTPdownld.onPreExecute()");
            this.dialog.setMessage(getString(R.string.working));
            this.dialog.show();
            Surl = HTPurl.getText().toString();
            ix = Surl.lastIndexOf("/");
            epoFN = Surl.substring(ix + 1);
            OK = true;
            epoPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), epoPathName);
            // make sure mtkutility/bin directory exists - create if it is missing
            if (!epoPath.exists()) {
                OK = epoPath.mkdirs();
            }
            if (!OK) {
                return;
            }
            epoPath = new File(epoPath.toString(), epoFN);
            if (epoPath.exists()) {
                epoPath.delete();
            }
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            mL.mLog(mL.VB1, "GetEPOFragment.HTPdownld.doInBackground()");
            if (!OK) {
                return null;
            }
            try {
                URL url = new URL(Surl);
                HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
                urlconn.setRequestMethod("GET");
                urlconn.setInstanceFollowRedirects(true);
                urlconn.connect();
                InputStream in = urlconn.getInputStream();
                FileOutputStream out = new FileOutputStream(epoPath.toString());
                int read;
                byte[] buffer = new byte[4096];
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                out.close();
                in.close();
                urlconn.disconnect();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        //        @Override
        //String values expected:  Action, Style, message, percent
        protected void onProgressUpdate(String... values) {
            mL.mLog(mL.VB1, "GetEPOFragment.HTPdownld.onProgressUpdate()");
//            mTv.append(msg + mL.NL);
//            mSv.fullScroll(View.FOCUS_DOWN);
        }

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB1, "GetEPOFragment.HTPdownld.onPostExecute()");
            if (dialog.isShowing()) dialog.dismiss();
            mL.showToast("HTTP " + getString(R.string.dlDone));
        }//onPostExecute()

    }//class HTPdownld

}//class GetEPOFragment
