package cod.ru.centre;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements TextWatcher, TextView.OnEditorActionListener {

    private static  Socket client;//создаёт соединение ServerSocket так же может соедениться с channel socket

    private static final int TAKE_PICTURE = 1;

    private static double versionNumber;//указывать текущий номер версии в этой переменной

    final int TYPE_PHOTO = 1;
    final int TYPE_VIDEO = 2;

    final int REQUEST_CODE_PHOTO = 1;
    final int REQUEST_CODE_VIDEO = 2;

    private LocationManager locationManager;

    DBHelper dbHelper;

    final String TAG = "myLogs";

    //-------ПЕРЕМЕННЫЕ ВЕРСИЙ--------
    static double versionName1;
    static double versionName2;
    //---------------КОНЕЦ-----------------------

    //-------ПЕРЕМЕННЫЕ ИНТЕРНЕТ АДРЕСОВ И ПОРТОВ--------
    InetAddress GlobalIpServer;
    private static InetAddress ipUpdatesServer;

    private static int portMainServer    = 59000;
    private static int portGPSServer     = 59000;
    private static int portUpdatesServer = 59000;
    //---------------КОНЕЦ-----------------------

    //-------БЛОК ПЕРМЕННЫХ ДЛЯ РАЗРИШЕНИЙ--------
    private static final int PERMISSION_REQUEST_CODE = 0;
    // объявляем разрешение, которое нам нужно получить
   private static final String READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    //---------------КОНЕЦ-----------------------

    long id;

    String latitude;
    String longitude;
    String time;
    String speed;
    String informationAboutFile = null;
    String date;
    String number;

    int pong;
    int pong1 =0;

    File directory;
    static File directory1;

    ImageView ivPhoto;
    VideoView videoView;

    AutoCompleteTextView mAutoCompleteFoto;
    AutoCompleteTextView mAutoCompleteVideo;

    final String[] VARIANTS = { "Барсик", "Мурка", "", "",// Получаем массив строк для автозаполнения
            "", "", "", "", "" };
    private boolean flag;
    private boolean flag1;

    final static int RQS_RECORDING = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);// Получаем ссылку на элементы в разметке
        //-------БЛОК УСЛОВИЙ ДЛЯ ПОЛУЧЕНИЯ РАЗРЕШЕНИЙ ДЛЯ УСТРОЙСТВА--------
        // проверяем разрешения: если они уже есть,
        // то приложение продолжает работу в нормальном режиме
        if (isPermissionGranted(READ_PHONE_STATE_PERMISSION)) {
            //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ОПРЕДЕЛЕНИЯ IMEI УСТРОЙСТВА--------
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            number = telephonyManager.getDeviceId();// определяем imei стройства
            //---------------КОНЕЦ-----------------------
        } else {
            // иначе запрашиваем разрешение у пользователя
            requestMultiplePermissions();
        }
        //---------------КОНЕЦ-----------------------

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // вертикальная оринтация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // или горизонтальная оринтация
        createDirectory();

        //-------БЛОК КОНСТРУКЦИЙ ДЛЯ УПРАВЛЕНИЯ ПОДСВЕТКОЙ ЭКРАНА--------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //---------------КОНЕЦ-----------------------

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVisibility(VideoView.INVISIBLE);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ВАРИАНТОВ ФРАЗ ПОДПИСИ ФОТО--------
        mAutoCompleteFoto = (AutoCompleteTextView) findViewById(R.id.autocomplete_message);
        mAutoCompleteFoto.setOnEditorActionListener(this);
        mAutoCompleteFoto.setVisibility(AutoCompleteTextView.INVISIBLE);
        mAutoCompleteFoto.addTextChangedListener(this);
        mAutoCompleteFoto.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, VARIANTS));// Подключаем адаптер для автозаполнения элемента AutoCompleteTextView
        mAutoCompleteFoto.setThreshold(1);// начинаем искать подсказки с первого введенного символа
        //---------------КОНЕЦ-----------------------

        //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ВАРИАНТОВ ФРАЗ ПОДПИСИ ВИДЕО--------
        mAutoCompleteVideo = (AutoCompleteTextView) findViewById(R.id.autocomplete_message1);
        mAutoCompleteVideo.setOnEditorActionListener(this);
        mAutoCompleteVideo.setVisibility(AutoCompleteTextView.INVISIBLE);
        mAutoCompleteVideo.addTextChangedListener(this);
        mAutoCompleteVideo.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, VARIANTS));// Подключаем адаптер для автозаполнения элемента AutoCompleteTextView
        mAutoCompleteVideo.setThreshold(1);// начинаем искать подсказки с первого введенного символа
        //---------------КОНЕЦ-----------------------

        //-------БЛОК КОНСТРУКЦИЙ РАБОТЫ С ОБНОВЛЕНИЯМИ--------
        try {
            versionNumber = Double.parseDouble(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            System.out.println("№ текущей версии  " + versionNumber);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        DeleteFilesUpdatings net = new DeleteFilesUpdatings();//ссылка на поток удаления предедущей версии
        net.execute();

        Thread threadsearchUpdatings = new Thread(new ThreadSearchUpdatings());//ссылка на поток поиск обновлений
        threadsearchUpdatings.start();//запуск потока

        versionName1 = versionNumber + 0.1;//увеличевает номер версии которую следует запустить на 1
        System.out.println("№ следующей версии: " + versionName1);

        //проверяем наличее в папке файла обновления если есть запускаем
        File file = new File("/storage/emulated/0/Download/downloadUpdatingsCentre/"+ versionName1 +".apk");
        if (file.exists()) {
            if (file.length()== 0) {//проверяем наличие бракованых обновлений если есть удаляем

                file.delete();

            }else {// если нету открываем файл

                openFilesUpdatings();
            }
        }
        //---------------КОНЕЦ-----------------------


        dbHelper = new DBHelper(this);//экземпляр dbHelper

        // -------БЛОК КОНСТРУКЦИЙ ПЕРЕВОДА ПОЛЬЗОВАТЕЛЯ В МЕНЮ GPS--------
        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);//проверяем вкл GPS

       if (enabled == false){
           //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ВЫВОДА СООБЩЕНИЯ ОБ IMEI УСТРОЙСТВА--------
           AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
           builder.setTitle("IMEI вашего устройства " + number)
                   .setMessage(R.string.message3)
                   //.setIcon(R.drawable.ic_android_cat)//Добавляем иконку к сообщению
                   .setCancelable(false)
                   .setNegativeButton(R.string.message5,
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   startActivity(new Intent(
                                           android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                   dialog.cancel();
                               }
                           });
           AlertDialog alert = builder.create();
           alert.show();
           //---------------КОНЕЦ-----------------------
       }
    }
    /**********************************БЛОК МЕТОДОВ РАЗРЕШЕНИЙ**********************************/
    private void requestMultiplePermissions() {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Разрешения получены", Toast.LENGTH_LONG).show();
                //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ОПРЕДЕЛЕНИЯ IMEI УСТРОЙСТВА--------
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                number = telephonyManager.getDeviceId();// определяем imei стройства
                //---------------КОНЕЦ-----------------------
            } else {
                Toast.makeText(MainActivity.this, "Разрешения не получены", Toast.LENGTH_LONG).show();
                requestMultiplePermissions();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    /**********************************КОНЕЦ БЛОКА МЕТОДОВ РАЗРЕШЕНИЙ**********************************/

    /**********************************БЛОК МЕТОДОВ GPS**********************************/
    @Override
    protected void onResume() {// В onResume ВЕШАЕМ СЛУШАТЕЛЯ НА ПРОВАЙДЕРА С ПОМОЩЬЮ МЕТОДА requestLocationUpdates
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                1000*10/*МИНИМАЛЬНОЕ ВРЕМЯ ЗАПРОСА КООРДИНАТ*/, 5/*РАСТОЯНИЕ ОТОЙДЯ НА КОТОРОЕ ОБНОВЛЯЮТСЯ КООРДИНАТЫ*/, locationListener);

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000*10, 5,
                locationListener);
    }

    @Override
    protected void onPause() {//ОТКЛЮЧАЕМ СЛУШАТЕЛЯ МЕТОДА removeUpdates
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            CoordinatesGPS net = new CoordinatesGPS();
            net.execute();
            if (pong == 0){
                CheckTheConnectionToTheServerGPS net1 = new CheckTheConnectionToTheServerGPS();
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

    /**********************************МЕТОДЫ УПРАВЛЕНИЯ ФОТО И ВИДЕО******************************/
    public void onClickPhoto(View view) {
        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);//проверяем вкл GPS

        if (enabled == false){
            //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ВЫВОДА СООБЩЕНИЯ ОБ IMEI УСТРОЙСТВА--------
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("IMEI вашего устройства " + number)
                    .setMessage(R.string.message3)
                    //.setIcon(R.drawable.ic_android_cat)//Добавляем иконку к сообщению
                    .setCancelable(false)
                    .setNegativeButton(R.string.message5,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            //---------------КОНЕЦ-----------------------
        }else {
            flag = true;
         /*  */
            DialogActionsFoto dialogActionsFoto = new DialogActionsFoto();
                    dialogActionsFoto.show(getFragmentManager(), "dialogActionsFoto");

        }
    }

    //-------БЛОК МЕТОДОВ ВЫБОРА ДЕЙСТВИЙ ПРЕДЛОЖЕНЫХ ДИАЛОГОМ --------
    public void fotoWithAudio(){

        informationAboutFile = "Фотография с аудио коментарием";

        try {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// Намерение для запуска камеры
            //Intent intent1 = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
           // intent1.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
           // startActivityForResult(intent1, RQS_RECORDING);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
            startActivityForResult(intent, TAKE_PICTURE);// Намерение для запуска камеры
            createDirectory();

            Dictaphone dictaphone = new Dictaphone(latitude, longitude, date,number);
            dictaphone.show(getFragmentManager(), "dictaphone");
        } catch (ActivityNotFoundException e) {
            // Выводим сообщение об ошибке
            Toast toast1 = Toast.makeText(getApplicationContext(),
                    R.string.message2,
                    Toast.LENGTH_LONG);
            toast1.setGravity(Gravity.CENTER, 0, 0);
            toast1.show();
        }
    }

    public void photo(){
        informationAboutFile = "Фотография без подписи и аудио файла.";
        try {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// Намерение для запуска камеры
            intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
            startActivityForResult(intent, TAKE_PICTURE);// Намерение для запуска камеры
            createDirectory();
        } catch (ActivityNotFoundException e) {
            // Выводим сообщение об ошибке
            Toast toast1 = Toast.makeText(getApplicationContext(),
                    R.string.message2,
                    Toast.LENGTH_LONG);
            toast1.setGravity(Gravity.CENTER, 0, 0);
            toast1.show();
        }
    }

    public void signAphoto(){
        if (TextUtils.isEmpty(informationAboutFile)) {//если informationAboutFile содержит ноль

            mAutoCompleteFoto.setVisibility(AutoCompleteTextView.VISIBLE);

        }
        informationAboutFile = mAutoCompleteFoto.getText().toString();// инфу с mAutoComplete помещаем в informationAboutFile
        if (!TextUtils.isEmpty(informationAboutFile)) {//если informationAboutFile не пустой

            mAutoCompleteFoto.setVisibility(AutoCompleteTextView.INVISIBLE);

            try {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// Намерение для запуска камеры
                intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
                startActivityForResult(intent, TAKE_PICTURE);// Намерение для запуска камеры

            } catch (ActivityNotFoundException e) {
                // Выводим сообщение об ошибке
                Toast toast1 = Toast.makeText(getApplicationContext(),
                        R.string.message2,
                        Toast.LENGTH_LONG);
                toast1.setGravity(Gravity.CENTER, 0, 0);
                toast1.show();
            }
        }
    }
    //---------------КОНЕЦ-----------------------

    public void mAUTOCompleteFoto(){//метод действия после нажатия на кнопку Foto

        informationAboutFile = mAutoCompleteFoto.getText().toString();// инфу с mAutoComplete помещаем в informationAboutFile
        if (!TextUtils.isEmpty(informationAboutFile)) {//если informationAboutFile не пустой

            mAutoCompleteFoto.setVisibility(AutoCompleteTextView.INVISIBLE);

            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// Намерение для запуска камеры
                intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
                startActivityForResult(intent, TAKE_PICTURE);// Намерение для запуска камеры

            } catch (ActivityNotFoundException e) {
                // Выводим сообщение об ошибке
                Toast toast1 = Toast.makeText(getApplicationContext(),
                        R.string.message2,
                        Toast.LENGTH_LONG);
                toast1.setGravity(Gravity.CENTER, 0, 0);
                toast1.show();
            }
        }
    }
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (actionId == EditorInfo.IME_ACTION_GO) {
            // обрабатываем нажатие кнопки GO
            if (flag == true){
                mAUTOCompleteFoto();
            }
                if (flag1 == true){
                    mAUTOCompleteVideo();
                }

            return true;
        }
        return false;
    }

    public void onClickVideo(View view) {
        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);//проверяем вкл GPS

        if (enabled == false) {
            //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ВЫВОДА СООБЩЕНИЯ ОБ IMEI УСТРОЙСТВА--------
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("IMEI вашего устройства " + number)
                    .setMessage(R.string.message3)
                    //.setIcon(R.drawable.ic_android_cat)//Добавляем иконку к сообщению
                    .setCancelable(false)
                    .setNegativeButton(R.string.message5,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            //---------------КОНЕЦ-----------------------
        } else {
            flag1 = true;

            if (TextUtils.isEmpty(informationAboutFile)) {//если informationAboutFile содержит ноль

                mAutoCompleteVideo.setVisibility(AutoCompleteTextView.VISIBLE);

            }
            informationAboutFile = mAutoCompleteVideo.getText().toString();// инфу с mAutoComplete помещаем в informationAboutFile

            if (!TextUtils.isEmpty(informationAboutFile)) {//если informationAboutFile не пустой

                mAutoCompleteVideo.setVisibility(AutoCompleteTextView.INVISIBLE);

                try {

                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_VIDEO));
                    startActivityForResult(intent, REQUEST_CODE_VIDEO);

                } catch (ActivityNotFoundException e) {
                    // Выводим сообщение об ошибке
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.message2,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        }
    }
    public void mAUTOCompleteVideo() {//метод действия после нажатия на кнопку Foto

        informationAboutFile = mAutoCompleteVideo.getText().toString();// инфу с mAutoComplete помещаем в informationAboutFile
        if (!TextUtils.isEmpty(informationAboutFile)) {//если informationAboutFile не пустой

            mAutoCompleteVideo.setVisibility(AutoCompleteTextView.INVISIBLE);

            try {

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_VIDEO));
                startActivityForResult(intent, REQUEST_CODE_VIDEO);

            } catch (ActivityNotFoundException e) {
                // Выводим сообщение об ошибке
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.message2,
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                mAutoCompleteFoto.setText(null);//обнуляем
                if (intent == null) {
                    Log.d(TAG, "Intent is null");
                } else {
                    mAutoCompleteFoto.setText(null);
                    Log.d(TAG, "Photo uri: " + intent.getData());
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) obj;
                            Log.d(TAG, "bitmap " + bitmap.getWidth() + " x "
                                    + bitmap.getHeight());
                            ivPhoto.setImageBitmap(bitmap);
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }

        if (requestCode == REQUEST_CODE_VIDEO) {
            if (resultCode == RESULT_OK) {
                mAutoCompleteVideo.setText(null);//обнуляем
                if (intent == null) {
                    Log.d(TAG, "Intent is null");
                } else {
                    Log.d(TAG, "Video uri: " + intent.getData());

                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }
    }
    /*********************************КОНЕЦ*********************************/

    /**********************************ИИНФОРМАЦИЯ О ФАЙЛЕ И ЕГО РАСПОЛОЖЕНИЕ**********************/
    private Uri generateFileUri(int type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd  HHmmss ").format(new Date());
        date = timeStamp;
        if(latitude == null) {
            latitude = "00.0000";
        }
        if(longitude == null) {
            longitude = "00.0000";
        }
        File file = null;
        switch (type) {
            case TYPE_PHOTO:
                file = new File(directory.getPath() + "/" + "photo "
                        + date + "  " + latitude + "    " + longitude + "     " + number + "      " + informationAboutFile + ".jpg");
                break;
            case TYPE_VIDEO:
                file = new File(directory.getPath() + "/" + "video "
                        + date + "  " + latitude + "    " + longitude + "     " + number + "      " + informationAboutFile + ".mp4");
                break;
        }
        Log.d(TAG, "fileName = " + file);
        return Uri.fromFile(file);
    }

    private void createDirectory() {
        directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Photo_and_Video");
        if (!directory.exists())
            directory.mkdirs();
    }
    /*********************************КОНЕЦ*********************************/

    /**********************************МЕТОДЫ ЗАГРУЗЧИКА**********************************/
    public void onClickSend(View view) throws FileNotFoundException {// кнопка отправить
        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);//проверяем вкл GPS

        if (enabled == false){
            //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ВЫВОДА СООБЩЕНИЯ ОБ IMEI УСТРОЙСТВА--------
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("IMEI вашего устройства " + number)
                    .setMessage(R.string.message3)
                    //.setIcon(R.drawable.ic_android_cat)//Добавляем иконку к сообщению
                    .setCancelable(false)
                    .setNegativeButton(R.string.message5,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            //---------------КОНЕЦ-----------------------
        }else {
            SendFiles net = new SendFiles();
            net.execute();
        }
    }

    public class SendFiles extends AsyncTask<Void, Integer, Void> {

        private long missingBytes;
        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(ProgressBar.INVISIBLE);// не видим
            progressBar.setVisibility(ProgressBar.VISIBLE);// после нажатия кнопки отправить становиться видимым

            Toast toast = Toast.makeText(getApplicationContext(),//создаем и отображаем текстовое уведомление "Загрузка"
                    R.string.message,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            try {
                GlobalIpServer  = InetAddress.getByName("volmed.org.ru");
                System.out.println("ip адрес домена ipMainServer на данный момент: "+ GlobalIpServer.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                if (GlobalIpServer != null) {
                    //----------------------------------------------
                    client = new Socket(GlobalIpServer, portMainServer);
                    //----------------------------------------------
                    int ping = 1;
                    int namberQuery = 8184;
                    DataOutputStream outData = new DataOutputStream(client.getOutputStream());//подключаемся к socket
                    outData.writeInt(ping);//отправляется запрос клиента серверу

                    InputStream in = client.getInputStream();
                    DataInputStream din = new DataInputStream(in);
                    pong1 = din.readInt(); // получаем ответ


                    outData.writeInt(namberQuery);//отправляется запрос клиента серверу

                    ArrayList<String> selectFiles = new ArrayList<>();

                    File folder = new File("/storage/emulated/0/Pictures/Photo_and_Video");

                    File[] listOfFiles = folder.listFiles(); // получить массив всех файлов в папке

                    for (File f : listOfFiles) {
                        selectFiles.add(f + ""); //отправка файлов из списка поочередно.
                    }

                    int countFiles = selectFiles.size();//создаём переменную countFiles (подсчёт файлов)
                    DataOutputStream outD; // переменная потока отправляемых данных

                    outD = new DataOutputStream(client.getOutputStream());
                    outD.writeInt(countFiles);//отсылаем количество файлов

                    for (int i = 0; i < countFiles; i++) {
                        File f = new File(selectFiles.get(i));

                        outD.writeLong(f.length());//отсылаем размер файла
                        outD.writeUTF(f.getName());//отсылаем имя файла
                        System.out.println("Отослано имя файла " + f.getName());
                        System.out.println("Отослана длина файла  " + f.length());
                        DataInputStream inpD;// переменная потока принемаемых данных
                        inpD = new DataInputStream(client.getInputStream());//принемаем из socket входной поток
                        missingBytes = inpD.readLong();//принемает ответ сервера (число)
                        System.out.println(missingBytes);

                        if (missingBytes > -1) {

                            numberMissingBytes(f, outD);

                        } else {
                            if (missingBytes == -2) {//если папка пуста

                                System.out.println("Пришёл ответ: Ничего делать не надо ");

                            } else {

                                if (missingBytes == -3) {//если таких нету

                                    System.out.println("Пришёл ответ: Такого файла нет");
                                    downloadFiles(f, outD);

                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Нет связи с Server_v0");
            }
            return null;
        }
        private void numberMissingBytes(File f, DataOutputStream outD) {//догрузка файла

            try {

                System.out.println("Файл отослан " + f.getName());
                FileInputStream fis = new FileInputStream(f);// create new file input stream
                fis.skip(missingBytes);// skip bytes from file input stream
                byte[] buffer = new byte[64 * 1024];// размер буфера
                int count;//колличество отправленых байт

                while ((count = fis.read(buffer)) != -1) {
                    outD.write(buffer, 0, count);

                }
                System.out.println("Файл отослан " + count);
                if(count == -1){
                    f.delete();
                }
                outD.flush();
                fis.close();// releases all system resources from the streams
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void downloadFiles(File f, DataOutputStream outD) {//удаление файлов из списка поочередно.

            try {
                FileInputStream in = new FileInputStream(f);
                byte[] buffer = new byte[64 * 1024];// размер буфера
                int count;//колличество отправленых байт

                while ((count = in.read(buffer)) != -1) {
                    outD.write(buffer, 0, count);

                }
                System.out.println("Файл отослан 2"+count);
                if(count == -1){
                    f.delete();
                }

                outD.flush();
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progressBar.setVisibility(ProgressBar.INVISIBLE);// после передачи файла становиться не видимым

            if (pong1 > 0) {
                Toast toast = Toast.makeText(getApplicationContext(),//создаем и отображаем текстовое уведомление
                        R.string.message1,
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                pong1 =0;
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),//создаем и отображаем текстовое уведомление
                        R.string.message6,
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            }
        }
    }
    /*********************************КОНЕЦ*********************************/

    /**********************************МЕТОДЫ МЕНЮ СПИСКА**********************************/
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
    /*********************************КОНЕЦ*********************************/

    /*****************************МЕТОДЫ ПРОВЕРКИ НАЛИЧИЯ ОБНОВЛЕНИЙ*******************************/
    public static synchronized void searchUpdatings() {//проверяем наличие обновлений

        try {
            ipUpdatesServer = InetAddress.getByName("volmed.org.ru");
            System.out.println("ip адрес домена ipUpdatesServer на данный момент: " + ipUpdatesServer.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        File file = new File("/storage/emulated/0/Download/downloadUpdatingsCentre/" + versionName1 + ".apk");//пророверяем наличие в паке файла обновления если есть не загружаем
        if (file.exists()) {
            System.out.println("Файл обновления уже загружен.");
        } else {
                try {
                    if (ipUpdatesServer != null) {
                        //----------------------------------------------
                        client = new Socket(ipUpdatesServer.getHostAddress(), portUpdatesServer);
                        //----------------------------------------------

                        int ping = 1;
                        int pong;
                        int namberQuery = 8418;

                        DataOutputStream outData = new DataOutputStream(client.getOutputStream());//подключаемся к socket
                        outData.writeInt(ping);//отправляется запрос клиента серверу

                        InputStream in = client.getInputStream();
                        DataInputStream din = new DataInputStream(in);
                        pong = din.readInt(); // получаем ответ

                        if (pong == 1){
                            outData.writeInt(namberQuery);//отправляется № запроса
                            double requestOnUpdating = versionNumber;
                        outData.writeDouble(requestOnUpdating);//отправляется запрос клиента серверу
                        int pong1 = din.readInt();// получаем ответ на наличие обновлений
                        if (pong1 == 1) {
                            client.close();
                        } else {
                            if (pong1 == 0) {
                                downloadUpdatings();
                            }
                        }
                    }
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    System.out.println("Нет связи с сервером обновлений, ip домена: " + ipUpdatesServer.getHostAddress());
                }
            }
        }
    public static void downloadUpdatings(){//загружаем обновления

        //---------------создаём папку для загрузок-----------------------
        directory1 = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "downloadUpdatingsCentre");
        if (!directory1.exists())
            directory1.mkdirs();
        //---------------конец-----------------------

        //---------------загружаем-----------------------
        try {
            InputStream in = client.getInputStream();
            DataInputStream din = new DataInputStream(in);

            long fileSize = din.readLong(); // получаем размер файла
            String fileName = din.readUTF(); //приём имени файла
            System.out.println("fileSize."+ fileSize);

            byte[] buffer = new byte[64 * 1024];
            FileOutputStream outF = new FileOutputStream("/storage/emulated/0/Download/downloadUpdatingsCentre//" + fileName);
            System.out.println("Загружаем обновления.");

            int count, total = 0;

            while ((count = din.read(buffer, 0, (int) Math.min(buffer.length, fileSize - total))) != -1) {
                total += count;
                outF.write(buffer, 0, count);

                if (total == fileSize) {
                    break;
                }
            }
            client.close();
        }catch(Exception e){
            // e.printStackTrace();
            System.out.println("Связь с сервером обновлений прекращена.");
        }
        //---------------конец-----------------------
    }

    public void openFilesUpdatings(){//метод открывает файл

        // Выводим сообщение об обновлениях
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.updatings,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File("/storage/emulated/0/Download/downloadUpdatingsCentre/"+ versionName1 +".apk")), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    /*********************************КОНЕЦ*********************************/

    /**************************КЛАСС УДАЛЕНИЯ СТАРЫХ ВЕРСИЙ ФОНОВЫЙ ПОТОК**************************/
    public class DeleteFilesUpdatings extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            versionName2 = versionNumber - 0.1;//уменьшает номер версии на 2 от текущей и удаляет его
            System.out.println("№ версии для удаления: " +versionName2 );
            //Delete update file if exists
            File file = new File("/storage/emulated/0/Download/downloadUpdatingsCentre/"+ versionName2 +".apk");
            if (file.exists()) {
                file.delete(); //отправка файлов из списка поочередно.
            }
            return null;
        }
    }
    /*********************************КОНЕЦ*********************************/

    /*****************************КЛАСС GPS TRACKER ФОНОВЫЙ ПОТОК**********************************/
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

    /*******ПРОВЕРЯЕМ НАЛИЧЕЕ СВЯЗИ С СЕРВЕРОМ GPS ФОНОВЫЙ ПОТОК**********/
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
    /*********************************КОНЕЦ**********************************/
}
