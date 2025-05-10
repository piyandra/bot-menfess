# Menfess Bot - Telegram Anonymous Message Bot

## Disclaimer
**CATATAN PENTING: Aplikasi ini hanya untuk penggunaan pribadi dan non-komersial. Dilarang keras menggunakan untuk tujuan komersial.**

## Fitur Utama

### 1. Pengiriman Pesan Anonim (Menfess)
- Kirim pesan teks, foto, atau video secara anonim ke channel telegram
- Dukungan untuk berbagai format media (foto, video, teks)
- Tampilan konfirmasi pengiriman yang interaktif dan menarik
- Pelacakan pesan dan status pengiriman

### 2. Validasi Pesan
- Sistem validasi pesan otomatis untuk memastikan kualitas konten
- Persyaratan minimal 3 kata dalam pesan
- Wajib menggunakan setidaknya satu hashtag (#) untuk pengkategorian
- Pemberitahuan langsung jika pesan tidak memenuhi standar

### 3. Notifikasi Pengguna
- Pemberitahuan real-time saat pesan berhasil terkirim
- Notifikasi saat seseorang membalas pesan anonim anda
- Format notifikasi yang menarik dengan emoji dan tautan langsung ke pesan
- Variasi gaya pesan untuk pengalaman pengguna yang lebih menyenangkan

## Kontak dan Bantuan
Untuk informasi lebih lanjut atau bantuan, silakan hubungi [@anggaran_apbn](https://t.me/anggaran_apbn) di Telegram.

## Panduan Deployment

### Prasyarat

1. **Install JDK 21**
   ```bash
   # Untuk Ubuntu/Debian
   sudo apt update
   sudo apt install openjdk-21-jdk
   
   # Untuk Windows
   # Download JDK 21 dari https://www.oracle.com/java/technologies/downloads/#java21
   # Jalankan installer dan ikuti petunjuk
   
   # Verifikasi instalasi
   java -version
   ```

2. **Install Maven**
   ```bash
   # Untuk Ubuntu/Debian
   sudo apt install maven
   
   # Untuk Windows
   # Download Maven dari https://maven.apache.org/download.cgi
   # Ekstrak file dan tambahkan bin directory ke PATH sistem
   
   # Verifikasi instalasi
   mvn -version
   ```

3. **MySQL Database**
   ```bash
   # Untuk Ubuntu/Debian
   sudo apt install mysql-server
   sudo mysql_secure_installation
   
   # Untuk Windows
   # Download MySQL dari https://dev.mysql.com/downloads/installer/
   # Jalankan installer dan ikuti petunjuk
   
   # Buat database
   mysql -u root -p
   CREATE DATABASE menfess;
   ```

### Konfigurasi Aplikasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/username/menfess-bot.git
   cd menfess-bot
   ```

2. **Konfigurasi application.properties**

   Buat file `src/main/resources/application.properties` dengan isi:

   ```properties
   # Server dan Nama Aplikasi
   spring.application.name=menfess
   server.port=8080
   
   # Konfigurasi Database
   spring.datasource.url=jdbc:mysql://localhost:3306/menfess
   spring.datasource.username=YOUR_DB_USERNAME
   spring.datasource.password=YOUR_DB_PASSWORD
   spring.datasource.hikari.minimum-idle=5
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.idle-timeout=600000
   spring.jpa.hibernate.ddl-auto=update
   
   # Konfigurasi Bot Telegram
   bot.token=YOUR_BOT_TOKEN
   channel.id=YOUR_CHANNEL_ID
   channel.username=YOUR_CHANNEL_USERNAME
   ```

   **Catatan Penting:**
    - Dapatkan `bot.token` dari [@BotFather](https://t.me/botfather) di Telegram
    - `channel.id` adalah ID numerik channel Telegram (harus diawali dengan tanda minus, misalnya `-1001234567890`)
    - `channel.username` adalah username channel tanpa tanda @ (contoh: `channelmenfesss`)
    - Pastikan bot sudah ditambahkan sebagai admin di channel dengan izin mengirim pesan

### Build dan Deploy

1. **Build dengan Maven**
   ```bash
   mvn clean package
   ```

2. **Jalankan Aplikasi**
   ```bash
   java -jar target/menfess-0.0.1-SNAPSHOT.jar
   ```

3. **Deployment dengan Docker (Opsional)**

   Buat file `Dockerfile`:
   ```dockerfile
   FROM eclipse-temurin:21-jdk
   VOLUME /tmp
   COPY target/*.jar app.jar
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

   Build dan jalankan Docker container:
   ```bash
   docker build -t menfess-bot .
   docker run -d -p 8080:8080 menfess-bot
   ```

## Pemecahan Masalah Umum

1. **Bot tidak merespon**
    - Periksa apakah token bot sudah benar
    - Pastikan bot sudah diaktifkan dengan `/start` di chat pribadi
    - Verifikasi log aplikasi untuk error

2. **Pesan tidak terkirim ke channel**
    - Pastikan bot telah ditambahkan sebagai admin channel
    - Verifikasi ID channel sudah benar
    - Periksa izin bot di channel (harus dapat mengirim pesan)

3. **Database connection error**
    - Verifikasi kredensial database di application.properties
    - Pastikan server MySQL berjalan
    - Periksa firewall tidak memblokir koneksi ke port MySQL

## Menggunakan Bot

1. Mulai percakapan dengan bot di Telegram
2. Kirim pesan teks dengan hashtag (minimal 3 kata)
3. Untuk pesan media, tambahkan caption dengan hashtag
4. Konfirmasi pengiriman dengan menekan tombol "Kirim"
5. Lihat pesan anda di channel yang ditentukan

---

© 2024 Menfess Bot - Untuk penggunaan pribadi dan non-komersial.