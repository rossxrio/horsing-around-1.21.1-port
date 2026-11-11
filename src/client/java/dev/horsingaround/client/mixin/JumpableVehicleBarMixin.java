package dev.horsingaround.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.horsingaround.ride.RideState;
import dev.horsingaround.ride.RideStateHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** The horse jump bar becomes the stamina bar, drawn with the vanilla jump bar sprites. */
@Mixin(Gui.class)
public abstract class JumpableVehicleBarMixin {
	@Unique
	private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("hud/jump_bar_background");
	@Unique
	private static final ResourceLocation EXHAUSTED_BACKGROUND = ResourceLocation.withDefaultNamespace("hud/jump_bar_cooldown");
	@Unique
	private static final ResourceLocation PROGRESS = ResourceLocation.withDefaultNamespace("hud/jump_bar_progress");
	@Unique
	private static final int EXHAUSTED_TINT = 0xFFFF7A6E;
	// EXHAUSTED_TINT, as the colour setting of the old sprite draw takes it.
	@Unique
	private static final float EXHAUSTED_TINT_RED = 1.0F;
	@Unique
	private static final float EXHAUSTED_TINT_GREEN = 0x7A / 255.0F;
	@Unique
	private static final float EXHAUSTED_TINT_BLUE = 0x6E / 255.0F;
	@Unique
	private static final int WIDTH = 182;
	@Unique
	private static final int HEIGHT = 5;

	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapMethod(method = "renderJumpMeter")
	private void horsingaround$staminaBar(final PlayerRideableJumping jumpable, final GuiGraphics graphics, final int left, final Operation<Void> original) {
		final RideState s = RideStateHolder.managedRide(this.minecraft.player.getVehicle());
		if (s == null) {
			original.call(jumpable, graphics, left);
			return;
		}
		final int top = graphics.guiHeight() - 32 + 3;
		graphics.blitSprite(s.exhausted ? EXHAUSTED_BACKGROUND : BACKGROUND, left, top, WIDTH, HEIGHT);
		final int filled = Mth.lerpDiscrete(s.stamina, 0, WIDTH);
		if (filled > 0) {
			// The sprite draw of this version takes its colour from the graphics, so the exhausted bar is tinted as in newer ones.
			if (s.exhausted) {
				graphics.setColor(EXHAUSTED_TINT_RED, EXHAUSTED_TINT_GREEN, EXHAUSTED_TINT_BLUE, 1.0F);
			}
			graphics.blitSprite(PROGRESS, WIDTH, HEIGHT, 0, 0, left, top, filled, HEIGHT);
			if (s.exhausted) {
				graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}
}
