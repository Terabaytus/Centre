package cod.ru.centre;

/**
 * Created by Admin on 10.08.2016.
 */
public class ThreadSearchUpdatings implements Runnable{
    @Override
    public void run() {// в новом потоке вызываем методы отвечающие за обновления

    MainActivity.searchUpdatings();


    }
}
