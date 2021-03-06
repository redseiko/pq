package com.github.pkunk.pq.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.service.GameplayService;
import com.github.pkunk.pq.service.GameplayServiceListener;
import com.github.pkunk.pq.util.Vfs;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;
import com.rosch.pq.remix.service.AlarmReceiver;
import com.rosch.pq.remix.ui.PlayerFragmentPagerAdapter;

import de.greenrobot.event.EventBus;

/**
 * User: pkunk
 * Date: 2012-01-25
 */
public class PhoneGameplayActivity extends AppCompatActivity implements GameplayServiceListener {
    private static final String TAG = PhoneGameplayActivity.class.getCanonicalName();

    private String playerId;

    private GameplayService service;
    private volatile boolean isBound = false;
    
    private Toolbar mainToolbar;    
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    
    private ViewPager contentViewPager;
    private PlayerFragmentPagerAdapter contentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ph_gameplay);
        
		setupToolbar();
        setupViewPager();		
        setupNavigationDrawer();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        playerId = Vfs.getPlayerId(this);
        if (playerId == null || playerId.length() == 0)
        {
            Intent intent = new Intent(PhoneGameplayActivity.this, PhoneRosterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

            PhoneGameplayActivity.this.finish();
            return;
        }
        
        AlarmReceiver.stopAlarm(this);
        startGameplayService();
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	mainToolbar.setTitle(contentPagerAdapter.getPageTitle(contentViewPager.getCurrentItem()));
    	onGameplay();
    }

    @Override
    protected void onStop()
    {
    	stopGameplayService();
        AlarmReceiver.startAlarm(this);
        
        super.onStop();
    }

    @Override
    public void onGameplay()
    {
        if (isBound)
        {
            Player player = service.getPlayer();
            updateUi(player, false);
        }
    }
    
    private void startGameplayService()
    {
        Intent intent = new Intent(this, GameplayService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    private void stopGameplayService()
    {
        if (isBound)
        {
            PhoneGameplayActivity.this.service.removeGameplayListener(PhoneGameplayActivity.this);
            unbindService(connection);
            isBound = false;            
        }
    }
    
    private void setupViewPager()
    {
        contentViewPager = (ViewPager) findViewById(R.id.content_viewpager);
        contentPagerAdapter = new PlayerFragmentPagerAdapter(
        	getSupportFragmentManager(), getResources().getStringArray(R.array.player_fragment_page_titles));
        
        contentViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
        	@Override
        	public void onPageSelected(int position)
        	{
        		mainToolbar.setTitle(contentPagerAdapter.getPageTitle(position));
        	}
        });

        contentViewPager.setAdapter(contentPagerAdapter);
        contentViewPager.setOffscreenPageLimit(6);
    }

    private void setupToolbar()
    {
    	mainToolbar = (Toolbar) findViewById(R.id.toolbar);
    	setSupportActionBar(mainToolbar);

    	getSupportActionBar().setHomeButtonEnabled(true);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer()
    {
    	drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mainToolbar, android.R.string.yes, android.R.string.no);

    	drawerLayout.setDrawerListener(drawerToggle);
    	drawerToggle.syncState();

    	Resources resources = getResources();    
    	List<String> drawerItems = new ArrayList<String>(Arrays.asList(resources.getStringArray(R.array.player_fragment_page_titles)));

    	drawerItems.add((String) resources.getText(R.string.drawer_item_roster_label));
    	drawerItems.add((String) resources.getText(R.string.drawer_item_about_label));
    	
    	drawerList = (ListView) findViewById(R.id.left_drawer);
    	drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems));

    	drawerList.setOnItemClickListener(new ListView.OnItemClickListener()
    	{
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    		{
    			drawerLayout.closeDrawers();
    			
    			int count = drawerList.getAdapter().getCount();

    			if (position < (count - 2)) {
    				((ViewPager) findViewById(R.id.content_viewpager)).setCurrentItem(position);
    			} else if (position == (count - 2)) {
    				openRoster();    				
    			} else if (position == (count - 1)) {
    				openAbout();
    			}
    		}
    	});
    }

    private void openRoster()
    {
        Intent intent = new Intent(this, PhoneRosterActivity.class);
        startActivity(intent);
    }

    private void openAbout()
    {
    	new DialogFragment() {
    		@Override
    		public Dialog onCreateDialog(Bundle savedInstancedState) {		
    			return new AlertDialog.Builder(getActivity())
    				.setTitle(R.string.about_dialog_title)
    				.setMessage(R.string.about_dialog_message)
    				.setPositiveButton(android.R.string.ok, null)
    				.create();
    		}
    	}.show(getSupportFragmentManager(), "about-dialog");
    }

    private void updateUi(Player player, boolean forceRefresh) {
    	EventBus eventBus = EventBus.getDefault();
    	eventBus.post(new PlayerModifiedEvent(player, forceRefresh));
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(TAG, "onServiceConnected");

            GameplayService.GameplayBinder binder = (GameplayService.GameplayBinder) service;
            PhoneGameplayActivity.this.service = binder.getService();
            isBound = true;

            PhoneGameplayActivity.this.service.addGameplayListener(PhoneGameplayActivity.this);

            Player player = PhoneGameplayActivity.this.service.getPlayer();
            if (player == null || !playerId.equals(player.getPlayerId())) {
                try {
                    Player savedPlayer = PhoneGameplayActivity.this.service.loadPlayer(playerId);
                    PhoneGameplayActivity.this.service.setPlayer(savedPlayer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                player = PhoneGameplayActivity.this.service.getPlayer();
                PhoneGameplayActivity.this.service.setWidgetOutdated();
            }

            if (player != null)
            {
            	// TODO: de-duplicate me from AlarmReceiver.
        		long lastAlarmTime = Vfs.readLastAlarmTime(PhoneGameplayActivity.this);		
        		Log.d("ALARM", "Last time is: " + lastAlarmTime);
        		
        		long remainingTime = System.currentTimeMillis() - lastAlarmTime;		
        		Log.d("ALARM", "Remaining time is: " + remainingTime);
        		
        		int tasksCompleted = player.executeTurnsForDuration(remainingTime);        		
        		Log.d("ALARM", "Tasks done: " + tasksCompleted);
        		
        		Vfs.writeLastAlarmTime(PhoneGameplayActivity.this);
        		player.savePlayer();
            	
                updateUi(player, true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            isBound = false;
        }
    };
}
