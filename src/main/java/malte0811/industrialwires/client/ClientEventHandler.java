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

import com.google.common.collect.ImmutableMap;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.BlockIWBase;
import malte0811.industrialwires.blocks.IMetaEnum;
import malte0811.industrialwires.mech_mb.MechMBPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

import static malte0811.industrialwires.client.render.TileRenderMechMB.BASE_MODELS;

@Mod.EventBusSubscriber(modid = IndustrialWires.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler {
	public static boolean shouldScreenshot = false;

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt) {
		for (BlockIWBase b : IndustrialWires.blocks) {
			Item blockItem = Item.getItemFromBlock(b);
			final ResourceLocation loc = b.getRegistryName();
			assert loc != null;
			ModelLoader.setCustomMeshDefinition(blockItem, stack -> new ModelResourceLocation(loc, "inventory"));
			Object[] v = ((IMetaEnum) b).getValues();
			for (int meta = 0; meta < v.length; meta++) {
				String location = loc.toString();
				String prop = "inventory,type=" + v[meta].toString().toLowerCase(Locale.US);
				try {
					ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
				} catch (NullPointerException npe) {
					throw new RuntimeException(b + " lacks an item!", npe);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void renderWorldLastLow(RenderWorldLastEvent ev) {
		if (shouldScreenshot) {
			Minecraft mc = Minecraft.getMinecraft();
			ITextComponent comp = ScreenShotHelper.saveScreenshot(mc.gameDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());//TODO
			mc.player.sendMessage(comp);
			shouldScreenshot = false;
		}
	}

	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent event) {
		for (MechMBPart type:MechMBPart.INSTANCES.values()) {
			ResourceLocation loc = type.getRotatingBaseModel();
			try {
				IModel model = ModelLoaderRegistry.getModel(loc);
				if (model instanceof OBJModel) {
					model = model.process(ImmutableMap.of("flip-v", "true"));
				}
				model.getTextures().forEach((rl)->event.getMap().registerSprite(rl));
				IBakedModel b = model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, (rl)->Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString()));
				BASE_MODELS.put(loc, b);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
