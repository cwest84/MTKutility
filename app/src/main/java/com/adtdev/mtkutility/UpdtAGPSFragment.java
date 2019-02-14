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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.adtdev.fileChooser.FileChooser;

import static android.app.Activity.RESULT_OK;

public class UpdtAGPSFragment extends Fragment {

    private myLibrary mL;

    private static final int REQUEST_PATH = 1;
    private static final int BUFFER_SIZE = 0x1000;
    private static final int EPO60 = 60;
    private static final int EPO72 = 72;
    private TextView tv1;
    private ScrollView mSv;
    private TextView mTv;
    private ProgressBar mProgress;
    private Button btnEfile;
    private TextView epoFile;
    private Button btnUpdtEPO;
    private Button btnResetEPO;
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private char[] hexChars;
    private File txtFile;
    private boolean fileOpen = false;
    private FileWriter out;

    private boolean ok = true;
    private boolean abort = false;
    private boolean doExtract = false;
    private String epoName, epoPath, msg;
    private int epoType;
    private int epoBlk;
    private int epoSeq = 0;
    private int epoPackets;
    private int maxBytes;
    private int maxPackets;
    private int buflen;
    private byte[] epo60 = new byte[191];
    private byte[] epo72 = new byte[227];
    private byte[] epoCMD;
    private byte[] rbuf = new byte[4096];
    private byte[] extract;
//    private byte[] epoDebug;
//    private int dx = 0;
    private FileInputStream is;
    private int bytesRead;

    private byte[] epoBytes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mL = Main.mL;
        mL.mLog(mL.VB0, "UpdtAGPSFragment.onCreateView()");

        View rootView = inflater.inflate(R.layout.updtagps, container, false);
        tv1 = rootView.findViewById(R.id.tv1);
        mSv = rootView.findViewById(R.id.mSv);
        mTv = rootView.findViewById(R.id.mTv);
        mProgress = getActivity().findViewById(R.id.circularProgressbar);
        epoFile = rootView.findViewById(R.id.epoFile);

