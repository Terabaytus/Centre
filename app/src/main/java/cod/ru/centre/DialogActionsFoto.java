package cod.ru.centre;

/**
 * Created by Admin on 17.04.2017.
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
//-------КЛАСС ДИОЛОГОВОГО ОКНА ДЛЯ ДЕСТВИЙ С ФОТОГРАФИЕЙ--------
public class DialogActionsFoto extends DialogFragment implements OnClickListener {


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Выберите действие с фотографией.");
        View view = inflater.inflate(R.layout.dialog_actions_foto, null);
        view.findViewById(R.id.btnAudioCommentary).setOnClickListener(this);
        view.findViewById(R.id.btnNoTags).setOnClickListener(this);
        view.findViewById(R.id.btnSign).setOnClickListener(this);
        return view;
    }

    public void onClick(View view) {
        System.out.println("Dialog 1: " + ((Button) view).getText());
        String nameButton = (String) ((Button) view).getText();
        if("Аудио коментарий".equals(nameButton)) {
                if (getActivity() != null) {
                    MainActivity act = (MainActivity) getActivity();
                    act.fotoWithAudio();
                }
            dismiss();
        }
        if("Без отметок".equals(nameButton)) {
            if (getActivity() != null) {
                MainActivity act = (MainActivity) getActivity();
                act.photo();
            }
            dismiss();
        }
        if("Подписать".equals(nameButton)) {
            if (getActivity() != null) {
                MainActivity act = (MainActivity) getActivity();
                act.signAphoto();
            }
            dismiss();
        }
    }
}
//---------------КОНЕЦ-----------------------