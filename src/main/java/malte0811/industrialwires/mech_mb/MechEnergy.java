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


import malte0811.industrialwires.IWConfig;
import net.minecraft.util.math.MathHelper;

public final class MechEnergy {
	private double speed = 0;
	public boolean invalid = false;
	public final double inertia;
	public final double weight;

	public MechEnergy(double weight, double intertia, double speed) {
		this.inertia = intertia;
		this.speed = speed;
		this.weight = weight;
	}

	public double getEnergy() {
		return .5 * inertia * speed * speed;
	}

	public double getSpeed() {
		return speed;
	}

	public void addEnergy(double energy) {
		if (energy <= 0) {
			return;
		}
		double targetEnergy = getEnergy() + energy;
		speed = Math.sqrt(2 * targetEnergy / inertia);
	}

	public void extractEnergy(double energy) {
		if (energy <= 0) {
			return;
		}
		double oldEnergy = getEnergy();
		energy = Math.min(energy, oldEnergy);
		speed = Math.sqrt(2 * (oldEnergy - energy) / inertia);
	}

	public void decaySpeed(boolean lossless) {
		//Decay should use https://www.researchgate.net/publication/241703345_Power_Loss_Prediction_in_High-Speed_Roller_Bearings and https://spectrum.ieee.org/superconducting-flywheel-grid-energy-storage (I guess on the last one)
		//W to J/t is 3.6 because 86400 (sec per day) divided by 24000 (ticks per MC day) is 3.6
		double energy = (lossless ? 0.00625 : 1) * 3.6 * Math.pow(1.125, 0.00545 * speed) * (weight/25000);
		//Example function graph is here https://www.desmos.com/calculator/qz4wrimxqf
		extractEnergy(energy);
		if (speed < 0.1) speed = 0;
	}

	private static final int TICKS_FOR_ADJUSTMENT = 30;
	private double targetSpeed;
	private double oldSpeed = -1;
	private int ticksTillReached = -1;

	//ONLY USE FOR SYNCING
	public void setTargetSpeed(double speed) {
		targetSpeed = speed;
		oldSpeed = getSpeed();
		ticksTillReached = TICKS_FOR_ADJUSTMENT;
	}

	public boolean clientUpdate() {
		if (ticksTillReached >= 0) {
			speed = ((TICKS_FOR_ADJUSTMENT - ticksTillReached) * targetSpeed +
					ticksTillReached * oldSpeed) / TICKS_FOR_ADJUSTMENT;
			ticksTillReached--;
			return true;
		} else {
			oldSpeed = -1;
			return false;
		}
	}

	//Sound helper methods

	public float getVolumeSlow() {
		return (1-getSoundLambda())*getTotalVolume();
	}

	public float getVolumeFast() {
		return getSoundLambda()*getTotalVolume();
	}

	public float getPitch() {
		return (float) Math.min(.3 * Math.sqrt(getSpeedForSound()), 1.25);
	}

	private float getTotalVolume() {
		if (invalid)
			return 0;
		float ret = (float) (inertia / 20e3 * Math.tanh(getSpeedForSound()/30));
		ret = Math.min(ret, 1F);
		return ret;
	}

	private float getSoundLambda() {
		return (float) MathHelper.clamp(getSpeedForSound() / 20 - .5F, 0, 1);
	}

	private double getSpeedForSound() {
		final double MIN_SPEED = 5;
		return getSpeed()-MIN_SPEED;//Volume should be zero by the time the sound stops
	}
}