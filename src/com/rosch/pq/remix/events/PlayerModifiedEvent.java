package com.rosch.pq.remix.events;

import com.github.pkunk.pq.gameplay.Player;

public class PlayerModifiedEvent
{
	public final Player player;
	public final boolean forceRefresh;
	
	public PlayerModifiedEvent(Player player, boolean forceRefresh)
	{
		this.player = player;
		this.forceRefresh = forceRefresh;
	}
}
