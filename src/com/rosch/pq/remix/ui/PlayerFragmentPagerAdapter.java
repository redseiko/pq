package com.rosch.pq.remix.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PlayerFragmentPagerAdapter extends FragmentPagerAdapter
{
	private String[] pageTitles;
	
	public PlayerFragmentPagerAdapter(FragmentManager fragmentManager, String[] pageTitles)
	{
		super(fragmentManager);
		
		this.pageTitles = pageTitles;
	}

	@Override
	public int getCount()
	{
		return 6;
	}
	
	@Override
	public Fragment getItem(int position)
	{
		switch (position)
		{
			case 0: return new CharacterFragment();
			case 1: return new SpellsFragment();
			case 2: return new EquipmentFragment();
			case 3: return new ItemsFragment();
			case 4: return new PlotFragment();
			case 5: return new QuestsFragment();
		}
		
		return null;
	}
	
	@Override
	public String getPageTitle(int position)
	{
		return pageTitles[position];
	}
}
