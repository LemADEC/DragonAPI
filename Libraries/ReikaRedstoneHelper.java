/*******************************************************************************
 * @author Reika
 * 
 * This code is the property of and owned and copyrighted by Reika.
 * Unless given explicit written permission - electronic writing is acceptable - no user may
 * copy, edit, or redistribute this source code nor any derivative works.
 * Failure to comply with these restrictions is a violation of
 * copyright law and will be dealt with accordingly.
 ******************************************************************************/
package Reika.DragonAPI.Libraries;

import net.minecraft.world.World;
import Reika.DragonAPI.DragonAPICore;

public final class ReikaRedstoneHelper extends DragonAPICore {

	/** Returns true on the postive redstone edge. Args: World, x, y, z, last power state*/
	public static boolean isPositiveEdge(World world, int x, int y, int z, boolean lastPower) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z) == lastPower)
			return false;
		if (!world.isBlockIndirectlyGettingPowered(x, y, z))
			return false;
		return true;
	}

	/** Returns true on the negative redstone edge. Args: World, x, y, z, last power state*/
	public static boolean isNegativeEdge(World world, int x, int y, int z, boolean lastPower) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z) == lastPower)
			return false;
		if (world.isBlockIndirectlyGettingPowered(x, y, z))
			return false;
		return true;
	}

	/** Returns true on a redstone edge. Args: World, x, y, z, last power state */
	public static boolean isEdge(World world, int x, int y, int z, boolean lastPower) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z) == lastPower)
			return false;
		return true;
	}

	/** Returns true if the redstone signal is greater or equal to a value. Args: World, x, y, z, level */
	public static boolean isRedstoneAtLevel(World world, int x, int y, int z, int level) {
		if (level > 15) {
			ReikaJavaLibrary.pConsole("Redstone level "+level+" is impossible!");
			return false;
		}
		int pwr = world.getBlockPowerInput(x, y, z);
		return (pwr >= level);
	}

	/** Returns true if the redstone signal is either zero or full (15). Args: World, x, y, z */
	public static boolean isBinaryValue(World world, int x, int y, int z) {
		int pwr = world.getBlockPowerInput(x, y, z);
		return (pwr == 0 || pwr == 15);
	}

}
