package com.github.pkunk.pq.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.service.GameplayService;
import com.github.pkunk.pq.service.GameplayServiceListener;
import com.github.pkunk.pq.ui.util.UiUtils;
import com.github.pkunk.pq.util.Vfs;
import com.rosch.pq.remix.R;
import com.rosch.pq.remix.events.PlayerModifiedEvent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ph_gameplay);
        
        ViewPager viewPager = (ViewPager) findViewById(R.id.content_viewpager);
        PlayerFragmentPagerAdapter pagerAdapter = new PlayerFragmentPagerAdapter(
        	getSupportFragmentManager(), getResources().getStringArray(R.array.player_fragment_page_titles));

		viewPager.setAdapter(pagerAdapter);
		viewPager.setOffscreenPageLimit(6);
		
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, android.R.string.yes, android.R.string.no);
        
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(
        	this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.player_fragment_page_titles)));
        
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((ViewPager) findViewById(R.id.content_viewpager)).setCurrentItem(position);
				drawerLayout.closeDrawers();
			}
        });
        
        TabLayout tabLayout = (TabLayout)  findViewById(R.id.content_tablayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        playerId = Vfs.getPlayerId(this);
        if (playerId == null || playerId.length() == 0) {
            Intent intent = new Intent(PhoneGameplayActivity.this, PhoneRosterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            PhoneGameplayActivity.this.finish();
            return;
        }

        // Bind to GameplayService
        Intent intent = new Intent(this, GameplayService.class);
//        startService(intent);   //todo: remove to let service die
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //taskBarUpdater = new TaskBarUpdater(this, R.id.ph_task_bar);
        //taskBarUpdater.execute();
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        if (isBound) {
            PhoneGameplayActivity.this.service.removeGameplayListener(PhoneGameplayActivity.this);
            unbindService(connection);
            isBound = false;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onGameplay() {
        if (isBound) {
            // Don't block this thread
            Player player = service.getPlayer();
            updateUi(player, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_roster:
                openRoster();
                return true;
            case R.id.menu_about:
                openAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openRoster() {
        Intent intent = new Intent(this, PhoneRosterActivity.class);
        startActivity(intent);
    }

    private void openAbout() {
        Dialog aboutDialog = UiUtils.aboutDialog(this);
        aboutDialog.show();
    }

    private void updateUi(Player player, boolean forceRefresh) {
    	EventBus eventBus = EventBus.getDefault();
    	eventBus.post(new PlayerModifiedEvent(player, forceRefresh));
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
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

            if (player != null) {
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
