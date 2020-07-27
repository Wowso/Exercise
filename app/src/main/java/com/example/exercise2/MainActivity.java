package com.example.exercise2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Delayed;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; //블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; //블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; //문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; //수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; //버퍼 내 문자 저장 위치
    public static Context mContext;

    private Button buttonBlueSet; //블루투스 연결 설정
    private Button buttonRecord_exercise; //운동 기록 버튼
    private Button buttonTest3; //미정 버튼
    private Button buttonExercise; //운동 선택 버튼


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup(); //변수 객체 초기화


    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch (view.getId()) {
            case R.id.button_bluetooth_set:
                //intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                //startActivity(intent);
                bluetooth_setup();
                break;
            case R.id.button_exercise:
                intent = new Intent(MainActivity.this, Select_exercise.class);
                startActivity(intent);
                break;
            case R.id.button_record_exercise:
                intent = new Intent(MainActivity.this,Record_exercise.class);
                startActivity(intent);
                break;
            case R.id.button_test3:

                break;
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
        }
    }
    public void setup()
    {
        buttonBlueSet = (Button)findViewById(R.id.button_bluetooth_set);
        buttonBlueSet.setOnClickListener(this);
        buttonExercise = (Button)findViewById(R.id.button_exercise);
        buttonExercise.setOnClickListener(this);
        buttonRecord_exercise = (Button)findViewById(R.id.button_record_exercise);
        buttonRecord_exercise.setOnClickListener(this);
        buttonTest3 = (Button)findViewById(R.id.button_test3);
        buttonTest3.setOnClickListener(this);

        mContext=this;
    }

    public void bluetooth_setup()
    {
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
    }

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

    }
    public void connectDevice(String deviceName){
        //페어링 된 디바이스들을 모두 탐색
        for(BluetoothDevice tempDevice : devices){
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if(deviceName.equals(tempDevice.getName())){
                bluetoothDevice = tempDevice;
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
    }

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
                Log.d("ttt","데이터 수신");
                while (!Thread.currentThread().isInterrupted()){
                    try{
                        //데이터를 수신했는지 확인
                        int byteAvailable = inputStream.available();
                        Log.d("ttt","데이터 수신");
                        //데이터가 수신된 경우
                        if(byteAvailable > 0)
                        {
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);

                            //입력 스트림에서 바이트를 한 바이트씩 읽어 옴
                            for(int i = 0; i< byteAvailable; i++){
                                byte tempByte = bytes[i];
                                //개행문자를 기준으로 받음(한줄)
                                Log.d("ttt",new String(bytes, "US-ASCII"));
                                if(tempByte == '\n'){
                                    //readBuffer 배열을 encodedByte로 복사

                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    //인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "US-ASCII");
                                    Log.d("ttt",text);
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((Exercise)Exercise.fContext).MessagePr(text);
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
                        Thread.sleep(1000);

                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }

    void sendData(String text){
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

}
