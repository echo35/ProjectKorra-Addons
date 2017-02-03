package ch.carbogen.korra.lavasurge;

import com.projectkorra.projectkorra.ability.Ability;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

/**
 * Created by Carbogen on 10/17/16.
 */
public class TempFallingBlock {
	public static ConcurrentHashMap<FallingBlock, TempFallingBlock> instances = new ConcurrentHashMap();
	private FallingBlock fallingblock;
	private Ability ability;
	private long creation;
	private boolean expire;

	public TempFallingBlock(Location location, Material material, byte data, Vector veloctiy, Ability ability) {
		this(location, material, data, veloctiy, ability, false);
	}

	public TempFallingBlock(Location location, Material material, byte data, Vector veloctiy, Ability ability, boolean expire) {
		this.fallingblock = location.getWorld().spawnFallingBlock(location, material, data);
		this.fallingblock.setVelocity(veloctiy);
		this.fallingblock.setDropItem(false);
		this.ability = ability;
		this.creation = System.currentTimeMillis();
		this.expire = expire;
		instances.put(this.fallingblock, this);
	}

	public static void manage() {
		Iterator var1 = instances.values().iterator();

		while(var1.hasNext()) {
			TempFallingBlock tfb = (TempFallingBlock)var1.next();
			if(tfb.canExpire() && System.currentTimeMillis() > tfb.getCreationTime() + 5000L) {
				tfb.remove();
			}
		}

	}

	public static TempFallingBlock get(FallingBlock fallingblock) {
		return isTempFallingBlock(fallingblock)?(TempFallingBlock)instances.get(fallingblock):null;
	}

	public static boolean isTempFallingBlock(FallingBlock fallingblock) {
		return instances.containsKey(fallingblock);
	}

	public static void removeFallingBlock(FallingBlock fallingblock) {
		if(isTempFallingBlock(fallingblock)) {
			fallingblock.remove();
			instances.remove(fallingblock);
		}

	}

	public static void removeAllFallingBlocks() {
		Iterator var1 = instances.keySet().iterator();

		while(var1.hasNext()) {
			FallingBlock fallingblock = (FallingBlock)var1.next();
			fallingblock.remove();
			instances.remove(fallingblock);
		}

	}

	public static List<TempFallingBlock> getFromAbility(Ability ability) {
		ArrayList tfbs = new ArrayList();
		Iterator var3 = instances.values().iterator();

		while(var3.hasNext()) {
			TempFallingBlock tfb = (TempFallingBlock)var3.next();
			if(tfb.getAbility().equals(ability)) {
				tfbs.add(tfb);
			}
		}

		return tfbs;
	}

	public void remove() {
		this.fallingblock.remove();
		instances.remove(this.fallingblock);
	}

	public FallingBlock getFallingBlock() {
		return this.fallingblock;
	}

	public Ability getAbility() {
		return this.ability;
	}

	public Material getMaterial() {
		return this.fallingblock.getMaterial();
	}

	public byte getData() {
		return this.fallingblock.getBlockData();
	}

	public Location getLocation() {
		return this.fallingblock.getLocation();
	}

	public long getCreationTime() {
		return this.creation;
	}

	public boolean canExpire() {
		return this.expire;
	}
}

