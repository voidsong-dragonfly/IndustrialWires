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
package malte0811.industrialWires;

 import blusunrize.immersiveengineering.ImmersiveEngineering;
 import blusunrize.immersiveengineering.api.MultiblockHandler;
 import blusunrize.immersiveengineering.api.energy.wires.WireApi;
 import com.google.common.collect.ImmutableMap;
 import malte0811.industrialWires.blocks.BlockIWBase;
 import malte0811.industrialWires.blocks.controlpanel.*;
 import malte0811.industrialWires.blocks.converter.*;
 import malte0811.industrialWires.blocks.hv.*;
 import malte0811.industrialWires.blocks.wire.*;
 import malte0811.industrialWires.compat.Compat;
 import malte0811.industrialWires.controlpanel.PanelComponent;
 import malte0811.industrialWires.controlpanel.PanelUtils;
 import malte0811.industrialWires.converter.EUCapability;
 import malte0811.industrialWires.converter.MechMBPart;
 import malte0811.industrialWires.converter.MultiblockMechMB;
 import malte0811.industrialWires.crafting.Recipes;
 import malte0811.industrialWires.entities.EntityBrokenPart;
 import malte0811.industrialWires.hv.MarxOreHandler;
 import malte0811.industrialWires.hv.MultiblockMarx;
 import malte0811.industrialWires.items.ItemIC2Coil;
 import malte0811.industrialWires.items.ItemKey;
 import malte0811.industrialWires.items.ItemPanelComponent;
 import malte0811.industrialWires.network.MessageGUIInteract;
 import malte0811.industrialWires.network.MessageItemSync;
 import malte0811.industrialWires.network.MessagePanelInteract;
 import malte0811.industrialWires.network.MessageTileSyncIW;
 import malte0811.industrialWires.util.CommandIW;
 import malte0811.industrialWires.util.MultiblockTemplateManual;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.network.datasync.DataSerializers;
 import net.minecraft.network.datasync.EntityDataManager;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.SoundEvent;
 import net.minecraftforge.event.RegistryEvent;
 import net.minecraftforge.fml.common.Loader;
 import net.minecraftforge.fml.common.Mod;
 import net.minecraftforge.fml.common.Mod.EventHandler;
 import net.minecraftforge.fml.common.SidedProxy;
 import net.minecraftforge.fml.common.event.FMLInitializationEvent;
 import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
 import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
 import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
 import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
 import net.minecraftforge.fml.common.network.NetworkRegistry;
 import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
 import net.minecraftforge.fml.common.registry.EntityRegistry;
 import net.minecraftforge.fml.common.registry.GameRegistry;
 import net.minecraftforge.fml.relauncher.Side;
 import net.minecraftforge.registries.IForgeRegistry;
 import org.apache.logging.log4j.Logger;

 import java.util.ArrayList;
 import java.util.List;

 import static malte0811.industrialWires.blocks.wire.BlockTypes_IC2_Connector.*;
 import static malte0811.industrialWires.converter.MechMBPart.EXAMPLE_MECHMB_LOC;
 import static malte0811.industrialWires.entities.EntityBrokenPart.MARKER_TEXTURE;
 import static malte0811.industrialWires.entities.EntityBrokenPart.RES_LOC_SERIALIZER;
 import static malte0811.industrialWires.wires.IC2Wiretype.*;

@Mod(modid = IndustrialWires.MODID, version = IndustrialWires.VERSION, dependencies = "required-after:immersiveengineering@[0.12-77,);after:ic2",
		certificateFingerprint = "7e11c175d1e24007afec7498a1616bef0000027d")
