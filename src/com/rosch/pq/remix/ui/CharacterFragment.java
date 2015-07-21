package com.rosch.pq.remix.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.gameplay.Stats;
import com.github.pkunk.pq.gameplay.Traits;
import com.github.pkunk.pq.ui.util.UiUtils;
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class CharacterFragment extends Fragment
{
	private TableLayout statsTable;
	private TextProgressBar levelProgressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.character_fragment, container, false);
		
		statsTable = (TableLayout) view.findViewById(R.id.ph_stats_table);
		levelProgressBar = (TextProgressBar) view.findViewById(R.id.ph_level_bar);
		
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
		
		if (event.forceRefresh || player.isTraitsUpdated())
			refreshTraitsTable(player.getTraits());
		
		refreshLevelProgressBar(player.getCurrentExp(), player.getMaxExp());
		
        if (event.forceRefresh || player.isStatsUpdated())
        	refreshStatsTable(player.getStats());
	}
	
	private void refreshTraitsTable(Traits traits)
	{
		View view = getView();
		
		((TextView) view.findViewById(R.id.info_name_and_race)).setText(getString(R.string.info_name_and_race, traits.getName(), traits.getRace()));
		((TextView) view.findViewById(R.id.info_level_and_class)).setText(getString(R.string.info_level_and_class, traits.getLevel(), traits.getRole()));
	}
	
	private void refreshLevelProgressBar(int currentExp, int maxExp)
	{
		levelProgressBar.setMax(maxExp);
		levelProgressBar.setProgress(currentExp);
		
		levelProgressBar.setText(getString(R.string.level_progressbar_text, (maxExp - currentExp)));
	}
	
	private void refreshStatsTable(Stats stats)
	{
        statsTable.removeAllViews();
    	
        TableRow headerStats = UiUtils.getHeaderRow(statsTable.getContext(), "Stat", "Value");
        statsTable.addView(headerStats);

        for (int i = 0; i < Stats.STATS_NUM; i++)
        {
            String statName = Stats.label[i];
            String statValue = String.valueOf(stats.get(i));

            TableRow row = UiUtils.getTableRow(statsTable.getContext(), statName, statValue);
            statsTable.addView(row);
        }	
	}
}
