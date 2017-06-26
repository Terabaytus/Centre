package cod.ru.centre;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.content.Context.TELEPHONY_SERVICE;
import static cod.ru.centre.MainActivity.RQS_RECORDING;

/**
 * Created by Admin on 17.04.2017.
 */

public class WorkWithFoto extends Fragment {

    private static final int TAKE_PICTURE = 1;
    final int TYPE_PHOTO = 1;
    final int REQUEST_CODE_PHOTO = 1;

    String latitude1;
    String longitude1;
    String informationAboutFile;
    String informationAboutAudioFile;

    File directory;
/*
    public WorkWithFoto(String latitude, String longitude) {
        latitude1 = latitude;
        longitude1 =longitude;
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
 /*   @Override
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


    /**********************************ИИНФОРМАЦИЯ О ФАЙЛЕ И ЕГО РАСПОЛОЖЕНИЕ**********************/
 /*  private Uri generateFileUri(int type) {
       String number;
       //-------БЛОК КОНСТРУКЦИЙ ДЛЯ ОПРЕДЕЛЕНИЯ IMEI УСТРОЙСТВА--------
       TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
       number = telephonyManager.getDeviceId();// определяем imei стройства
       //---------------КОНЕЦ-----------------------
       String timeStamp = new SimpleDateFormat("yyyyMMdd  HHmmss ").format(new Date());
       String date = timeStamp;
        if(latitude1 == null) {
            latitude1 = "00.0000";
        }
        if(longitude1 == null) {
            longitude1 = "00.0000";
        }
        File file = null;
        switch (type) {
            case TYPE_PHOTO:
                file = new File(directory.getPath() + "/" + "photo "
                        + date + "  " + latitude1 + "    " + longitude1 + "     " + number + "      " + informationAboutFile + ".jpg");
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


}
