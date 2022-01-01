package hohserg.soulkeeper.blocks

import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemBlock}

trait ItemBlockProvider {
  this: Block =>

  def getItemBlock: Item

}
