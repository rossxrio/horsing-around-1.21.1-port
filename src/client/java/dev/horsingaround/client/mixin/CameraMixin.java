package dev.horsingaround.client.mixin;

import dev.horsingaround.client.RideCamera;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// (A low priority: first among the mods working on the camera as vanilla finishes placing it, see horsingaround$eyes.)
@Mixin(value = Camera.class, priority = 900)
public abstract class CameraMixin {
	@Shadow
	private @Nullable Entity entity;
	@Shadow
	private boolean detached;
	// Set in the constructor hook below: Mixin drops an initialiser here (javac numbers the constructor's return after it,
	// so Mixin takes it for constructor body), which left this null and crashed the first ridden frame.
	@Unique
	private Minecraft minecraft;
	@Shadow
	private boolean initialized;
	@Shadow
	private float partialTickTime;
	@Shadow
	private BlockGetter level;
	@Shadow
	private Vec3 position;
	@Shadow
	private float xRot;
	@Shadow
	private float yRot;

	/** Render-thread scratch for the first-person nod. */
	@Unique
	private static final float[] NOD = new float[1];

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Shadow
	protected abstract void setPosition(double x, double y, double z);

	@Inject(method = "<init>", at = @At("RETURN"))
	private void horsingaround$init(final CallbackInfo ci) {
		this.minecraft = Minecraft.getInstance();
	}

	/**
	 * Places the third-person camera while riding (over the shoulder or centred, instead of vanilla's placement). Another
	 * camera mod that is driving the camera (see {@link RideCamera#isActive}) is left alone.
	 */
	@WrapMethod(method = "setup")
	private void horsingaround$rideCamera(
		final BlockGetter level, final Entity e, final boolean detached, final boolean mirrored, final float partialTicks, final Operation<Void> original
	) {
		if (this.minecraft.player != null) {
			RideCamera.updateFrame(this.minecraft, partialTicks);
		}
		if (RideCamera.isActive(e, this.minecraft)) {
			this.level = level;
			this.horsingaround$place(e, partialTicks);
			return;
		}
		original.call(level, e, detached, mirrored, partialTicks);
	}

	// The third-person ride camera in place of vanilla's setup (the rest of what setup does is set here).
	@Unique
	private void horsingaround$place(final Entity e, final float partialTicks) {
		this.initialized = true;
		this.entity = e;
		this.detached = true;
		this.partialTickTime = partialTicks;
		this.horsingaround$placeRide(e, partialTicks);
	}

	/** The third-person riding camera's rotation and place (see {@link RideCamera#place}). */
	@Unique
	private void horsingaround$placeRide(final Entity e, final float partialTicks) {
		final float yaw = e.getViewYRot(partialTicks);
		final float pitch = e.getViewXRot(partialTicks);
		this.setRotation(yaw, pitch);
		RideCamera.place(e, partialTicks, yaw, pitch);
		this.setPosition(RideCamera.cameraX(), RideCamera.cameraY(), RideCamera.cameraZ());
	}

	/**
	 * In first person the eyes follow the saddle, before another mod works on the view from here. Real Camera puts the view on
	 * the rider's drawn head, which already rides the saddle: on top of that, every step's easing moved the view again, into
	 * the horse on the way up.
	 */
	@Inject(method = "setup", at = @At("RETURN"))
	private void horsingaround$eyes(final BlockGetter level, final Entity e, final boolean detached, final boolean mirrored, final float partialTicks, final CallbackInfo ci) {
		this.horsingaround$firstPerson(partialTicks);
	}

	@Unique
	private void horsingaround$firstPerson(final float partialTicks) {
		if (this.detached || !RideCamera.isFirstPersonRide(this.entity, this.minecraft)) {
			return;
		}
		final double lift = RideCamera.firstPersonOffset(partialTicks, NOD);
		if (NOD[0] != 0.0F) {
			this.setRotation(this.yRot, this.xRot + NOD[0]);
		}
		if (lift != 0.0) {
			this.setPosition(this.position.x, this.position.y + lift, this.position.z);
		}
	}
}
