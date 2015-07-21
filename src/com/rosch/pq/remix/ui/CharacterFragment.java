package com.rosch.pq.remix.ui;

import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.gameplay.Stats;
import com.github.pkunk.pq.gameplay.Traits;
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class CharacterFragment extends Fragment
{
	private ListView attributesListView;
	private AttributesListAdapter attributesListAdapter;
	
	private TextProgressBar levelProgressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.character_fragment, container, false);
		
		attributesListView = (ListView) view.findViewById(R.id.attributes_listview);
		attributesListView.addHeaderView(inflater.inflate(R.layout.character_exp_cardview, attributesListView, false));
		attributesListView.addHeaderView(inflater.inflate(R.layout.character_info_cardview, attributesListView, false));
		
		attributesListAdapter = new AttributesListAdapter();
		attributesListView.setAdapter(attributesListAdapter);
		
		levelProgressBar = (TextProgressBar) attributesListView.findViewById(R.id.ph_level_bar);
		
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
			refreshTraits(player.getTraits());
		
		refreshLevelProgressBar(player.getCurrentExp(), player.getMaxExp());
		
        if (event.forceRefresh || player.isStatsUpdated())
        {
        	attributesListAdapter.setAttributeValues(player.getStats());
        	attributesListAdapter.notifyDataSetChanged();
        }
	}
	
	private class AttributesListAdapter extends BaseAdapter
	{
		private List<Integer> attributeValues = Collections.<Integer>emptyList();
		
		public void setAttributeValues(List<Integer> values)
		{
			attributeValues = values;
		}
		
		@Override
		public int getCount()
		{
			return attributeValues.size();
		}

		@Override
		public Object getItem(int position)
		{
			return attributeValues.get(position);
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
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.attribute_listitem, parent, false);
			
			((TextView) convertView.findViewById(R.id.attribute_title)).setText(Stats.label[position]);
			((TextView) convertView.findViewById(R.id.attribute_value)).setText(String.valueOf(attributeValues.get(position)));
			
			return convertView;
		}
	}
	
	private void refreshTraits(Traits traits)
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
}
