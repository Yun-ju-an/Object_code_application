package com.example.myoungjimohaji;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainPortal extends AppCompatActivity {
    Button mypagebtn, mapbtn, btnSend;

    FirebaseAuth mAuth;

    // 대화창
    TextInputEditText sendMessage;
    ListView listView;
    ArrayList<MessageItem> messageItems = new ArrayList<>();
    ChatAdapter adapter;
    FirebaseDatabase firebaseChat;
    DatabaseReference chatRef;

    static String userName, userPhNo, userStdNo, userId, userPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainportal);

        Intent mypage = new Intent(MainPortal.this, MyPageActivity.class);
        Intent map = new Intent(MainPortal.this, MapActivity.class);

        loadAllUserData(); // 인텐트를 통해 유저정보를 전달받음

        mypagebtn = (Button) findViewById(R.id.mypagebtn);
        mapbtn = (Button) findViewById(R.id.mapbtn);

        // 대화창 구현부분
        btnSend = (Button) findViewById(R.id.btnSend);
        sendMessage = (TextInputEditText) findViewById(R.id.sendMessage);
        listView = (ListView) findViewById(R.id.listview);

        mAuth = FirebaseAuth.getInstance();

        try {
            if (mAuth.getCurrentUser().isAnonymous()) {
                sendMessage.setEnabled(false);
                sendMessage.setText("비회원은 회원가입 후 이용 가능합니다.");
            } // 회원일 경우만 채팅창 활성화
        } catch (Exception e) {
            Intent main = new Intent(MainPortal.this, MainActivity.class);
            customToast("오류: " + e);
            startActivity(main);
        }

        adapter = new ChatAdapter(messageItems, getLayoutInflater(), userStdNo);
        listView.setAdapter(adapter);

        //Firebase DB관리 객체와 'caht'노드 참조객체 얻어오기
        firebaseChat= FirebaseDatabase.getInstance();
        chatRef= firebaseChat.getReference("chat");

//        firebaseDB에서 채팅 메세지들 실시간 읽어오기..
//        'chat'노드에 저장되어 있는 데이터들을 읽어오기
//        chatRef에 데이터가 변경되는 것을 듣는 리스너 추가
        chatRef.addChildEventListener(new ChildEventListener() {
            //새로 추가된 것만 줌 ValueListener는 하나의 값만 바뀌어도 처음부터 다시 값을 줌
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //새로 추가된 데이터(값 : MessageItem객체) 가져오기
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
//                customToast(messageItem.getStdNo());

                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                messageItems.add(messageItem);

                //리스트뷰를 갱신
                adapter.notifyDataSetChanged();
                listView.setSelection(messageItems.size() - 1); //리스트뷰의 마지막 위치로 스크롤 위치 이동
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mypagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user.isAnonymous()) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainPortal.this);
                    dlg.setTitle("로그인후 이용 바랍니다.");
                    dlg.setMessage("비회원은 내정보를 이용하실 수 없습니다.");

                    dlg.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.getCurrentUser().delete();
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dlg.show();
                } else {
                    sendAllUserData(mypage, userName, userPhNo, userStdNo, userId, userPw); // 인텐트를 통해 유저정보를 전달함
                    startActivity(mypage);
                }
            }
        }); // 내정보페이지 클릭시

        mapbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(map);
            }
        }); // 맵 페이지 클릭시

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser().isAnonymous()) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainPortal.this);
                    dlg.setTitle("로그인후 이용 바랍니다.");
                    dlg.setMessage("비회원은 내정보를 이용하실 수 없습니다.");

                    dlg.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.getCurrentUser().delete();
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dlg.show();
                } else {
                    if (TextUtils.isEmpty(sendMessage.getText().toString().trim())) {
                        sendMessage.setError("내용을 적어주세요");
                        sendMessage.requestFocus();
                    } else {
                        //firebase DB에 저장할 값들( 닉네임, 메세지, 프로필 이미지URL, 시간)
                        String stdNo = userStdNo;
                        String message= sendMessage.getText().toString();

                        //메세지 작성 시간 문자열로..
                        Calendar calendar= Calendar.getInstance(); //현재 시간을 가지고 있는 객체
                        String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

                        //firebase DB에 저장할 값(MessageItem객체) 설정
                        MessageItem messageItem= new MessageItem(userStdNo, message, time);
                        //'char'노드에 MessageItem객체를 통해
                        chatRef.push().setValue(messageItem);

                        //EditText에 있는 글씨 지우기
                        sendMessage.setText("");

                        //소프트키패드를 안보이도록..
                        InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                    }
                }
            }
        }); // 메시지 전송

    }

    private void loadAllUserData() { // 인텐트를 통해 정보를 로드
        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String pNo = intent.getStringExtra("pNo");
        String stdNo = intent.getStringExtra("stdNo");
        String id = intent.getStringExtra("id");
        String pw = intent.getStringExtra("pw");

        userName = name;
        userPhNo = pNo;
        userStdNo = stdNo;
        userId = id;
        userPw = pw;
//        t1.setText(name); 데이터 넘어가는지 확인하기 위한 부분
//        t2.setText(pNo);
//        t3.setText(stdNo);
//        t4.setText(id);
//        t5.setText(pw);
    }

    public void sendAllUserData(Intent intent, String name, String pNo, String stdNo, String id, String pw) {
        intent.putExtra("name", name);
        intent.putExtra("pNo", pNo);
        intent.putExtra("stdNo", stdNo);
        intent.putExtra("id", id);
        intent.putExtra("pw", pw);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainPortal.this, MainActivity.class));
        } else if (TextUtils.isEmpty(userStdNo) && !mAuth.getCurrentUser().isAnonymous()) {
            mAuth.signOut();
            startActivity(new Intent(MainPortal.this, MainActivity.class));
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

}
