package com.carbogen.korra.ferrokinesis;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.SubElement;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityModule;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;

/**
 * Created by Carbogen on 18/01/16.
 */
public class Info extends MultiAbilityModule
{
	static boolean debug = false;
	static String name = "FerroKinesis";
	static String version = "v1.0.0";
	static String author = "Carbogen";
	static String permission = "bending.ability.FerroKinesis";
	static String config = "ExtraAbilities.Carbogen.FerroKinesis.";
	public static int maxblocks;
	public static int maxrange;
	static String description = "FerroKinesis is a term derived from the Latin word Ferrum (iron) and " +
			"the Greek Kinema (movement). This ability allows a skilled metalbending to levitate a metallic " +
			"block for manipulation. The block can be pushed or pulled by the player using the hotbar slots. " +
			"The block is picked up and dropped by left-clicking. If dropped in mid-air, the block will fall. " +
			"Tapping shift while in control of a block will cause the block to stay airborn, allowing you to maneuver " +
					((maxblocks == 1)? "an additional block." : "up to "+(maxblocks-1)+" additional blocks.");

	public Info()
	{
		super(name);
	}

	public static void debug(Object message)
	{
		if(debug)
			ProjectKorra.plugin.getLogger().info(message.toString());
	}

	@Override
	public void onThisLoad()
	{
//		ProjectKorra.plugin.getConfig().addDefault(config + "MaxSourceRange", 8);
		ProjectKorra.plugin.getConfig().addDefault(config + "maximum_blocks", 3);
		ProjectKorra.plugin.getConfig().addDefault(config + "maximum_range", 8);
//		ProjectKorra.plugin.getConfig().addDefault(config + "WarmUpTime", 4000);
		ProjectKorra.plugin.saveConfig();
		ProjectKorra.plugin.getServer().getPluginManager().addPermission(new Permission(permission, PermissionDefault.TRUE));
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new Trigger(), ProjectKorra.plugin);

		maxblocks = ProjectKorra.plugin.getConfig().getInt(config+"maximum_blocks");
		maxrange = ProjectKorra.plugin.getConfig().getInt(config+"maximum_range");

		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new Runnable()
		{
			public void run()
			{
				Instance.progressAll();
			}
		}, 0, 1);
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	@Override
	public String getElement()
	{
		return Element.Earth.toString();
	}

	@Override
	public SubElement getSubElement()
	{
		return SubElement.Metalbending;
	}

	@Override
	public ArrayList<MultiAbilityManager.MultiAbilitySub> getAbilities()
	{
		ArrayList<MultiAbilityManager.MultiAbilitySub> array = new ArrayList<>();

		for(int i = 0; i < 9; i++)
		{
			array.add(new MultiAbilityManager.MultiAbilitySub(getName() + " Range " + (i+1), Element.Earth, getSubElement()));
		}

		return array;
	}

	@Override
	public String getAuthor()
	{
		return author;
	}

	@Override
	public String getDescription()
	{
		return getName() + " " + getVersion() + " from " + getAuthor() + "\n" +
				description;
	}

	@Override
	public boolean isShiftAbility()
	{
		return true;
	}

	@Override
	public boolean isHarmlessAbility()
	{
		return true;
	}
}
