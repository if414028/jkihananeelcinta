# Design review: Pendaftaran Hananeel Cinta

## Summary

Android native (Kotlin/XML), untuk jemaat gereja. Pekerjaan utama layar ini adalah
membuat akun tanpa membebani pengguna dengan profil panjang. Audit awal berdasarkan
kode: **Needs work**. Jeda 1,5 detik dan penghilangan field lama membuat pendaftaran bermasalah.
Desain baru berfokus pada sambutan gereja yang hangat, enam kelompok singkat, seluruh field lama, dan foto yang bisa ditinjau. Prinsip Apple diterjemahkan ke Android Material.

## Improvements

- **High — waktu tunggu buatan.** Alur register kini langsung berpindah dengan fade
  180 ms, tanpa spinner setiap langkah. `motion.md › Providing feedback`:
  “Aim for brevity and precision in feedback animations.” Animasi mengikuti pengaturan
  animator perangkat; hanya penyimpanan akun yang memperlihatkan progress sebenarnya.
- **High — informasi lama hilang.** Seluruh field kini tampil dalam Akun, Data diri,
  Profil, Baptis & Gereja, Keluarga, serta Foto & Konfirmasi. `layout.md › Visual hierarchy`:
  Pengelompokan mengikuti prinsip hierarki informasi; seluruh field tetap dapat diakses.
- **Medium — error terlalu dini.** Validasi dilakukan saat tombol lanjut dipilih. Error berada dalam `TextInputLayout`; input pertama yang salah
  mendapat fokus. `text-fields.md › Best practices`: “Validate fields when it makes sense.”
- **Medium — data mudah hilang.** Draft bertahan di ViewModel saat rotasi dan perpindahan
  langkah. Password hanya berada di memori; setelah proses aplikasi mati, pengguna
  perlu mengisi password kembali. Error jaringan tetap terlihat dan tidak menutup form.

## Token system

| Peran | Light | Dark | Kontras terhadap surface light / dark |
|---|---|---|---|
| Surface | `#FCFAFC` | `#181417` | — |
| Content | `#241C22` | `#F7EFF3` | 15.99 / 16.15 |
| Secondary | `#70616A` | `#C4B4BD` | 5.61 / 9.22 |
| Accent | `#800020` | `#F4B4C8` | 10.43 / 10.60 |
| Error | `#AD223A` | `#FFB1B9` | 6.59 / 10.63 |
| Field | `#FFFFFF` | `#231D21` | — |

Button light: putih pada burgundy, 10.83:1. Button dark: `#35101D` pada
`#F4B4C8`, 9.82:1. Body dan input 16 sp; label sekunder 14 sp; judul 34 sp
San Francisco Bold. Body memakai font Regular asli; label/tombol memakai Semibold asli. Pada skala teks
150% ke atas, judul memakai basis 24 sp (tetap mengikuti skala sistem), instruksi
dipersingkat, dan nama langkah aktif digabungkan dengan hitungan langkah. Petunjuk
footer yang berulang disembunyikan untuk memberi ruang pada input.

Kontrol memiliki target minimal 48 dp; field dan tombol utama minimal 56 dp.
Tinggi fleksibel, field satu kolom, scroll, dan insets sistem/keyboard menggantikan
ukuran sdp/ssp. `accessibility.md › Vision`: “Support larger text sizes.”

## Craft notes

**Medium — identitas visual.** Burgundy asli Hananeel dipertahankan untuk aksi utama.
Seluruh font asli dipertahankan; hierarki dibentuk dengan ukuran, bobot, dan ruang.
Sambutan dan penyebutan keluarga gereja membuat arahan ini spesifik untuk Hananeel,
bukan halaman promosi produk. Marquee, testimonial, badge, dan ilustrasi dekoratif
dihapus dari rencana karena tidak membantu pendaftaran. Ini keputusan desain.

Instruksi pengguna untuk mempertahankan font dan semua field menggantikan pilihan
tipografi skill. GSAP/AIDA pemasaran tidak relevan pada formulir Android; motion
memakai animator native. Tidak ada font baru atau download font saat runtime.

Compact:

```text
Back        Hananeel Cinta       Masuk
            Selamat datang.
            Penjelasan singkat
           Langkah 1 dari 6
           Akun / Data diri / Profil
Email
Password                         tampilkan
Ulangi password                  tampilkan
                 [Lanjutkan]
```

Regular/tablet: struktur sama, kolom berpusat dibatasi 600 dp. Tidak ada field yang
dipaksa menjadi dua kolom; keyboard, font besar, dan landscape tetap bisa memakai scroll.

## What works

Firebase tetap memiliki autentikasi; profil dan NIJ tetap berasal dari CMS. Recovery
`401`, timeout, dan konflik registrasi tetap dipertahankan. Status baptis kosong
dikirim sebagai `unknown`; status pernikahan yang dilewati tidak diasumsikan `single`.

## Validation

Build APK, unit tests pemetaan/recovery dan validasi form, serta tes instrumentasi
emulator untuk inline errors, enam kelompok, semua field, foto, font lama, tombol kembali, dan activity recreation.
Screenshot aktual dibuat untuk pemeriksaan visual light/dark dan teks besar. Tidak
ada akun Firebase baru yang dibuat saat pengujian UI.

Verifikasi desain sebelumnya: 23 tes unit lulus; 3 tes instrumentasi lulus pada
mode terang, serta 3 tes yang sama lulus pada mode gelap dengan lebar 320 dp
dan skala teks 200%. Pemeriksaan mencakup pemulihan seluruh field lama,
foto saat activity recreation, font asli, payload lengkap, dan multipart JPEG.

Perubahan validasi dan status keluarga: 25 tes unit dan 4 tes instrumentasi lulus.
Field wajib diperiksa saat Lanjutkan, label tanpa penanda wajib/opsional, dan
pergantian status keluarga mempertahankan draft sambil menyembunyikan field yang
tidak sesuai. Screenshot diperiksa setelah dialog System UI emulator ditutup.
