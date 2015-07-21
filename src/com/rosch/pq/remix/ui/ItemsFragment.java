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
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class ItemsFragment extends Fragment
{
	private ListView itemsListView;
	private ItemsListAdapter itemsListAdapter;

	private TextProgressBar encumberanceProgressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.items_fragment, container, false);
		
		itemsListAdapter = new ItemsListAdapter();

		itemsListView = (ListView) view.findViewById(R.id.items_listview);
		itemsListView.setAdapter(itemsListAdapter);
		
		encumberanceProgressBar = (TextProgressBar) view.findViewById(R.id.ph_encum_bar);
		
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
		
		if (event.forceRefresh || player.isItemsUpdated())
		{
			itemsListAdapter.setItems(player.getInventory());
			itemsListAdapter.notifyDataSetChanged();
		}
		
		refreshEncumberanceProgressBar(player.getCurrentEncumbrance(), player.getMaxEncumbrance());
	}
	
	private class ItemsListAdapter extends BaseAdapter
	{
		private List<Map.Entry<String, Integer>> itemsList = Collections.emptyList();
		
		public void setItems(Map<String, Integer> items)
		{
			itemsList = new ArrayList<Map.Entry<String, Integer>>(items.entrySet());
		}
		
		@Override
		public int getCount()
		{
			return itemsList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return itemsList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (position > 0)
				position = (itemsList.size() - position);

			if (convertView == null)
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listitem, parent, false);
			
			String itemTitle = itemsList.get(position).getKey();
			int itemQuantity = itemsList.get(position).getValue();
			
			((TextView) convertView.findViewById(R.id.item_title)).setText(itemTitle);
			((TextView) convertView.findViewById(R.id.item_quantity)).setText(itemQuantity > 1 ? String.valueOf(itemQuantity) : "");
			
			return convertView;
		}
		
	}
	
	private void refreshEncumberanceProgressBar(int currentEncumberance, int maxEncumberance)
	{
		encumberanceProgressBar.setMax(maxEncumberance);
		encumberanceProgressBar.setProgress(currentEncumberance);
		
		encumberanceProgressBar.setText(getString(R.string.encumberance_progressbar_text, currentEncumberance, maxEncumberance));
	}
}
