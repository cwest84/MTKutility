package com.adtdev.mtkutility;
/**
 * @author Alex Tauber
 *
 * This file is part of the open source Android app MTKutility2. You can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 3 of the License. This extends to files included that were authored by
 * others and modified to make them suitable for this app. All files included were subject to
 * open source licensing.
 *
 * MTKutility2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You can review a copy of the
 * GNU General Public License at http://www.gnu.org/licenses.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class HelpFragment extends Fragment {
    private myLibrary myLib;
    private View mV;
    private WebView wv;
    String helpXML = "file:///android_asset/MTKhelp.html";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myLib = Main.mL;
        myLib.mLog(myLib.VB0, "HelpFragment.onCreateView()");

        // Inflate the layout for this fragment
        mV =  inflater.inflate(R.layout.webview, container, false);
        wv = mV.findViewById(R.id.webview);
        wv.loadUrl(helpXML);

        return mV;
    }
}
