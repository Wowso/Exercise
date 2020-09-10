package com.example.exercise2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exercise2.ui.exercise.ExerciseFragment;
import com.example.exercise2.ui.main.MainFragment;
import com.example.exercise2.ui.record.RecordFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화
    static final int REQ_ADD_FDATE = 1;
    static final int REQ_ADD_EDATE = 2;
    private static BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private static Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private static BluetoothDevice bluetoothDevice = null; //블루투스 디바이스
    private static BluetoothSocket bluetoothSocket = null; //블루투스 소켓
    private static OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private static InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private static Thread workerThread = null; //문자열 수신에 사용되는 쓰레드
    private static byte[] readBuffer; //수신 된 문자열을 저장하기 위한 버퍼
    private static int readBufferPosition; //버퍼 내 문자 저장 위치
    public static Context mContext;

    private TextView textView_name;
    private TextView textView;

    private int mYear =0;
    private int mMonth =0;
    private int mDay =0;

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    } //Toolbar 옵션 메뉴 만들기

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                bluetooth_setup();
                return true;
            case R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } //Toolbar 옵션 버튼 클릭시

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    } //선택한 Fragment를 띄워줌

    @Override
    public void onBackPressed() {
        String getFragment = new String();
        int index_num=0;
        View view = getVisibleFragment().getView();
        getFragment = getVisibleFragment().toString();
        index_num = getFragment.indexOf("{",12);
        if(index_num != -1)
            getFragment = getFragment.substring(0,index_num);

        if(getFragment.equals("MainFragment"))
        {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
            // 현재 표시된 Toast 취소
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                finish();
                toast.cancel();
            }
            super.onBackPressed();
        }//뒤로가기 두번시 종료(2초안에)
        else if(getFragment.equals("Exercise_Select_Fragment"))
        {
            Navigation.findNavController(view).navigate(R.id.action_nav_select_to_nav_main);
        }
        else if(getFragment.equals("ExerciseFragment"))
        {
            ExerciseFragment ef = getVisibleFragment_exercise();
            if(ef != null)
            {
                ef.End_flag(view);
            }
        }
        else if(getFragment.equals("RecordFragment"))
        {
            Navigation.findNavController(view).navigate(R.id.action_nav_record_to_nav_main);
        }
        else if(getFragment.equals("CommunityFragment"))
        {
            Navigation.findNavController(view).navigate(R.id.action_nav_community_to_nav_main);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK) { //사용을 눌렀을때
                    selectBluetoothDevice(); //블루투스 디바이스 선택 함수 호출
                }
                else { //취소를 눌렀을때
                    //처리할코드
                }
                break;
            case REQ_ADD_FDATE:
            case REQ_ADD_EDATE:
                if(resultCode == RESULT_OK)
                {
                    mYear = data.getExtras().getInt("mYear");
                    mMonth = data.getExtras().getInt("mMonth");
                    mDay = data.getExtras().getInt("mDay");

                    RecordFragment rf = getVisibleFragment_record();
                    if (rf != null) {
                        rf.date_picker(mYear, mMonth, mDay, requestCode);
                    }
                }
                break;
        }

    }

    public RecordFragment getVisibleFragment_record() {
        for(Fragment fragment: getSupportFragmentManager().getFragments())
        {
            if(fragment.isVisible())
            {
                return ((RecordFragment)fragment.getChildFragmentManager().getPrimaryNavigationFragment());
            }
        }
        return null;
    }

    public ExerciseFragment getVisibleFragment_exercise() {
        for(Fragment fragment: getSupportFragmentManager().getFragments())
        {
            if(fragment.isVisible())
            {
                return ((ExerciseFragment)fragment.getChildFragmentManager().getPrimaryNavigationFragment());
            }
        }
        return null;
    }

    public MainFragment getVisibleFragment_main() {
        for(Fragment fragment: getSupportFragmentManager().getFragments())
        {
            if(fragment.isVisible())
            {
                return ((MainFragment)fragment.getChildFragmentManager().getPrimaryNavigationFragment());
            }
        }
        return null;
    }

    public Fragment getVisibleFragment() {
        for(Fragment fragment: getSupportFragmentManager().getFragments())
        {
            if(fragment.isVisible())
            {
                return ((Fragment)fragment.getChildFragmentManager().getPrimaryNavigationFragment());
            }
        }
        return null;
    }

    private void setup() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_main,R.id.nav_select, R.id.nav_record, R.id.nav_community)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View view = navigationView.getHeaderView(0);
        textView_name = (TextView)view.findViewById(R.id.text_title);
        textView_name.setText("기기번호 : "+data_move("certifi_key"));
        textView = (TextView)view.findViewById(R.id.textView);
        textView.setText("인증되었습니다.");

        devices_check();


    } //Toolbar 및 네비게이션 활성화

    public void devices_check()
    {
        if(bluetoothDevice == null)
        {
            MainFragment mf = getVisibleFragment_main();
            if (mf != null) {
                mf.text_change(0);
            }
        }
        else if(bluetoothDevice !=null)
        {
            MainFragment mf = getVisibleFragment_main();
            if (mf != null) {
                mf.text_change(1);
            }
        }
    }

    public void open_datePicker(int Year, int Month, int Day, int key) {
        Intent intent;
        intent = new Intent(MainActivity.this, datePickerActivity.class);
        intent.putExtra("mYear", Year);
        intent.putExtra("mMonth", Month);
        intent.putExtra("mDay",Day);
        if(key == 1)
            startActivityForResult(intent, REQ_ADD_FDATE);
        else if(key == 2)
            startActivityForResult(intent, REQ_ADD_EDATE);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment).commit();
    } //Fragment 화면 전환 - 쓰이지않음

    public String data_move(String key) {
        Intent intent = getIntent();
        return intent.getExtras().getString(key);
    } //intent로 넘겨받은 데이터 출력

    public void bluetooth_setup() {
        //블루투스 활성화 부분
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        if(bluetoothAdapter == null) {//디바이스가 블루투스를 지원하지 않는 경우
            //처리 코드
            finish(); //강제 종료
        }else{
            if(bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            }
            else { //블루투스 비활성화 상태(블루투스off)
                //블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //선택한 값이 onActivityResult 함수에서 콜백
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }
    } //블루투스 초기 셋팅

    private void selectBluetoothDevice() {
        //이미 페어링 되어있는 블루투스 기기 찾기
        devices = bluetoothAdapter.getBondedDevices();

        //페어링 된 디바이스의 크기를 저장
        int pariedDeviceCount = devices.size();

        //페어링 되어있는 장치가 없는 경우

        if(pariedDeviceCount == 0){
            //페어링을 하기위한 함수 호출
        }
        //페어링을 하기위한 함수 호출
        else{
            //디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");

            //페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            //모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices)
            {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            //List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });
            //해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setCancelable(false);
            //다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    } //블루투스 디바이스 선택창

    public void connectDevice(String deviceName){
        //페어링 된 디바이스들을 모두 탐색
        for(BluetoothDevice tempDevice : devices){
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if(deviceName.equals(tempDevice.getName())){
                bluetoothDevice = tempDevice;
                devices_check();
                break;
            }
        }

        //UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            //데이터 송,수신 스트림을 얻어옵니다.
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            //데이터 수신 함수 호출
            receiveData();
        }catch (IOException e){
            e.printStackTrace();
        }
    } //블루투스 디바이스 선택

    private void receiveData() {
        final Handler handler = new Handler();
        //데이터를 수신하기 위한 버퍼를 생성
        Log.d("ttt","receive input");
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        //데이터를 수신하기 위한 쓰레드 생성
        Log.d("ttt","receive Thread Create");
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    try{
                        //데이터를 수신했는지 확인
                        int byteAvailable = inputStream.available();
                        //데이터가 수신된 경우
                        if(byteAvailable > 0)
                        {
                            Log.d("ttt","데이터 수신");
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);

                            //입력 스트림에서 바이트를 한 바이트씩 읽어 옴
                            for(int i = 0; i< byteAvailable; i++){
                                byte tempByte = bytes[i];
                                //개행문자를 기준으로 받음(한줄)

                                if(tempByte == '\n'){
                                    //readBuffer 배열을 encodedByte로 복사

                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    //인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ExerciseFragment tf = getVisibleFragment_exercise();
                                            if (tf != null) {
                                                tf.MessagePr(text);
                                            }
                                        }
                                    });
                                }// 개행문자가 아닐 경우
                                else{
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    try{
                        //1초마다 받아옴
                        Thread.sleep(10);

                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    } //블루투스를 통해 받은 데이터 출력

    public void sendData(String text){
        if(bluetoothDevice !=null)
        {
            //문자열에 개행문자("\n")를 추가
            text += "\n";
            Log.d("ttt",text);
            try{
                //데이터 송신
                outputStream.write(text.getBytes());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    } //블루투스를 통해 보내는 데이터
}
