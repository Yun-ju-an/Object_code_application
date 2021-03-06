package com.example.myoungjimohaji;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FindActivity extends AppCompatActivity {

    TextInputEditText stdNo_id, stdNo_pw,phNo,id;
    Button findId, findPw, backbtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        stdNo_id = (TextInputEditText) findViewById(R.id.stdNoId);
        stdNo_pw = (TextInputEditText) findViewById(R.id.stdNoPw);
        phNo = (TextInputEditText) findViewById(R.id.phNo);
        id = (TextInputEditText) findViewById(R.id.id);

        findId = (Button) findViewById(R.id.findId);
        findPw = (Button) findViewById(R.id.findPw);
        backbtn = (Button) findViewById(R.id.backbtn);

        mAuth = FirebaseAuth.getInstance();

        findId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stdNo_input = stdNo_id.getText().toString().trim();

                if (TextUtils.isEmpty(stdNo_input)) {
                    stdNo_id.setError("????????? ??????????????????"); // ????????? ???????????????
                    stdNo_id.requestFocus();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

                    Query checkStdno = reference.orderByChild("stdNo").equalTo(stdNo_input);

                    checkStdno.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                stdNo_id.setError(null);

                                stdNo_id.setText("");
                                // ????????? ???????????? ?????????
                                String findOutId = snapshot.child(stdNo_input).child("id").getValue(String.class);

                                // ????????? ??????
                                AlertDialog.Builder dlg = new AlertDialog.Builder(FindActivity.this);
                                dlg.setTitle("????????? ??????");
                                dlg.setMessage("?????????: '" + findOutId + "'");

                                dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                dlg.show();

                            } else {
                                stdNo_id.setError("?????? ????????? ???????????? ????????????.");
                                stdNo_id.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });

        findPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stdNo_input = stdNo_pw.getText().toString().trim();
                String phNo_input = phNo.getText().toString().trim();
                String id_input = id.getText().toString().trim();

                if (TextUtils.isEmpty(stdNo_input)) {
                    stdNo_pw.setError("????????? ??????????????????"); // ????????? ???????????????
                    stdNo_pw.requestFocus();
                } else if (TextUtils.isEmpty(phNo_input)) {
                    phNo.setError("????????????????????? ??????????????????"); // ????????????????????? ???????????????
                    phNo.requestFocus();
                } else if (TextUtils.isEmpty(id_input)) {
                    id.setError("???????????? ??????????????????");
                    id.requestFocus();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

                    Query checkStdno = reference.orderByChild("stdNo").equalTo(stdNo_input);

                    checkStdno.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                stdNo_pw.setError(null);

                                String phnoFromDB = snapshot.child(stdNo_input).child("pNo").getValue(String.class);
                                String idFromDB = snapshot.child(stdNo_input).child("id").getValue(String.class);

                                if (phNo_input.equals(phnoFromDB)) {
                                    phNo.setError(null);

                                    if (id_input.equals(idFromDB)) {
                                        id.setError(null);
                                        stdNo_pw.setText("");
                                        phNo.setText("");
                                        id.setText("");

                                        String findOutPw = snapshot.child(stdNo_input).child("pw").getValue(String.class);

                                        AlertDialog.Builder dlg = new AlertDialog.Builder(FindActivity.this);
                                        dlg.setTitle("???????????? ??????");
                                        dlg.setMessage("???????????? : '" + findOutPw + "'");

                                        dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        dlg.show();
                                    } else {
                                        id.setError("?????? ?????????[???????????????]??? ???????????? ????????????.");
                                        id.requestFocus();
                                    }
                                } else {
                                    phNo.setError("?????? ??????????????? ???????????? ????????????.");
                                    phNo.requestFocus();
                                }
                            } else {
                                stdNo_pw.setError("?????? ????????? ???????????? ????????????.");
                                stdNo_pw.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        stdNo_id.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    findId.performClick();
                    return true;
                }
                return false;
            }
        }); // ????????? ????????? ?????? ??? ??????

        stdNo_pw.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    phNo.requestFocus();
                    return true;
                }
                return false;
            }
        }); // ???????????? ????????? ?????? ?????? ??? ??????

        phNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    id.requestFocus();
                    return true;
                }
                return false;
            }
        }); // ???????????? ?????? ??? ??????

        id.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    findPw.performClick();
                    return true;
                }
                return false;
            }
        }); // ????????? ?????? ??? ??????

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
