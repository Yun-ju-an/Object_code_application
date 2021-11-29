package com.example.myoungjimohaji;

import android.content.DialogInterface;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyPageActivity extends AppCompatActivity {

    Button btnrecify, btnlogout, btnexit, btnBack;
    TextInputEditText stdNo, userName, phNo, userId, userPassword;

    String dbName, dbPhNo, dbStdNo, dbId, dbPw;

    private FirebaseAuth mAuth;
    DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent main = new Intent(MyPageActivity.this, MainPortal.class);

        btnrecify = (Button) findViewById(R.id.btnrecify);
        btnlogout = (Button) findViewById(R.id.btnlogout);
        btnexit = (Button) findViewById(R.id.btnexit);
        btnBack = (Button) findViewById(R.id.btnBack);

        stdNo = (TextInputEditText) findViewById(R.id.stdNo);
        userName = (TextInputEditText) findViewById(R.id.userName);
        phNo = (TextInputEditText) findViewById(R.id.phNo);
        userId = (TextInputEditText) findViewById(R.id.userId);
        userPassword = (TextInputEditText) findViewById(R.id.userPassword);

        loadAllUserData();
        settingUserData(dbName, dbPhNo, dbStdNo, dbId, dbPw);

        mAuth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference("users");

        stdNo.setEnabled(false); // 학번칸 수정 불가능하게
        userId.setEnabled(false); // 아이디 수정 불가능하게

        userId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customToast("아이디는 고유정보로 수정 불가능합니다.");
                userPassword.requestFocus();
            }
        }); // 아이디 클릭시 메시지와 함께 다음칸으로 이동

        stdNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customToast("학번은 고유정보로 수정 불가능합니다.");
                userName.requestFocus();
            }
        }); // 학번 클릭시 메시지와 함께 다음칸으로 이동

        userName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    phNo.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 이름에서 엔터시 다음칸

        phNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    userPassword.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 전화번호에서 엔터시 비밀번호칸

        userPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    btnrecify.performClick();
                    return true;
                }
                return false;
            }
        }); // 비밀번호칸에서 엔터시 정보수정 클릭

        btnrecify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updata();
            }
        }); // 정보수정 버튼

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAllUserData(main, dbName, dbPhNo, dbStdNo, dbId, dbPw);
                startActivity(main);
            }
        }); // 뒤로가기 버튼

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MyPageActivity.this);

                dlg.setTitle("로그아웃");
                dlg.setMessage("로그아웃 하시겠습니까?");

                dlg.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        customToast("로그아웃 되었습니다.");
                        dialog.dismiss();
                        finish();
                    }
                });

                dlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        customToast("취소되었습니다.");
                        dialog.dismiss();
                    }
                });

                dlg.show();

            }
        }); // 로그아웃 버튼

        btnexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MyPageActivity.this);

                dlg.setTitle("회원탈퇴");
                dlg.setMessage("회원탈퇴 하시겠습니까? [되돌릴 수 없습니다.]");

                dlg.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.getCurrentUser().delete();
                        reference.child(dbStdNo).removeValue();
                        customToast("회원탈퇴 되었습니다.");
                        dialog.dismiss();
                        finish();
                    }
                });

                dlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        customToast("회원탈퇴가 취소되었습니다.");
                        dialog.dismiss();
                    }
                });

                dlg.show();
            }
        }); // 회원탈퇴 버튼
    }

    private void loadAllUserData() { // 인텐트를 통해 정보를 로드
        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String pNo = intent.getStringExtra("pNo");
        String stdNo = intent.getStringExtra("stdNo");
        String id = intent.getStringExtra("id");
        String pw = intent.getStringExtra("pw");

        dbName = name;
        dbPhNo = pNo;
        dbStdNo = stdNo;
        dbId = id;
        dbPw = pw;
    } // 인텐트를 통한 유저정보 로드

    private void settingUserData(String dbName,String dbPhNo,String dbStdNo,String dbId,String dbPw) {
        stdNo.setText(dbStdNo);
        userName.setText(dbName);
        phNo.setText(dbPhNo);
        userId.setText(dbId);
        userPassword.setText(dbPw);
    } // 유저정보 기입

    private void updata() {
        if (isNameChanged() || isPhNoChanged() || isPasswordChanged()) {
            customToast("정보가 업데이트 되었습니다.");
        } else {
            customToast("업데이트 내용이 없습니다.");
        }
    }

    public void sendAllUserData(Intent intent, String name, String pNo, String stdNo, String id, String pw) {
        intent.putExtra("name", name);
        intent.putExtra("pNo", pNo);
        intent.putExtra("stdNo", stdNo);
        intent.putExtra("id", id);
        intent.putExtra("pw", pw);
    }

    private boolean isNameChanged() {
        if (!dbName.equals(userName.getText().toString().trim())) {
            reference.child(dbStdNo).child("name").setValue(userName.getText().toString().trim());
            dbName = userName.getText().toString().trim();
            return true;
        } else {
            return false;
        }
    }

    private boolean isPhNoChanged() {
        if (!dbPhNo.equals(phNo.getText().toString().trim())) {
            reference.child(dbStdNo).child("pNo").setValue(phNo.getText().toString().trim());
            dbPhNo = phNo.getText().toString().trim();
            return true;
        } else {
            return false;
        }
    }

    private boolean isPasswordChanged() {
        if (!dbPw.equals(userPassword.getText().toString().trim())) {
            reference.child(dbStdNo).child("pw").setValue(userPassword.getText().toString().trim());
            dbPw = userPassword.getText().toString().trim();
            mAuth.getCurrentUser().updatePassword(dbPw);
            return true;
        } else {
            return false;
        }
    }

    public void customToast(String s) { // 커스텀토스트 정의
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MyPageActivity.this, MainActivity.class));
        } else if (TextUtils.isEmpty(dbStdNo) && !mAuth.getCurrentUser().isAnonymous()) {
            mAuth.signOut();
            startActivity(new Intent(MyPageActivity.this, MainActivity.class));
        }
    }
}
