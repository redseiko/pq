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
import com.github.pkunk.pq.util.PqUtils;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class PlotFragment extends Fragment
{
	private TableLayout plotTable;
	private ScrollView plotTableScroll;
	private TextProgressBar developmentProgressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plot_fragment, container, false);
		
		plotTable = (TableLayout) view.findViewById(R.id.ph_plot_table);
		plotTableScroll = (ScrollView) view.findViewById(R.id.ph_plot_scroll);
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
			refreshPlotTable(player.getPlot());
		
		refreshDevelopmentProgressBar(player.getCurrentPlotProgress(), player.getMaxPlotProgress());
	}

	private void refreshPlotTable(List<String> plot)
	{
        plotTable.removeAllViews();

        int lastIndex = plot.size() - 1;

        for (int i = lastIndex; i >= 0; i--) {
            TableRow row = UiUtils.getCheckedRow(plotTable.getContext(), i != lastIndex, plot.get(i));
            plotTable.addView(row);
        }
        
        plotTableScroll.fullScroll(ScrollView.FOCUS_UP);
	}
	
	private void refreshDevelopmentProgressBar(int currentPlotProgress, int maxPlotProgress)
	{
		developmentProgressBar.setMax(maxPlotProgress);
		developmentProgressBar.setProgress(currentPlotProgress);
		
		String roughTime = PqUtils.roughTime(maxPlotProgress - currentPlotProgress);
		developmentProgressBar.setText(getString(R.string.development_progressbar_text, roughTime));
	}
}
