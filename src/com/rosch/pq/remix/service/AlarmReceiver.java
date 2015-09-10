package com.rosch.pq.remix.service;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.util.Vfs;

public class AlarmReceiver extends BroadcastReceiver
{
	public static void startAlarm(Context context)
	{		
        Vfs.writeLastAlarmTime(context);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 6000, 6000, getAlarmPendingIntent(context));
        
        Log.d("ALARM", "Alarm started...");
	}
	
	public static void stopAlarm(Context context)
	{
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(getAlarmPendingIntent(context));
		
		Log.d("ALAR", "Alarm stopped.");
	}
	
    private static PendingIntent getAlarmPendingIntent(Context context)
    {
    	return PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), 0);
    }	
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d("ALARM", "Alarm firing!");
		
		String playerId = Vfs.getPlayerId(context);
		Player player = null;
		
		if (playerId == null)
			return;
		
		try
		{
			player = Player.loadPlayer(Vfs.readPlayerFromFile(context, playerId));
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
			return;
		}	
		
		long lastAlarmTime = Vfs.readLastAlarmTime(context);		
		Log.d("ALARM", "Last time is: " + lastAlarmTime);
		
		long remainingTime = System.currentTimeMillis() - lastAlarmTime;		
		Log.d("ALARM", "Remaining time is: " + remainingTime);
		
		int tasksCompleted = player.executeTurnsForDuration(remainingTime);		
		Log.d("ALARM", "Tasks done: " + tasksCompleted);
		
		try
		{
			Vfs.writePlayerToFile(context, playerId, player.savePlayer());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Vfs.writeLastAlarmTime(context);
		
		Log.d("ALARM", "Alarm finished.");
	}
}
