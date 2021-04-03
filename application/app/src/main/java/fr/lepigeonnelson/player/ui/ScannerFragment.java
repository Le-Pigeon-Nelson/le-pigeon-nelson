package fr.lepigeonnelson.player.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import fr.lepigeonnelson.player.MainActivity;
import fr.lepigeonnelson.player.R;
import fr.lepigeonnelson.player.broadcastplayer.ServerDescription;

public class ScannerFragment extends Fragment {
    private CodeScanner mCodeScanner;
    private boolean mPermission;
    private AddServerFragment addFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        addFragment = null;
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(activity, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = result.getText();

                        if (!isValidURL(url)) {
                            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                            alertDialog.setTitle("Adresse incorrecte");
                            alertDialog.setMessage("L'adresse du serveur n'est pas correctement formatée.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Annuler",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            mCodeScanner.startPreview();
                                        }
                                    });
                            alertDialog.show();
                        } else if (activity.hasServerWithAddress(url)) {
                            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                            alertDialog.setTitle("Adresse existante");
                            alertDialog.setMessage("Ce serveur est déjà disponible dans la liste des serveurs.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Annuler",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            mCodeScanner.startPreview();
                                        }
                                    });
                            alertDialog.show();
                        }
                        else {
                            Log.d("EditServerFragment", "save modifications");
                            ServerDescription newDescription = new ServerDescription(url);
                            activity.updateServer(newDescription);

                            Log.d("ScannerFragment", "Back to main fragment");
                            NavHostFragment.findNavController(getParentFragment()).popBackStack();
                        }
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });

        mPermission = false;
        activity.checkCameraPermission(this);
        return root;
    }

    private boolean isValidURL(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermission)
            mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public void setPermission(boolean b) {
        mPermission = b;
        if (mPermission)
            mCodeScanner.startPreview();
    }

    public void setAddFragment(AddServerFragment addFragment) {
        this.addFragment = addFragment;
    }
}
