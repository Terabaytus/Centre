package cod.ru.centre;

import android.app.DialogFragment;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import java.io.File;

import static android.R.attr.width;
import static cod.ru.centre.R.attr.height;


/**
 * Created by Admin on 18.04.2017.
 */

public class Dictaphone extends DialogFragment implements View.OnClickListener {

    private MediaRecorder mediaRecorder;
    String fileName;
    
    public Dictaphone(String latitude, String longitude, String date, String number){
        createDirectory(latitude, longitude, date, number);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Прокоментируйте фотографию.");
        View view = inflater.inflate(R.layout.dialog_dictophone, null);
        view.findViewById(R.id.btnstart).setOnClickListener(this);
        view.findViewById(R.id.btnstop).setOnClickListener(this);

        return view;
    }
   /* public void onResume(){
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(width, height);
        window.setGravity(Gravity.CENTER);
    }*/
    public void onClick(View view) {
        String nameButton = (String) ((Button) view).getText();
        if("Старт".equals(nameButton)) {
            recordStart();
            System.out.println("Начало записи " + ((Button) view).getText());
        }

        if("Стоп".equals(nameButton)) {
            recordStop();
            System.out.println("Конец записи " + ((Button) view).getText());
            dismiss();
        }

    }
    public void recordStart() {

        try {
            releaseRecorder();

            File outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recordStop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**********************************ИИНФОРМАЦИЯ О ФАЙЛЕ И ЕГО РАСПОЛОЖЕНИЕ**********************/
    private void createDirectory(String latitude, String longitude, String date, String number) {
        String informationAboutAudioFile = "Аудио запись к файлу";
        if(latitude == null) {
            latitude = "00.0000";
        }
        if(longitude == null) {
            longitude = "00.0000";
        }

        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Photo_and_Video/" + "audio "
                + date + "  " + latitude + "    " + longitude + "     " + number + "      " + informationAboutAudioFile + ".3gpp";

        File directory; directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Photo_and_Video");
        if (!directory.exists())
            directory.mkdirs();
    }
    /*********************************КОНЕЦ*********************************/
}
