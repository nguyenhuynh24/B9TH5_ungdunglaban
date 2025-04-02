package com.example.b9th5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private ImageView compassImageView;
    private TextView angleTextView;

    // Mảng lưu giá trị cảm biến
    private float[] accelerometerValues = new float[3];
    private float[] magnetometerValues = new float[3];
    // Ma trận xoay và góc hướng
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassImageView = findViewById(R.id.compassImageView);
        angleTextView = findViewById(R.id.angleTextView);

        // Lấy SensorManager và các cảm biến
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Kiểm tra xem thiết bị có cảm biến cần thiết không
        if (accelerometer == null || magnetometer == null) {
            Toast.makeText(this, "Device does not support required sensors", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký listener cho cả hai cảm biến
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký listener để tiết kiệm pin
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Lấy dữ liệu từ cảm biến
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerValues, 0, magnetometerValues.length);
        }

        // Tính toán ma trận xoay từ dữ liệu gia tốc và từ trường
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerValues);
        if (success) {
            // Lấy góc hướng từ ma trận xoay
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            // Góc azimuth (hướng bắc) nằm ở orientationAngles[0] (radian)
            float azimuthInRadians = orientationAngles[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

            // Chuẩn hóa góc về khoảng [0, 360]
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360;
            }

            // Xoay kim la bàn (âm để đảo chiều vì góc dương là ngược kim đồng hồ trong Android)
            compassImageView.setRotation(-azimuthInDegrees);

            // Hiển thị góc lệch so với hướng bắc
            angleTextView.setText("Angle: " + String.format("%.1f°", azimuthInDegrees));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Xử lý khi độ chính xác thay đổi (nếu cần)
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Toast.makeText(this, "Sensor accuracy is unreliable", Toast.LENGTH_SHORT).show();
        }
    }
}