package com.example.myoungjimohaji;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;

import com.google.firebase.FirebaseApp;
import com.gun0912.tedpermission.PermissionListener;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


public class MainActivity extends AppCompatActivity {
    // ???????????? ??????
    TextInputEditText IdText, PwText; // ???????????? ??????????????? ?????? ???????????????
    Button Login, Loginanonymus, NewId, Findbtn, idLogin, stdnoLogin; // ?????????, ????????????, ?????????, ?????????????????? ?????? ????????? ??????
    TextInputLayout idLayout;

    // ???????????? ?????????????????? ?????? ?????? ???????????? ??????
    private FirebaseAuth mAuth;

    String status = "?????????";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        tedPermission(); // ???????????? ????????????

        // ???????????? ????????????
        Intent login = new Intent(MainActivity.this, MainPortal.class); // ???????????? ??????
        Intent newid = new Intent(MainActivity.this, NewId.class); // ??????????????? ??????
        Intent findid = new Intent(MainActivity.this, FindActivity.class); // ?????????/???????????? ?????? ??? ??????

        // ?????? ????????? ???????????? ??????
        IdText = (TextInputEditText) findViewById(R.id.idText); // ??????????????????
        PwText = (TextInputEditText) findViewById(R.id.pwText); // ?????????????????????

        Login = (Button) findViewById(R.id.Loginbtn); // ???????????????
        NewId = (Button) findViewById(R.id.NewIdbtn); // ??????????????????
        Findbtn = (Button) findViewById(R.id.Findbtn); // ?????????/????????????????????????
        Loginanonymus = (Button) findViewById(R.id.Loginanonymus); // ?????????????????????
        idLogin = (Button) findViewById(R.id.idLogin); // ???????????????????????????
        stdnoLogin = (Button) findViewById(R.id.stdnoLogin);  // ???????????????????????????

        idLayout = (TextInputLayout) findViewById(R.id.idLayout);

        // Initialize Firebase Auth, Firebase, ?????? ?????????
        mAuth = FirebaseAuth.getInstance();