@Mod.EventBusSubscriber
public class IndustrialWires {
	public static final String MODID = "industrialwires";
	public static final String VERSION = "${version}";
	public static final String MODNAME = "Industrial Wires";
	public static final ResourceLocation TINNITUS_LOC = new ResourceLocation(IndustrialWires.MODID, "tinnitus");
	public static final ResourceLocation LADDER_START = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_start");//~470 ms ~=9 ticks
	public static final ResourceLocation LADDER_MIDDLE = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_middle");
	public static final ResourceLocation LADDER_END = new ResourceLocation(IndustrialWires.MODID, "jacobs_ladder_end");//~210 ms ~= 4 ticks
	public static final ResourceLocation MARX_BANG = new ResourceLocation(IndustrialWires.MODID, "marx_bang");
	public static final ResourceLocation MARX_POP = new ResourceLocation(IndustrialWires.MODID, "marx_pop");
	public static final ResourceLocation TURN_FAST = new ResourceLocation(IndustrialWires.MODID, "mech_mb_fast");
	public static final ResourceLocation TURN_SLOW = new ResourceLocation(IndustrialWires.MODID, "mech_mb_slow");

	public static final List<BlockIWBase> blocks = new ArrayList<>();
	public static final List<Item> items = new ArrayList<>();

	@GameRegistry.ObjectHolder(MODID+":"+BlockIC2Connector.NAME)
	public static BlockIC2Connector ic2conn = null;
	@GameRegistry.ObjectHolder(MODID+":"+BlockMechanicalConverter.NAME)
	public static BlockMechanicalConverter mechConv = null;
	@GameRegistry.ObjectHolder(MODID+":"+BlockJacobsLadder.NAME)
	public static BlockJacobsLadder jacobsLadder = null;
	@GameRegistry.ObjectHolder(MODID+":"+BlockPanel.NAME)
	public static BlockPanel panel = null;
	@GameRegistry.ObjectHolder(MODID+":"+BlockHVMultiblocks.NAME)
	public static BlockHVMultiblocks hvMultiblocks = null;
	@GameRegistry.ObjectHolder(MODID+":"+BlockMechanicalMB.NAME)
	public static BlockMechanicalMB mechanicalMB = null;
	@GameRegistry.ObjectHolder(MODID+":"+ BlockGeneralHV.NAME)
	public static BlockGeneralHV generalHV = null;

	@GameRegistry.ObjectHolder(MODID+":"+ItemIC2Coil.NAME)
	public static ItemIC2Coil coil = null;
	@GameRegistry.ObjectHolder(MODID+":"+ItemPanelComponent.NAME)
	public static ItemPanelComponent panelComponent = null;
	@GameRegistry.ObjectHolder(MODID+":"+ItemKey.ITEM_NAME)
	public static ItemKey key = null;


