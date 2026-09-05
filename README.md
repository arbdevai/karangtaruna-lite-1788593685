# Karang Taruna RT

Aplikasi administrasi keuangan Karang Taruna tingkat RT berbasis Jetpack Compose, Kotlin, dan Firebase Spark.

## Fitur Utama

- **Saldo Kas & Kartu Fisik Digital**: Visual kartu debit matte graphite menampilkan saldo kas real-time.
- **Pemasukan & Pengeluaran**: Pencatatan transaksi terinci dengan kategori dan audit trail.
- **Iuran Warga**: Pencatatan iuran per periode bulanan, status bayar/belum bayar, dan otomatis tercatat ke pemasukan kas.
- **Data Warga**: Manajemen direktori warga tanpa data sensitif yang tidak perlu.
- **Otorisasi Role**: `ADMIN`, `TREASURER`, dan `VIEWER` ditegakkan pada Firestore Security Rules.
- **GitHub CI Build**: Build dan pengujian otomatis via GitHub Actions menghasilkan file APK installable tanpa kompilasi lokal.

## Setup Firebase

1. Buat project Firebase baru pada tier Spark (Free).
2. Aktifkan **Email/Password** pada Firebase Authentication.
3. Buat database **Cloud Firestore** dan terapkan `firestore.rules` serta `firestore.indexes.json`.
4. Unduh `google-services.json` dan letakkan di folder `app/`.

## CI/CD GitHub Actions

Workflow `.github/workflows/android.yml` otomatis menjalankan:
- Wrapper validation & lint checks
- Automated unit tests (`BusinessLogicTest`)
- Build APK Debug (`karangtaruna-debug`) dan APK Release (`karangtaruna-release-unsigned`)
- Pengunggahan artifact APK yang langsung bisa diunduh dari tab Actions.
