package ch.carbogen.korra.lavasurge;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Created by Carbogen on 10/17/16.
 */
public class LavaSurge extends LavaAbility implements AddonAbility {
	private int prepareRange;
	private int damage;
	private int waves;
	private long cooldown;
	public static int surgeInterval = 300;
	public static int mudPoolRadius = 2;
	public static long mudCreationInterval = 100L;
	public static Material[] mudTypes;
	private Block source;
	private TempBlock sourceTB;
	private int wavesOnTheRun = 0;
	private int fireTicks = 0;
	private long lastSurgeTime = 0L;
	private boolean mudFormed = false;
	private boolean doNotSurge = false;
	public boolean started = false;
	private List<Block> mudArea = new ArrayList();
	private ListIterator<Block> mudAreaItr;
	private List<TempBlock> mudBlocks = new ArrayList();
	private List<Player> blind = new ArrayList();
	Random rand = new Random();

	static {
		mudTypes = new Material[]{Material.SAND, Material.CLAY, Material.STAINED_CLAY, Material.GRASS, Material.DIRT, Material.MYCEL, Material.SOUL_SAND, Material.RED_SANDSTONE, Material.SANDSTONE};
	}

	public LavaSurge(Player player) {
		super(player);
		if(this.bPlayer.canBend(this)) {
			if(hasAbility(player, LavaSurge.class)) {
				LavaSurge ms = (LavaSurge)getAbility(player, LavaSurge.class);
				if(ms.hasStarted()) {
					return;
				}

				ms.remove();
			}

			this.setFields();
			if(this.getSource()) {
				this.start();
				this.bPlayer.addCooldown(this);
			}

		}
	}

	public void setFields() {
		this.prepareRange = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.Earth.LavaSurge.SourceRange");
		this.damage = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.Earth.LavaSurge.Damage");
		this.waves = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.Earth.LavaSurge.Waves");
		this.cooldown = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.Carbogen.Earth.LavaSurge.Cooldown");
		this.fireTicks = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.Earth.LavaSurge.FireTicks");
	}

	private boolean getSource() {
		Block block = this.getMudSourceBlock(this.prepareRange);
		if(block != null && this.isEarthbendable(block)) {
			this.source = block;
			this.sourceTB = new TempBlock(this.source, Material.MAGMA, (byte) 0);
			return true;
		}

		return false;
	}

	private void startSurge() {
		for (Block b : GeneralMethods.getBlocksAroundPoint(source.getLocation(), 1)) {
			if (b.getLocation().getBlockY() == source.getLocation().getBlockY())
				mudBlocks.add(new TempBlock(b, Material.STATIONARY_LAVA, (byte) 0));
		}
		this.started = true;
	}

	private boolean hasStarted() {
		return this.started;
	}

	public static void mudSurge(Player player) {
		if(hasAbility(player, LavaSurge.class)) {
			(getAbility(player, LavaSurge.class)).startSurge();
		}
	}

	private Block getMudSourceBlock(int range) {
		Block testBlock = GeneralMethods.getTargetedLocation(this.player, (double)range, getTransparentMaterial()).getBlock();
		if(this.isEarthbendable(testBlock)) {
			return testBlock;
		} else {
			Location loc = this.player.getEyeLocation();
			Vector dir = this.player.getEyeLocation().getDirection().clone().normalize();

			for(int i = 0; i <= range; ++i) {
				Block block = loc.clone().add(dir.clone().multiply(i == 0?1:i)).getBlock();
				if(!GeneralMethods.isRegionProtectedFromBuild(this.player, "LavaSurge", block.getLocation()) && this.isEarthbendable(block)) {
					return block;
				}
			}

			return null;
		}
	}

	private void surge() {
		if(this.wavesOnTheRun >= this.waves) {
			this.doNotSurge = true;
		} else if(!this.doNotSurge) {
			Iterator var2 = this.mudBlocks.iterator();

			while(var2.hasNext()) {
				TempBlock tb = (TempBlock)var2.next();
				Vector direction = GeneralMethods.getDirection(tb.getLocation().add(0.0D, 1.0D, 0.0D), GeneralMethods.getTargetedLocation(this.player, 30)).multiply(0.07D);
				double x = this.rand.nextDouble() / 5.0D;
				double z = this.rand.nextDouble() / 5.0D;
				x = this.rand.nextBoolean()?-x:x;
				z = this.rand.nextBoolean()?-z:z;
				new TempFallingBlock(tb.getLocation().add(0.0D, 1.0D, 0.0D), Material.MAGMA, (byte) 0, direction.clone().add(new Vector(x, 0.2D, z)), this);
				playEarthbendingSound(tb.getLocation());
			}

			++this.wavesOnTheRun;
		}
	}

	private void affect() {
		Iterator var2 = TempFallingBlock.getFromAbility(this).iterator();

		while(true) {
			label45:
			while(var2.hasNext()) {
				TempFallingBlock tfb = (TempFallingBlock)var2.next();
				FallingBlock fb = tfb.getFallingBlock();
				if(fb.isDead()) {
					tfb.remove();
				} else {
					Iterator var5 = GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 1.5D).iterator();

					while(true) {
						while(true) {
							if(!var5.hasNext()) {
								continue label45;
							}

							Entity e = (Entity)var5.next();
							if(fb.isDead()) {
								tfb.remove();
							} else if(e instanceof LivingEntity) {
								e.setFireTicks(this.fireTicks);
								((LivingEntity) e).setNoDamageTicks(0);
								DamageHandler.damageEntity(e, (double)this.damage, this);
								fb.setVelocity(fb.getVelocity().multiply(0.5D));
								e.setVelocity(fb.getVelocity());
								tfb.remove();
							}
						}
					}
				}
			}

			return;
		}
	}

	public void progress() {
		if(this.player.isOnline() && !this.player.isDead()) {
			if(!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
				this.remove();
			} else if(this.started && System.currentTimeMillis() > this.lastSurgeTime + (long)surgeInterval) {
				this.surge();
				this.affect();
				if(TempFallingBlock.getFromAbility(this).isEmpty()) {
					this.remove();
				}
			}
		} else {
			this.remove();
		}
	}

	public void remove() {
		this.sourceTB.revertBlock();
		for (TempBlock tb : mudBlocks)
			tb.revertBlock();
		super.remove();
	}

	public long getCooldown() {
		return this.cooldown;
	}

	public Location getLocation() {
		return null;
	}

	public String getName() {
		return "LavaSurge";
	}

	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isSneakAbility() {
		return true;
	}

	public String getAuthor() {
		return "Carbogen and jedk1";
	}

	public String getVersion() {
		return "3.0";
	}

	public String getDescription() {
		return getName() + " version " + getVersion() + " created by " + getAuthor() + "\n"
				+ "This ability lets an earthbender send a surge of lava in any direction, knocking back enemies and inflicting heavy damage and burns. To use, select a source of earth and click in any direction.";
	}

	public void load() {
		FileConfiguration config = ProjectKorra.plugin.getConfig();
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSurge.Cooldown", Integer.valueOf(6000));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSurge.Damage", Double.valueOf(5.0));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSurge.Waves", Integer.valueOf(5));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSurge.SourceRange", Integer.valueOf(7));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSurge.FireTicks", Integer.valueOf(100));
		config.options().copyDefaults(true);
		ProjectKorra.plugin.saveConfig();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new Trigger(), ProjectKorra.plugin);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, TempFallingBlock::manage, 1, 1);
		ProjectKorra.plugin.getLogger().info(String.format("%s %s, developed by %s, has been loaded.", getName(), getVersion(), getAuthor()));
	}

	public void stop() {
	}
}

