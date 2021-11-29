package com.example.myoungjimohaji;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;

public class NewId extends AppCompatActivity {

    // 오브젝트 선언
    TextInputEditText StdNum, Name, Phnum, Id, Pw;
    Button btnCreate, backbtn;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    private FirebaseAuth mAuth; // 클라우드 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_create);

        // 실제 어플 화면의 객체와 연결
        StdNum = (TextInputEditText) findViewById(R.id.stdNo);
        Name = (TextInputEditText) findViewById(R.id.myname);
        Phnum = (TextInputEditText) findViewById(R.id.pnumber);
        Id = (TextInputEditText) findViewById(R.id.uid); // 이메일형식으로
        Pw = (TextInputEditText) findViewById(R.id.upassword); // 최소6자리 이상

        btnCreate = (Button) findViewById(R.id.joinbtn);
        backbtn = (Button) findViewById(R.id.backbtn);

        // Initialize Firebase Auth, Firebase, 상기 초기화
        mAuth = FirebaseAuth.getInstance();

        btnCreate.setOnClickListener(new View.OnClickListener() { // 로그인 버튼 클릭시
            @Override
            public void onClick(View v) {
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("users"); // 학번으로 데이터베이스명 설정

                String stdno = StdNum.getText().toString().trim();
                String name = Name.getText().toString().trim();
                String phno = Phnum.getText().toString().trim();
                String id = Id.getText().toString().trim();
                String pw = Pw.getText().toString().trim();

                if (TextUtils.isEmpty(stdno)) {
                    StdNum.setError("학번을 입력해주세요"); // 학번이 공란일경우
                    StdNum.requestFocus();
                } else if (TextUtils.isEmpty(name)) {
                    Name.setError("이름을 입력해주세요"); // 이름이 공란일경우
                    Name.requestFocus();
                } else if (TextUtils.isEmpty(phno)) {
                    Phnum.setError("전화번호를 입력해주세요"); // 전화번호가 공란일경우
                    Phnum.requestFocus();
                } else if (TextUtils.isEmpty(id)) {
                    Id.setError("아이디[이메일형식]를 입력해주세요"); // 아이디가 공란일경우
                    Id.requestFocus();
                } else if (!id.contains("@")){
                    Id.setError("아이디는 이메일형식입니다.");
                    Id.requestFocus();
                }
                    else if (TextUtils.isEmpty(pw)) {
                    Pw.setError("비밀번호를 입력해주세요"); // 비밀번호가 공란일경우
                    Pw.requestFocus();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                    Query checkStdno = reference.orderByChild("stdNo").equalTo(stdno);

                    checkStdno.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                StdNum.setError("해당 학번이 이미 존재합니다.");
                                customToast(v, "이미 회원가입되어있습니다. 로그인 바랍니다.");
                            } else  {
                                UserHelperClass helper = new UserHelperClass(stdno, name, phno, id, pw); // helper를 통한 선언

                                mAuth.createUserWithEmailAndPassword(id, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) { // 작업이 성공적이라면
                                            customToast(v, "회원가입 되었습니다.");
                                            reference.child(stdno).setValue(helper); // 정상적인 회원가입이 되었다면 데이터베이스에 저장
                                            mAuth.signOut();
                                            startActivity(new Intent(NewId.this, MainActivity.class));
                                        } else {
                                            customToast(v, "회원가입 오류: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        }); // 가입버튼 클릭

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }); // 뒤로가기버튼 클릭

        StdNum.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Name.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 학번칸에서 엔터칠때

        Name.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Phnum.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 이름칸에서 엔터칠때

        Phnum.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Id.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 휴대폰칸에서 엔터칠때

        Id.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Pw.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 아이디칸에서 엔터칠때

        Pw.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    btnCreate.performClick();
                    return true;
                }
                return false;
            }
        }); // 비밀번호카에서 엔터칠때 가입버튼 클릭
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
