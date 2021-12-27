package hohserg.soulkeeper

import hohserg.soulkeeper.proxy.CommonProxy
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Mod, SidedProxy}

@Mod(modid = Main.modid, name = "Soulkeeper", modLanguage = "scala", dependencies = "required-after:codechickenlib")
object Main {
  final val modid = "soulkeeper"

  @SidedProxy(serverSide = "hohserg.soulkeeper.proxy.CommonProxy", clientSide = "hohserg.soulkeeper.proxy.ClientProxy")
  var proxy: CommonProxy = _

  final val debugMode = true


  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    proxy.preInit(event)
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    proxy.init(event)
  }

  @EventHandler
  def postInit(event: FMLPostInitializationEvent): Unit = {
    proxy.postInit(event)
  }

}
