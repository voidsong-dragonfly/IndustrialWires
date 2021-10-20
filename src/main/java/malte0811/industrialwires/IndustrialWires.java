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
package malte0811.industrialwires;

 import blusunrize.immersiveengineering.api.MultiblockHandler;
 import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
 import malte0811.industrialwires.blocks.BlockIWBase;
 import malte0811.industrialwires.blocks.TEDataFixer;
 import malte0811.industrialwires.blocks.converter.BlockMechanicalMB;
 import malte0811.industrialwires.blocks.converter.TileEntityMechMB;
 import malte0811.industrialwires.entities.EntityBrokenPart;
 import malte0811.industrialwires.mech_mb.MechMBPart;
 import malte0811.industrialwires.mech_mb.MultiblockMechMB;
 import malte0811.industrialwires.network.MessageTileSyncIW;
 import malte0811.industrialwires.util.MultiblockTemplateManual;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.datasync.DataSerializers;
 import net.minecraft.network.datasync.EntityDataManager;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.SoundEvent;
 import net.minecraft.util.datafix.FixTypes;
 import net.minecraftforge.common.util.ModFixs;
 import net.minecraftforge.event.RegistryEvent;
 import net.minecraftforge.fml.common.FMLCommonHandler;
 import net.minecraftforge.fml.common.Mod;
 import net.minecraftforge.fml.common.Mod.EventHandler;
 import net.minecraftforge.fml.common.SidedProxy;
 import net.minecraftforge.fml.common.event.FMLInitializationEvent;
 import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
 import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
 import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
 import net.minecraftforge.fml.common.network.NetworkRegistry;
 import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
 import net.minecraftforge.fml.common.registry.EntityRegistry;
 import net.minecraftforge.fml.common.registry.GameRegistry;
 import net.minecraftforge.fml.relauncher.Side;
 import net.minecraftforge.oredict.OreDictionary;
 import org.apache.logging.log4j.Logger;

 import java.util.ArrayList;
 import java.util.List;

 import static malte0811.industrialwires.entities.EntityBrokenPart.MARKER_TEXTURE;
 import static malte0811.industrialwires.entities.EntityBrokenPart.RES_LOC_SERIALIZER;
 import static malte0811.industrialwires.mech_mb.MechMBPart.EXAMPLE_MECHMB_LOC;

 @Mod(modid = IndustrialWires.MODID, version = IndustrialWires.VERSION, dependencies = "required-after:immersiveengineering@[0.12-86,);required-after:forge@[14.23.3.2694,)",
		certificateFingerprint = "7e11c175d1e24007afec7498a1616bef0000027d",
		updateJSON = "https://raw.githubusercontent.com/malte0811/IndustrialWires/MC1.12/changelog.json")
@Mod.EventBusSubscriber
public class IndustrialWires {
	public static final String MODID = "industrialwires";
	public static final String VERSION = "${version}";
	public static final String MODNAME = "Industrial Wires";
	public static final int DATAFIXER_VER = 1;
	public static final SoundEvent TURN_FAST = createSoundEvent(new ResourceLocation(IndustrialWires.MODID, "mech_mb_fast"));
	public static final SoundEvent TURN_SLOW = createSoundEvent(new ResourceLocation(IndustrialWires.MODID, "mech_mb_slow"));
	public static final SoundEvent MMB_BREAKING = createSoundEvent(new ResourceLocation(IndustrialWires.MODID, "mech_mb_breaking"));

	private static final SoundEvent createSoundEvent(ResourceLocation loc) {
		return new SoundEvent(loc).setRegistryName(loc);
	}

	public static final List<BlockIWBase> blocks = new ArrayList<>();
	public static final List<Item> items = new ArrayList<>();

	@GameRegistry.ObjectHolder(MODID+":"+BlockMechanicalMB.NAME)
	public static BlockMechanicalMB mechanicalMB = null;

	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	public static Logger logger;
	@Mod.Instance(MODID)
	public static IndustrialWires instance = new IndustrialWires();
	@SidedProxy(clientSide = "malte0811.industrialwires.client.ClientProxy", serverSide = "malte0811.industrialwires.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		logger = e.getModLog();
		new IWConfig();

		GameRegistry.registerTileEntity(TileEntityMechMB.class, new ResourceLocation(MODID, "mechMB"));

		DataSerializers.registerSerializer(RES_LOC_SERIALIZER);
		MARKER_TEXTURE = EntityDataManager.createKey(EntityBrokenPart.class, RES_LOC_SERIALIZER);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "broken_part"), EntityBrokenPart.class,
				"broken_part", 0, this, 64, 5, true);

		proxy.preInit();
		// This has to run before textures are stitched, i.e. in preInit
		MechMBPart.preInit();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {

		event.getRegistry().register(new BlockMechanicalMB());
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().register(TURN_FAST);
		event.getRegistry().register(TURN_SLOW);
		event.getRegistry().register(MMB_BREAKING);
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

		OreDictionary.registerOre("blockBearing", new ItemStack(IEObjects.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()));

		MultiblockMechMB.INSTANCE = new MultiblockMechMB();
		MultiblockHandler.registerMultiblock(MultiblockMechMB.INSTANCE);
		MultiblockHandler.registerMultiblock(new MultiblockTemplateManual(EXAMPLE_MECHMB_LOC));

		packetHandler.registerMessage(MessageTileSyncIW.HandlerClient.class, MessageTileSyncIW.class, 0, Side.CLIENT);

		MechMBPart.init();
		ModFixs fixer = FMLCommonHandler.instance().getDataFixer().init(MODID, DATAFIXER_VER);
		fixer.registerFix(FixTypes.BLOCK_ENTITY, new TEDataFixer());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit();
	}
}
