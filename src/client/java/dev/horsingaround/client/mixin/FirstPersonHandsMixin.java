package dev.horsingaround.client.mixin;

import static dev.horsingaround.ride.RideTuning.FP_HAND_BOB;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.horsingaround.client.RideCamera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** First-person hands and held items bob with the saddle, more the faster the horse goes. */
@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonHandsMixin {
	@Inject(method = "renderHandsWithItems", at = @At("HEAD"))
	private void horsingaround$handBob(
		final float partialTicks,
		final PoseStack poseStack,
		final MultiBufferSource.BufferSource collector,
		final LocalPlayer player,
		final int lightCoords,
		final CallbackInfo ci
	) {
		final float lift = RideCamera.frameLift();
		if (lift != 0.0F) {
			final float bob = lift * FP_HAND_BOB * (0.4F + 0.6F * RideCamera.frameLimbSpeed());
			poseStack.translate(0.0F, bob, 0.0F);
			poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(bob * 30.0F));
		}
	}
}
