package com.carbogen.korra.ferrokinesis;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

/**
 * Created by Carbogen on 18/01/16.
 */
public class Trigger implements Listener
{
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e)
	{
		if(e.isSneaking())
		{
            String abil = GeneralMethods.getBoundAbility(e.getPlayer());

            if(abil == null)
                return;

            new Instance(e.getPlayer(), "Sneak");
		}
	}

//	@EventHandler
//	public void onDeath(EntityDeathEvent e)
//	{
//		for(Player p : Instance.instances.keySet())
//			if(e.getEntity().getEntityId() == Instance.instances.get(p).controlled_fb.getEntityId())
//			{
//				Instance.instances.get(p).controlled_fb = GeneralMethods.spawnFallingBlock(e.getEntity().getLocation().add(new Vector(0, 0.5, 0)), Instance.instances.get(p).controlled_fb.getMaterial());
//			}
//	}

	@EventHandler
	public void onClick(PlayerInteractEvent e)
	{
		if(e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_AIR))
		{
			String abil = GeneralMethods.getBoundAbility(e.getPlayer());

			if(abil == null)
				return;

			new Instance(e.getPlayer(), "Click");
		}
	}

//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void onDrop(EntityChangeBlockEvent e)
//	{
//		for(Player p : Instance.instances.keySet())
//			if(e.getEntity().getEntityId() == Instance.instances.get(p).controlled_fb.getEntityId())
//			{
//				e.setCancelled(true);
//			}
//	}



}
