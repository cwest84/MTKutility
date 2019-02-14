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
 * CheckEPOFragment
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class GetLogFragment extends Fragment {

    private myLibrary mL;

    private String[] parms;

    private View rootView;
    public TextView tv1;
    private ScrollView mSv;
    private TextView mTv;
    public ProgressBar mProgress;
    private Button btnRun;
    private Button btnErase;
    private boolean ok = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mL = Main.mL;
        mL.mLog(mL.VB0, "GetLogFragment.onCreateView()");

        rootView = inflater.inflate(R.layout.getlog, container, false);
//        LogTxt = rootView.findViewById(R.id.LogTxt);
        tv1 = rootView.findViewById(R.id.tv1);
        mSv = rootView.findViewById(R.id.mSv);
        mTv = rootView.findViewById(R.id.mTv);
        mProgress = getActivity().findViewById(R.id.circularProgressbar);

        btnRun = rootView.findViewById(R.id.run);
        btnRun.setTransformationMethod(null);
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetLogFragment - button " + btnRun.getText() + " pressed");
                new logDownload(getActivity()).execute();
            }
        });

        btnErase = rootView.findViewById(R.id.erase);
        btnErase.setTransformationMethod(null);
        btnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "GetLogFragment - button " + btnErase.getText() + " pressed");
                new eraseLog(getActivity()).execute();
            }
        });

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mL.mLog(mL.VB0, "GetLogFragment.onViewCreated()");
        mTv.append(mL.strGPS + mL.NL);
        if (mL.logRecCount < 1) {
            btnRun.setEnabled(false);
//            btnErase.setEnabled(false);
            mL.showToast(String.format(this.getString(R.string.noLogRecs), mL.NL));
        }
    }//onViewCreated()

    private void appendMsg(String msg){
        mTv.append(msg + mL.NL);
        scrollDown();
    }//appendMsg()

    private void scrollDown(){
        final ScrollView scrollView = getActivity().findViewById(R.id.mSv);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }//scrollDown()

    private class logDownload extends AsyncTask<Void, String, Void> {

        ProgressBar mProgress = getActivity().findViewById(R.id.circularProgressbar);
        TextView tv1 = getActivity().findViewById(R.id.tv1);
        TextView tv2 = getActivity().findViewById(R.id.tv2);
        //        final int blkSize = 0x800;
        final int blkSize = mL.downBlockSize;

        private Context mContext;
        private String file_time_stamp;
        private String reply = null;
        private String msg;
        private int bytesRead = 0;
        private int retry = 0;
        private BufferedOutputStream bin_out = null;
        private boolean running = true;
        private int offset = 0;
        private String cmd;
        private BufferedOutputStream binOout = null;
        private String[] sArray;
        private int s3len;
        private int pct;
        private String tmp;
        private int bRead = 0;
        private int bMax = 100;
        private Date dstart;
        private Date dend;

        public logDownload(Context context) {
            mL.mLog(mL.VB1, "GetLogFragment.updateAGPS.updateAGPS()");
            mContext = context;
        }//updateAGPS()

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB0, "GetLogFragment.updateAGPS.onPreExecute()");
            initProgress();
            btnRun.setEnabled(false);
            btnErase.setEnabled(false);
            dstart = new Date();
            mTv.append("Log downlaod started " + mL.SDF.format(dstart) + mL.NL);
            scrollDown();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            String curFunc = "GetLogFragment.updateAGPS.doInBackground()";
            mL.mLog(mL.VB1, curFunc);
            curFunc = String.format("+++ %1$s +++",curFunc);
            byte[] binBytes;

            //create temp output
            mL.binFileIsOpen = mL.BINopen();
            if (!mL.binFileIsOpen){
                mL.aborting = true;
                mL.mLog(mL.ABORT, String.format("%1$s aborting - open %2$s failed +++", curFunc, mL.binFile));
                return null;
            }

            if (mL.stopNMEA) {
                mL.NMEAstop();
            }
            if (mL.stopLOG) {
                mL.LOGstop();
            }
            getBytesToRead();
            mL.resetBuffers();
            bRead = 0;
