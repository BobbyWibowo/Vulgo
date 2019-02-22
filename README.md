# Vulgo

Gunakan Vulgo untuk mempublikasikan barang kamu yang hilang.

Kenapa kamu harus memakai Vulgo?

* Kamu bisa lebih mudah mempublikasi barang kamu yang hilang dengan adanya fitur upload foto dan lokasi di peta.
* Adanya fitur "Bagikan" yang dapat digunakan untuk mempublikasikan barang hilang kamu ke aplikasi lain, jadi kamu tetap bisa menggapai orang yang masih belum memakai Vulgo.
* Orang lain bisa memberikan informasi ke kamu melalui fitur komentar.
* Aplikasi bekerja secara "real-time". Jadi kamu tidak perlu "reload" aplikasi untuk lihat komentar paling baru di broadcast kamu.
* Mudah digunakan!

## Instructions

* Create a new file named `google_maps_api.xml` at `app` directory containing this value:
> ```xml
> <resources>
>     <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_API_KEY</string>
> </resources>
> ```
> Replace `YOUR_API_KEY` with your Google Maps' API KEY.
* Make symbolic links of `app/google_maps_api.xml` to `app/src/release/res/values/google_maps_api.xml` and `app/src/debug/res/values/google_maps_api.xml`.
* Configure Firebase integration through Android Studio. It should end up with a configuration file named `google-services.json` in `app` directory. For more in-depth instruction for this, search around.

## Notice

This project contain many deprecated features by today's standards (early-2019), as this was written late-2017 to early-2018.
