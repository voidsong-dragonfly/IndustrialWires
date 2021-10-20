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

package malte0811.industrialwires.util;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialwires.mech_mb.MultiblockMechMB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;

public final class MiscUtils {
	private MiscUtils() {
	}

	public static BlockPos offset(BlockPos p, EnumFacing f, boolean mirror, Vec3i relative) {
		return offset(p, f, mirror, relative.getX(), relative.getZ(), relative.getY());
	}
	/**
	 * @param mirror inverts right
	 */
	public static BlockPos offset(BlockPos p, EnumFacing f, boolean mirror, int right, int forward, int up) {
		if (mirror) {
			right *= -1;
		}
		return p.offset(f, forward).offset(f.rotateY(), right).add(0, up, 0);
	}

	public static Vec3d offset(Vec3d p, EnumFacing f, boolean mirror, Vec3d relative) {
		return offset(p, f, mirror, relative.x, relative.z, relative.y);
	}

	public static Vec3d offset(Vec3d p, EnumFacing f, boolean mirror, double right, double forward, double up) {
		if (mirror) {
			right *= -1;
		}
		return offset(offset(p, f, forward), f.rotateY(), right).add(0, up, 0);
	}

	public static Vec3d offset(Vec3d in, EnumFacing f, double amount) {
		if (amount==0) {
			return in;
		}
		return in.add(f.getXOffset() * amount, f.getYOffset() * amount, f.getZOffset() * amount);
	}

	/**
	 * Calculates the parameters for offset to generate here from origin
	 *
	 * @return right, forward, up
	 */
	public static BlockPos getOffset(Vec3i origin, EnumFacing f, boolean mirror, Vec3i here) {
		int dX = here.getX()-origin.getX();
		int dZ = here.getZ()-origin.getZ();
		int forward = 0;
		int right = 0;
		int up = here.getY() - origin.getY();
		switch (f) {
			case NORTH:
				forward = dZ;
				right = -dX;
				break;
			case SOUTH:
				forward = -dZ;
				right = dX;
				break;
			case WEST:
				right = dZ;
				forward = dX;
				break;
			case EAST:
				right = -dZ;
				forward = -dX;
				break;
		}
		if (mirror) {
			right *= -1;
		}
		return new BlockPos(right, forward, up);
	}

	@Nonnull
	public static AxisAlignedBB apply(@Nonnull Matrix4 mat, @Nonnull AxisAlignedBB in) {
		Vec3d min = new Vec3d(in.minX, in.minY, in.minZ);
		Vec3d max = new Vec3d(in.maxX, in.maxY, in.maxZ);
		min = mat.apply(min);
		max = mat.apply(max);
		return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	@SideOnly(Side.CLIENT)
	public static Vec2f subtract(Vec2f a, Vec2f b) {
		return new Vec2f(a.x-b.x, a.y-b.y);
	}

	@SideOnly(Side.CLIENT)
	public static Vec2f add(Vec2f a, Vec2f b) {
		return new Vec2f(a.x+b.x, a.y+b.y);
	}

	@SideOnly(Side.CLIENT)
	public static Vec2f scale(Vec2f a, float f) {
		return new Vec2f(a.x*f, a.y*f);
	}

	@SideOnly(Side.CLIENT)
	public static Vector3f withNewY(Vec2f in, float y) {
		return new Vector3f(in.x, y, in.y);
	}

	public static int count1Bits(int i) {
		int ret = 0;
		for (int j = 0; j < 32; j++) {
			ret += (i>>>j)&1;
		}
		return ret;
	}

	public static String toSnakeCase(String in) {
		StringBuilder ret = new StringBuilder(in.length());
		ret.append(in.charAt(0));
		for (int i = 1;i<in.length();i++) {
			char here = in.charAt(i);
			if (Character.isUpperCase(here)) {
				ret.append('_').append(Character.toLowerCase(here));
			} else {
				ret.append(here);
			}
		}
		return ret.toString();
	}

	public static MultiblockHandler.IMultiblock getMBFromName(String s) {
		for (MultiblockHandler.IMultiblock mb:MultiblockHandler.getMultiblocks()) {
			if (mb.getUniqueName().equals(s)) {
				return mb;
			}
		}
		return MultiblockMechMB.INSTANCE;
	}
}