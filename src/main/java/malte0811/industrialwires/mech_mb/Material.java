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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

public enum Material {
	//Metal materials in order of maxV
	Lead(11.34, 12, "blocks/storage_lead"),
	Gold(19.3, 100, new ResourceLocation("minecraft", "blocks/gold_block")),
	Silver(10.49, 170, "blocks/storage_silver"),
	Electrum((Silver.density + Gold.density) / 2e3, (Silver.tensileStrength + Gold.tensileStrength) / 2e6, "blocks/storage_electrum"), //Tensile strength is a guess ((GOLD+SILVER)/2), if anyone has better data I'll put it in
	Aluminum(2.7, 45, "blocks/storage_aluminum"),
	Nickel(8.908, 165, "blocks/storage_nickel"),
	Uranium(19.1, 400, "blocks/storage_uranium_side"), // This is a bit silly. But why not.
	Copper(8.96, 220, "blocks/storage_copper"),
	Tin(5.7, 220, "blockTin", true),
	Iron(7.874, 350, new ResourceLocation("minecraft", "blocks/iron_block")),
	Titanium(4.51, 293, "blockTitanium", true),
	Constantan(8.885, 600, "blocks/storage_constantan"),
	Bronze(7.6, 550, "blockBronze", true),
	Iridium(22.56, 2000, "blockIridium", true),
	Steel(7.874, 1250, "blocks/storage_steel"),
	Tungsten(19.25, 3900, "blockTungsten", true),
	Tungstensteel(13.6, 3200, "blockTungstensteel", true),
    MaragingSteel(8.1, 2400, "blockMaragingSteel", true),
	//Now for the fiber ones
	BasaltFiber(2.65, 3100, "blockBasaltFiber", true),
	CarbonFiber(1.6, 5600, "blockCarbonFiber", true),
	CarbonNanotube(1.3, 17000,  "blockCarbonNanotube", true);

	//Removed diamond because they cause the wrong kind of incentive (more diamond use in MC tech mods) & don't make much sense. Use carbon fiber/carbon nanotubes
	//DIAMOND(3.5, 2800, new ResourceLocation("minecraft", "blocks/diamond_block")),

	public final double density; //in kg/m^3
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
	Material(double density, double tensileStrength, String path, boolean getAtlasSprite) {
		this.density = density*1e3;
		this.tensileStrength = tensileStrength*1e6;
		this.blockTexture = null;
		//Because we here at Industrial Wires hate our sanity, the universe, and ourselves : PersonTheCat and Silfryi present : "Getting the texture of an arbitrary block is a pain in our collective behinds"
		ItemStack stack = OreDictionary.getOres(path).isEmpty() ? ItemStack.EMPTY : OreDictionary.getOres(path).get(0);
		this.sprite = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null).getQuads(Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItemDamage()), EnumFacing.UP, 0L).get(0).getSprite();
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
