package com.whut.JieShe;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.whut.JieShe.bean.MyUser;
import com.tapadoo.alerter.Alerter;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.ResetPasswordByEmailListener;
import cn.bmob.v3.listener.SaveListener;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private AVLoadingIndicatorView mProgressView;
    private View mLoginFormView;
    private TextView mForgetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        // Set up the login form.
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.user_name);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        BootstrapButton mEmailSignInButton = (BootstrapButton) findViewById(R.id.email_register_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        //注册按钮
        final TextView registerView = (TextView) findViewById(R.id.no_account_register);
        registerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    registerView.setTextColor(Color.GREEN);
                    return true;
                }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    int color = ContextCompat.getColor(LoginActivity.this, R.color.bootstrap_gray);
                    registerView.setTextColor(color);
                    Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        //重置密码按钮
        final TextView forgetPasswordView = (TextView) findViewById(R.id.forget_password);
        forgetPasswordView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    forgetPasswordView.setTextColor(Color.GREEN);
                    return true;
                }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    int color = ContextCompat.getColor(LoginActivity.this, R.color.bootstrap_alert_cross_default);
                    forgetPasswordView.setTextColor(color);
//                    Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
//                    startActivity(intent);

                    String username = mUserNameView.getText().toString();
                    if(username.equals("")){
                        Alerter.create(LoginActivity.this)
                                .setTitle("提示")
                                .setText("请在上方输入需要重置的用户名")
                                .setIcon(R.drawable.alerter_ic_face)
                                .show();
                        return true;
                    }

                    BmobQuery<MyUser> query = new BmobQuery<MyUser>();
                    query.addWhereEqualTo("username",username);
                    query.findObjects(LoginActivity.this, new FindListener<MyUser>() {
                        @Override
                        public void onSuccess(List<MyUser> list) {
                            if(list.size()==0){
                                Alerter.create(LoginActivity.this)
                                        .setTitle("提示")
                                        .setText("没有此用户")
                                        .setIcon(R.drawable.alerter_ic_face)
                                        .show();
                            }
                            else{
                                String emailAdd = list.get(0).getEmail();
                                BmobUser.resetPasswordByEmail(LoginActivity.this, emailAdd,
                                        new ResetPasswordByEmailListener() {
                                            @Override
                                            public void onSuccess() {
                                                Alerter.create(LoginActivity.this)
                                                        .setTitle("密码重置")
                                                        .setText("密码重置邮件已经发送至您的邮箱")
                                                        .setIcon(R.drawable.alerter_ic_face)
                                                        .show();
                                            }

                                            @Override
                                            public void onFailure(int i, String s) {
                                                Alerter.create(LoginActivity.this)
                                                        .setTitle("密码重置")
                                                        .setText("密码重置邮件发送失败")
                                                        .setIcon(R.drawable.alerter_ic_face)
                                                        .show();

                                            }
                                        });
                            }


                        }

                        @Override
                        public void onError(int i, String s) {

                        }
                    });


                    return true;
                }
                return false;
            }
        });
        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = (AVLoadingIndicatorView) findViewById(R.id.register_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUserNameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(userName)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mProgressView.show();

            final MyUser bu = new MyUser();
            bu.setUsername(userName);
            bu.setPassword(password);
            bu.login(this, new SaveListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                    mProgressView.hide();

                    BmobIMUserInfo info = new BmobIMUserInfo();
                    info.setUserId(bu.getObjectId());
                    info.setName(bu.getUsername());
                    info.setAvatar(bu.getAvatarUrl());
                    BmobIM.getInstance().updateUserInfo(info);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }

                @Override
                public void onFailure(int i, String s) {
                    Toast.makeText(LoginActivity.this, i+s, Toast.LENGTH_SHORT).show();
                    mProgressView.hide();
                }
            });

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserNameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }
}

