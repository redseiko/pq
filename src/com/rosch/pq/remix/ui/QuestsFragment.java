package com.rosch.pq.remix.ui;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.ui.util.UiUtils;
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class QuestsFragment extends Fragment 
{
	private TableLayout questsTable;
	private ScrollView questsTableScroll;
	private TextProgressBar completionProgressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.quests_fragment, container, false);
		
		questsTable = (TableLayout) view.findViewById(R.id.ph_quests_table);
		questsTableScroll = (ScrollView) view.findViewById(R.id.ph_quests_scroll);
		completionProgressBar = (TextProgressBar) view.findViewById(R.id.ph_quests_bar);
		
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
		
		if (event.forceRefresh || player.isQuestsUpdated())
			refreshQuestsTable(player.getQuests());
		
		refreshCompletionProgressBar(player.getCurrentQuestProgress(), player.getMaxQuestProgress());
	}

	private void refreshQuestsTable(List<String> quests)
	{
        questsTable.removeAllViews();
        
        int lastIndex = quests.size() - 1;

        for (int i = lastIndex; i >= 0; i--)
        {
            TableRow row = UiUtils.getCheckedRow(questsTable.getContext(), i != lastIndex, quests.get(i));
            questsTable.addView(row);
        }
        
        questsTableScroll.fullScroll(ScrollView.FOCUS_UP);
	}
	

	private void refreshCompletionProgressBar(int currentQuestProgress, int maxQuestProgress)
	{
		completionProgressBar.setMax(maxQuestProgress);
		completionProgressBar.setProgress(currentQuestProgress);
		
		int percentage = (currentQuestProgress * 100 / maxQuestProgress);
		completionProgressBar.setText(getString(R.string.completion_progressbar_text, percentage));
	}
}
