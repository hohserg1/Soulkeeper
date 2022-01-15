package hohserg.soulkeeper.render

import java.awt.image.BufferedImage
import java.util
import java.util.{Optional, function}

import codechicken.lib.texture.{TextureDataHolder, TextureUtils}
import com.google.common.collect.ImmutableList
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.handlers.ModelRegistration
import hohserg.soulkeeper.utils.LambdaUtils
import hohserg.soulkeeper.utils.color.RGBA
import javax.vecmath.Matrix4f
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel, ItemCameraTransforms, ItemOverrideList}
import net.minecraft.client.renderer.texture.{TextureAtlasSprite, TextureMap}
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.BannerPattern
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.model.ItemLayerModel
import org.apache.commons.lang3.tuple.Pair

object RhShieldRenderer extends TileEntityItemStackRenderer {
  var cameraTransformType: TransformType = TransformType.FIRST_PERSON_LEFT_HAND

  class ShieldBakedModel(base: IBakedModel) extends BakedModelDelegate(base) {
    override def handlePerspective(cameraTransformType: ItemCameraTransforms.TransformType): Pair[_ <: IBakedModel, Matrix4f] = {
      RhShieldRenderer.cameraTransformType = cameraTransformType
      super.handlePerspective(cameraTransformType)
    }
  }


  private def getPreparedTextureLocation(pattern: String) = {
    new ResourceLocation(Main.modid, "builtintexture_" + pattern)
  }

  def preparedShieldPatternTextures(textureMap: TextureMap): Unit =
    (BannerPattern.values().toSeq.map(pattern => pattern.getFileName) :+ "soulkeeper")
      .foreach { pattern =>
        val patternTextureLocation = new ResourceLocation("minecraft", "textures/entity/shield/" + pattern + ".png")
        val w = 20
        val h = 20

        val resultImage1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        resultImage1.setRGB(0, 0, w, h, (0 until w * h).map(_ => 0).toArray, 0, 0)

        val resultImage2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        resultImage2.setRGB(0, 0, w, h, (0 until w * h).map(_ => 0).toArray, 0, 0)

        val image = MinecraftForgeClient.getImageLayer(patternTextureLocation, Minecraft.getMinecraft.getResourceManager)

        for {
          x <- 0 to 9
          y <- 0 to 19
          color = RGBA.fromARGB(image.getRGB(x + 2, y + 2)).toHSBA
        } {
          if (color.getB != 0) {
            resultImage1.setRGB(x, y, RGBA.fromARGB(0x805CD3D8).toHSBA.setB(color.getB).toRGBA.argb())
            resultImage2.setRGB(x, y, RGBA.fromARGB(0x80C7FFB2).toHSBA.setB(color.getB).toRGBA.argb())
          }
        }

        TextureUtils.getTextureSpecial(textureMap, getPreparedTextureLocation(pattern).toString)
          .addTexture(new TextureDataHolder(resultImage1))
        //.addTexture(new TextureDataHolder(resultImage2))
      }

  val getDisignModel: String => IBakedModel = LambdaUtils.memoize((pattern: String) => {
    val preparedSprite = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(getPreparedTextureLocation(pattern).toString)

    val quads = ItemLayerModel.getQuadsForSprite(1, preparedSprite, DefaultVertexFormats.ITEM, Optional.empty())

    val model = new IBakedModel {
      override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] =
        if (side == null)
          quads
        else
          ImmutableList.of()

      override def isAmbientOcclusion: Boolean = false

      override def isGui3d: Boolean = false

      override def isBuiltInRenderer: Boolean = false

      override def getParticleTexture: TextureAtlasSprite = preparedSprite

      override def getOverrides: ItemOverrideList = ItemOverrideList.NONE
    }

    model
  })

  override def renderByItem(stack: ItemStack, partialTicks: Float): Unit = {
    GlStateManager.pushMatrix()
    GlStateManager.translate(0.5, 0.5, 0.5)
    if (CapabilityXPContainer(stack).getXp > 0) {
      renderHandle(stack)
      renderDesign(stack)
      renderShield(stack)
    } else
      renderEmpty(stack)
    GlStateManager.popMatrix()

  }

  def renderEmpty(stack: ItemStack): Unit =
    Minecraft.getMinecraft.getRenderItem.renderItem(stack, ModelRegistration.shieldEmptyBakedModel)

  private def renderShield(stack: ItemStack): Unit =
    Minecraft.getMinecraft.getRenderItem.renderItem(stack, ModelRegistration.shieldEnchantedBakedModel)


  private def renderDesign(stack: ItemStack): Unit = {
    val pattern = stack.getOrCreateSubCompound("shieldData").getString("pattern")

    val pixel = 1d / 16

    GlStateManager.pushMatrix()
    GlStateManager.translate(-pixel * 3, -1d / 16 * 8, -pixel * 6.5)
    GlStateManager.scale(1 + pixel * 4, 1 + pixel * 4, 0.5)
    Minecraft.getMinecraft.getRenderItem.renderItem(stack, getDisignModel(if (pattern == "") "soulkeeper" else pattern))
    GlStateManager.popMatrix()
  }

  private def renderHandle(stack: ItemStack): Unit =
    if (cameraTransformType != TransformType.GUI)
      Minecraft.getMinecraft.getRenderItem.renderItem(stack, ModelRegistration.shieldHandleBakedModel)

}
