package rs.elfak.jajac.geowarfare.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rs.elfak.jajac.geowarfare.App;
import rs.elfak.jajac.geowarfare.services.BackgroundLocationService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent backgroundLocationIntent = new Intent(App.getContext(), BackgroundLocationService.class);
            context.startService(backgroundLocationIntent);
        }
    }

}
