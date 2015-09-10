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
import com.github.pkunk.pq.ui.view.TextProgressBar;
import com.github.pkunk.pq.util.PqUtils;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class PlotFragment extends Fragment
{
	private ListView plotListView;
	private PlotListAdapter plotListAdapter;
	
	private TextProgressBar developmentProgressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plot_fragment, container, false);
		
		plotListView = (ListView) view.findViewById(R.id.plot_listview);
		
		plotListAdapter = new PlotListAdapter();
		plotListView.setAdapter(plotListAdapter);
		
		developmentProgressBar = (TextProgressBar) view.findViewById(R.id.ph_plot_bar);
		
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
		
		if (event.forceRefresh || player.isPlotUpdated())
		{
			plotListAdapter.setPlot(player.getPlot());
			plotListAdapter.notifyDataSetChanged();
		}
		
		refreshDevelopmentProgressBar(player.getCurrentPlotProgress(), player.getMaxPlotProgress());
	}
	
	private class PlotListAdapter extends BaseAdapter
	{
		private List<String> plotList = Collections.<String>emptyList();
		
		public void setPlot(List<String> plot)
		{
			plotList = plot;
		}
		
		@Override
		public int getCount()
		{
			return plotList.size();
		}
		
		@Override
		public Object getItem(int position)
		{
			return plotList.get(position);
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
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plot_listitem, parent, false);
			
			String plotTitle = plotList.get(position);
			
			((TextView) convertView.findViewById(R.id.plot_title)).setText(plotTitle);
			
			return convertView;
		}
	}
	
	private void refreshDevelopmentProgressBar(int currentPlotProgress, int maxPlotProgress)
	{
		developmentProgressBar.setMax(maxPlotProgress);
		developmentProgressBar.setProgress(currentPlotProgress);
		
		String roughTime = PqUtils.roughTime(maxPlotProgress - currentPlotProgress);
		developmentProgressBar.setText(getString(R.string.development_progressbar_text, roughTime));
	}
}
