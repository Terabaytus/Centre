package cod.ru.centre;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;//версия таблицы
    public static final String DATABASE_NAME = "navigation";
    public static final String TABLE_COORDINATES_CENTRE = "coordinates_centre";

    public static final String KEY_ID = "_id";
    public static final String KEY_ID_TELEPHONE = "id_telephone";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_TIMEMODIFY = "timemodify";
    public static final String KEY_SPEED = "speed";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//вызывается когда нужно создать таблицу
        db.execSQL("create table " + TABLE_COORDINATES_CENTRE + "(" + KEY_ID
                + " integer primary key," + KEY_ID_TELEPHONE + " text," + KEY_LATITUDE + " text," +
                " text," + KEY_LONGITUDE + " text," + KEY_TIMEMODIFY + " text," + KEY_SPEED + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//удаляет таблицу при обновлении версии
        db.execSQL("drop table if exists " + TABLE_COORDINATES_CENTRE);

        onCreate(db);

    }
}