        btnEfile = rootView.findViewById(R.id.btnEfile);
        btnEfile.setTransformationMethod(null);
        btnEfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetLogFragment - button " + btnEfile.getText() + " pressed");
                epoName = "";
                epoPath = "";
                btnUpdtEPO.setEnabled(false);
                mTv.setText("");
                epoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/mtkutility/epo";
                Intent intent = new Intent(getActivity(), FileChooser.class);
                intent.putExtra("method", myLibrary.doLOCAL);
                intent.putExtra("root", "/storage");
                intent.putExtra("start", epoPath);
                intent.putExtra("nofolders", false);
                intent.putExtra("showhidden", false);
                startActivityForResult(intent, REQUEST_PATH);
            }
        });

        btnUpdtEPO = rootView.findViewById(R.id.btnUpdtEPO);
        btnUpdtEPO.setTransformationMethod(null);
        btnUpdtEPO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetLogFragment - button " + btnUpdtEPO.getText() + " pressed");
                new UpdtAGPSFragment.updateAGPS(getActivity()).execute();
            }
        });

        btnResetEPO = rootView.findViewById(R.id.btnResetEPO);
        btnResetEPO.setTransformationMethod(null);
        btnResetEPO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetLogFragment - button " + btnResetEPO.getText() + " pressed");
                new UpdtAGPSFragment.resetEPO(getActivity()).execute();
            }
        });


        return rootView;
    }//onCreateView()

    public void onViewCreated(View view, Bundle savedInstanceState) {
//        mL.mLog(mL.VB0, "UpdtAGPSFragment.onViewCreated()");
        btnUpdtEPO.setEnabled(false);
        buildPackets();
        mTv.append(mL.strAGPS + mL.NL);
        scrollDown();
    }//onViewCreated()

    private void buildPackets() {
        mL.mLog(mL.VB0, "UpdtAGPSFragment.buildPackets()");
        int i;
        // build EPO binary packet type 722
        epo60 = new byte[191];
        //initialize SAT data and variable bytes
        for (i = 6; i < 189; i++) {
            epo60[i] = (byte) 0x00;
        }
        //fill static bytes
        epo60[0] = (byte) 0x04; //preamble - 2 bytes
        epo60[1] = (byte) 0x24;
        epo60[2] = (byte) 0xBF; //packet length - 2 bytes
        epo60[3] = (byte) 0x00;
        epo60[4] = (byte) 0xD2; //command ID - 2 bytes
        epo60[5] = (byte) 0x02;
        epo60[189] = (byte) 0x0D; // carriage return
        epo60[190] = (byte) 0x0A; // line feed


        // build EPO binary packet type 723
        epo72 = new byte[227];
        //initialize SAT data and variable bytes
        for (i = 6; i < 225; i++) {
            epo72[i] = (byte) 0x00;
        }
        //fill static bytes
        epo72[0] = (byte) 0x04; //preamble - 2 bytes
        epo72[1] = (byte) 0x24;
        epo72[2] = (byte) 0xE3; //packet length - 2 bytes
        epo72[3] = (byte) 0x00;
        epo72[4] = (byte) 0xD3; //command ID - 2 bytes
        epo72[5] = (byte) 0x02;
        epo72[225] = (byte) 0x0D; // carriage return
        epo72[226] = (byte) 0x0A; // line feed
        //initialize SAT data bytes
        for (i = 6; i < 225; i++) {
            epo72[i] = (byte) 0x00;
        }
    }//buildPackets()

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // determine which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                epoName = data.getStringExtra("GetFileName");
                epoPath = data.getStringExtra("GetPath");
                if (checkEPOfile()) {
                    btnUpdtEPO.setEnabled(true);
                }
                epoFile.setText(epoName);
            }
        }
    }//onActivityResult()

    private boolean checkEPOfile() {
        try {
            File epoFILE = new File(epoPath);
            epoBytes = new byte[(int) epoFILE.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(epoFILE));
            dis.readFully(epoBytes);

//            is = new FileInputStream(epoPath);
//            epoBytes = IOUtils.toByteArray(is);
            ok = determinetype();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok;
    }//checkEPOfile()

    private boolean determinetype() {
        ok = false;
        if (epoBytes[0] == epoBytes[EPO60] && epoBytes[1] == epoBytes[EPO60 + 1] && epoBytes[2] == epoBytes[EPO60 + 2]) {
            if ((epoBytes.length % 1920) == 0) {
                maxBytes = mL.AGPSsize * 4 * 32 * EPO60;
                if (maxBytes > epoBytes.length) maxBytes = epoBytes.length;
                maxPackets = maxBytes / EPO60;
                epoType = EPO60;
                epoBlk = 187;
                epoCMD = new byte[191];
                epoCMD = epo60;
                epoPackets = epoBytes.length / EPO60;
                msg= epoBytes.length + " bytes " + getString(R.string.epo60);
                ok = true;
            } else {
                ok = false;
            }
        } else if (epoBytes[0] == epoBytes[EPO72] && epoBytes[1] == epoBytes[EPO72 + 1] && epoBytes[2] == epoBytes[EPO72 + 2]) {
            if ((epoBytes.length % 2304) == 0) {
                maxBytes = mL.AGPSsize * 4 * 32 * EPO72;
                if (maxBytes > epoBytes.length) maxBytes = epoBytes.length;
                maxPackets = maxBytes / EPO72;
                epoType = EPO72;
                epoBlk = 223;
                epoCMD = new byte[227];
                epoCMD = epo72;
//                epoDebug = new byte[216];
                epoPackets = epoBytes.length / EPO72;
                msg =epoBytes.length + " bytes " + getString(R.string.epo72);
                ok = true;
            } else {
                ok = false;
            }
        }
        if (ok) {
            mTv.append(msg + " = " + epoPackets + " SETs\n");
            mTv.append("processing " + maxPackets + " SETs\n");
            scrollDown();
        } else {
            mTv.setText(getString(R.string.badEPO) + "\n");
            scrollDown();
            Toast.makeText(getActivity(), getString(R.string.badEPO), Toast.LENGTH_LONG).show();
        }
        return ok;
    }//determinetype()

    private void getEPOsetting(int iff) {
        //update AGPS info
        ok = false;
        int rpt = 5;
        while (rpt > 0 && !ok) {
            mL.parms = mL.mtkCmd("PMTK607", "PMTK707", mL.cmdTimeOut);
            mL.goSleep(250);
            rpt--;
            if (mL.parms == null) continue;
            switch (iff) {
                case 0:
                    if (Integer.valueOf(mL.parms[1]) != 0) ok = true;
                    break;
                case 1:
                    if (Integer.valueOf(mL.parms[1]) == 0) ok = true;
                    break;
            }
        }
        mL.strAGPS = mL.parms[1] + " EPO sets";
        if (Integer.valueOf(mL.parms[1]) > 0) {
            Date dd = mL.dateCalc(Integer.valueOf(mL.parms[4]), Integer.valueOf(mL.parms[5]));
            mL.strAGPS = mL.strAGPS + " expires " + mL.SDF.format(dd);
            mL.fillGPSstats();
        }
    }//getEPOsetting()

    private void scrollDown(){
        final ScrollView scrollView = getActivity().findViewById(R.id.mSv);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private class resetEPO extends AsyncTask<Void, String, Void> {
        private Context mContext;
        ProgressDialog dialog;

        public resetEPO(Context context) {
            mL.mLog(mL.VB0, "UpdtAGPSFragment.resetEPO.resetEPO()");
            mContext = context;
        }//updateAGPS()

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB1, "UpdtAGPSFragment.resetEPO.onPreExecute()");
            dialog = new ProgressDialog(mL.mContext);
            dialog.setMessage(getString(com.adtdev.fileChooser.R.string.working));
            dialog.show();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            mL.mLog(mL.VB1, "UpdtAGPSFragment.resetEPO.doInBackground()");
            //delete the MTK logger EPO data
            ok = false;
            int rpt = 10;
            while (rpt > 0 && !ok) {
                mL.parms = mL.mtkCmd("PMTK127", "PMTK001,127", mL.cmdTimeOut);
                if (mL.parms != null) ok = true;
                mL.goSleep(500);
                rpt--;
            }

            if (ok){
                int result = Integer.valueOf(mL.parms[2]);
                if (result == 3) {
                    getEPOsetting(1);
                }
            }

            return null;
        }//doInBackground()

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB1, "UpdtAGPSFragment.resetEPO.onPostExecute()");
            btnEfile.setEnabled(true);
            btnUpdtEPO.setEnabled(false);
            btnResetEPO.setEnabled(true);
            mTv.append(mL.strAGPS + mL.NL);
            scrollDown();
            if (dialog.isShowing()) dialog.dismiss();
        }//onPostExecute()
    }//class resetEPO

    private class updateAGPS extends AsyncTask<Void, String, Void> {
        /*
        NOTE: this module incldues the option of creating a seperate debuf file for the binary traffic
        Set the next variable false to turn this on */
        private boolean doBINdebug = false;
        private ProgressDialog dialog = new ProgressDialog(mL.mContext);
        ProgressBar mProgress = getActivity().findViewById(R.id.circularProgressbar);
        TextView tv1 = getActivity().findViewById(R.id.tv1);
        TextView tv2 = getActivity().findViewById(R.id.tv2);

        final int blkSize = 0x0800;
        final int SATstart = 8;
        private Context mContext;
        private String msg;
        private int epoRead = 0;
        private int CMDix = SATstart;
        private int BUFix = 0;
        private int pct;
        private int bMax = 100;
        private int rereads = 10;
        private String[] parms;
        private Date dstart;
        private Date dend;

        public updateAGPS(Context context) {
            mL.mLog(mL.VB0, "UpdtAGPSFragment.updateAGPS.updateAGPS()");
            mContext = context;
        }//updateAGPS()

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB0, "UpdtAGPSFragment.updateAGPS.onPreExecute()");
            initProgress();
            btnEfile.setEnabled(false);
            btnUpdtEPO.setEnabled(false);
            btnResetEPO.setEnabled(false);
            dstart = new Date();
            mTv.append("AGPS update started " + mL.SDF.format(dstart) + mL.NL);
            scrollDown();
            this.dialog.setMessage(getString(R.string.intializing));
            this.dialog.show();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            mL.mLog(mL.VB0, "UpdtAGPSFragment.updateAGPS.doInBackground()");

            parms = mL.mtkCmd("PMTK607", "PMTK707", mL.cmdTimeOut);
            if (parms != null) {
                if (Integer.valueOf(parms[1]) != 0) {
                    //delete the MTK logger EPO data
                    mL.mtkCmd("PMTK127", "PMTK001,127", mL.cmdTimeOut);
                }
            }

            if (mL.stopNMEA) {
                mL.NMEAstop();
            }
            if (mL.stopLOG) {
                mL.LOGstop();
            }

            //Switch the protocol to BINARY mode
            mL.mLog(mL.VB0, "***************** switching to binary mode *****************");
            mL.sendCommand("PMTK253,1,0");
            //wait for command to take effect
            mL.goSleep(5000);

            if (doBINdebug) openEPOdebugFile();
            mL.mLog(mL.VB2, String.format("+++ EPO blocks start **** %1$d EPO%2$d packets", epoPackets, epoType));
            while (BUFix < maxBytes) {
                mL.mLog(mL.VB1, String.format("+++ EPO blocks loop **** BUFix=%1$d of %2$d ***", BUFix, maxBytes));
                for (int lx = 0; lx < epoType; lx++) {
                    epoCMD[CMDix] = epoBytes[BUFix];
                    CMDix++;
                    BUFix++;
                }
                mL.mLog(mL.VB2, String.format("+++ EPO blocks loop **** epoRead=%1$d of %2$d ***", epoRead, maxPackets));
                epoRead++;
                if (CMDix > epoBlk) {
                    if (dialog.isShowing()) dialog.dismiss();
                    // 3 blocks of EPO have been transferred - time to send binary command
                    // set binary command sequence number
                    ok = false;
                    sendEPOcmd();
                    if (!ok) {
                        msg = "ABORTING - no bin reply received ***";
                        mL.mLog(mL.VB0, "+++ EPO blocks loop **** " + msg);
                        break;
                    }
                    CMDix = SATstart;
                    epoSeq++;
                }
            }
            //check SETs trnasferred - send las record if less than 3
            if (CMDix < epoBlk) {
                mL.mLog(mL.VB2, String.format("+++ EPO blocks loop **** last SETS-CMDix=%1$d epoBlk=%2$d ***", CMDix, epoBlk));
                sendEPOcmd();
            }
            //send end of records command
            for (int i = SATstart; i < epoBlk; i++) {
                epoCMD[i] = 0x00;
            }
            epoCMD[6] = (byte) 0xFF;
            epoCMD[7] = (byte) 0xFF;
            epoCMD[epoBlk + 1] = (byte) 0x00;
            //set packet checksum - exclusive OR of bytes between the preamble and checksum
            for (int i = 2; i < epoBlk + 1; i++) {
                epoCMD[epoBlk + 1] ^= epoCMD[i];
            }
            mL.mLog(mL.VB1, String.format("+++ EPO blocks loop **** sending end packet ****"));
            mL.sendBytes(epoCMD);
            mL.goSleep(5000);

            //end biuanary mode
            mL.mLog(mL.VB0, "***************** switching to normal mode *****************");
            mL.sendBytes(myLibrary.binPMTK253);

            //update AGPS info
            if (ok) getEPOsetting(0);

            if (mL.stopNMEA) {
                mL.NMEAstart();
            }
            return null;
        }//doInBackground()

        private void sendEPOcmd() {
            mL.mLog(mL.VB2, "UpdtAGPSFragment.updateAGPS.sendEPOcmd()");
            epoCMD[6] = (byte) (epoSeq & 0xFF);
            epoCMD[7] = (byte) ((epoSeq >> 8) & 0xFF);
            epoCMD[epoBlk + 1] = (byte) 0x00;
            //set packet checksum - exclusive OR of bytes between the preamble and checksum
            for (int i = 2; i < epoBlk + 1; i++) {
                epoCMD[epoBlk + 1] ^= epoCMD[i];
            }
            ok = false;
            mL.sendBytes(epoCMD);
            if (doBINdebug) writeHEX(">", epoCMD);
            rereads = 20;
            while (rereads > 0) {
                rbuf = mL.readBytes(mL.cmdTimeOut * 4);
                if (rbuf == null || rbuf.length == 0) {
                    msg = "ABORTING - no bin reply received ***";
                    mL.mLog(mL.VB0, "+++ EPO blocks loop **** rbuf is null");
                    break;
                }
                mL.mLog(mL.VB2, String.format("+++ EPO blocks loop **** rereads=%1$d  rbuf.length=%2$d", rereads, rbuf.length));
                if (doBINdebug) writeHEX(Integer.toString(rbuf.length) + "<", rbuf);
                for (int j = 0; j < rbuf.length; j++) {
                    // Check if this is the start of a new message
                    if ((!doExtract) && (rbuf[j] == 0x04)) {
                        mL.mLog(mL.VB2, "+++ EPO blocks loop **** doExtract=true");
                        doExtract = true;
                        buflen = 0;
                        extract = new byte[12];
                    }
                    if (doExtract) {
                        extract[buflen] = rbuf[j];
                        if ((buflen == 1) && (extract[buflen] != 0x24)) {
                            mL.mLog(mL.VB2, "+++ EPO blocks loop **** doExtract=false");
                            doExtract = false;
                        }
                        buflen++;
                        if (buflen > 11) {
                            mL.mLog(mL.VB2, String.format("+++ EPO blocks loop **** buflen>11, doExtract=false, rereads=%1$d", rereads) );
                            doExtract = false;
                            mL.mLog(mL.VB2,bytesToHex(extract));
                            if (doBINdebug) writeHEX("<<", extract);
                            if (extract[6] == epoCMD[6] && extract[7] == epoCMD[7]) { //received reply for EPO send
                                if (extract[8] == 0x01) {
                                    mL.mLog(mL.VB2, "+++ EPO blocks loop **** record accepted");
                                    ok = true;
                                    publishProgress(" ");
                                    rereads = 1;
                                    //clear EPO sets for next loop to have correctly filled record when less than 3 sets left
                                    for (int i = SATstart; i < epoBlk; i++) {
                                        epoCMD[i] = 0x00;
                                    }
                                }
                            }

                        }
                    }
                }
                rereads--;
            }
            if (!ok) {
                msg = "ABORTING - no bin reply received ***";
                mL.mLog(mL.VB0, "+++ EPO blocks loop **** " + msg);
                abort = true;
            }
        }//sendEPOcmd()

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB0, "UpdtAGPSFragment.updateAGPS.onPostExecute()");
//            mL.NMEAstart();
            if (abort) {
                mTv.append(msg + "\n");
                scrollDown();
            }

            btnEfile.setEnabled(true);
            btnUpdtEPO.setEnabled(false);
            btnResetEPO.setEnabled(true);
            dend = new Date();
            long diff = dend.getTime() - dstart.getTime();
            diff = diff / 1000;
            long minutes = diff / 60;
            long seconds = diff - (minutes * 60);
            long hours = minutes / 60;
            mTv.append("AGPS update ended " + mL.SDF.format(dend) + mL.NL);
            mTv.append(String.format("AGPS update time %1$d hours, %2$d minutes, %3$d seconds", hours, minutes, seconds) + mL.NL);
            mTv.append(mL.strAGPS + mL.NL);
            scrollDown();
        }//onPostExecute()

        @Override
        protected void onProgressUpdate(String... values) {
//            mL.mLog(mL.VB2, "UpdtAGPSFragment.updateAGPS.onProgressUpdate()");
            pct = (BUFix * 100) / maxBytes;
            if (pct > 100) {
                pct = 100;
            }
            mProgress.setProgress(pct);
            tv1.setText(Integer.toString(epoRead));
        }//onProgressUpdate()

        private void initProgress() {
            mL.mLog(mL.VB1, "UpdtAGPSFragment.updateAGPS.initProgress()");
            mProgress.setProgress(0);   // Main Progress
            mProgress.setMax(100); // Maximum Progress
            mProgress.setSecondaryProgress(100); // Secondary Progress
            tv1.setText("0");
            tv2.setText("SETs");
        }//initProgress()

        private void openEPOdebugFile(){
            mL.mLog(mL.VB3, "UpdtAGPSFragment.updateAGPS.openEPOdebugFile()");
            txtFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "mtkutility/epo.txt");
            if (!txtFile.exists()) {
                try {
                    txtFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                out = new FileWriter (txtFile);
                fileOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }//openEPOdebugFile()

        private void writeHEX(String io, byte[] bytes) {
            String txt = io + bytesToHex(bytes) + ">\n";
            if (fileOpen) {
                try {
                    out.append(txt);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//writeHEX()

        private String bytesToHex(byte[] bytes) {
            hexChars = new char[(bytes.length * 2)];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }//bytesToHex()
    }//class updateAGPS
}//class UpdtAGPSFragment

