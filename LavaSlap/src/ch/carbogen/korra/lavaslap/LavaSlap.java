package ch.carbogen.korra.lavaslap;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.firebending.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class LavaSlap extends LavaAbility implements AddonAbility {
	private int speed;
	private int range;
	private long cooldown;
	private long duration;
	private long cleanup;
	private double damage;
	private boolean wave;
	private Location location;
	private Vector direction;
	private Vector blockdirection;
	private int step;
	private int counter;
	private long time;
	private boolean complete;
	Random rand = new Random();
	private List<Location> flux = new ArrayList();

	public LavaSlap(Player player) {
		super(player);
		if(this.bPlayer.canBend(this) && this.bPlayer.canLavabend()) {
			this.setFields();
			this.time = System.currentTimeMillis();
			if(this.prepareLine()) {
				this.bPlayer.addCooldown(this);
				this.start();
			}

		}
	}

	public void setFields() {
		this.speed = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.Earth.LavaSlap.Speed");
		if(this.speed < 1) {
			this.speed = 1;
		}

		this.range = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.Earth.LavaSlap.Range");
		this.cooldown = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.Carbogen.Earth.LavaSlap.Cooldown");
		this.duration = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.Carbogen.Earth.LavaSlap.Duration");
		this.cleanup = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.Carbogen.Earth.LavaSlap.Cleanup");
		this.damage = ProjectKorra.plugin.getConfig().getDouble("ExtraAbilities.Carbogen.Earth.LavaSlap.Damage");
		this.wave = ProjectKorra.plugin.getConfig().getBoolean("ExtraAbilities.Carbogen.Earth.LavaSlap.Wave");
	}

	public void progress() {
		if(this.player != null && this.player.isOnline()) {
			if(!this.bPlayer.canBendIgnoreCooldowns(this)) {
				this.remove();
			} else {
				++this.counter;
				if(!this.complete) {
					if(this.speed <= 1 || this.counter % this.speed == 0) {
						for(int location = 0; location <= 2; ++location) {
							++this.step;
							this.progressFlux();
						}
					}
				} else if(System.currentTimeMillis() > this.time + this.duration) {
					Iterator var2 = this.flux.iterator();

					while(var2.hasNext()) {
						Location var3 = (Location)var2.next();
						new RegenTempBlock(var3.getBlock(), Material.STONE, (byte) 0, this.cleanup + (long)this.rand.nextInt(1000));
					}

					this.remove();
					return;
				}

			}
		} else {
			this.remove();
		}
	}

	private boolean prepareLine() {
		this.direction = this.player.getEyeLocation().getDirection().setY(0).normalize();
		this.blockdirection = this.direction.clone().setX((float)Math.round(this.direction.getX()));
		this.blockdirection = this.blockdirection.setZ((float)Math.round(this.direction.getZ()));
		Location origin = this.player.getLocation().add(0.0D, -1.0D, 0.0D).add(this.blockdirection.multiply(2));
		if(!isEarthbendable(this.player, origin.getBlock())) {
			return false;
		} else {
			BlockIterator bi = new BlockIterator(this.player.getWorld(), origin.toVector(), this.direction, 0.0D, this.range);

			while(bi.hasNext()) {
				Block b = bi.next();
				if(b != null && b.getY() > 1 && b.getY() < 255) {
					if(isWater(b)) {
						break;
					}

					while(!isEarthbendable(this.player, b)) {
						b = b.getRelative(BlockFace.DOWN);
						if(b == null || b.getY() < 1 || b.getY() > 255 || isEarthbendable(this.player, b)) {
							break;
						}
					}

					while(!this.isTransparent(b.getRelative(BlockFace.UP))) {
						b = b.getRelative(BlockFace.UP);
						if(b == null || b.getY() < 1 || b.getY() > 255 || isEarthbendable(this.player, b.getRelative(BlockFace.UP))) {
							break;
						}
					}

					if(!isEarthbendable(this.player, b)) {
						break;
					}

					this.flux.add(b.getLocation());
					Block left = b.getRelative(this.getLeftBlockFace(GeneralMethods.getCardinalDirection(this.blockdirection)), 1);
					this.expand(left);
					Block right = b.getRelative(this.getLeftBlockFace(GeneralMethods.getCardinalDirection(this.blockdirection)).getOppositeFace(), 1);
					this.expand(right);
				}
			}

			return true;
		}
	}

	private void progressFlux() {
		Iterator var2 = this.flux.iterator();

		while(var2.hasNext()) {
			Location location = (Location)var2.next();
			if(this.flux.indexOf(location) <= this.step) {
				new RegenTempBlock(location.getBlock(), Material.STATIONARY_LAVA, (byte) 1, this.duration + this.cleanup);
				this.location = location;
				if(this.flux.indexOf(location) == this.step) {
					Block above = location.getBlock().getRelative(BlockFace.UP);
					ParticleEffect.LAVA.display((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.0F, 2, above.getLocation(), 257.0D);
					this.applyDamageFromWave(above.getLocation());
					if(this.wave && this.isTransparent(above)) {
						new RegenTempBlock(location.getBlock().getRelative(BlockFace.UP), Material.STATIONARY_LAVA, (byte) 1, (long)(this.speed * 150));
					}
				}
			}
		}

		if(this.step >= this.flux.size()) {
			this.wave = false;
			this.complete = true;
			this.time = System.currentTimeMillis();
		}

	}

	private void applyDamageFromWave(Location location) {
		Iterator var3 = GeneralMethods.getEntitiesAroundPoint(location, 1.5D).iterator();

		while(var3.hasNext()) {
			Entity entity = (Entity)var3.next();
			if(entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId()) {
				DamageHandler.damageEntity(entity, this.damage, this);
				new FireDamageTimer(entity, this.player);
			}
		}

	}

	private void expand(Block block) {
		if(block != null && block.getY() > 1 && block.getY() < 255) {
			if(isWater(block)) {
				return;
			}

			while(!this.isEarthbendable(block)) {
				block = block.getRelative(BlockFace.DOWN);
				if(block == null || block.getY() < 1 || block.getY() > 255 || this.isEarthbendable(block)) {
					break;
				}
			}

			while(!this.isTransparent(block.getRelative(BlockFace.UP))) {
				block = block.getRelative(BlockFace.UP);
				if(block == null || block.getY() < 1 || block.getY() > 255 || this.isEarthbendable(block.getRelative(BlockFace.UP))) {
					break;
				}
			}

			if(!this.isEarthbendable(block)) {
				return;
			}

			this.flux.add(block.getLocation());
		}

	}

	public BlockFace getLeftBlockFace(BlockFace forward) {
		switch(forward) {
			case NORTH:
				return BlockFace.WEST;
			case EAST:
				return BlockFace.NORTH;
			case SOUTH:
				return BlockFace.EAST;
			case WEST:
				return BlockFace.SOUTH;
			case NORTH_EAST:
				return BlockFace.NORTH_WEST;
			case NORTH_WEST:
				return BlockFace.SOUTH_WEST;
			case SOUTH_EAST:
				return BlockFace.NORTH_EAST;
			case SOUTH_WEST:
				return BlockFace.SOUTH_EAST;
			default:
				return BlockFace.NORTH;
		}
	}

	public long getCooldown() {
		return this.cooldown;
	}

	public Location getLocation() {
		return this.location;
	}

	public String getName() {
		return "LavaSlap";
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
			+ "This offensive ability enables a Lavabender to create a wave of lava, swiftly progressing forward and hurting/burning anything in its way. To use, simply swing your arm towards a target and the ability will activate.";
	}

	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new Trigger(), ProjectKorra.plugin);
		FileConfiguration config = ProjectKorra.plugin.getConfig();
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Range", Integer.valueOf(12));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Cooldown", Integer.valueOf(6000));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Duration", Integer.valueOf(4000));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Cleanup", Integer.valueOf(1000));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Damage", Double.valueOf(6.0D));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Speed", Integer.valueOf(1));
		config.addDefault("ExtraAbilities.Carbogen.Earth.LavaSlap.Wave", Boolean.valueOf(true));
		config.options().copyDefaults(true);
		ProjectKorra.plugin.saveConfig();
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, RegenTempBlock::manage, 1, 1);
		ProjectKorra.plugin.getLogger().info(String.format("%s %s, developed by %s, has been loaded.", getName(), getVersion(), getAuthor()));

	}

	public void stop() {
	}
}