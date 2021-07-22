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

package malte0811.industrialwires.mech_mb;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialwires.util.LocalSidedWorld;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

public enum Material {
	Copper(8.96, 220, "blocks/storage_copper"),
	Aluminium(2.7, 45, "blocks/storage_aluminum"),
	Lead(11.34, 12, "blocks/storage_lead"),
	Silver(10.49, 170, "blocks/storage_silver"),
	Nickel(8.908, 165, "blocks/storage_nickel"),
	Gold(19.3, 100, new ResourceLocation("minecraft", "blocks/gold_block")),
	Constantan(8.885, 600, "blocks/storage_constantan"),
	//Tensile strength is a guess ((GOLD+SILVER)/2), if anyone has better data I'll put it in
	Electrum((Silver.density + Gold.density) / 2e3, (Silver.tensileStrength + Gold.tensileStrength) / 2e6, "blocks/storage_electrum"),
	Iron(7.874, 350, new ResourceLocation("minecraft", "blocks/iron_block")),
	Steel(7.874, 1250, "blocks/storage_steel"),
    MaragingSteel(8.1, 2400,  Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(OreDictionary.getOres("blockMaragingSteel").isEmpty() ? ItemStack.EMPTY : OreDictionary.getOres("blockMaragingSteel").get(0) , null, null).getParticleTexture()),
    Tungstensteel(13.6, 3200, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(OreDictionary.getOres("blockTungstensteel").isEmpty() ? ItemStack.EMPTY : OreDictionary.getOres("blockTungstensteel").get(0) , null, null).getParticleTexture()),
	//Now for the fiber ones
	BasaltFiber(2.65, 3100, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(OreDictionary.getOres("blockBasaltFiber").isEmpty() ? ItemStack.EMPTY : OreDictionary.getOres("blockBasaltFiber").get(0) , null, null).getParticleTexture()),
	CarbonFiber(1.6, 5600, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(OreDictionary.getOres("blockCarbonFiber").isEmpty() ? ItemStack.EMPTY : OreDictionary.getOres("blockCarbonFiber").get(0) , null, null).getParticleTexture());
    //Removed diamond because they cause the wrong kind of incentive & don't make much sense. Use carbon fiber
	//DIAMOND(3.5, 2800, new ResourceLocation("minecraft", "blocks/diamond_block")),
	//Removed this because I made IE Uranium UO2, and because I need manual display space for other stuff
	//URANIUM(19.1, 400, "blocks/storage_uranium_side");// This is a bit silly. But why not.
	//in kg/m^3
	public final double density;
	public final double tensileStrength;
	public final ResourceLocation blockTexture;
	public final TextureAtlasSprite sprite;

	// density as parameter: g/cm^3
	// tStrength: MPa
	// assumes that resource domain is IE
	Material(double density, double tensileStrength, String path) {
		this.density = density*1e3;
		this.tensileStrength = tensileStrength*1e6;
		this.blockTexture = new ResourceLocation(ImmersiveEngineering.MODID, path);
		this.sprite = null;
	}
	Material(double density, double tensileStrength, ResourceLocation loc) {
		this.density = density*1e3;
		this.tensileStrength = tensileStrength*1e6;
		this.blockTexture = loc;
		this.sprite = null;
	}
	Material(double density, double tensileStrength, TextureAtlasSprite loc) {
		this.density = density*1e3;
		this.tensileStrength = tensileStrength*1e6;
		this.blockTexture = null;
		this.sprite = loc;
	}

	public boolean matchesBlock(ItemStack block, String prefix) {
		int[] ids = OreDictionary.getOreIDs(block);
		for (int i : ids) {
			if (OreDictionary.getOreName(i).equalsIgnoreCase(prefix + oreName())) {
				return true;
			}
		}
		return false;
	}

	public String oreName() {
		return name();
	}

	public boolean matchesBlock(LocalSidedWorld w, BlockPos relative, String prefix) {
		return Utils.isOreBlockAt(w.getWorld(), w.getRealPos(relative), prefix+oreName());
	}
}
