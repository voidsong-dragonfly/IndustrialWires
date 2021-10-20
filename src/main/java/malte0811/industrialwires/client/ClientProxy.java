/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2018 malte0811
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */
package malte0811.industrialwires.client;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.collect.ImmutableList;
import malte0811.industrialwires.CommonProxy;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.converter.TileEntityMechMB;
import malte0811.industrialwires.client.multiblock_io_model.MBIOModelLoader;
import malte0811.industrialwires.client.render.EntityRenderBrokenPart;
import malte0811.industrialwires.client.render.TileRenderMechMB;
import malte0811.industrialwires.entities.EntityBrokenPart;
import malte0811.industrialwires.mech_mb.MechEnergy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import static malte0811.industrialwires.IndustrialWires.TURN_FAST;
import static malte0811.industrialwires.IndustrialWires.TURN_SLOW;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		OBJLoader.INSTANCE.addDomain(IndustrialWires.MODID);
		ModelLoaderRegistry.registerLoader(new MBIOModelLoader());
		TileRenderMechMB tesr = new TileRenderMechMB();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechMB.class, tesr);
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(tesr);
		RenderingRegistry.registerEntityRenderingHandler(EntityBrokenPart.class, EntityRenderBrokenPart::new);
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

	private Map<BlockPos, List<ISound>> playingSounds = new HashMap<>();


	@Override
	public void updateMechMBTurningSound(TileEntityMechMB te, MechEnergy energy) {
		SoundHandler sndHandler = ClientUtils.mc().getSoundHandler();
		List<ISound> soundsAtPos;
		if (playingSounds.containsKey(te.getPos())) {
			soundsAtPos = playingSounds.get(te.getPos());
			soundsAtPos.removeIf(s -> !sndHandler.isSoundPlaying(s));
			if (soundsAtPos.isEmpty()) {
				playingSounds.remove(te.getPos());
			}
		} else {
			soundsAtPos = ImmutableList.of();
		}
		boolean hasSlow = false, hasFast = false;
		for (ISound s:soundsAtPos) {
			if (s.getSoundLocation().equals(TURN_FAST.getSoundName())) {
				hasFast = true;
			} else if (s.getSoundLocation().equals(TURN_SLOW.getSoundName())) {
				hasSlow = true;
			}
		}
		if (!hasSlow && energy.getVolumeSlow() > 0) {
			ISound snd = new IWTickableSound(TURN_SLOW, SoundCategory.BLOCKS, energy::getVolumeSlow, energy::getPitch,
					te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
			sndHandler.playSound(snd);
			addSound(te.getPos(), snd);
		}
		if (!hasFast && energy.getVolumeFast() > 0) {
			ISound snd = new IWTickableSound(TURN_FAST, SoundCategory.BLOCKS, energy::getVolumeFast, energy::getPitch,
					te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
			sndHandler.playSound(snd);
			addSound(te.getPos(), snd);
		}
	}

	private void addSound(BlockPos pos, ISound sound) {
		List<ISound> allForPos = playingSounds.get(pos);
		if (allForPos==null) {
			allForPos = new ArrayList<>();
		}
		allForPos.add(sound);
		if (allForPos.size()==1) {
			playingSounds.put(pos, allForPos);
		}
	}

	@Override
	public void stopAllSoundsExcept(BlockPos pos, Set<?> excluded) {
		if (playingSounds.containsKey(pos)) {
			SoundHandler manager = Minecraft.getMinecraft().getSoundHandler();
			List<ISound> sounds = playingSounds.get(pos);
			List<ISound> toRemove = new ArrayList<>(sounds.size()-excluded.size());
			for (ISound sound:sounds) {
				if (!excluded.contains(sound)) {
					manager.stopSound(sound);
					toRemove.add(sound);
				}
			}
			sounds.removeAll(toRemove);
			if (sounds.isEmpty()) {
				playingSounds.remove(pos);
			}
		}
	}

	@Override
	public boolean isSingleplayer() {
		return Minecraft.getMinecraft().isSingleplayer();
	}

	@Override
	public boolean isValidTextureSource(ItemStack stack) {
		if (!super.isValidTextureSource(stack)) {
			return false;
		}
		IBakedModel texModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack,
				null, null);
		TextureAtlasSprite sprite = texModel.getParticleTexture();
		//noinspection ConstantConditions
		if (sprite == null || sprite.hasAnimationMetadata() || sprite.getFrameTextureData(0) == null) {
			return false;
		}
		int[][] data = sprite.getFrameTextureData(0);
		for (int[] datum : data) {
			if (datum != null ) {
				for (int i : datum) {
					if ((i >>> 24) != 255) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
