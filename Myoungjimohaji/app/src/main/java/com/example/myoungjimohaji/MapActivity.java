package com.example.myoungjimohaji;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.core.utilities.Utilities;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.tasks.OnSuccessListener;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import noman.googleplaces.PlacesListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesListener {
    // 지도 사용을 위한 사전 선언
    private static final int REQUEST_CODE_PERMISSIONS = 1000;
    private FusedLocationProviderClient mFusedLocationClient;

    private GoogleMap gMap;

    Button btnBack, btnSearch, btnAll, btnFood, btnCafe, btnPlay, btnGPS, btnSchool, btnRandom;
    TextInputEditText editPlace;

    Marker selectedMarker; // 랜덤에서 선택될 마커
    List<Marker> previous_marker = null; // 내 주변마커 담을 변수

    List<Place> my_list = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnAll = (Button) findViewById(R.id.btnAll);
        btnFood = (Button) findViewById(R.id.btnFood);
        btnCafe = (Button) findViewById(R.id.btnCafe);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnGPS = (Button) findViewById(R.id.btnGPS);
        btnSchool = (Button) findViewById(R.id.btnSchool);
        btnRandom = (Button) findViewById(R.id.btnRandom);

        editPlace = (TextInputEditText) findViewById(R.id.editPlace);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // 현재의 내 위치를 받아올 객체 선언

        previous_marker = new ArrayList<Marker>(); // 마커 초기화

        LatLng campus_position = new LatLng(37.579883, 126.923398); // 인문캠 기준
        // 초기 생성 시 기준은 학교로 함
        Location standard_location = new Location(LocationManager.GPS_PROVIDER);
        standard_location.setLatitude(37.579883);
        standard_location.setLongitude(126.923398);

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
//                tedPermission(v);
                onLastLocationButtonClicked(v);
            }
        }); // GPS버튼 클릭시

        btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                finish();
                }
            }); // 뒤로가기 클릭시

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonClicked(v);
            }
        }); // 검색버튼 클릭시

        editPlace.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    btnSearch.performClick();
                    return true;
                }
                return false;
            }
        }); // 검색어 입력창 엔터시

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMap.clear();//지도 클리어
                if (previous_marker != null)
                    previous_marker.clear(); // 지역정보 마커 클리어

                showPlaceFood(campus_position, standard_location); // 음식점 검색 후 추가
                showPlaceCafe(campus_position, standard_location); // 카페 검색 후 추가
                showPlacePlay(campus_position, standard_location); // 놀거리 검색 후 추가
            }
        }); // 전체버튼

        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMap.clear();//지도 클리어

                if (previous_marker != null)
                    previous_marker.clear();//지역정보 마커 클리어

                showPlaceFood(campus_position, standard_location); // 음식점 검색 후 추가
            }
        }); // 음식버튼

        btnCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMap.clear();//지도 클리어

                if (previous_marker != null)
                    previous_marker.clear();//지역정보 마커 클리어

                showPlaceCafe(campus_position, standard_location); // 음식점 검색 후 추가
            }
        }); // 카페버튼

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMap.clear();//지도 클리어

                if (previous_marker != null)
                    previous_marker.clear();//지역정보 마커 클리어

                showPlacePlay(campus_position, standard_location); // 음식점 검색 후 추가
            }
        }); // 놀거리버튼

        btnSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaultLocation(gMap);
            }
        }); // 학교 근처로 위치 변경 및 학교 아이콘 표시

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = null;
                if (my_list.isEmpty()) {
                    customToast("추첨할 내용이 없습니다.");
                    return;
                }

                Place randomPlace = randomPlace(my_list); // 랜덤위치 한개 추첨
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapActivity.this);

                LatLng latLng = new LatLng(randomPlace.getLatitude(), randomPlace.getLongitude());

                if (randomPlace.getTypes()[0].contains("restaurant") || randomPlace.getTypes()[0].contains("meal_takeaway")) { // 장소 유형이 음식점이라면
                    type = "먹거리";
                } else if (randomPlace.getTypes()[0].contains("cafe")) { // 장소 유형이 카페라면
                    type = "카페";
                } else {
                    type = "놀거리";
                }

                dlg.setTitle("추천: " + type);
                dlg.setMessage(randomPlace.getName() + "가 추천되었습니다.");
                dlg.setPositiveButton("좋아요!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); // 줌을 해주고싶지만 Google map api 이슈때문에 불가능..
                        dialog.dismiss();
                    }
                });

                dlg.setNegativeButton("싫어요 :(", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnRandom.performClick();
                        dialog.dismiss();
                        return;
                    }
                });

                dlg.show();
            }
        }); // 랜덤버튼 클릭시 현재 표시된 마커 중 랜덤으로 추천해서 표시함
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);
        setDefaultLocation(gMap);
    } // 지도생성


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onLastLocationButtonClicked(View view) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSIONS);
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(MapActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .title("현재 위치")
                                .snippet(location.getLatitude() + "/" + location.getLongitude()));

                    gMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                }
                else{
//                    Toast.makeText(this,"권한 체크 거부됨",Toast.LENGTH_SHORT).show();
                    gMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
                }
            }
        });
    } // 내위치 가져오기

    public void searchButtonClicked(View view) {
        //찾는 위치는 editText에 담겨있고, 그 데이터를 가져온다.
        String place = editPlace.getText().toString();

        if (TextUtils.isEmpty(place)) {
            customToast("검색할 내용을 입력해주세요");
            return;
        }

        //구글의 Geocoder을 활용해 그 데이터의 정보를 가져온다.
        Geocoder coder = new Geocoder(getApplicationContext());
        //리스트에 담아주고,
        List<Address> list = null;
        try{
            list = coder.getFromLocationName(place,1);
        } catch(IOException e){
            e.printStackTrace();
        }
        if (list.size() > 0) {
            //그 정보의 좌표값을 가져온다.
            Address addr = list.get(0);
            double lat = addr.getLatitude();
            double lng = addr.getLongitude();
            //이제 등록을 하여서
            LatLng geoPoint = new LatLng(lat,lng);
            //카메라를 줌해주고
            gMap.animateCamera(CameraUpdateFactory.newLatLng(geoPoint));
            //마커를 등록해준다.
            MarkerOptions marker = new MarkerOptions();
            marker.position(geoPoint);
            marker.title(place).snippet(lat + "/" + lng);
            gMap.addMarker(marker);
        } else {
            customToast("검색결과가 없습니다.");
        }
    } // 단일항목 검색

    @Override
    public void onPlacesFailure(PlacesException e) {

    } // 사용안함

    @Override
    public void onPlacesStart() {

    } // 사용안함

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                my_list = new ArrayList<>(); // 내 리스트 초기화
                for (noman.googleplaces.Place place : places) {

                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    String markerSnippet = getCurrentAddress(place.getLatitude(), place.getLongitude()); // 현재위치 기준일 경우

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    if (place.getTypes()[0].contains("restaurant") || place.getTypes()[0].contains("meal_takeaway")) { // 장소 유형이 음식점 또는 포장전문이라면
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.food_icon));
                    } else if (place.getTypes()[0].contains("cafe")) { // 장소 유형이 카페라면
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cafe_icon));
                    } else {
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.play_icon));
                    }

                    Marker item = gMap.addMarker(markerOptions);
                    previous_marker.add(item);
                    if (place != null) {
                        my_list.add(place);
                    }
                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
    } // 위치찾기 성공 시

    public String getCurrentAddress(double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation( latitude, longitude, 100);
        } catch (IOException ioException) { //네트워크 문제
            customToast("지오코더 서비스 사용불가");
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            customToast("잘못된 GPS좌표");
            return "잘못된 GPS 좌표";
        } if (addresses == null || addresses.size() == 0) {
            customToast("주소 미발견");
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    } // 현재위치 문자형반환

    @Override
    public void onPlacesFinished() {

    } // 사용안함

    public void showPlaceFood(LatLng latlng, Location location) {

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) // 음식점
                .build()
                .execute();

        gMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    public void showPlacePlay(LatLng latlng, Location location) {

        new NRPlaces.Builder() // 볼링장
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.BOWLING_ALLEY)
                .build()
                .execute();

        new NRPlaces.Builder() // 헬스장
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.GYM)
                .build()
                .execute();

        new NRPlaces.Builder() // 영화관
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.MOVIE_THEATER)
                .build()
                .execute();

        new NRPlaces.Builder() // 쇼핑몰
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.SHOPPING_MALL)
                .build()
                .execute();

        new NRPlaces.Builder() // 사우나
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.SPA)
                .build()
                .execute();

        gMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    public void showPlaceCafe(LatLng latlng, Location location) {

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("Your_Google_Places_Key")
                .latlng(location.getLatitude(), location.getLongitude()) // 현재 기준위치
                .radius(500) // 500 미터 내에서 검색
                .type(PlaceType.CAFE) // 카페
                .build()
                .execute();

        gMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    public void setDefaultLocation(GoogleMap googleMap) {
        gMap = googleMap;
        //디폴트 위치, 인문캠퍼스
        LatLng DEFAULT_LOCATION = new LatLng(37.579883, 126.923398);
        String markerTitle = "명지대학교";
        String markerSnippet = "인문캠퍼스";

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school_icon));

        gMap.addMarker(markerOptions);
        gMap.animateCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    private void tedPermission(View v) {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                customToast("권한 허가");
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                customToast("권한 거부\n" + deniedPermissions.toString());
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
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

    public Place randomPlace(List<Place> list) {
        return list.get((int) Math.floor((Math.random() * list.size())));
    }

}
