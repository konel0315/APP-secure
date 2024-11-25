package com.example.basicapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class example1 extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.bt1);  // R.layout.bt1에 로그인 레이아웃이 있다고 가정

        // 입력 칸 초기화
        emailEditText = findViewById(R.id.editTextTextEmailAddress);  // 이메일 입력칸 ID
        passwordEditText = findViewById(R.id.editTextTextPassword);  // 비밀번호 입력칸 ID

        // 로그인 버튼 초기화
        Button loginButton = findViewById(R.id.button);  // 로그인 버튼 ID
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // POST 요청 비동기 실행
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendPostRequest();
                    }
                }).start();
            }
        });
    }

    private void sendPostRequest() {
        String urlString = "http://222.107.221.139:8080/api/question/login";  // 서버 URL (비워둠)
        String username = emailEditText.getText().toString().trim();  // 이메일 입력값
        String password = passwordEditText.getText().toString().trim();  // 비밀번호 입력값

        // JSON 데이터 생성
        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\",\n" +
                "    \"password\": \"" + password + "\"\n" +
                "}";

        try {
            // 서버 URL 연결
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);  // Output stream 사용

            // JSON 데이터 전송
            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));  // UTF-8로 인코딩하여 전송
            os.flush();
            os.close();

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 서버 응답 처리
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 서버 응답을 확인하고, 성공 메시지 표시
                if (response.toString().equals("0")) {
                    // 응답이 "1"이면 Toast로 축하 메시지 출력
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(example1.this, "축하합니다", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 응답이 0이 아닌 경우 다른 처리
                    Log.e("PostRequestError", "서버 응답: " + response.toString());
                }
            } else {
                // 응답 실패
                Log.e("PostRequestError", "POST 요청 실패: 응답 코드 " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            // 예외 발생 시 로그 출력
            Log.e("PostRequestError", "POST 요청 실패", e);
        }
    }
}