//            mL.mLog(mL.VB1, String.format("%1$s bRead=%2$d bMax=%3$d ", curFunc, bRead, bMax));
            while ((bRead < bMax)) {
                mL.mLog(mL.VB1, String.format("%1$s bRead=%2$d bMax=%3$d ", curFunc, bRead, bMax));
                int redo  = 0;
                do {binBytes = processBLK();
                    if (mL.aborting) return null;
                    redo += 1;
                } while (!mL.OK && redo < mL.cmdTimeOut);
                if (!mL.OK){
                    mL.aborting = true;
                    mL.mLog(mL.ABORT, String.format("%1$s aborting download after %2$d blocks read +++", curFunc, bRead));
                    return null;
                }
                mL.mLog(mL.VB1, String.format("%1$s received %2$d bytes", curFunc, binBytes.length));
                if (binBytes.length> 0) {
                    try {
                        mL.bOut.write(binBytes);
                        mL.mLog(mL.VB1, String.format("%1$s wrote %2$d bytes to file", curFunc, binBytes.length));
                        offset += binBytes.length;
                        bRead += binBytes.length;
                    } catch (IOException e) {
                        mL.aborting = true;
                        mL.mLog(mL.ABORT, String.format("%1$s aborting download after %d blocks read +++", curFunc, bRead));
                        return null;
                    }
                    publishProgress(" ");
                }
            }
            mL.BINclose();
            if (mL.stopNMEA) {
                mL.NMEAstart();
            }
            return null;
        }//doInBackground()

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB0, "GetLogFragment.updateAGPS.onPostExecute()");
            if (mL.bOut != null) {
                try {
                    mL.bOut.flush();
                    mL.bOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mL.aborting) {
                mL.mLog(mL.VB1, msg);
                return;
            }

            Date dt = new Date();
            File nn = new File(mL.binPath, mL.FDF.format(dt) + ".bin");
            // rename binary file to today's date and time
            mL.binFile.renameTo(nn);
            mTv.append(getText(R.string.created) + nn.toString() + mL.NL);
//            btnErase.setVisibility(View.VISIBLE);
            btnRun.setEnabled(true);
            btnErase.setEnabled(true);
            dend = new Date();
            long diff = dend.getTime() - dstart.getTime();
            diff = diff / 1000;
            long minutes = diff / 60;
            long seconds = diff - (minutes * 60);
            long hours = minutes / 60;
            mTv.append("Log downlaod ended " + mL.SDF.format(dend) + mL.NL);
            mTv.append(String.format("Log downlaod time %1$d hours, %2$d minutes, %3$d seconds", hours, minutes, seconds) + mL.NL);
            scrollDown();
        }//onPostExecute()

