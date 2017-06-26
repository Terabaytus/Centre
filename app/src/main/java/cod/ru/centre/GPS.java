package cod.ru.centre;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Created by Admin on 19.04.2017.
 */

public class GPS extends Fragment {


    private static Socket client;//создаёт соединение ServerSocket так же может соедениться с channel socket


    private LocationManager locationManager;

    DBHelper dbHelper;

    //-------ПЕРЕМЕННЫЕ ИНТЕРНЕТ АДРЕСОВ И ПОРТОВ--------
    InetAddress GlobalIpServer;
    private static int portGPSServer = 59000;
    //---------------КОНЕЦ-----------------------

    long id;

    String latitude;
    String longitude;
    String time;
    String speed;
    String number;

    int pong;

    /**********************************БЛОК МЕТОДОВ GPS**********************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        dbHelper = new DBHelper(this.getActivity());//экземпляр dbHelper
    }


    @Override
    public void onResume() {// В onResume ВЕШАЕМ СЛУШАТЕЛЯ НА ПРОВАЙДЕРА С ПОМОЩЬЮ МЕТОДА requestLocationUpdates
        super.onResume();


        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER/*ТИП ПРОВАЙДЕРА*/,// НА ВХОД ЕМУ ПОДАЁМ
                1000 * 10/*МИНИМАЛЬНОЕ ВРЕМЯ ЗАПРОСА КООРДИНАТ*/, 5/*РАСТОЯНИЕ ОТОЙДЯ НА КОТОРОЕ ОБНОВЛЯЮТСЯ КООРДИНАТЫ*/, locationListener);

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000*10, 5,
                locationListener);
    }

    @Override
    public void onPause() {//ОТКЛЮЧАЕМ СЛУШАТЕЛЯ МЕТОДА removeUpdates
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {//LocationListener СЛУШАТЕЛЬ РЕАЛИЗУЕТ ИНТЕРФЕЙС locationListener СО СЛЕДУЮЩИМИ МЕТОДАМИ

        @Override
        public void onLocationChanged(Location location) {//МЕТОД onLocationChanged НОВЫЕ ДАННЫЕ О МЕСТО ПОЛОЖЕНИИ
            showLocation(location);                       //ЗДЕСЬ ВЫЗЫВАЕМ СВОЙ МЕТОД showLocation(location)КОТОРЫЙ НА ЭКРАНЕ ОТОБРОЗИТ ДАННЫЕ О МЕСТО ПОЛОЖЕНИИ
            GPS.CoordinatesGPS net = new GPS.CoordinatesGPS();
            net.execute();
            if (pong == 0){
                GPS.CheckTheConnectionToTheServerGPS net1 = new GPS.CheckTheConnectionToTheServerGPS();
                net1.execute();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {// В ЭТОМ МЕТОДЕ МЫ ВЫВОДИМ НОВЫЙ СТАТУС НА ЭКРАН
            //А ТАК ЖЕ ПРОВЕРЯЕМ ДОСТУПЕН ТОТ ИЛИ ИНОЙ ПРОВАЙДЕР
        }

        @Override
        public void onProviderDisabled(String provider) {//УКАЗАНЫЙ ПРОВАЙДЕР БЫЛ ОТКЛЮЧОН ПОЛЬЗОВАТЕЛЕМ
            checkDisabled1();
            checkDisabled2();
        }

        @Override
        public void onProviderEnabled(String provider) {//УКАЗАНЫЙ ПРОВАЙДЕР БЫЛ ВКЛЮЧОН ПОЛЬЗОВАТЕЛЕМ

        }

    };

    private void showLocation(Location location) {
        if (location == null)
            return;

        latitude = formatLocation1(location);
        longitude = formatLocation2(location);
        time = formatLocation5(location);
        speed = formatLocation6(location);
    }

    public String formatLocation1(Location location) {// НА ВХОД БЕРЁТ Location location
        if (location == null)                         //ЧЕТАЕТ ИЗ НЕГО ДАННЫЕ И ВЫДАЁТ СТРОКУ
            return "";                                //ШИРОТА, ДОЛГОТА, ВРЕМЯ ОПРЕДЕЛЕНИЯ
        return String.format(Locale.ENGLISH,//В зависимости от указанной страны используются разные разделители для тысяч.
                "%1$.4f",
                location.getLatitude());

    }

    public String formatLocation2(Location location) {// НА ВХОД БЕРЁТ Location location
        if (location == null)                         //ЧЕТАЕТ ИЗ НЕГО ДАННЫЕ И ВЫДАЁТ СТРОКУ
            return "";                                //ШИРОТА, ДОЛГОТА, ВРЕМЯ ОПРЕДЕЛЕНИЯ
        return String.format(Locale.ENGLISH,//В зависимости от указанной страны используются разные разделители для тысяч.
                "%1$.4f",
                location.getLongitude());

    }

    public String formatLocation5(Location location) {// НА ВХОД БЕРЁТ Location location
        if (location == null)                         //ЧЕТАЕТ ИЗ НЕГО ДАННЫЕ И ВЫДАЁТ СТРОКУ
            return "";                                //ШИРОТА, ДОЛГОТА, ВРЕМЯ ОПРЕДЕЛЕНИЯ
        return String.format(
                "%1$tF %1$tT",
                location.getTime());

    }

    public String formatLocation6(Location location) {// НА ВХОД БЕРЁТ Location location
        if (location == null)                         //ЧЕТАЕТ ИЗ НЕГО ДАННЫЕ И ВЫДАЁТ СТРОКУ
            return "";                                //ШИРОТА, ДОЛГОТА, ВРЕМЯ ОПРЕДЕЛЕНИЯ
        return String.format(Locale.ENGLISH,//В зависимости от указанной страны используются разные разделители для тысяч.
                "%d",0,/*"%1$.0f"*/
                location.getSpeed());

    }

    private void checkDisabled1() {//ОПРЕДЕЛЯЕТ ВКЛЮЧЕНЫ ИЛИ ВЫКЛЮЧЕНЫ ПРОВАЙДЕРЫ МЕТОДОМ isProviderEnabled
        latitude = "00.0000";//И ОТОБРАЖАЕТ ЭТУ НФОРМАЦИЮ НА ЭКРАНЕ

    }

    private void checkDisabled2() {//ОПРЕДЕЛЯЕТ ВКЛЮЧЕНЫ ИЛИ ВЫКЛЮЧЕНЫ ПРОВАЙДЕРЫ МЕТОДОМ isProviderEnabled
        longitude = "00.0000";//И ОТОБРАЖАЕТ ЭТУ НФОРМАЦИЮ НА ЭКРАНЕ

    }
    /*********************************КОНЕЦ БЛОКА МЕТОДОВ GPS*********************************/

    /*****************************КЛАСС GPS TRACKER **********************************/

    public class CoordinatesGPS extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
// TODO Auto-generated method stub

            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put(DBHelper.KEY_ID_TELEPHONE, number);
            contentValues.put(DBHelper.KEY_LATITUDE, latitude);
            contentValues.put(DBHelper.KEY_LONGITUDE, longitude);
            contentValues.put(DBHelper.KEY_TIMEMODIFY, time);
            contentValues.put(DBHelper.KEY_SPEED, speed);

            database.insert(DBHelper.TABLE_COORDINATES_CENTRE, null, contentValues);//добавляем значение в таблицу


            Cursor cursor = database.query(DBHelper.TABLE_COORDINATES_CENTRE, null, null, null, null, null, null);//делаем запрос на выборку БД

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                int id_telephoneIndex = cursor.getColumnIndex(DBHelper.KEY_ID_TELEPHONE);
                int latitudeIndex = cursor.getColumnIndex(DBHelper.KEY_LATITUDE);
                int longitudeIndex = cursor.getColumnIndex(DBHelper.KEY_LONGITUDE);
                int timemodifyIndex = cursor.getColumnIndex(DBHelper.KEY_TIMEMODIFY);
                int speedIndex = cursor.getColumnIndex(DBHelper.KEY_SPEED);

                do {
                    Log.d("mLog", "ID = " + cursor.getInt(idIndex) +
                            ", id_telephone = " + cursor.getString(id_telephoneIndex) +
                            ", latitude = " + cursor.getString(latitudeIndex) +
                            ", longitude = " + cursor.getString(longitudeIndex) +
                            ", timemodify = " + cursor.getString(timemodifyIndex) +
                            ", speed = " + cursor.getString(speedIndex));

                    id  = cursor.getLong(idIndex);
                    number  = cursor.getString(id_telephoneIndex);
                    latitude = cursor.getString(latitudeIndex);
                    longitude = cursor.getString(longitudeIndex);
                    time = cursor.getString(timemodifyIndex);
                    speed = cursor.getString(speedIndex);

                    if(pong == 1) {
                        try {
                            if (GlobalIpServer != null) {
                                //----------------------------------------------
                                client = new Socket(GlobalIpServer, portGPSServer);
                                //----------------------------------------------

                                int ping = 1;
                                int namberQuery = 8814;

                                DataOutputStream outData = new DataOutputStream(client.getOutputStream());//подключаемся к socket
                                outData.writeInt(ping);//отправляется запрос клиента серверу

                                InputStream in = client.getInputStream();
                                DataInputStream din = new DataInputStream(in);
                                pong = din.readInt(); // получаем ответ

                                outData.writeInt(namberQuery);//отправляется № запроса
                                outData.writeUTF(number);//отправляется запрос клиента серверу
                                outData.writeUTF(latitude);//отправляется запрос клиента серверу
                                outData.writeUTF(longitude);//отправляется запрос клиента серверу
                                outData.writeUTF(time);//отправляется запрос клиента серверу
                                outData.writeUTF(speed);//отправляется запрос клиента серверу

                                database.delete(DBHelper.TABLE_COORDINATES_CENTRE, DBHelper.KEY_ID + "=" + id, null);
                                // client.close();
                            }
                        } catch (IOException e) {
                            //e.printStackTrace();
                            pong = 0;
                            System.out.println("Нет связи с сервером GPS.");
                        }
                    }
                } while (cursor.moveToNext());
            } else
                Log.d("mLog","0 rows");

            cursor.close();
            dbHelper.close();


            return null;

        }
/*********************************КОНЕЦ **********************************/
    }
    /*********************************КОНЕЦ*********************************/

    /*******ПРОВЕРЯЕМ НАЛИЧЕЕ СВЯЗИ С СЕРВЕРОМ GPS **********/
   public class CheckTheConnectionToTheServerGPS extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                GlobalIpServer  = InetAddress.getByName("volmed.org.ru");
                System.out.println("ip адрес домена ipGPSServer на данный момент: "+ GlobalIpServer.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                if (GlobalIpServer != null) {
                    //----------------------------------------------
                    client = new Socket(GlobalIpServer, portGPSServer);
                    //----------------------------------------------
                    int ping = 1;
                    DataOutputStream outData = new DataOutputStream(client.getOutputStream());//подключаемся к socket
                    outData.writeInt(ping);//отправляется запрос клиента серверу

                    InputStream in = client.getInputStream();
                    DataInputStream din = new DataInputStream(in);
                    pong = din.readInt(); // получаем размер файла
                    System.out.println("С сервером GPS есть контакт." + pong);
                    client.close();
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Не удалось соедениться с сервером GPS.");
            }
            return null;
        }
    }
}
    /*********************************КОНЕЦ**********************************/

