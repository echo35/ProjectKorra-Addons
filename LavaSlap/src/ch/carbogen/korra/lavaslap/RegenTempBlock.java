package ch.carbogen.korra.lavaslap;


import com.projectkorra.projectkorra.util.TempBlock;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Created by Carbogen on 10/17/16.
 */
public class RegenTempBlock {
	public static ConcurrentHashMap<Block, Long> blocks = new ConcurrentHashMap();
	public static ConcurrentHashMap<Block, TempBlock> temps = new ConcurrentHashMap();
	public static ConcurrentHashMap<Block, BlockState> states = new ConcurrentHashMap();

	public RegenTempBlock(Block block, Material material, byte data, long delay) {
		this(block, material, data, delay, true);
	}

	public RegenTempBlock(Block block, Material material, byte data, long delay, boolean temp) {
		if (blocks.containsKey(block)) {
			blocks.replace(block, Long.valueOf(System.currentTimeMillis() + delay));
			block.setType(material);
			block.setData(data);
		} else {
			blocks.put(block, Long.valueOf(System.currentTimeMillis() + delay));
			if (TempBlock.isTempBlock(block)) {
				TempBlock.get(block).revertBlock();
			}

			if (temp) {
				TempBlock tb = new TempBlock(block, material, data);
				temps.put(block, tb);
			} else {
				states.put(block, block.getState());
				if (material != null) {
					block.setType(material);
					block.setData(data);
				}
			}
		}

	}

	public static void manage() {
		Iterator var1 = blocks.keySet().iterator();

		while (var1.hasNext()) {
			Block b = (Block) var1.next();
			if (System.currentTimeMillis() >= ((Long) blocks.get(b)).longValue()) {
				if (temps.containsKey(b)) {
					TempBlock bs = (TempBlock) temps.get(b);
					bs.revertBlock();
					temps.remove(b);
				}

				if (states.containsKey(b)) {
					BlockState bs1 = (BlockState) states.get(b);
					bs1.update(true);
					states.remove(b);
				}

				blocks.remove(b);
			}
		}

	}

	public static void revert(Block block) {
		if (blocks.containsKey(block)) {
			if (TempBlock.isTempBlock(block) && temps.containsKey(block)) {
				TempBlock tb = TempBlock.get(block);
				tb.revertBlock();
				temps.remove(block);
			}

			if (states.containsKey(block)) {
				((BlockState) states.get(block)).update(true);
				states.remove(block);
			}

			blocks.remove(block);
		}

	}

	public static void revertAll() {
		Iterator var1 = blocks.keySet().iterator();

		while (var1.hasNext()) {
			Block b = (Block) var1.next();
			if (temps.containsKey(b)) {
				TempBlock tb = (TempBlock) temps.get(b);
				tb.revertBlock();
			}

			if (states.containsKey(b)) {
				((BlockState) states.get(b)).update(true);
			}
		}

		temps.clear();
		states.clear();
		blocks.clear();
	}

	public static boolean hasBlock(Block block) {
		return blocks.containsKey(block);
	}

	public static boolean isTempBlock(Block block) {
		return temps.containsKey(block);
	}

	public static boolean isBlockState(Block block) {
		return states.containsKey(block);
	}
}