package com.rosch.pq.remix.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class StatusBarFragment extends Fragment
{
	private TextView taskText;
	private TextProgressBar taskProgressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.statusbar_fragment, container, false);
		
		taskText = (TextView) view.findViewById(R.id.ph_task_text);
		taskProgressBar = (TextProgressBar) view.findViewById(R.id.ph_task_bar);
		
		return view;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		EventBus.getDefault().register(this);
	}
	
	@Override
	public void onStop()
	{
		EventBus.getDefault().unregister(this);
		
		super.onStop();
	}
	
	@Subscribe
	public void onEventMainThread(PlayerModifiedEvent event)
	{
		Player player = event.player;
		
		refreshTask(player.getCurrentTask());
		refreshTaskProgressBar(player.getCurrentTaskTime());
	}

	private void refreshTask(String currentTask)
	{
		taskText.setText(currentTask);
	}
	
	@SuppressLint("NewApi")
	private void refreshTaskProgressBar(int taskTime)
	{
		taskProgressBar.setMax(taskTime);
		taskProgressBar.setProgress(0);
		
		ObjectAnimator animation = ObjectAnimator.ofInt(taskProgressBar, "progress", 0, taskTime);
		animation.setDuration(taskTime);
		animation.start();
	}	
}
