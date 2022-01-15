package hohserg.soulkeeper.render.enchant.color

import hohserg.soulkeeper.utils.ByteClassLoader
import hohserg.soulkeeper.utils.ByteClassLoader._
import net.minecraft.client.model.ModelElytra
import net.minecraft.client.renderer.entity.RenderLivingBase
import net.minecraft.client.renderer.entity.layers.LayerElytra
import net.minecraft.inventory.EntityEquipmentSlot
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type
import org.objectweb.asm.tree._

import scala.collection.JavaConverters._

object CustomLayerElytraFactory {
  def create(cl: ByteClassLoader)(render: RenderLivingBase[_], layer: LayerElytra): LayerElytra =
    cl.findClass(className)
      .getDeclaredConstructor(classOf[RenderLivingBase[_]], classOf[LayerElytra])
      .newInstance(render, layer).asInstanceOf[LayerElytra]

  val className = "hohserg.soulkeeper.render.enchant.color.CustomLayerElytra"

  def bytes(): (String, Array[Byte]) = {
    val cn = template
    cn.name = canonical(className)
    cn.superName = canonical(classOf[LayerElytra].getName)
    cn.methods.clear()

    val LayerElytraCN = toClassNode(classOf[LayerElytra])

    val doRenderLayerMN = LayerElytraCN.methods.asScala.find(mn => mn.name == "doRenderLayer" || mn.name == "func_177141_a").get
    doRenderLayerMN.instructions =
      doRenderLayerMN.instructions
        .flatMap {
          case insnMethod: MethodInsnNode if insnMethod.name == "renderEnchantedGlint" || insnMethod.name == "func_188364_a" =>
            Seq(
              new FieldInsnNode(GETSTATIC, Type.getInternalName(classOf[EntityEquipmentSlot]), "CHEST", Type.getDescriptor(classOf[EntityEquipmentSlot])),
              new MethodInsnNode(
                INVOKESTATIC,
                canonical(classOf[CustomEnchantColor].getName),
                "renderEnchGlint",
                Type.getMethodDescriptor(classOf[CustomEnchantColor].getDeclaredMethods.find(m => m.getName == "renderEnchGlint").get),
                false)
            )
          case insnNode =>
            Seq(insnNode)
        }
    cn.methods.add(doRenderLayerMN)


    val constructor = new MethodNode(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[RenderLivingBase[_]]), Type.getType(classOf[LayerElytra])), null, null)
    constructor.instructions = new InsnList
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new VarInsnNode(ALOAD, 1))
    constructor.instructions.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(classOf[LayerElytra]), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[RenderLivingBase[_]])), false))
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new VarInsnNode(ALOAD, 2))
    constructor.instructions.add(new FieldInsnNode(GETFIELD, Type.getInternalName(classOf[LayerElytra]), choiceObf("modelElytra", "field_188357_c"), Type.getDescriptor(classOf[ModelElytra])))
    constructor.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, choiceObf("modelElytra", "field_188357_c"), Type.getDescriptor(classOf[ModelElytra])))
    constructor.instructions.add(new InsnNode(RETURN))
    cn.methods.add(constructor)

    className -> toBytes(cn)
  }
}
