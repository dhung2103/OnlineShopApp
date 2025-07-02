# Google Maps Setup Guide

## Vấn đề: Bản đồ hiển thị trắng

Để khắc phục vấn đề bản đồ hiển thị trắng, bạn cần thực hiện các bước sau:

## 1. Lấy Google Maps API Key

### Bước 1: Tạo API Key
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Kích hoạt **Maps SDK for Android** API:
   - Đi tới APIs & Services > Library
   - Tìm kiếm "Maps SDK for Android"
   - Click và Enable API

### Bước 2: Tạo API Key
1. Đi tới APIs & Services > Credentials
2. Click "Create Credentials" > "API Key"
3. Copy API key được tạo

### Bước 3: Giới hạn API Key (Recommended)
1. Click vào API key vừa tạo
2. Trong "Application restrictions", chọn "Android apps"
3. Thêm package name: `com.example.onlineshopapp`
4. Thêm SHA-1 fingerprint của debug keystore:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

## 2. Cập nhật API Key trong project

### Bước 1: Cập nhật local.properties
Mở file `local.properties` và thay thế `YOUR_ACTUAL_MAPS_API_KEY_HERE` bằng API key thực:

```properties
MAPS_API_KEY=AIzaSyBvOkBw_gLQ4yOLHaEhMQoPuBCS0gA5C30
```

### Bước 2: Rebuild project
```bash
./gradlew clean build
```

## 3. Kiểm tra các permission

Đảm bảo file `AndroidManifest.xml` có các permission sau:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 4. Debugging

Nếu bản đồ vẫn hiển thị trắng:

### Kiểm tra Logcat
Tìm các error liên quan đến:
- API key issues
- Network connectivity
- Location permissions

### Các lỗi thường gặp:

1. **API key không hợp lệ**:
   ```
   Google Maps Android API: Authorization failure
   ```
   → Kiểm tra lại API key và package name

2. **API chưa được enable**:
   ```
   Google Maps Android API: This API project is not authorized to use this API
   ```
   → Enable Maps SDK for Android trong Google Cloud Console

3. **Network issues**:
   ```
   Failed to load map
   ```
   → Kiểm tra kết nối internet và firewall

## 5. Test với mock data

Nếu API vẫn chưa hoạt động, app sẽ fallback về mock data từ `MockApiHelper`. Đảm bảo mock data có tọa độ hợp lệ:

```kotlin
StoreLocation(
    id = 1,
    name = "Main Store",
    address = "456 Shop Street, Shopping District, City",
    latitude = 10.7769, // Ho Chi Minh City coordinates
    longitude = 106.7009,
    phone = "+84 123 456 789",
    operatingHours = "Mon-Sat: 9:00 - 20:00, Sun: 10:00 - 18:00"
)
```

## 6. Kiểm tra device/emulator

- Đảm bảo device/emulator có Google Play Services
- Emulator cần sử dụng image có Google APIs
- Kiểm tra GPS settings trên device

## Troubleshooting Commands

```bash
# Clean và rebuild
./gradlew clean build

# Kiểm tra dependencies
./gradlew app:dependencies

# Kiểm tra API key trong built APK
aapt dump badging app-debug.apk | grep google
```
