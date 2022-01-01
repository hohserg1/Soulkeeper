package hohserg.soulkeeper.render

import java.util
import java.util.Optional

import com.google.common.collect.ImmutableList
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.items.tools.RhTool
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model._
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.client.model.ItemLayerModel

class RhToolModel(base: IBakedModel, enchantedTextureName: String, emptyTextureName: String) extends BakedModelDelegate(base) {

  def createModelForTexture(textureName: String): IBakedModel = {
    val atlas = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(textureName)
    val quads = ItemLayerModel.getQuadsForSprite(1, atlas, DefaultVertexFormats.ITEM, Optional.empty())

    new BakedModelDelegate(base) {
      override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] =
        if (side == null)
          quads
        else
          ImmutableList.of()

      override def getParticleTexture: TextureAtlasSprite = atlas
    }
  }

  lazy val enchantedModel = createModelForTexture(enchantedTextureName)
  lazy val emptyModel = createModelForTexture(emptyTextureName)

  override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] = ImmutableList.of()

  override def getOverrides: ItemOverrideList = new ItemOverrideList(ImmutableList.of()) {
    //Minecraft.getMinecraft.getRenderItem.renderItemOverlayIntoGUI()
    override def handleItemState(originalModel: IBakedModel, stack: ItemStack, world: World, entity: EntityLivingBase): IBakedModel = {
      val xp = CapabilityXPContainer(stack).getXp
      if (xp > 0)
        enchantedModel
      else
        emptyModel
    }
  }
}
