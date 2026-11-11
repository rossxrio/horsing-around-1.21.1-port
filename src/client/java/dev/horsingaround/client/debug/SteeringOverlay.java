package dev.horsingaround.client.debug;

import dev.horsingaround.ride.RideState;
import dev.horsingaround.ride.RideStateHolder;
import dev.horsingaround.ride.RideTuning;
import java.util.Locale;
import net.minecraft.client.Minecraft;





import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;

/**
 * With entity hitboxes shown (F3+B), draws how the ridden horse is picking its way, for play-testing: its collision box
 * (green, at the tick's position), the rider's line
 * (white), where the horse is heading (yellow), its detour round something and the way it planned (aqua), what its look-ahead found along the
 * rider's line (red: a wall, orange: danger, blue: a jumpable gap), the ledge it is jumping (green), and the ground
 * carrying its front and back on a step (magenta); how hard it is cutting round, while it does. Debug only, so it may allocate.
 * Needs gizmos, which came in 1.21.11; before, it draws nothing.
 */
public final class SteeringOverlay {
	private static final String[] GAITS = {"stop", "walk", "trot", "canter", "gallop"};

	private SteeringOverlay() {
	}

	/** Called at the end of each client tick, while that tick's gizmos are being collected. */
	public static void tick(final Minecraft minecraft) {






































	}




















}
