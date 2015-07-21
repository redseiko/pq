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

import com.github.pkunk.pq.gameplay.Equips;
import com.github.pkunk.pq.gameplay.Player;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class EquipmentFragment extends Fragment
{
	private ListView equipmentListView;
	private EquipmentListAdapter equipmentListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.equip_fragment, container, false);
		
		equipmentListView = (ListView) view.findViewById(R.id.equipment_listview);
		
		equipmentListAdapter = new EquipmentListAdapter();
		equipmentListView.setAdapter(equipmentListAdapter);
		
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
		
		if (event.forceRefresh || player.isEquipUpdated())
		{
			equipmentListAdapter.setEquipment(player.getEquip());
			equipmentListAdapter.notifyDataSetChanged();
		}
	}
	
	private class EquipmentListAdapter extends BaseAdapter
	{
		private List<String> equipmentList = Collections.<String>emptyList();
		
		public void setEquipment(List<String> equipmentList)
		{
			this.equipmentList = equipmentList;
		}

		@Override
		public int getCount()
		{
			return equipmentList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return equipmentList.get(position);
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
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.equipment_listitem, parent, false);
			
			String slotTitle = Equips.label[position];
			String itemTitle = equipmentList.get(position);
			
			((TextView) convertView.findViewById(R.id.equipment_slot_title)).setText(slotTitle);
			((TextView) convertView.findViewById(R.id.equipment_item_title)).setText(itemTitle);
			
			return convertView;
		}
	}
}
