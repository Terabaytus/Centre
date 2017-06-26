package cod.ru.centre;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Admin on 29.08.2016.
 */
public class AutoStart extends BroadcastReceiver {

    public AutoStart() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startServiceIntent = new Intent(context, AutoStart.class);
            context.startService(startServiceIntent);
        }
    }
}
