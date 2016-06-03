package openbeautyfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openbeautyfacts.github.scrachx.openfood.R;
import openbeautyfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openbeautyfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;

public class BarCodeScannerFragment extends BaseFragment implements MessageDialogFragment.MessageDialogListener,
        ZXingScannerView.ResultHandler, CameraSelectorDialogFragment.CameraSelectorDialogListener {

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String CAMERA_ID = "CAMERA_ID";
    private static final String RING_STATE = "RING_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private boolean mRing;
    private boolean mAutoFocus;
    private int mCameraId = -1;
    private List<BarcodeFormat> mFormats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        final SharedPreferences settings = getActivity().getSharedPreferences("camera", 0);

        mScannerView = new ZXingScannerView(getActivity());
        if(state != null) {
            mRing = state.getBoolean(RING_STATE, false);
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else {
            mRing = settings.getBoolean("ring", false);
            mFlash = settings.getBoolean("flash", false);
            mAutoFocus = settings.getBoolean("focus", true);
            mCameraId = -1;
        }
        setupFormats();
        return mScannerView;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem;

        if(mRing) {
            menuItem = menu.add(Menu.NONE, R.id.menu_ring, 0, R.string.ring_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_ring, 0, R.string.ring_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        if(mFlash) {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        if(mAutoFocus) {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        menuItem = menu.add(Menu.NONE, R.id.menu_camera_selector, 0, R.string.select_camera);
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        final SharedPreferences settings = getActivity().getSharedPreferences("camera", 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (item.getItemId()) {
            case R.id.menu_ring:
                mRing = !mRing;
                if(mRing) {
                    item.setTitle(R.string.ring_on);
                    editor.putBoolean("ring", true);
                } else {
                    item.setTitle(R.string.ring_off);
                    editor.putBoolean("ring", false);
                }
                editor.apply();
                return true;
            case R.id.menu_flash:
                mFlash = !mFlash;
                if(mFlash) {
                    item.setTitle(R.string.flash_on);
                    editor.putBoolean("flash", true);
                } else {
                    item.setTitle(R.string.flash_off);
                    editor.putBoolean("flash", false);
                }
                editor.apply();
                mScannerView.setFlash(mFlash);
                return true;
            case R.id.menu_auto_focus:
                mAutoFocus = !mAutoFocus;
                if(mAutoFocus) {
                    item.setTitle(R.string.auto_focus_on);
                    editor.putBoolean("focus", true);
                } else {
                    item.setTitle(R.string.auto_focus_off);
                    editor.putBoolean("focus", false);
                }
                editor.apply();
                mScannerView.setAutoFocus(mAutoFocus);
                return true;
            case R.id.menu_camera_selector:
                mScannerView.stopCamera();
                DialogFragment cFragment = CameraSelectorDialogFragment.newInstance(this, mCameraId);
                cFragment.show(getActivity().getSupportFragmentManager(), "camera_selector");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putBoolean(RING_STATE, mRing);
    }

    @Override
    public void handleResult(Result rawResult) {
        if(mRing) {
            try {
                ToneGenerator beep = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                beep.startTone(ToneGenerator.TONE_PROP_BEEP);
            } catch (Exception e) {}
        }
        if (!rawResult.getText().isEmpty()) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if(isConnected) {
                FoodAPIRestClientUsage api = new FoodAPIRestClientUsage();
                api.getProduct(rawResult.getText(), getActivity(), mScannerView, this);
            } else {
                SharedPreferences settings = getActivity().getSharedPreferences("temp", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("barcode", rawResult.getText());
                editor.apply();
                Intent intent = new Intent(getActivity(), SaveProductOfflineActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Resume the camera
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onCameraSelected(int cameraId) {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    public void setupFormats() {
        mFormats = new ArrayList<>();
        mFormats.add(BarcodeFormat.UPC_A);
        mFormats.add(BarcodeFormat.UPC_E);
        mFormats.add(BarcodeFormat.EAN_13);
        mFormats.add(BarcodeFormat.EAN_8);
        mFormats.add(BarcodeFormat.RSS_14);
        mFormats.add(BarcodeFormat.CODE_39);
        mFormats.add(BarcodeFormat.CODE_93);
        mFormats.add(BarcodeFormat.CODE_128);
        mFormats.add(BarcodeFormat.ITF);
        mScannerView.setFormats(mFormats);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}