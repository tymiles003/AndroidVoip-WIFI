package com.byteshaft.wifimessenger.activities;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.wifimessenger.R;
import com.byteshaft.wifimessenger.activities.MainActivity;
import com.byteshaft.wifimessenger.utils.AppGlobals;

/**
 * Created by sachinta on 12/8/2015.
 */
public class ProfileDialog extends Dialog implements View.OnClickListener {

    public Activity activity;
    public Dialog dialog;
    public Button yes, no;

    TextView tvuname, tvStatus;
    Button btn_edit, btn_editDone, btn_save, btn_cancel;
    LinearLayout ll_normal, ll_editable;
    EditText et_uname;

    public ProfileDialog(Activity a){
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        requestWindowFeature (Window.FEATURE_NO_TITLE);
        setContentView (R.layout.profile_dialog);

        getWindow ().setBackgroundDrawable (new ColorDrawable (Color.TRANSPARENT));

        tvuname = (TextView) findViewById (R.id.profile_tv_uname);
        tvStatus = (TextView) findViewById (R.id.tv_profile_status);
        et_uname = (EditText) findViewById (R.id.profile_et_username);

        ll_normal = (LinearLayout) findViewById (R.id.layout_name_normal);
        ll_editable = (LinearLayout) findViewById (R.id.layout_name_editable);

        btn_edit = (Button) findViewById (R.id.profile_btn_edit);
        btn_editDone = (Button) findViewById (R.id.profile_btn_edit_done);
        btn_save = (Button) findViewById (R.id.profile_btn_save);
        btn_cancel = (Button) findViewById (R.id.profile_btn_cancel);

        btn_edit.setOnClickListener (this);
        btn_editDone.setOnClickListener (this);
        btn_save.setOnClickListener (this);
        btn_cancel.setOnClickListener (this);

        tvuname.setText ("Name : " + AppGlobals.getName ());

        if (AppGlobals.isServiceOn ()){
            tvStatus.setTextColor (Color.parseColor ("#76ff03"));
            tvStatus.setText ("Online");
        }
        else {
            tvStatus.setTextColor (Color.parseColor ("#ff1744"));
            tvStatus.setText ("Offline");
        }

    }

    @Override
    public void onClick (View v) {
        int id = v.getId ();
        switch (id){

            case R.id.profile_btn_edit:
            {
                ll_normal.setVisibility (View.GONE);
                ll_editable.setVisibility (View.VISIBLE);
                break;
            }

            case R.id.profile_btn_edit_done:
            {
                if (!TextUtils.isEmpty (et_uname.getText ().toString ())){
                    tvuname.setText ("Name : " + et_uname.getText ().toString ());
                }

                ll_normal.setVisibility (View.VISIBLE);
                ll_editable.setVisibility (View.GONE);
                break;
            }

            case R.id.profile_btn_cancel:
            {
                dismiss ();
                break;
            }

            case R.id.profile_btn_save:
            {
                if (!TextUtils.isEmpty (tvuname.getText ().toString ())){
                    AppGlobals.putName (tvuname.getText ().toString ().replace ("Name : ", ""));
                    Toast.makeText (getContext (), "Name changed successfully!", Toast.LENGTH_SHORT).show ();
                    MainActivity.refreshUsername ();
                    dismiss ();
                }
                break;
            }


        }
    }
}
