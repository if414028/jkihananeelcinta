# Kontrak lengkap register mobile

Mobile mempertahankan seluruh field formulir lama dan Firebase Authentication.
CMS perlu memperluas `POST /api/v1/auth/register` agar field di bawah tersimpan.
API yang sudah deployed belum mendukung semua tambahan ini. Dokumen ini adalah
kontrak untuk penyesuaian CMS berikutnya, bukan klaim bahwa server sudah mendukungnya.

## Format request

- Tanpa foto: `application/json`.
- Dengan foto: `multipart/form-data`, file JPEG bernama `profile_photo`.
- Header `Authorization: Bearer <Firebase ID token>`, `Accept: application/json`,
  `X-App-Platform: android`, dan `X-App-Version` tetap digunakan.
- Email dan UID berasal dari token Firebase. Password, role, NIJ, dan FCM token
  tidak dikirim dalam payload profil. NIJ tetap dibuat CMS.
- Field teks kosong diabaikan. Nama anak/saudara dipisahkan per baris pada formulir,
  dikirim sebagai array JSON atau field multipart berulang `children_names[]` dan
  `siblings_names[]`. Boolean multipart menggunakan `1`/`0` agar sesuai Laravel.

| Kategori | Field formulir lama | Nama payload CMS | Tipe / batas |
|---|---|---|---|
| Akun | Email, password, ulangi password | Firebase Authentication | Tidak masuk payload profil |
| Data diri | Nama pengguna / panggilan | `nickname` | string, 100 |
| Data diri | Nama lengkap | `full_name` | wajib, string, 255 |
| Data diri | Jenis kelamin | `gender` | wajib, `male` / `female` |
| Data diri | Tempat lahir | `place_of_birth` | wajib, string, 100 |
| Data diri | Tanggal lahir | `date_of_birth` | wajib, `YYYY-MM-DD`, sebelum hari ini |
| Data diri | Nomor telepon | `phone_number` | wajib, string, 7–30; awalan 0 menjadi +62 |
| Profil | Alamat | `address` | wajib, string, 2000 |
| Profil | Golongan darah | `blood_type` | wajib, `A`, `B`, `AB`, `O` |
| Profil | Pendidikan terakhir | `last_education` | wajib, label lama: SD, SMP, SMA, D1/D2/D3, S1, S2, S3, Lainnya; 100 |
| Profil | Pekerjaan | `occupation` | wajib, string, 150; pilihan lama tersedia, boleh mengetik detail |
| Baptis & Gereja | Status baptis selam | `baptism_status` | `unknown`, `not_baptized`, `baptized` |
| Baptis & Gereja | Tanggal baptis selam | `baptism_date` | wajib jika baptized; `YYYY-MM-DD`, tidak di masa depan |
| Baptis & Gereja | Gereja tempat baptis selam | `baptism_church` | string, 255 |
| Baptis & Gereja | Baptis Roh Kudus | `holy_spirit_baptism` | boolean; diabaikan jika belum dipilih |
| Baptis & Gereja | Gereja asal | `church_origin` | string, 255 |
| Baptis & Gereja | Alasan pindah gereja | `reason_to_move_church` | string, 2000 |
| Keluarga | Status pernikahan | `marital_status` | wajib, `single`, `married`, `widowed`, `divorced` |
| Keluarga | Status dalam keluarga | `family_status` | label lama: Kepala Keluarga, Istri, Anak, Janda, Duda, Single - Belum Menikah; 100 |
| Keluarga | Nama istri | `wife_name` | string, 255 |
| Keluarga | Nama suami | `husband_name` | string, 255 |
| Keluarga | Nama anak | `children_names` | array string, masing-masing 255 |
| Keluarga | Nama saudara kandung | `siblings_names` | array string, masing-masing 255 |
| Foto & Konfirmasi | Foto diri / selfie | `profile_photo` | file JPEG, maksimal 5 MiB |

Pilihan pendidikan diploma dikirim dengan label asli `D1, D2, D3`. Tanggal baptis
hanya dikirim jika status baptized. Gereja tempat baptis yang sudah diisi tetap
dipertahankan ketika pengguna mengganti status, sehingga perubahan pilihan tidak
menghapus input tanpa sengaja. Pada kategori keluarga, Kepala Keluarga menyembunyikan
nama suami, Istri menyembunyikan nama istri, dan Anak menyembunyikan nama anak.
Nilai field tersembunyi tetap dipertahankan dalam draft ketika berganti pilihan,
tetapi tidak divalidasi, ditampilkan di konfirmasi, atau dikirim dalam payload.
Status keluarga lainnya menampilkan ketiga field tersebut.

Sembilan field wajib di mobile adalah jenis kelamin, nomor telepon, tempat lahir,
tanggal lahir, alamat, golongan darah, pendidikan terakhir, pekerjaan, dan status
pernikahan. Label field tidak diberi penanda wajib/opsional. Validasi dilakukan
ketika pengguna menekan Lanjutkan (dan sebelum submit akhir), bukan saat field
kehilangan fokus. Nama, email, password, dan konfirmasi tetap mengikuti validasi
akun yang sudah ada.

## Penyimpanan dan response CMS

Perlu memperluas validasi, model/skema penyimpanan, dan resource profil mobile.
CMS menyimpan foto pada storage CMS dan mengembalikan URL melalui field yang sudah
ada, `profile_photo_url`. Mobile tidak memakai Firebase Storage untuk registrasi.
Field tambahan di atas perlu dikembalikan pada `data.profile` dari register,
`/auth/session`, dan `/me`, agar data tetap tersedia setelah login ulang.
`holy_spirit_baptism` dikembalikan sebagai boolean JSON, nama anak/saudara sebagai
array string. Envelope, account UID, status aktif, dan NIJ mengikuti kontrak lama.

Foto draft disimpan di direktori privat aplikasi dan bertahan saat activity dibuat
ulang; foto dihapus saat diganti, dihapus pengguna, pendaftaran berhasil, atau
pengguna meninggalkan pendaftaran. Foto serta input dipertahankan saat request
gagal agar dapat dicoba lagi. Password hanya bertahan dalam memori.

## Verifikasi setelah CMS diperbarui

Daftar dengan semua field terisi dan foto, lalu periksa penyimpanan CMS serta
response register, `/me`, dan login ulang. Uji request JSON tanpa foto dan multipart
dengan foto, termasuk lebih dari satu anak/saudara, boolean `1`/`0`, validasi tanggal,
serta retry setelah timeout. Field yang belum diterima CMS tidak boleh dilaporkan
sebagai telah tersimpan. Pengujian mobile saat ini tidak membuat akun Firebase baru.