//        @Override
        //String values expected:  Action, Style, message, percent
        protected void onProgressUpdate(String... values) {
            pct = (bRead * 100) / bMax;
            if (pct > 100) {pct = 100;}
            mL.mLog(mL.VB1, String.format("+++ onProgressUpdate +++ bRead=%1$d bMax=%2$d  %3$d percent", bRead, bMax, pct));
            mProgress.setProgress(pct);
            tv1.setText(Integer.toString(bRead));
        }//onProgressUpdate()

        void getBytesToRead() {
            String curFunc = "GetLogFragment.updateAGPS.getBytesToRead()";
            mL.mLog(mL.VB1, curFunc);
            curFunc = String.format("+++ %1$s +++",curFunc);

            // Query the RCD_ADDR (data log Next Write Address).
            parms = mL.mtkCmd("PMTK182,2,8", "PMTK182,3,8", mL.cmdTimeOut * 2);
            if (parms == null) {
                mL.aborting = true;
                mL.mLog(mL.ABORT, String.format("%1$s fatal error +++ PMTK182,2,8 failed +++"));
                return;
            }

            bMax = Integer.parseInt(parms[3], 16);
            mL.mLog(mL.VB1, String.format("%1$s next write address: %2$d", curFunc, bMax));
        }//getBytesToRead()

        void initProgress() {
            String curFunc = "GetLogFragment.updateAGPS.initProgress()";
            mL.mLog(mL.VB1, curFunc);
            curFunc = String.format("+++ %1$s +++", curFunc);

            mProgress.setProgress(0);   // Main Progress
            mProgress.setMax(100); // Maximum Progress
            mProgress.setSecondaryProgress(100); // Secondary Progress
            tv1.setText("0");
            tv2.setText("bytes");
        }//initProgress()

        byte[] processBLK() {
            String curFunc = "GetLogFragment.updateAGPS.processBLK()";
            mL.mLog(mL.VB1, curFunc);
            curFunc = String.format("+++ %1$s +++", curFunc);

            for (int i=0; i<=mL.cmdRetries; i++) {

                cmd = String.format("PMTK182,7,%08X,%08X", offset, blkSize);
//            mL.mLog(mL.VB1, "+++ sending " + cmd);
                if (i > 0) {
                    mL.mLog(mL.VB2, String.format("%1$s retrying command for %2$d time", curFunc, i));
                }
                mL.sendCommand(cmd);
                mL.goSleep(250);
//            mL.mLog(mL.VB1, "+++ wating for  PMTK182,8");
                reply = mL.waitForReply("PMTK182,8", mL.cmdTimeOut * 2);
                if (reply.length() != 0) {
                    sArray = reply.split(",");
                    int retAddrs = (int) Long.parseLong(sArray[2], 16);
                    if (retAddrs != offset) {
                        mL.mLog(mL.VB1, String.format("%1$s return address %2$d not equal to request address %3$d", curFunc, retAddrs, offset));
                        mL.OK = false;
                        continue;
                    }

                    //drop ending * and checksum digits
                    sArray[3] = sArray[3].substring(0, sArray[3].length() - 3);
                    s3len = sArray[3].length();
                    mL.mLog(mL.VB1, String.format("%1$s received s3len %2$d bytes", curFunc, s3len));
                    mL.mLog(mL.VB2, String.format("%1$s <%2$s>", curFunc, sArray[3]));
                    // string returned length needs to be twice the blkSize
                    if (s3len % 2 != 0) {
                        mL.mLog(mL.VB1, String.format("%1$s needed even byte count-received %2$d", curFunc, s3len));
                        mL.OK = false;
                        continue;
                    }
                    if (s3len / 2 != blkSize) {
                        mL.mLog(mL.VB1, String.format("%1$s needed %2$d byte count-received %3$d", curFunc, blkSize, s3len));
                        mL.OK = false;
                        continue;
                    }
                    break;
                }
            }
            if (reply.length() == 0) {
                mL.OK = false;
                return null;
            }

            //convert returned value a to binary string
            String string_byte;
            int j = 0;
            mL.mLog(mL.VB1, String.format("%1$s s3len= %2$d blkSize= %3$d", curFunc, s3len, blkSize));
            byte[] binArray = new byte[s3len / 2];
            for (int i = 0; i < (s3len); i += 2) {
                string_byte = sArray[3].substring(i, i + 2);
                try {
                    binArray[j] = (byte) (Integer.parseInt(string_byte, 16) & 0xFF);
                    j++;
                } catch (NumberFormatException e) {
                    mL.aborting = true;
                    mL.OK = false;
                    mL.mLog(mL.ABORT, String.format("%1$s aborting download on conversion error +++", curFunc));
                    return null;
                }
            }
            mL.mLog(mL.VB1, String.format("%1$s return size is %2$d characters", curFunc, binArray.length));
            mL.OK = true;
            return binArray;
        }
    }//class updateAGPS

    private class eraseLog extends AsyncTask<Void, String, Void> {
        private ProgressDialog dialog = new ProgressDialog(mL.mContext);
        private Context mContext;
        String msg;
        private String[] parms;
        private boolean OK;
        private int ix;

        public eraseLog(Context context) {
            mL.mLog(mL.VB0, "GetLogFragment.eraseLog.eraseLog()");
            mContext = context;
        }//eraseLog()

        @Override
        protected void onPreExecute() {
            mL.mLog(mL.VB0, "GetLogFragment.eraseLog.onPreExecute()");
            btnRun.setEnabled(false);
            btnErase.setEnabled(false);
            this.dialog.setMessage(getString(R.string.working));
            this.dialog.show();
        }//onPreExecute()

        @Override
        protected Void doInBackground(Void... params) {
            String curFunc = "GetLogFragment.eraseLog.doInBackground()";
            mL.mLog(mL.VB0, curFunc);
            curFunc = String.format("+++ %1$s +++",curFunc);

            if (mL.stopNMEA) {
                mL.NMEAstop();
            }
            if (mL.stopLOG) {
                mL.LOGstop();
            }

            mL.mLog(mL.VB1, String.format("%1$s mtkCmd(PMTK182,6,1, PMTK001,182,6)", curFunc));
            parms = mL.mtkCmd("PMTK182,6,1", "PMTK001,182,6", mL.cmdTimeOut*2);
            if (parms == null) {
                mL.aborting = true;
                mL.mLog(mL.ABORT, String.format("%1$s fatal error-PMTK182,6,1 failed ****", curFunc));
                return null;
            }
            if (parms[3].equals("3")){
                msg = mContext.getString(R.string.erased);
                publishProgress();
                OK = false;
                ix = 0;
                do {//get log record count
                    parms = mL.mtkCmd("PMTK182,2,10", "PMTK182,3,10", mL.cmdTimeOut);
                    if (parms != null && parms[0].contains("PMTK182") && parms[1].contains("3")) {
                        OK = true;
                        mL.logRecCount = Integer.parseInt(parms[3], 16);
                        mL.strGPS = Integer.toString(mL.logRecCount) + " log records";
                        msg = mL.strGPS;
                        publishProgress();
                        mL.mLog(mL.VB0, String.format("%1$s log has %2$d records ******", curFunc, mL.logRecCount));
                    }
                } while (!OK && ix <= 10);
            }

            if (mL.stopNMEA) {
                mL.NMEAstart();
            }
            return null;
        }//doInBackground()

        @Override
        protected void onProgressUpdate(String... values) {
            mL.mLog(mL.VB0, "GetLogFragment.eraseLog.onProgressUpdate()");
            mTv.append(msg + mL.NL);
            scrollDown();
        }

        @Override
        protected void onPostExecute(Void param) {
            mL.mLog(mL.VB0, "GetLogFragment.eraseLog.onPostExecute()");
            if (dialog.isShowing()) dialog.dismiss();
            btnRun.setEnabled(true);
            btnErase.setEnabled(true);
        }//onPostExecute()
    }//class eraseLog
}//class GetLogFragment
