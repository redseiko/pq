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
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class ItemsFragment extends Fragment
{
	private TableLayout itemsTable;
	private ScrollView itemsTableScroll;
	private TextProgressBar encumberanceProgressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.items_fragment, container, false);
		
		itemsTable = (TableLayout) view.findViewById(R.id.ph_items_table);
		itemsTableScroll = (ScrollView) view.findViewById(R.id.ph_items_scroll);
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
			refreshItemsTable(player.getInventory(), player.isGoldUpdated());
		
		refreshEncumberanceProgressBar(player.getCurrentEncumbrance(), player.getMaxEncumbrance());
	}

	private void refreshItemsTable(Map<String, Integer> items, boolean goldUpdated)
	{
        itemsTable.removeAllViews();

        TableRow header = UiUtils.getHeaderRow(itemsTable.getContext(), "Item", "Qty");
        itemsTable.addView(header);
        
        for (Map.Entry<String,Integer> item : items.entrySet())
        {
            TableRow row = UiUtils.getTableRow(itemsTable.getContext(), item.getKey(), item.getValue().toString());
            itemsTable.addView(row);
        }
        
        itemsTableScroll.fullScroll(goldUpdated ? ScrollView.FOCUS_UP : ScrollView.FOCUS_DOWN);
	}
	
	private void refreshEncumberanceProgressBar(int currentEncumberance, int maxEncumberance)
	{
		encumberanceProgressBar.setMax(maxEncumberance);
		encumberanceProgressBar.setProgress(currentEncumberance);
		
		encumberanceProgressBar.setText(getString(R.string.encumberance_progressbar_text, currentEncumberance, maxEncumberance));
	}
}
