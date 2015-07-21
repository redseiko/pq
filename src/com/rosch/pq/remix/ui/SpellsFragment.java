package com.rosch.pq.remix.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.util.Roman;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class SpellsFragment extends Fragment
{
	private ListView spellsListView;
	private SpellsListAdapter spellsListAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.spells_fragment, container, false);
		
		spellsListView = (ListView) view.findViewById(R.id.spells_listview);
		
		spellsListAdapter = new SpellsListAdapter();
		spellsListView.setAdapter(spellsListAdapter);
		
		
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
		{
			spellsListAdapter.setSpells(player.getSpellBook());
			spellsListAdapter.notifyDataSetChanged();
		}
	}
	
	private class SpellsListAdapter extends BaseAdapter
	{
		private List<Map.Entry<String, Roman>> spellsList = Collections.emptyList();
		
		public void setSpells(Map<String, Roman> spellsMap)
		{
			spellsList = new ArrayList<Map.Entry<String, Roman>>(spellsMap.entrySet());
		}
		
		@Override
		public int getCount()
		{
			return spellsList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return spellsList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spell_listitem, parent, false);
			
			Map.Entry<String, Roman> spell = spellsList.get(position);
			
			((TextView) convertView.findViewById(R.id.spell_title)).setText(spell.getKey());
			((TextView) convertView.findViewById(R.id.spell_level_value)).setText(getString(R.string.spell_level_text, spell.getValue().getRoman()));			
			
			return convertView;
		}
	}
}
