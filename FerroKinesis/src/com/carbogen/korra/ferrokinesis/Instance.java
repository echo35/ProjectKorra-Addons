package com.carbogen.korra.ferrokinesis;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.material.Door;
import org.bukkit.material.Lever;
import org.bukkit.material.Openable;
import org.bukkit.material.TrapDoor;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Carbogen on 18/01/16.
 */
public class Instance
{
	public static ConcurrentHashMap<Player, Instance> instances = new ConcurrentHashMap<>();
	public static List<Material> metallicblocks = Arrays.asList(new Material[] {Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.QUARTZ_BLOCK});

	public Instance(Player player, String trigger)
	{
		if(instances.containsKey(player))
		{
			instances.get(player).drop();
			return;
		}

		if(!isEligible(player))
			return;

		this.player = player;
        this.trigger = trigger;


		Info.debug("New FerroKinesis from " + player.getName());

        if (trigger == "Click")
    		setupClick();
        else if (trigger == "Sneak")
            setupSneak();
	}

	public static boolean isEligible(Player player)
	{
		BendingPlayer bender = GeneralMethods.getBendingPlayer(player.getName());

		if(!bender.hasElement(Element.Earth))
			return false;

		if(!EarthMethods.canMetalbend(player))
			return false;

		if(bender.isOnCooldown(Info.name))
			return false;

		if(!GeneralMethods.getBoundAbility(player).equalsIgnoreCase(Info.name))
			return false;

		return true;
	}

	public static void progressAll()
	{
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new Runnable()
		{
			public void run()
			{
				for(Player p : instances.keySet())
				{
					instances.get(p).progress();
				}
			}
		}, 0, 1);
	}

	private Player player;
//	private List<FallingBlock> fallingBlocks = new ArrayList<>();
//	private int controlled_fb = 0;
	public FallingBlock controlled_fb;
//	private List<Block> blocks = new ArrayList<>();
	private int slot = -1;
	private int moveslot;
	private double range = 0;
    private String trigger = "None";

	public void setupClick() {
		Block metalblock = GeneralMethods.getTargetedLocation(player, Info.maxrange).add(player.getEyeLocation().getDirection()).getBlock();

		//Block metalblock = EarthMethods.getEarthSourceBlock(player, Info.maxrange, false, false, true);
		if (metalblock == null)
			return;

		if (metallicblocks.contains(metalblock.getType())) {
			range = metalblock.getLocation().distance(player.getLocation());


			moveslot = player.getInventory().getHeldItemSlot();

			//		blocks.add(metalblock);
			Material type = metalblock.getType();
			metalblock.setType(Material.AIR);

			//		slot = fallingBlocks.size()-1;

			MultiAbilityManager.bindMultiAbility(player, Info.name);
			controlled_fb = GeneralMethods.spawnFallingBlock(metalblock.getLocation().add(0, 0.6, 0), type);

			slot = (int) Math.round(range * ((double) Info.maxrange / 9.0) - 1);
			//		player.sendMessage("Slot " + slot);
			player.getInventory().setHeldItemSlot(slot);

			GeneralMethods.getBendingPlayer(player.getName()).addCooldown(Info.name, 100);
			instances.put(player, this);
		}
	}

    public void setupSneak()
    {
		moveslot = player.getInventory().getHeldItemSlot();
		player.getInventory().setHeldItemSlot(4);

		instances.put(player, this);
	}

	public void drop()
	{
		Location target_loc = GeneralMethods.getTargetedLocation(player, range+1);
		if((controlled_fb.getLocation().getBlock().getType() != Material.AIR &&
				controlled_fb.getLocation().getBlock().getType() != Material.WATER &&
				controlled_fb.getLocation().getBlock().getType() != Material.STATIONARY_WATER) ||
				(target_loc.getBlock().getType() != Material.AIR &&
				target_loc.getBlock().getType() != Material.WATER &&
				target_loc.getBlock().getType() != Material.STATIONARY_WATER
				))
			return;

		if(target_loc.distance(controlled_fb.getLocation()) >= 1)
			if(EarthMethods.isTransparentToEarthbending(player, target_loc.getBlock()))
				target_loc.getBlock().setType(controlled_fb.getMaterial());
			else controlled_fb.getLocation().getBlock().setType(controlled_fb.getMaterial());
		else controlled_fb.getLocation().getBlock().setType(controlled_fb.getMaterial());
		controlled_fb.remove();
		remove();
	}

	public void progress()
	{
		slot = player.getInventory().getHeldItemSlot();
		range = (slot+1)*((double) Info.maxrange/9.0);

		if (this.trigger == "Click") {
			if (controlled_fb.isDead())
				remove();
			//player.sendMessage("Slot " + slot + ", Range: " + range);

			Vector v = GeneralMethods.getDirection(
					controlled_fb.getLocation(),
					player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(range))
//					GeneralMethods.getTargetedLocation(player, range)
			).multiply(0.6);

			if (controlled_fb.getLocation().getY() - controlled_fb.getLocation().getBlockY() > 0.5)
				v = v.add(new Vector(0, 0.5, 0));

			controlled_fb.setVelocity(v);
		}

		else if (this.trigger == "Sneak") {
			if (!player.isSneaking())
				remove();
			Location location = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(range));
			List<EntityType> metallicEntities = Arrays.asList(
					new EntityType[] {
						EntityType.MINECART, EntityType.MINECART_CHEST, EntityType.MINECART_COMMAND, EntityType.MINECART_CHEST,
						EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER, EntityType.MINECART_MOB_SPAWNER, EntityType.MINECART_TNT
					}
			);

			List<Material> metallicMaterials = Arrays.asList(
					new Material[] {
						Material.IRON_AXE, Material.IRON_BARDING, Material.IRON_BLOCK, Material.IRON_BOOTS, Material.IRON_CHESTPLATE, Material.IRON_DOOR,
						Material.IRON_FENCE, Material.IRON_HELMET, Material.IRON_HELMET, Material.IRON_HOE, Material.IRON_INGOT, Material.IRON_LEGGINGS,
						Material.IRON_PICKAXE, Material.IRON_PLATE, Material.IRON_SPADE, Material.IRON_SWORD, Material.IRON_TRAPDOOR
					}
			);

			for (Entity entity : player.getWorld().getEntities()) {
				if (entity.getLocation().distance(location) <= 6) {
					if (metallicEntities.contains(entity.getType()) ||
							(entity.getType() == EntityType.DROPPED_ITEM && metallicMaterials.contains(((Item) entity).getItemStack().getType()))
					) {
						if (entity.getType() == EntityType.DROPPED_ITEM && metallicMaterials.contains(((Item) entity).getItemStack().getType())) {
							((Item) entity).setPickupDelay(5);
						}
						Vector v = GeneralMethods.getDirection(entity.getLocation(), location).multiply(0.6);
						entity.setVelocity(v);
					}
				}
			}
		}
	}

	public void remove()
	{
		instances.remove(player);
		MultiAbilityManager.unbindMultiAbility(player);
		player.getInventory().setHeldItemSlot(moveslot);
		GeneralMethods.getBendingPlayer(player.getName()).addCooldown(Info.name, 100);
	}
}
