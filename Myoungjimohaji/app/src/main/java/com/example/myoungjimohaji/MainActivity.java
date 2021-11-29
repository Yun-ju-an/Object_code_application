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
    // 오브젝트 선언
    TextInputEditText IdText, PwText; // 아이디와 비밀번호를 담을 객체명할당
    Button Login, Loginanonymus, NewId, Findbtn, idLogin, stdnoLogin; // 로그인, 회원가입, 아이디, 비밀번호찾기 버튼 객체명 할당
    TextInputLayout idLayout;

    // 클라우드 데이터베이스 연결 관련 인스턴스 선언
    private FirebaseAuth mAuth;

    String status = "아이디";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        tedPermission(); // 위치관련 권한요청

        // 연결화면 정리구분
        Intent login = new Intent(MainActivity.this, MainPortal.class); // 메인포털 연결
        Intent newid = new Intent(MainActivity.this, NewId.class); // 회원가입창 연결
        Intent findid = new Intent(MainActivity.this, FindActivity.class); // 아이디/비밀번호 찾기 창 연결

        // 실제 화면과 오브젝트 연결
        IdText = (TextInputEditText) findViewById(R.id.idText); // 아이디입력창
        PwText = (TextInputEditText) findViewById(R.id.pwText); // 비밀번호입력창

        Login = (Button) findViewById(R.id.Loginbtn); // 로그인버튼
        NewId = (Button) findViewById(R.id.NewIdbtn); // 회원가입버튼
        Findbtn = (Button) findViewById(R.id.Findbtn); // 아이디/비밀번호찾기버튼
        Loginanonymus = (Button) findViewById(R.id.Loginanonymus); // 익명로그인버튼
        idLogin = (Button) findViewById(R.id.idLogin); // 아이디로로그인버튼
        stdnoLogin = (Button) findViewById(R.id.stdnoLogin);  // 학번으로로그인버튼

        idLayout = (TextInputLayout) findViewById(R.id.idLayout);

        // Initialize Firebase Auth, Firebase, 상기 초기화
        mAuth = FirebaseAuth.getInstance();

        Login.setOnClickListener(new View.OnClickListener() { // 로그인버튼 클릭시
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                } catch (Exception e) { }


                if (status == "아이디") {
                    String id = IdText.getText().toString().trim(); // 아이디입력창에서 아이디 받아오기
                    String pw = PwText.getText().toString().trim(); // 비밀번호입력창에서 비밀번호 받아오기
                    if (TextUtils.isEmpty(id)) {
                        IdText.setError("아이디를 입력해주세요"); // 아이디가 공란일경우
                        IdText.requestFocus();
                    } else if (TextUtils.isEmpty(pw)) {
                        PwText.setError("비밀번호를 입력해주세요"); // 비밀번호가 공란일경우
                        PwText.requestFocus();
                    } else {

                        findStdno(v, login, id);

                        mAuth.signInWithEmailAndPassword(id, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    customToast(v, "로그인 되었습니다.");
                                    startActivity(login);
                                } else {
                                    customToast(v, "로그인에 실패하였습니다.");
                                }
                            }
                        });
                    }
                } else if (status == "학번") {
                    String id = IdText.getText().toString(); // 아이디입력창에서 아이디 받아오기
                    String pw = PwText.getText().toString(); // 비밀번호입력창에서 비밀번호 받아오기
                    if (TextUtils.isEmpty(id)) {
                        IdText.setError("아이디를 입력해주세요"); // 아이디가 공란일경우
                        IdText.requestFocus();
                    } else if (TextUtils.isEmpty(pw)) {
                        PwText.setError("비밀번호를 입력해주세요"); // 비밀번호가 공란일경우
                        PwText.requestFocus();
                    } else {
                        stdUser(v, login);
                    }
                }
            }
        }); // 로그인버튼 클릭시

        Loginanonymus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInAnonymously().addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            customToast(v, "비회원으로 로그인합니다.");
                            startActivity(login);
                        } else {
                            customToast(v, "비회원로그인 실패");
                        }
                    }
                });
            }
        }); // 비회원로그인버튼 클릭시

        NewId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(newid);
            }
        }); // 회원가입버튼 클릭시

        Findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(findid);
            }
        }); // 아이디/비밀번호 찾기 클릭시

        idLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "아이디";
                idLayout.setHint(status);
            }
        }); // 아이디로 로그인되게

        stdnoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "학번";
                idLayout.setHint(status);
            }
        }); // 학번으로 로그인되게

        IdText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    PwText.requestFocus();
                    return true;
                }
                return false;
            }
        }); // 아이디에서 엔터칠때

        IdText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Login.performClick();
                    return true;
                }
                return false;
            }
        }); // 비밀번호에서 엔터치면 로그인버튼 클릭

    }

    // 활동을 초기화할 때 사용자가 현재 로그인되어있는지 확인함(기존 로그인 내역 확인)
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(MainActivity.this, MainPortal.class)); // 이미 로그인이 되어있다면 메인포털로 이동하기
        }
    }

    private void findStdno(View v, Intent intent,String userId) { // 아이디로 로그인시 사용자 정보를 가져오기 위한 함수
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkStdno = reference.orderByChild("stdNo");

        checkStdno.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    IdText.setError(null);
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue(); // 검색결과가 된 snapshot을 모두 hasmap에 집어넣음

                    String userStdno = null; // 찾은 후에 넣을 key값을 미리 만들어둠

                    for (Map.Entry<String, Object> entry: map.entrySet()) { // 반복문으로 hasmap객체에 있는 내용을 반복함
                        String temp = snapshot.child(entry.getKey()).child("id").getValue(String.class); // 객체 내부에 있는 id와 user가 입력한 id가 일치하면
//                        customToast(v, entry.getKey()); // 잘 조회되는지 확인하기 위함
                        if (temp.equals(userId)) {
                            userStdno = entry.getKey(); // 반복을 정지하고 해당 key값을 찾아옴
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
                        customToast(v, "데이터베이스 연결 실패");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                customToast(v, "데이터베이스 에러" + error);
            }
        });
    } // 아이디로 로그인시 데이터베이스 연결

    private void stdUser(View v, Intent intent) { // 학번로그인시 사용되는 내용 -> 방식 == 학번으로 사용자데이터베이스 구축 -> 학번입력시 비밀번호를 갖고와 맞는지 확인하고 맞다면 로그인
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

                                    customToast(v, "로그인 되었습니다.");

                                    startActivity(intent);
                                } else {
                                    customToast(v, "로그인에 실패하였습니다.");
                                }
                            }
                        });

                    } else {
                        PwText.setError("잘못된 비밀번호입니다.");
                        PwText.requestFocus();
                    }
                }
                else {
                    IdText.setError("해당 학번이 존재하지 않습니다.");
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
                // 권한 요청 성공
                //Toast.makeText(LoginActivity.this, "권한 성공", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                // Toast.makeText(LoginActivity.this, "권한 실패", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
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