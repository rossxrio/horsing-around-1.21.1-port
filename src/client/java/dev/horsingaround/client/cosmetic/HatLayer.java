package dev.horsingaround.client.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.horsingaround.HorsingAround;
import dev.horsingaround.net.HatPayload;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import dev.horsingaround.client.render.state.PlayerRenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Draws a supporter's cowboy hat in their colour (the felt texture is grey, tinted by the colour). */
public final class HatLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HorsingAround.MOD_ID, "textures/entity/cowboy_hat.png");

	private final HatModel model;

	public HatLayer(final RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, final EntityModelSet models) {
		super(renderer);
		this.model = new HatModel(models.bakeLayer(HatModel.LAYER));
	}

	@Override
	public void render(
		final PoseStack poseStack, final MultiBufferSource buffers, final int lightCoords, final AbstractClientPlayer player, final float limbSwing,
		final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float headYaw, final float headPitch
	) {
		final int color = Hats.colorFor(player);
		if (color == HatPayload.NONE || player.isInvisible()) {
			return;
		}
		// The head, as it is posed this frame (a rider's included).
		this.getParentModel().copyPropertiesTo(this.model);
		this.model.renderToBuffer(
			poseStack, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), lightCoords, LivingEntityRenderer.getOverlayCoords(player, 0.0F), 0xFF000000 | color
		);
	}
}