	@GameRegistry.ObjectHolder("ic2:te")
	public static Block ic2TeBlock = null;

	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	public static Logger logger;
	@Mod.Instance(MODID)
	public static IndustrialWires instance = new IndustrialWires();
	public static CreativeTabs creativeTab = new CreativeTabs(MODID) {

		@Override
		public ItemStack getTabIconItem() {
			if (coil!=null) {
				return new ItemStack(coil, 1, 2);
			} else {
				return new ItemStack(panel, 1, 3);
			}
		}
	};
	@SidedProxy(clientSide = "malte0811.industrialWires.client.ClientProxy", serverSide = "malte0811.industrialWires.CommonProxy")
	public static CommonProxy proxy;
	public static boolean hasIC2;
	public static boolean hasTechReborn;
	public static boolean isOldIE;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		hasIC2 = Loader.isModLoaded("ic2");
		hasTechReborn = Loader.isModLoaded("techreborn");
		{
			double ieThreshold = 12.74275;
			String ieVer = Loader.instance().getIndexedModList().get(ImmersiveEngineering.MODID).getDisplayVersion();
			int firstDash = ieVer.indexOf('-');
			String end = ieVer.substring(firstDash+1);
			String start = ieVer.substring(0, firstDash);
			end = end.replaceAll("[^0-9]", "");
			start = start.replaceAll("[^0-9]", "");
			double ieVerDouble = Double.parseDouble(start+"."+end);
			isOldIE = ieVerDouble<ieThreshold;
		}
		logger = e.getModLog();
		new IWConfig();
		if (hasIC2) {
			GameRegistry.registerTileEntity(TileEntityIC2ConnectorTin.class, MODID + ":ic2ConnectorTin");
			GameRegistry.registerTileEntity(TileEntityIC2ConnectorCopper.class, MODID + ":ic2ConnectorCopper");
			GameRegistry.registerTileEntity(TileEntityIC2ConnectorGold.class, MODID + ":ic2ConnectorGold");
			GameRegistry.registerTileEntity(TileEntityIC2ConnectorHV.class, MODID + ":ic2ConnectorHV");
			GameRegistry.registerTileEntity(TileEntityIC2ConnectorGlass.class, MODID + ":ic2ConnectorGlass");
			// Dummy TE's with bad names used to update old TE's to the proper names
			GameRegistry.registerTileEntity(DummyTEs.TinDummy.class, MODID + "ic2ConnectorTin");
			GameRegistry.registerTileEntity(DummyTEs.CopperDummy.class, MODID + "ic2ConnectorCopper");
			GameRegistry.registerTileEntity(DummyTEs.GoldDummy.class, MODID + "ic2ConnectorGold");
			GameRegistry.registerTileEntity(DummyTEs.HVDummy.class, MODID + "ic2ConnectorHV");
			GameRegistry.registerTileEntity(DummyTEs.GlassDummy.class, MODID + "ic2ConnectorGlass");

			if (IWConfig.enableConversion) {
				GameRegistry.registerTileEntity(TileEntityIEMotor.class, MODID + ":ieMotor");
				GameRegistry.registerTileEntity(TileEntityMechICtoIE.class, MODID + ":mechIcToIe");
				GameRegistry.registerTileEntity(TileEntityMechIEtoIC.class, MODID + ":mechIeToIc");
			}
		}
		GameRegistry.registerTileEntity(TileEntityMechMB.class, MODID + ":mechMB");
		GameRegistry.registerTileEntity(TileEntityJacobsLadder.class, MODID + ":jacobsLadder");
		GameRegistry.registerTileEntity(TileEntityMarx.class, MODID + ":marx_generator");
		GameRegistry.registerTileEntity(TileEntityPanel.class, MODID + ":control_panel");
		GameRegistry.registerTileEntity(TileEntityRSPanelConn.class, MODID + ":control_panel_rs");
		GameRegistry.registerTileEntity(TileEntityPanelCreator.class, MODID + ":panel_creator");
		GameRegistry.registerTileEntity(TileEntityUnfinishedPanel.class, MODID + ":unfinished_panel");
		GameRegistry.registerTileEntity(TileEntityComponentPanel.class, MODID + ":single_component_panel");
		GameRegistry.registerTileEntity(TileEntityDischargeMeter.class, MODID + ":discharge_meter");

