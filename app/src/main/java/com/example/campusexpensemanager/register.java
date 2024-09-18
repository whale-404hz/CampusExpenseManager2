package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class register extends AppCompatActivity {

    EditText etUsername, etPassword, etEmail;
    Button btRegister;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo các view và trợ giúp cơ sở dữ liệu
        etUsername = findViewById(R.id.edtUser);
        etPassword = findViewById(R.id.edtPass);
        etEmail = findViewById(R.id.edtGmail);
        btRegister = findViewById(R.id.btnRegister);
        dbHelper = new DatabaseHelper(this);

        // Lắng nghe sự kiện khi nút đăng ký được nhấn
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Kiểm tra hợp lệ của các trường nhập liệu
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(register.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(register.this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
                } else {
                    // Thêm người dùng vào cơ sở dữ liệu
                    boolean success = dbHelper.addUser(username, email, password);
                    if (success) {
                        Toast.makeText(register.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(register.this, Login.class);
                        startActivity(intent);
                        // Chuyển đến trang đăng nhập hoặc trang chủ sau khi đăng ký thành công
                        // startActivity(new Intent(Register.this, LoginActivity.class));
                    } else {
                        Toast.makeText(register.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}