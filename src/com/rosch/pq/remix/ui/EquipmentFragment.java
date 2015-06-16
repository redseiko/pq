package com.rosch.pq.remix.ui;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.github.pkunk.pq.gameplay.Equips;
import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.ui.util.UiUtils;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class EquipmentFragment extends Fragment
{
	private TableLayout equipTable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.equip_fragment, container, false);
		equipTable = (TableLayout) view.findViewById(R.id.ph_equip_table);		
		
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
			refreshEquipTable(player.getEquip());
	}

	private void refreshEquipTable(List<String> equipment)
	{
        equipTable.removeAllViews();

        for (int i = 0; i < Equips.EQUIP_NUM; i++)
        {
            String equipName = Equips.label[i];
            String equipItem = equipment.get(i);
            
            TableRow row = UiUtils.getTableRow(equipTable.getContext(), equipName, equipItem);
            equipTable.addView(row);
        }
	}	
}
