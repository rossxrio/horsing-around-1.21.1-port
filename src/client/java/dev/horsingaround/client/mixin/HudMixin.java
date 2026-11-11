package dev.horsingaround.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.horsingaround.client.RideCamera;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.horsingaround.ride.RideState;
import dev.horsingaround.ride.RideStateHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The centre crosshair shows while the riding camera is over the shoulder (aiming goes where it points). The stamina
 * bar (in the jump bar's slot) shows whenever stamina is not full, whatever else wants the slot; before 1.21.6 the slot
 * was the jump bar's alone (no locator bar), so there is nothing to patch. Applied after other mods' HUD hooks
 * (priority), so the stamina bar's claim on the slot wraps theirs (Icy's Better Horses hands the slot to the experience
 * bar while riding in survival).
 */
@Mixin(value = Gui.class, priority = 1500)
public abstract class HudMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
	private boolean horsingaround$shoulderCrosshair(final boolean firstPerson) {
		return firstPerson || RideCamera.isOverShoulder(this.minecraft.getCameraEntity(), this.minecraft);
	}

}
