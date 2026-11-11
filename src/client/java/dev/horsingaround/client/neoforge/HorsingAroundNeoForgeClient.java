package dev.horsingaround.client.neoforge;

import dev.horsingaround.HorsingAround;
import dev.horsingaround.MixinAudit;
import dev.horsingaround.client.HorsingAroundClient;
import dev.horsingaround.client.config.HorseSettingsScreen;
import dev.horsingaround.client.cosmetic.HatLayer;
import dev.horsingaround.client.cosmetic.HatModel;
import dev.horsingaround.client.cosmetic.Hats;
import dev.horsingaround.net.PlayerHatPayload;
import dev.horsingaround.net.RulesPayload;
import dev.horsingaround.net.ServerRules;
import dev.horsingaround.net.StylePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;



import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;



import dev.horsingaround.neoforge.HorsingAroundNeoForge;
import net.neoforged.neoforge.common.NeoForge;

/** NeoForge's client entry point, next to the common one (neoforge/HorsingAroundNeoForge). */
@Mod(value = HorsingAround.MOD_ID, dist = Dist.CLIENT)
public final class HorsingAroundNeoForgeClient {
	public HorsingAroundNeoForgeClient(final IEventBus modBus, final ModContainer mod) {
		// (On the main thread once every mod is loaded: the Entity Model Features hook needs that mod set up.)
		modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(HorsingAroundClient::init));







		HorsingAroundNeoForge.clientRules = (rules, context) -> HorsingAroundClient.acceptRules(rules);
		HorsingAroundNeoForge.clientStyle = (style, context) -> ServerRules.accept(style);
		HorsingAroundNeoForge.clientHats = (hat, context) -> Hats.receive(hat);

		modBus.addListener(RegisterKeyMappingsEvent.class, event -> {


			event.register(HorsingAroundClient.SETTINGS_KEY);
			event.register(HorsingAroundClient.SWAP_SHOULDER_KEY);
		});
		modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event -> event.registerLayerDefinition(HatModel.LAYER, HatModel::createLayer));
		modBus.addListener(EntityRenderersEvent.AddLayers.class, HorsingAroundNeoForgeClient::addHatLayers);
		mod.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new HorseSettingsScreen(parent));
		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> HorsingAroundClient.tick(Minecraft.getInstance()));
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> HorsingAroundClient.joining());
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> HorsingAroundClient.left());
		if (MixinAudit.ENABLED) {
			// (Once the game is up: its first screen, the title screen or NeoForge's loading warnings.)
			NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
				MixinAudit.run("client");
				Minecraft.getInstance().stop();
			});
		}
	}

	/** The supporters' cowboy hat, on players and mannequins. */
	private static void addHatLayers(final EntityRenderersEvent.AddLayers event) {






		for (final PlayerSkin.Model skin : event.getSkins()) {
			if (event.getSkin(skin) instanceof PlayerRenderer player) {
				addHatLayer(player, event.getEntityModels());
			}
		}

	}

	private static void addHatLayer(final PlayerRenderer renderer, final EntityModelSet models) {
		if (renderer != null) {
			renderer.addLayer(new HatLayer(renderer, models));
		}
	}
}
