package net.minecraft.client.renderer

object CurrentTextureIdLens {
  def get:Int = GlStateManager.textureState(GlStateManager.activeTextureUnit).textureName

}
