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
                    stdNo_id.setError("학번을 입력해주세요"); // 학번이 공란인경우
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
                                // 찾아낸 아이디를 변수화
                                String findOutId = snapshot.child(stdNo_input).child("id").getValue(String.class);

                                // 팝업창 구현
                                AlertDialog.Builder dlg = new AlertDialog.Builder(FindActivity.this);
                                dlg.setTitle("아이디 찾기");
                                dlg.setMessage("아이디: '" + findOutId + "'");

                                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                dlg.show();

                            } else {
                                stdNo_id.setError("해당 학번이 존재하지 않습니다.");
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
                    stdNo_pw.setError("학번을 입력해주세요"); // 학번이 공란인경우
                    stdNo_pw.requestFocus();
                } else if (TextUtils.isEmpty(phNo_input)) {
                    phNo.setError("휴대전화번호를 입력해주세요"); // 휴대전화번호가 공란인경우
                    phNo.requestFocus();
                } else if (TextUtils.isEmpty(id_input)) {
                    id.setError("아이디를 입력해주세요");
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
                                        dlg.setTitle("비밀번호 찾기");
                                        dlg.setMessage("비밀번호 : '" + findOutPw + "'");

                                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        dlg.show();
                                    } else {
                                        id.setError("해당 아이디[이메일형식]가 존재하지 않습니다.");
                                        id.requestFocus();
                                    }
                                } else {
                                    phNo.setError("해당 전화번호가 존재하지 않습니다.");
                                    phNo.requestFocus();
                                }
                            } else {
                                stdNo_pw.setError("해당 학번이 존재하지 않습니다.");
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
        }); // 아이디 찾기칸 입력 후 엔터

        stdNo_pw.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    phNo.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 비밀번호 찾기칸 학번 입력 후 엔터

        phNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    id.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 휴대전화 입력 후 엔터

        id.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    findPw.performClick();
                    return true;
                }
                return false;
            }
        }); // 아이디 입력 후 엔터

    }

    public void customToast(View view, String s) { // 커스텀토스트 정의
        LayoutInflater inflater = getLayoutInflater(); // LayouyInflater객체 생성

        View layout = inflater.inflate( // LayouyInflater객채의 inflate함수를 사용하여 layout폴더의 toast_layout.xml파일 탐색후 layout에 할당
                R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout));

        TextView text = layout.findViewById(R.id.text); // 표시할 메시지 text변수로 선언

        Toast toast = new Toast(this); // Toast객체 생성
        text.setText(s); // text에 내가 넣고싶은 내용 할당
        text.setTextSize(15); // text사이즈 조절
        text.setTextColor(Color.BLACK); // 글자 색 설정
        toast.setDuration(Toast.LENGTH_SHORT); // 글자 길이 짧은문자
        toast.setView(layout); // layout에 할당해놓은 layout으로 설정
        toast.show();  // toast띄우기
    }
}
