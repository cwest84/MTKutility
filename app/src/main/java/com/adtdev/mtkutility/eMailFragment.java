package com.adtdev.mtkutility;
/**
 * @author Alex Tauber
 * <p>
 * This file is part of the open source Android app mtkutility. You can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 3 of the License. This extends to files included that were authored by
 * others and modified to make them suitable for this app. All files included were subject to
 * open source licensing.
 * <p>
 * mtkutility is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You can review a copy of the
 * GNU General Public License at http://www.gnu.org/licenses.
 */

import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import com.adtdev.fileChooser.FileChooser;
import static android.app.Activity.RESULT_OK;

public class eMailFragment extends Fragment {

    private myLibrary mL;

    public static final int REQUEST_PATH = 1;
    private TextView lfileName;
    private Button getefile;
    private Button sendefile;
    private String startPath;

    @Override
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mL = Main.mL;
        mL.mLog(mL.VB0, "eMailFragment.onCreateView()");

        View rootView = inflater.inflate(R.layout.email, container, false);
        lfileName = rootView.findViewById(R.id.lfileName);

        getefile = rootView.findViewById(R.id.getefile);
        getefile.setTransformationMethod(null);
        getefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "+++ eMailFragment.onCreateView() +++ button " + getefile.getText() + " pressed");
                getfile();
            }
        });

        sendefile = rootView.findViewById(R.id.sendefile);
        sendefile.setEnabled(false);
        sendefile.setTransformationMethod(null);
        sendefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mL.mLog(mL.VB0, "+++ eMailFragment.onCreateView() +++ button " + sendefile.getText() + " pressed");
                mL.sendEmail(0);
            }
        });

        return rootView;
    }

    private void getfile() {
        mL.mLog(mL.VB0, "eMailFragment.getfile()");
        startPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        startPath = startPath + "/mtkutility";
        Intent intent = new Intent(mL.mContext, FileChooser.class);
        intent.putExtra("method", myLibrary.doLOCAL);
        intent.putExtra("root", startPath);
        intent.putExtra("start", startPath);
        intent.putExtra("nofolders", true);
        intent.putExtra("showhidden", false);
        startActivityForResult(intent, REQUEST_PATH);
    }//getfile()

    // Listen for results.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                mL.eFile = new File(data.getStringExtra("GetPath"));
                lfileName.setText(data.getStringExtra("GetFileName"));
                sendefile.setEnabled(true);
            }
        }
    }//onActivityResult()
}
