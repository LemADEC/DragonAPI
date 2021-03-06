/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaVectorHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.ExtraUtilsHandler;
import Reika.DragonAPI.ModInteract.ItemHandlers.TinkerBlockHandler;
import Reika.RotaryCraft.Registry.BlockRegistry;

public final class RayTracer {

	private double originX;
	private double originY;
	private double originZ;
	private double targetX;
	private double targetY;
	private double targetZ;

	public boolean softBlocksOnly = false;
	public boolean allowFluids = true;
	public boolean uniDirectionalChecks = false;

	private final ArrayList<BlockKey> forbiddenBlocks = new ArrayList();
	private final ArrayList<BlockKey> allowedBlocks = new ArrayList();

	public RayTracer(double x1, double y1, double z1, double x2, double y2, double z2) {
		originX = x1;
		originY = y1;
		originZ = z1;
		targetX = x2;
		targetY = y2;
		targetZ = z2;
	}

	public void setOrigins(double x1, double y1, double z1, double x2, double y2, double z2) {
		originX = x1;
		originY = y1;
		originZ = z1;
		targetX = x2;
		targetY = y2;
		targetZ = z2;
	}

	public void offset(double dx, double dy, double dz) {
		this.offset(dx, dy, dz, dx, dy, dz);
	}

	public void offset(double dx1, double dy1, double dz1, double dx2, double dy2, double dz2) {
		originX += dx1;
		originY += dy1;
		originZ += dz1;
		targetX += dx2;
		targetY += dy2;
		targetZ += dz2;
	}

	public void addOpaqueBlock(Block b) {
		this.addOpaqueBlock(b, -1);
	}

	public void addOpaqueBlock(Block b, int meta) {
		forbiddenBlocks.add(new BlockKey(b, meta));
	}

	public void addTransparentBlock(Block b) {
		this.addTransparentBlock(b, -1);
	}

	public void addTransparentBlock(Block b, int meta) {
		allowedBlocks.add(new BlockKey(b, meta));
	}

	public boolean isClearLineOfSight(World world) {
		Vec3 vec1 = Vec3.createVectorHelper(originX, originY, originZ);
		Vec3 vec2 = Vec3.createVectorHelper(targetX, targetY, targetZ);
		if (uniDirectionalChecks && new DecimalPosition(vec1).hashCode() < new DecimalPosition(vec2).hashCode()) {
			Vec3 vec = vec1;
			vec1 = vec2;
			vec2 = vec;
		}
		Vec3 ray = ReikaVectorHelper.subtract(vec1, vec2);
		double dx = vec2.xCoord-vec1.xCoord;
		double dy = vec2.yCoord-vec1.yCoord;
		double dz = vec2.zCoord-vec1.zCoord;
		double dd = ReikaMathLibrary.py3d(dx, dy, dz);
		for (double d = 0.25; d <= dd; d += 0.25) {
			Vec3 vec0 = ReikaVectorHelper.scaleVector(ray, d);
			Vec3 vec = ReikaVectorHelper.scaleVector(ray, d-0.25);
			vec0.xCoord += vec1.xCoord;
			vec0.yCoord += vec1.yCoord;
			vec0.zCoord += vec1.zCoord;
			vec.xCoord += vec1.xCoord;
			vec.yCoord += vec1.yCoord;
			vec.zCoord += vec1.zCoord;
			MovingObjectPosition mov = world.rayTraceBlocks(vec, vec0);
			if (mov != null) {
				if (mov.typeOfHit == MovingObjectType.BLOCK) {
					int bx = mov.blockX;
					int by = mov.blockY;
					int bz = mov.blockZ;
					if (this.isNonTerminal(bx, by, bz)) {
						if (this.isDisallowedBlock(world, bx, by, bz)) {
							//ReikaJavaLibrary.pConsole(mov+":"+world.getBlock(bx, by, bz), Side.SERVER);
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean isNonTerminal(int x, int y, int z) {
		if (x == MathHelper.floor_double(originX) && y == MathHelper.floor_double(originY) && z == MathHelper.floor_double(originZ))
			return false;
		if (x == MathHelper.floor_double(targetX) && y == MathHelper.floor_double(targetY) && z == MathHelper.floor_double(targetZ))
			return false;
		return true;
	}

	private boolean isDisallowedBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		BlockKey key = new BlockKey(b, meta);
		if (allowedBlocks.contains(key))
			return false;
		if (forbiddenBlocks.contains(key))
			return true;
		if (!allowFluids && ReikaBlockHelper.isLiquid(b))
			return true;
		return !ReikaWorldHelper.softBlocks(world, x, y, z) || (softBlocksOnly && ReikaBlockHelper.isCollideable(world, x, y, z));
	}

	public boolean isBlockPassable(World world, int x, int y, int z) {
		return !this.isDisallowedBlock(world, x, y, z);
	}

	public static RayTracer getVisualLOS() {
		RayTracer trace = new RayTracer(0, 0, 0, 0, 0, 0);

		trace.addTransparentBlock(Blocks.glass);
		trace.addTransparentBlock(Blocks.ice);
		trace.addTransparentBlock(Blocks.glass_pane);
		trace.addTransparentBlock(Blocks.iron_bars);
		trace.addTransparentBlock(Blocks.fence);
		trace.addTransparentBlock(Blocks.nether_brick_fence);
		trace.addTransparentBlock(Blocks.mob_spawner);
		trace.addTransparentBlock(Blocks.leaves);
		trace.addTransparentBlock(Blocks.leaves2);
		trace.addTransparentBlock(Blocks.tallgrass);
		trace.allowFluids = true;

		if (ModList.CHROMATICRAFT.isLoaded()) {
			addCCGlass(trace);
		}
		if (ModList.ROTARYCRAFT.isLoaded()) {
			addRCGlass(trace);
		}
		if (ModList.EXTRAUTILS.isLoaded()) {
			if (ExtraUtilsHandler.getInstance().deco2ID != null) {
				trace.addTransparentBlock(ExtraUtilsHandler.getInstance().deco2ID, 1);
				trace.addTransparentBlock(ExtraUtilsHandler.getInstance().deco2ID, 2);
				trace.addTransparentBlock(ExtraUtilsHandler.getInstance().deco2ID, 4);
			}
		}
		if (ModList.TINKERER.isLoaded() && TinkerBlockHandler.getInstance().clearGlassID != null) {
			trace.addTransparentBlock(TinkerBlockHandler.getInstance().clearGlassID);
		}

		return trace;
	}

	@ModDependent(ModList.CHROMATICRAFT)
	private static void addCCGlass(RayTracer trace) {
		trace.addTransparentBlock(ChromaBlocks.GLASS.getBlockInstance());
		trace.addTransparentBlock(ChromaBlocks.SELECTIVEGLASS.getBlockInstance());
		trace.addTransparentBlock(ChromaBlocks.DOOR.getBlockInstance());
		trace.addTransparentBlock(ChromaBlocks.PYLON.getBlockInstance());
	}

	@ModDependent(ModList.ROTARYCRAFT)
	private static void addRCGlass(RayTracer trace) {
		trace.addTransparentBlock(BlockRegistry.BLASTGLASS.getBlockInstance());
		trace.addTransparentBlock(BlockRegistry.BLASTPANE.getBlockInstance());
	}

	public double getLength() {
		return ReikaMathLibrary.py3d(originX-targetX, originY-targetY, originZ-targetZ);
	}

}
