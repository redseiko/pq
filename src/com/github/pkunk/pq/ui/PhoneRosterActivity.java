package com.github.pkunk.pq.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.pkunk.pq.init.Res;
import com.github.pkunk.pq.service.GameplayService;
import com.github.pkunk.pq.util.Vfs;
import com.rosch.pq.remix.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * User: pkunk
 * Date: 2012-02-12
 */
public class PhoneRosterActivity extends AppCompatActivity
{
	private static final String TAG = PhoneRosterActivity.class.getCanonicalName();

	private GameplayService service;
	private volatile boolean isBound = false;

	private Toolbar rosterToolbar;
	private ListView savesListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ph_roster);

		rosterToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(rosterToolbar);

		ActionBar actionBar = (ActionBar) getSupportActionBar();
		actionBar.setTitle(R.string.roster_activity_title);

		savesListView = (ListView) findViewById(R.id.roster_saves);
		
		RosterSavesAdapter adapter = new RosterSavesAdapter();
		adapter.setData(this);
		
		savesListView.setAdapter(adapter);
		savesListView.setOnItemClickListener(adapter);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		Intent intent = new Intent(this, GameplayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop()
	{
		if (isBound)
		{
			unbindService(connection);
			isBound = false;
		}

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.roster_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_new_character:
				createNewPlayer();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "onServiceConnected");

			GameplayService.GameplayBinder binder = (GameplayService.GameplayBinder) service;
			PhoneRosterActivity.this.service = binder.getService();
			isBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d(TAG, "onServiceDisconnected");
			isBound = false;
		}
	};

	private void createNewPlayer()
	{
		Intent intent = new Intent(this, PhoneNewPlayerActivity.class);
		startActivity(intent);
	}
	
	private class RosterSavesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
	{
		private List<PlayerAnnotation> rosterSaves;
		
		public void setData(Activity activity)
		{
			rosterSaves = new ArrayList<PlayerAnnotation>();
			
			String[] saveFiles = Vfs.getPlayersSaveFiles(activity);
			Arrays.sort(saveFiles);
			
			Map<String, List<String>> statusMap = Vfs.readEntryFromFiles(activity, saveFiles, "Annotation");
			
			for (String file : saveFiles)
			{
				PlayerAnnotation player = getStatus(statusMap.get(file));
				rosterSaves.add(player);
			}		
		}

		@Override
		public int getCount()
		{
			return rosterSaves.size();
		}

		@Override
		public Object getItem(int position)
		{
			return rosterSaves.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent)
		{
			if (convertView == null)
			{
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.roster_entry_listitem, parent, false);
			}
			
			View view = convertView;
			PlayerAnnotation player = rosterSaves.get(position);
			
			((TextView) view.findViewById(R.id.player_status1)).setText(player.status1);
			((TextView) view.findViewById(R.id.player_status2)).setText(player.status2);
			((TextView) view.findViewById(R.id.player_status3)).setText(player.status3);
			
			view.findViewById(R.id.roster_entry_status).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					((ListView) parent).performItemClick(view, position, getItemId(position));
				}
			});
			
			view.findViewById(R.id.roster_entry_delete_image).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					((ListView) parent).performItemClick(view, position, getItemId(position));
				}
			});
			
			return view;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			int viewId = view.getId();
			PlayerAnnotation player = rosterSaves.get(position);
			
			switch (viewId)
			{
				case R.id.roster_entry_status:
					selectPlayer(player.playerId);
					break;
					
				case R.id.roster_entry_delete_image:
					showDeletePlayerConfirmation(player.playerId, player.name);
					break;
			}
		}
	}

	private PlayerAnnotation getStatus(List<String> strings)
	{
		PlayerAnnotation result = new PlayerAnnotation();
		for (String s : strings) {
			String entry[] = s.split(Vfs.EQ);
			if ("playerId".equals(entry[0])) {
				result.playerId = entry[1];
			} else if ("status1".equals(entry[0])) {
				result.status1 = entry[1];
			} else if ("status2".equals(entry[0])) {
				result.status2 = entry[1];
			} else if ("status3".equals(entry[0])) {
				result.status3 = entry[1];
			} else if ("name".equals(entry[0])) {
				result.name = entry[1];
			}
		}
		return result;
	}

	private static class PlayerAnnotation
	{
		private String playerId = null;
		private String status1 = "";
		private String status2 = "";
		private String status3 = "";
		private String name = null;
	}
	
	private void selectPlayer(String playerId) {
		Vfs.setPlayerId(this, playerId);
		Intent intent = new Intent(this, PhoneGameplayActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	
	private void showDeletePlayerConfirmation(final String playerId, String name)
	{
		String confirmationText = "Terminate " + Res.MERITS.pick() + " " + name + "?";

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(confirmationText)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				killPlayer(playerId);
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create()
		.show();
	}
	
	private void killPlayer(String playerId)
	{
		String currentPlayerId = Vfs.getPlayerId(this);

		if (playerId.equals(currentPlayerId))
		{
			Vfs.setPlayerId(this, null);
			service.removePlayer();
		}
		
		Vfs.deletePlayerFiles(this, playerId);
		
		RosterSavesAdapter adapter = (RosterSavesAdapter) savesListView.getAdapter();
		
		adapter.setData(this);
		adapter.notifyDataSetChanged();
	}
}
