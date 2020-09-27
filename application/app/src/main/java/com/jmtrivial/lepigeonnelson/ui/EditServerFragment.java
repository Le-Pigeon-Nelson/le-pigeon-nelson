package com.jmtrivial.lepigeonnelson.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.navigation.fragment.NavHostFragment;

import com.jmtrivial.lepigeonnelson.MainActivity;
import com.jmtrivial.lepigeonnelson.R;
import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class EditServerFragment extends Fragment {
    private MainActivity activity;
    private ServerDescription server;
    private CheckBox force;
    private CheckBox refresh;
    private NumberPicker period;
    private EditText name;
    private EditText address;
    private EditText description;
    private EditText encoding;
    private Button button;

    private boolean modified;
    private View fragment;
    private Button deleteButton;

    // TODO: add a button to delete a server in this fragment

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        fragment = inflater.inflate(R.layout.fragment_edit_server, container, false);

        force = fragment.findViewById(R.id.force_server_properties);

        force.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setModified();
                        LinearLayout panel = (LinearLayout) fragment.findViewById(R.id.option_panel);
                        if (isChecked) {
                            panel.setVisibility(View.VISIBLE);
                        } else {
                            panel.setVisibility(View.GONE);
                        }
                    }
                }
        );

        refresh = fragment.findViewById(R.id.refresh_messages);
        refresh.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setModified();
                        LinearLayout panel = (LinearLayout) fragment.findViewById(R.id.period_frame);
                        if (isChecked) {
                            panel.setVisibility(View.VISIBLE);
                        } else {
                            panel.setVisibility(View.GONE);
                        }
                    }
                }
        );

        period = fragment.findViewById(R.id.default_period);
        period.setMinValue(1);
        period.setMaxValue(60 * 60);
        period.setValue(15);
        period.setWrapSelectorWheel(true);

        period.setOnValueChangedListener(
                new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        setModified();
                    }
                }
        );


        TextWatcher modificationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("EditServerFragment", "text changed");
                setModified();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do
            }
        };

        name = fragment.findViewById(R.id.server_name);
        name.addTextChangedListener(modificationWatcher);

        address = fragment.findViewById(R.id.server_address);
        address.addTextChangedListener(modificationWatcher);

        description = fragment.findViewById(R.id.server_description);
        description.addTextChangedListener(modificationWatcher);

        encoding = fragment.findViewById(R.id.server_encoding);
        encoding.addTextChangedListener(modificationWatcher);

        button = fragment.findViewById(R.id.save_server);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        applyModifications();
                    }
                }
        );


        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // delete server
                        activity.deleteSelectedServer();
                        NavHostFragment.findNavController(EditServerFragment.this).popBackStack();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        deleteButton = fragment.findViewById(R.id.delete_server);
        deleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage("Voulez-vous vraiment supprimer le serveur?").setPositiveButton("Oui", dialogClickListener)
                                .setNegativeButton("Annuler", dialogClickListener).show();

                    }
                }
        );

        return fragment;
    }


    private void setModified() {
        modified = true;
        button.setEnabled(true);
    }

    public boolean isModified() {
        return modified;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setActiveFragment(MainActivity.EDIT_SERVER_FRAGMENT, this);
        activity.setToolbarTitle("Édition d'un serveur");
        server = activity.getEditedServer();

        force.setChecked(!server.isSelfDescribed());

        address.setText(server.getUrl());
        name.setText(server.getName());
        encoding.setText(server.getEncoding());
        description.setText(server.getDescription());

        refresh.setChecked(server.getPeriod() != 0);
        if (server.getPeriod() == 0)
            period.setValue(15);
        else
            period.setValue(server.getPeriod());

        if (activity.isEditedServerNew()) {
            deleteButton.setVisibility(View.INVISIBLE);
        }
        else {
            deleteButton.setVisibility(View.VISIBLE);
        }

        modified = false;
        button.setEnabled(false);
    }

    public void applyModifications() {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(fragment.getApplicationWindowToken(), 0);

        if (!modified) {
            // should not append
            Log.d("EditServerFragment", "no modification has been identified");
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        // first check if the url is valid
        if (!isValidURL()) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle("Adresse incorrecte");
            alertDialog.setMessage("L'adresse du serveur n'est pas correctement formatée.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Annuler",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        } else if (activity.hasServerWithAddress(address.getText().toString())) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle("Adresse existante");
            alertDialog.setMessage("Ce serveur est déjà disponible dans la liste des serveurs.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Annuler",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else {
            Log.d("EditServerFragment", "save modifications");
            ServerDescription newDescription = buildDescriptionFromForm();
            activity.updateEditedServer(newDescription);

            NavHostFragment.findNavController(this).popBackStack();
        }

    }

    private ServerDescription buildDescriptionFromForm() {
        ServerDescription result = new ServerDescription(address.getText().toString());
        result.setName(name.getText().toString()).setDescription(description.getText().toString())
                .setEncoding(encoding.getText().toString()).setPeriod(period.getValue());
        result.setIsEditable(true);
        result.setIsSelfDescribed(!force.isChecked());
        if (!refresh.isChecked())
            result.setPeriod(0);

        return result;
    }


    private boolean isValidURL() {
        String url = address.getText().toString();
        return Patterns.WEB_URL.matcher(url).matches();
    }

}
