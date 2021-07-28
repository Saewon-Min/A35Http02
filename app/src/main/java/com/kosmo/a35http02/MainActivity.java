package com.kosmo.a35http02;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String TAG = "KOSMO123";

    EditText user_id, user_pw;
    TextView textResult;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textResult = (TextView)findViewById(R.id.text_result);
        user_id = (EditText)findViewById(R.id.user_id);
        user_pw = (EditText)findViewById(R.id.user_pw);
        Button btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            /*
            execute()를 통해 서버와 통신한다.
            이때 전달되는 파라미터는 총 3개이다.
            첫번째는 요청URL, 두번째와 세번째는 서버로 전송될
            아이디/패스워드이다.
             */
                new AsyncHttpRequest().execute(
                        "http://192.168.0.10:8082/jsonrestapi/android/memberLogin.do",
                        "id="+user_id.getText().toString(),
                        "pass="+user_pw.getText().toString()
                );
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setCancelable(true); // back 버튼을 누르면 대화창 닫힘
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIcon(android.R.drawable.ic_dialog_email);
        dialog.setTitle("로그인 처리중");
        dialog.setMessage("서버로부터 응답을 기다리고 있습니다.");

    } //// onCreate()

    class AsyncHttpRequest extends AsyncTask<String, Void, String>{

        // doInBackground() 메소드 실행 전에 호출됨
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 진행 대화창 띄워주기
            if(!dialog.isShowing()){
                dialog.show();
            }
        }


        // execute() 호출시 전달된 3개의 파라미터를 가변인자를 통해 받아옴
        @Override
        protected String doInBackground(String... strings) {

            StringBuffer receiveData = new StringBuffer();

            try{
                URL url = new URL(strings[0]); // 파라미터1 : 요청 URL
                HttpURLConnection conn =
                        (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStream out = conn.getOutputStream();
                out.write(strings[1].getBytes()); // 파라미터2 : 아이디
                out.write("&".getBytes()); // &를 사용하여 쿼리스트링 형태로 만들어준다.
                out.write(strings[2].getBytes()); // 파라미터3 : 패스워드
                out.flush();
                out.close();

                if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                    // Rest API 서버에서 내려주는 JSON 데이터를 읽어서 저장한다.
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(),
                                    "UTF-8")
                    );

                    String data;

                    while((data=reader.readLine()) !=null){
                        // 내용을 한줄씩 읽어서 StringBuffer 객체에 저장한다.
                        receiveData.append(data+"\r\n");
                    }
                    reader.close();
                }else {
                    Log.i(TAG, "HTTP OK 연결 실패");
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            // 저장된 내용을 로그로 출력한 후 onPostExecute()로 반환한다.
            Log.i(TAG, receiveData.toString());
            return receiveData.toString();
        }


        // 통신 중 진행 사항은 해당 메소드에서 확인(여기서는 사용안함)
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        // doInBackground()가 정상 종료되면 해당 함수가 호출된다.
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            StringBuffer sb = new StringBuffer();
            try{
                /*
                {
                "isLogin":1,
                "memberInfo":{
                        "id":"kosmo","pass":"1234","name":"코스모","regidate":"2021-05-17"
                        }
                }
                 */

                // JSON 객체를 1차로 파싱함

                JSONObject jsonObject = new JSONObject(s);

                // 로그인 성공 여부를 판단해야함
                int success = Integer.parseInt(jsonObject.getString("isLogin"));

                if(success==1){
                    sb.append("로그인 성공\n");

                    // 객체 안에 회원 정보를 저장한 또 하나의 JSON 객체가 있으므로 파싱
                    String id = jsonObject.getJSONObject("memberInfo").getString("id").toString();
                    String pass = jsonObject.getJSONObject("memberInfo").getString("pass").toString();
                    String name = jsonObject.getJSONObject("memberInfo").getString("name").toString();

                    sb.append("회원정보\n");
                    sb.append("아이디 : "+id+"\n");
                    sb.append("패스워드 : "+pass+"\n");
                    sb.append("이름 : "+name+"\n");

                }else {
                    sb.append("로그인 실패");
                }


            }catch (Exception e){
                e.printStackTrace();
            }

            // 대화창 닫기
            dialog.dismiss();
            // 파싱된 내용을 텍스트뷰와 토스트로 출력
            textResult.setText(sb.toString());
            Toast.makeText(getApplicationContext(),
                    sb.toString(),
                    Toast.LENGTH_LONG).show();

        }
    }
}