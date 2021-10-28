package com.example.image_upload_database_example;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {
    private static String ip = "172.30.1.31";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "testDatabase";
    private static String userNmae = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://" + ip + ":" + port +"/" + database;

    private Connection connection = null;

    private TextView textView;
    private EditText editTextIndex;
    private EditText editTextFileName;
    private ImageView imageView;

    private Button fetchBtn, insertBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

        textView = findViewById(R.id.textViewStatus);
        editTextIndex = findViewById(R.id.editTextNumber);
        editTextFileName = findViewById(R.id.editTextFileName);
        imageView = findViewById(R.id.imageView);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try{
            Class.forName(Classes);
            connection = DriverManager.getConnection(url, userNmae, password);
            textView.setText("SUCCESS");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            textView.setText("ERROR");
        } catch (SQLException e){
            e.printStackTrace();
            textView.setText("FAILURE");
        }

        fetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringFilePath = Environment.getExternalStorageDirectory().getPath() + "/Download/" +
                        editTextFileName.getText().toString() + ".jpeg";

                Bitmap bitmap = BitmapFactory.decodeFile(stringFilePath);

                imageView.setImageBitmap(bitmap);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                byte[] bytesImage = byteArrayOutputStream.toByteArray();

                try{
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO TEST (C2, Image) VALUES (?, ?)");
                    preparedStatement.setInt(1, Integer.valueOf(editTextIndex.getText().toString()));
                    preparedStatement.setBytes(2, bytesImage);

                    preparedStatement.execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Statement statement = connection.createStatement();

                    ResultSet resultSet = statement.executeQuery("SELECT Image from TEST where C2 = " + editTextIndex.getText().toString() + ";");
                    resultSet.next();
                    byte[] bytesImageDB = resultSet.getBytes(1);
                    Bitmap bitmapImageDB = BitmapFactory.decodeByteArray(bytesImageDB, 0, bytesImageDB.length);

                    imageView.setImageBitmap(bitmapImageDB);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}