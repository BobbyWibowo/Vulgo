package me.fiery.bobby.vulgo.translations.firebase_database;

/**
 * Created by bobby on 12/20/17.
 */

import com.google.firebase.database.DatabaseError;

public class TDatabaseError
{
    public static String GetDatabaseErrorTranslation(DatabaseError databaseError)
    {
        int code = databaseError.getCode();

        switch (code)
        {
            case DatabaseError.DISCONNECTED:
                return "Koneksi terputus. Coba lagi?";
            case DatabaseError.EXPIRED_TOKEN:
                return "Sesi anda telah habis. Silahkan login kembali!";
            case DatabaseError.INVALID_TOKEN:
                return "Sesi anda tidak valid!";
            case DatabaseError.NETWORK_ERROR:
                return "Terjadi masalah koneksi. Coba lagi?";
            case DatabaseError.PERMISSION_DENIED:
                return "Kamu tidak memiliki izin untuk melakukan aksi ini!";
            default:
                return "Terjadi error yang tidak terduga. Coba lagi?";
        }
    }
}
