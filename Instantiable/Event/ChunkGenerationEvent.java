/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable.Event;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;


/** Calls like setBlock are SAFE to do! */
public class ChunkGenerationEvent extends ChunkEvent {

	public final IChunkProvider generator;

	public ChunkGenerationEvent(Chunk c) {
		super(c);
		generator = ((WorldServer)c.worldObj).theChunkProviderServer.currentChunkProvider;
	}

	public static void fire(Chunk c) {
		MinecraftForge.EVENT_BUS.post(new ChunkGenerationEvent(c));
	}

}