		DataSerializers.registerSerializer(RES_LOC_SERIALIZER);
		MARKER_TEXTURE = EntityDataManager.createKey(EntityBrokenPart.class, RES_LOC_SERIALIZER);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "broken_part"), EntityBrokenPart.class,
				"broken_part", 0, this, 64, 5, true);

		proxy.preInit();
		Compat.preInit();
		MarxOreHandler.preInit();
		MechMBPart.preInit();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {

		if (IWConfig.enableConversion&&hasIC2) {
			event.getRegistry().register(new BlockMechanicalConverter());
		}
		if (hasIC2) {
			event.getRegistry().register(new BlockIC2Connector());
		}
		event.getRegistry().register(new BlockJacobsLadder());
		event.getRegistry().register(new BlockPanel());
		event.getRegistry().register(new BlockHVMultiblocks());
		event.getRegistry().register(new BlockMechanicalMB());
		event.getRegistry().register(new BlockGeneralHV());
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		for (BlockIWBase b:blocks) {
			event.getRegistry().register(b.createItemBlock());
		}

		if (hasIC2) {
			event.getRegistry().register(new ItemIC2Coil());
		}
		event.getRegistry().register(new ItemPanelComponent());
		event.getRegistry().register(new ItemKey());
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		registerSound(event.getRegistry(), TINNITUS_LOC);
		registerSound(event.getRegistry(), LADDER_START);
		registerSound(event.getRegistry(), LADDER_MIDDLE);
		registerSound(event.getRegistry(), LADDER_END);
		registerSound(event.getRegistry(), MARX_BANG);
		registerSound(event.getRegistry(), MARX_POP);
		registerSound(event.getRegistry(), TURN_FAST);
		registerSound(event.getRegistry(), TURN_SLOW);
	}

	private static void registerSound(IForgeRegistry<SoundEvent> reg, ResourceLocation loc) {
		reg.register(new SoundEvent(loc).setRegistryName(loc));
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		Recipes.addRecipes(event.getRegistry());
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		MultiblockMarx.INSTANCE = new MultiblockMarx();
		MultiblockHandler.registerMultiblock(MultiblockMarx.INSTANCE);
		MultiblockMechMB.INSTANCE = new MultiblockMechMB();
		MultiblockHandler.registerMultiblock(MultiblockMechMB.INSTANCE);
		MultiblockHandler.registerMultiblock(new MultiblockTemplateManual(EXAMPLE_MECHMB_LOC));

		packetHandler.registerMessage(MessageTileSyncIW.HandlerClient.class, MessageTileSyncIW.class, 0, Side.CLIENT);
		packetHandler.registerMessage(MessagePanelInteract.HandlerServer.class, MessagePanelInteract.class, 1, Side.SERVER);
		packetHandler.registerMessage(MessageGUIInteract.HandlerServer.class, MessageGUIInteract.class, 2, Side.SERVER);
		packetHandler.registerMessage(MessageItemSync.HandlerServer.class, MessageItemSync.class, 3, Side.SERVER);

		if (hasIC2) {
			ResourceLocation tex = new ResourceLocation(MODID, "blocks/ic2_conn_tin");
			float[] uvs = {3, 4, 11, 12};
			WireApi.registerFeedthroughForWiretype(TIN, new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
							IndustrialWires.MODID + ":blocks/ic2_conn_tin"), tex, uvs, .5, .5,
					ic2conn.getDefaultState().withProperty(BlockIC2Connector.TYPE, TIN_CONN),
					1/64F, TIN.getTransferRate(), f->(float)Math.ceil(f));

			WireApi.registerFeedthroughForWiretype(COPPER_IC2, new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
							IndustrialWires.MODID + ":blocks/ic2_conn_copper"), tex, uvs, .5, .5,
					ic2conn.getDefaultState().withProperty(BlockIC2Connector.TYPE, COPPER_CONN),
					1/64F, COPPER_IC2.getTransferRate(), f->(float)Math.ceil(f));

			WireApi.registerFeedthroughForWiretype(GOLD, new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_mv",
							IndustrialWires.MODID + ":blocks/ic2_conn_gold"), tex, uvs, .5625, .5625,
					ic2conn.getDefaultState().withProperty(BlockIC2Connector.TYPE, GOLD_CONN),
					1/64F, GOLD.getTransferRate(), f->(float)Math.ceil(f));

			WireApi.registerFeedthroughForWiretype(HV, new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_hv",
							IndustrialWires.MODID + ":blocks/ic2_conn_hv"), tex, uvs, .75, .75,
					ic2conn.getDefaultState().withProperty(BlockIC2Connector.TYPE, HV_CONN),
					1/64F, HV.getTransferRate(), f->(float)Math.ceil(f));

			WireApi.registerFeedthroughForWiretype(GLASS, new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"),
					ImmutableMap.of("#immersiveengineering:blocks/connector_connector_hv",
							IndustrialWires.MODID + ":blocks/ic2_conn_glass"), tex, uvs, .75, .75,
					ic2conn.getDefaultState().withProperty(BlockIC2Connector.TYPE, GLASS_CONN),
					1/64F, GLASS.getTransferRate(), f->(float)Math.ceil(f));
		}
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		IWPotions.init();
		Compat.init();
		MarxOreHandler.init();
		PanelComponent.init();
		if (hasIC2) {
			EUCapability.register();
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
        PanelUtils.PANEL_ITEM = Item.getItemFromBlock(panel);
        proxy.postInit();
	}
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandIW());
	}
}