        Login.setOnClickListener(new View.OnClickListener() { // ??????????????? ?????????
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                } catch (Exception e) { }


                if (status == "?????????") {
                    String id = IdText.getText().toString().trim(); // ???????????????????????? ????????? ????????????
                    String pw = PwText.getText().toString().trim(); // ??????????????????????????? ???????????? ????????????
                    if (TextUtils.isEmpty(id)) {
                        IdText.setError("???????????? ??????????????????"); // ???????????? ???????????????
                        IdText.requestFocus();
                    } else if (TextUtils.isEmpty(pw)) {
                        PwText.setError("??????????????? ??????????????????"); // ??????????????? ???????????????
                        PwText.requestFocus();
                    } else {

                        findStdno(v, login, id);

                        mAuth.signInWithEmailAndPassword(id, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    customToast(v, "????????? ???????????????.");
                                    startActivity(login);
                                } else {
                                    customToast(v, "???????????? ?????????????????????.");
                                }
                            }
                        });
                    }
                } else if (status == "??????") {
                    String id = IdText.getText().toString(); // ???????????????????????? ????????? ????????????
                    String pw = PwText.getText().toString(); // ??????????????????????????? ???????????? ????????????
                    if (TextUtils.isEmpty(id)) {
                        IdText.setError("???????????? ??????????????????"); // ???????????? ???????????????
                        IdText.requestFocus();
                    } else if (TextUtils.isEmpty(pw)) {
                        PwText.setError("??????????????? ??????????????????"); // ??????????????? ???????????????
                        PwText.requestFocus();
                    } else {
                        stdUser(v, login);
                    }
                }
            }
        }); // ??????????????? ?????????

        Loginanonymus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInAnonymously().addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            customToast(v, "??????????????? ??????????????????.");
                            startActivity(login);
                        } else {
                            customToast(v, "?????????????????? ??????");
                        }
                    }
                });
            }
        }); // ???????????????????????? ?????????

        NewId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(newid);
            }
        }); // ?????????????????? ?????????

        Findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(findid);
            }
        }); // ?????????/???????????? ?????? ?????????

        idLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "?????????";
                idLayout.setHint(status);
            }
        }); // ???????????? ???????????????

        stdnoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "??????";
                idLayout.setHint(status);
            }
        }); // ???????????? ???????????????

        IdText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    PwText.requestFocus();
                    return true;
                }
                return false;
            }
        }); // ??????????????? ????????????

        IdText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Login.performClick();
                    return true;
                }
                return false;
            }
        }); // ?????????????????? ???????????? ??????????????? ??????

    }

    // ????????? ???????????? ??? ???????????? ?????? ???????????????????????? ?????????(?????? ????????? ?????? ??????)
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(MainActivity.this, MainPortal.class)); // ?????? ???????????? ??????????????? ??????????????? ????????????
        }
    }

    private void findStdno(View v, Intent intent,String userId) { // ???????????? ???????????? ????????? ????????? ???????????? ?????? ??????
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkStdno = reference.orderByChild("stdNo");

        checkStdno.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    IdText.setError(null);
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue(); // ??????????????? ??? snapshot??? ?????? hasmap??? ????????????

                    String userStdno = null; // ?????? ?????? ?????? key?????? ?????? ????????????

                    for (Map.Entry<String, Object> entry: map.entrySet()) { // ??????????????? hasmap????????? ?????? ????????? ?????????
                        String temp = snapshot.child(entry.getKey()).child("id").getValue(String.class); // ?????? ????????? ?????? id??? user??? ????????? id??? ????????????
//                        customToast(v, entry.getKey()); // ??? ??????????????? ???????????? ??????
                        if (temp.equals(userId)) {
                            userStdno = entry.getKey(); // ????????? ???????????? ?????? key?????? ?????????
                            break;
                        }
                    }

                    if (!TextUtils.isEmpty(userStdno)) {
                        String passwordFromDB = snapshot.child(userStdno).child("pw").getValue(String.class);
                        String nameFromDB = snapshot.child(userStdno).child("name").getValue(String.class);
                        String pNoFromDB = snapshot.child(userStdno).child("pNo").getValue(String.class);
                        String stdNoFromDB = snapshot.child(userStdno).child("stdNo").getValue(String.class);
                        String idFromDB = snapshot.child(userStdno).child("id").getValue(String.class);
                        sendAllUserData(intent, nameFromDB, pNoFromDB, stdNoFromDB, idFromDB, passwordFromDB);
                    } else {
                        customToast(v, "?????????????????? ?????? ??????");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                customToast(v, "?????????????????? ??????" + error);
            }
        });
    } // ???????????? ???????????? ?????????????????? ??????

    private void stdUser(View v, Intent intent) { // ?????????????????? ???????????? ?????? -> ?????? == ???????????? ??????????????????????????? ?????? -> ??????????????? ??????????????? ????????? ????????? ???????????? ????????? ?????????
        String userEnteredUserStdno = IdText.getText().toString().trim();
        String userEnteredUserPassword = PwText.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        Query checkStdno = reference.orderByChild("stdNo").equalTo(userEnteredUserStdno);

        checkStdno.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    IdText.setError(null);

                    String passwordFromDB = snapshot.child(userEnteredUserStdno).child("pw").getValue(String.class);

                    if (passwordFromDB.equals(userEnteredUserPassword)) {

                        PwText.setError(null);

                        String nameFromDB = snapshot.child(userEnteredUserStdno).child("name").getValue(String.class);
                        String pNoFromDB = snapshot.child(userEnteredUserStdno).child("pNo").getValue(String.class);
                        String stdNoFromDB = snapshot.child(userEnteredUserStdno).child("stdNo").getValue(String.class);
                        String idFromDB = snapshot.child(userEnteredUserStdno).child("id").getValue(String.class);

                        mAuth.signInWithEmailAndPassword(idFromDB, passwordFromDB).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    sendAllUserData(intent, nameFromDB, pNoFromDB, stdNoFromDB, idFromDB, passwordFromDB);

                                    customToast(v, "????????? ???????????????.");

                                    startActivity(intent);
                                } else {
                                    customToast(v, "???????????? ?????????????????????.");
                                }
                            }
                        });

                    } else {
                        PwText.setError("????????? ?????????????????????.");
                        PwText.requestFocus();
                    }
                }
                else {
                    IdText.setError("?????? ????????? ???????????? ????????????.");
                    IdText.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void sendAllUserData(Intent intent, String name, String pNo, String stdNo, String id, String pw) {
        intent.putExtra("name", name);
        intent.putExtra("pNo", pNo);
        intent.putExtra("stdNo", stdNo);
        intent.putExtra("id", id);
        intent.putExtra("pw", pw);
    }

    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // ?????? ?????? ??????
                //Toast.makeText(LoginActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // ?????? ?????? ??????
                // Toast.makeText(LoginActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    public void customToast(View view, String s) { // ?????????????????? ??????
        LayoutInflater inflater = getLayoutInflater(); // LayouyInflater?????? ??????

        View layout = inflater.inflate( // LayouyInflater????????? inflate????????? ???????????? layout????????? toast_layout.xml?????? ????????? layout??? ??????
                R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout));

        TextView text = layout.findViewById(R.id.text); // ????????? ????????? text????????? ??????

        Toast toast = new Toast(this); // Toast?????? ??????
        text.setText(s); // text??? ?????? ???????????? ?????? ??????
        text.setTextSize(15); // text????????? ??????
        text.setTextColor(Color.BLACK); // ?????? ??? ??????
        toast.setDuration(Toast.LENGTH_SHORT); // ?????? ?????? ????????????
        toast.setView(layout); // layout??? ??????????????? layout?????? ??????
        toast.show();  // toast?????????
    }
}