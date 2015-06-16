package com.rosch.pq.remix.ui;

import java.util.Map;

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
import com.github.pkunk.pq.util.Roman;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class SpellsFragment extends Fragment
{
	private TableLayout spellsTable;
	private ScrollView spellsTableScroll;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.spells_fragment, container, false);
		
		spellsTable = (TableLayout) view.findViewById(R.id.ph_spell_table);
		spellsTableScroll = (ScrollView) view.findViewById(R.id.ph_spell_scroll);
		
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
		
		if (event.forceRefresh || player.isSpellsUpdated())
			refreshSpellTable(player.getSpellBook());
	}

	private void refreshSpellTable(Map<String, Roman> spells)
	{
		spellsTable.removeAllViews();
		
        TableRow header = UiUtils.getHeaderRow(spellsTable.getContext(), "Spell", "Level");
        spellsTable.addView(header);
        
        for (Map.Entry<String, Roman> spell : spells.entrySet())
        {
            TableRow row = UiUtils.getTableRow(spellsTable.getContext(), spell.getKey(), spell.getValue().toString());
            spellsTable.addView(row);
        }
        
        spellsTableScroll.fullScroll(ScrollView.FOCUS_DOWN);
	}
}
