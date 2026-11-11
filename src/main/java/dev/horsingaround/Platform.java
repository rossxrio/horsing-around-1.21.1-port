package dev.horsingaround;

import java.nio.file.Path;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * What the common code asks of the mod loader it runs on, Fabric, NeoForge or Forge (the entry points are in fabric/,
 * neoforge/ and forge/).
 */
public final class Platform {
	private Platform() {
	}

	public static Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static boolean isModLoaded(final String id) {
		return ModList.get().isLoaded(id);
	}

	/** This mod's version, as the player sees it in the mod list. */
	public static String modVersion() {
		return ModList.get().getModContainerById(HorsingAround.MOD_ID).map(mod -> mod.getModInfo().getVersion().toString()).orElse("");
	}

	/** Whether the player's client runs the mod (it said it takes this payload). */
	public static boolean canSend(final ServerPlayer player, final CustomPacketPayload.Type<?> type) {
		return player.connection.hasChannel(type);
	}

	public static void send(final ServerPlayer player, final CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}
}
