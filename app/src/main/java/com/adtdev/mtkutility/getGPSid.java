package com.adtdev.mtkutility;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by User on 12/10/2017.
 */

public class getGPSid extends DialogFragment {
    private myLibrary myLib;

    public interface GPSdialogListener {
        void onClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        myLib = Main.mL;
        myLib.appPrefs = getActivity().getSharedPreferences("otherprefs", Context.MODE_PRIVATE);
        myLib.appPrefEditor = myLib.appPrefs.edit();
        myLib.mLog(myLib.VB0, "getGPSid.onCreateDialog()");

        final ArrayAdapter<String> BTnameArray = new
                ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        final ArrayAdapter<String> BTmacArray = new
                ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name to an array adapter to show in a ListView
                BTnameArray.add(device.getName());
                BTmacArray.add(device.getAddress());
            }
        }else {
            Toast.makeText(getActivity(), "no paired devices found", Toast.LENGTH_LONG).show();
        }
        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(getActivity());
//        builderSingle.setTitle(getActivity().getString(R.string.selOne));
        builderSingle.setTitle("select GPS device");
        builderSingle.setNegativeButton(getActivity().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogNegativeClick(getGPSid.this);
            }
        });

        builderSingle.setAdapter(BTnameArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myLib.GPSname = BTnameArray.getItem(which);
                myLib.GPSmac = BTmacArray.getItem(which);
//                mL.appPrefEditor.putString("GPSname", mL.GPSname);
                myLib.appPrefEditor.putString("GPSmac", myLib.GPSmac);
                myLib.appPrefEditor.commit();
                mListener.onClick(getGPSid.this);
            }
        });
        return builderSingle.create();
    }//onCreateDialog()

    GPSdialogListener mListener;

    // onAttach(Context) not called prior to API 23. Use onAttach(Activity) instead
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attach(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            attach(getActivity());
        }
    }

    protected void attach(Context context) {
        try{mListener = (GPSdialogListener) getTargetFragment();}
        catch (ClassCastException e){ myLib.buildCrashReport(e);}
    }
}
