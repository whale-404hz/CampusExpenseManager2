package com.example.campusexpensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "qlct.db"; // Tên cơ sở dữ liệu
    private static final int DATABASE_VERSION = 2; // Phiên bản cơ sở dữ liệu
    private final Context context;
    private String databasePath;
    private SQLiteDatabase database;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
    }

    // Kiểm tra xem cơ sở dữ liệu đã tồn tại hay chưa
    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            String path = databasePath;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // Cơ sở dữ liệu chưa tồn tại
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    // Sao chép cơ sở dữ liệu từ thư mục assets vào thư mục databases của ứng dụng
    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME); // Đọc cơ sở dữ liệu từ assets
        String outFileName = databasePath; // Nơi cơ sở dữ liệu sẽ được lưu trữ

        File databaseDir = new File(databasePath).getParentFile();
        if (!databaseDir.exists()) {
            databaseDir.mkdirs();
        }

        OutputStream output = new FileOutputStream(outFileName); // Tạo luồng ghi vào database

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }

    // Tạo cơ sở dữ liệu nếu chưa tồn tại
    public void createDatabase() throws IOException {
        boolean dbExists = checkDatabase();
        if (!dbExists) {
            try {
                copyDatabase(); // Sao chép cơ sở dữ liệu từ thư mục assets nếu chưa tồn tại
            } catch (IOException e) {
                throw new Error("Lỗi khi sao chép cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);
        insertSampleData(db); // Thêm dữ liệu mẫu
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Thêm dữ liệu mẫu vào bảng users
    private void insertSampleData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, "john_doe");
        values.put(COLUMN_EMAIL, "john@example.com");
        values.put(COLUMN_PASSWORD, hashPassword("password123")); // Mã hóa mật khẩu
        db.insert(TABLE_USERS, null, values);

        values.put(COLUMN_USERNAME, "jane_doe");
        values.put(COLUMN_EMAIL, "jane@example.com");
        values.put(COLUMN_PASSWORD, hashPassword("password456")); // Mã hóa mật khẩu
        db.insert(TABLE_USERS, null, values);
    }
    //cai nay de co loi thi xoa
    public String getPasswordForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = null;
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            hashedPassword = cursor.getString(0);
        }
        cursor.close();
        return hashedPassword;
    }

    // Cập nhật mật khẩu cho người dùng dựa trên tên người dùng
    public boolean updatePasswordForUser(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", hashPassword(newPassword)); // Băm mật khẩu trước khi lưu

        // Cập nhật mật khẩu cho user có tên username
        int rows = db.update("users", values, "username = ?", new String[]{username});
        return rows > 0;
    }
    //toi khuc nay
    // Hàm băm mật khẩu bằng SHA-256
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi khi mã hóa mật khẩu", e);
        }
    }

    // Thêm người dùng vào cơ sở dữ liệu
    public boolean addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password)); // Mã hóa mật khẩu trước khi lưu

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // Trả về true nếu thêm thành công
    }
}
