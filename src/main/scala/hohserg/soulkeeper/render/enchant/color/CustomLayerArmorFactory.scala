package hohserg.soulkeeper.render.enchant.color

import hohserg.soulkeeper.utils.ByteClassLoader
import hohserg.soulkeeper.utils.ByteClassLoader._
import net.minecraft.client.model.ModelBase
import net.minecraft.client.renderer.entity.RenderLivingBase
import net.minecraft.client.renderer.entity.layers.{LayerArmorBase, LayerBipedArmor}
import net.minecraft.entity.EntityLivingBase
import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.objectweb.asm.tree._

import scala.collection.JavaConverters._

object CustomLayerArmorFactory {
  def create(cl: ByteClassLoader)(render: RenderLivingBase[EntityLivingBase], layer: LayerBipedArmor): LayerBipedArmor =
    cl.findClass(className)
      .getDeclaredConstructor(classOf[RenderLivingBase[_]], classOf[LayerBipedArmor])
      .newInstance(render, layer).asInstanceOf[LayerBipedArmor]


  val className = "hohserg.soulkeeper.render.enchant.color.CustomLayerArmor"

  def bytes(): (String, Array[Byte]) = {
    val cn = template
    cn.name = canonical(className)
    cn.superName = canonical(classOf[LayerBipedArmor].getName)
    cn.methods.clear()

    val LayerArmorBaseCN = toClassNode(classOf[LayerArmorBase[_]])

    val renderArmorLayerMN = LayerArmorBaseCN.methods.asScala.find(mn => mn.name == "renderArmorLayer" || mn.name == "func_188361_a").get

    renderArmorLayerMN.instructions =
      renderArmorLayerMN.instructions
        .flatMap {
          case insnMethod: MethodInsnNode =>
            if (insnMethod.name == "isLegSlot" || insnMethod.name == "func_188363_b") {
              Seq(
                new InsnNode(POP),
                new InsnNode(POP),
                new InsnNode(ICONST_1)
              )
            } else if (insnMethod.name == "renderEnchantedGlint" || insnMethod.name == "func_188364_a") {
              Seq(
                new VarInsnNode(ALOAD, 9),
                new MethodInsnNode(
                  INVOKESTATIC,
                  canonical(classOf[CustomEnchantColor].getName),
                  "renderEnchGlint",
                  Type.getMethodDescriptor(classOf[CustomEnchantColor].getDeclaredMethods.find(m => m.getName == "renderEnchGlint").get),
                  false)
              )
            } else
              Seq(insnMethod)
          case insnNode =>
            Seq(insnNode)
        }
    cn.methods.add(renderArmorLayerMN)

    val doRenderLayerMN = LayerArmorBaseCN.methods.asScala.find(mn => mn.name == "doRenderLayer" || mn.name == "func_177141_a").get
    doRenderLayerMN.instructions =
      doRenderLayerMN.instructions
        .map {
          case insnMethod: MethodInsnNode =>
            new MethodInsnNode(insnMethod.getOpcode, cn.name, insnMethod.name, insnMethod.desc, insnMethod.itf)
          case insnNode =>
            insnNode
        }
    cn.methods.add(doRenderLayerMN)

    val constructor = new MethodNode(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[RenderLivingBase[_]]), Type.getType(classOf[LayerBipedArmor])), null, null)
    constructor.instructions = new InsnList
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new VarInsnNode(ALOAD, 1))
    constructor.instructions.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(classOf[LayerBipedArmor]), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[RenderLivingBase[_]])), false))
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new VarInsnNode(ALOAD, 2))
    constructor.instructions.add(new FieldInsnNode(GETFIELD, Type.getInternalName(classOf[LayerArmorBase[_]]), ByteClassLoader.choiceObf("modelLeggings", "field_177189_c"), Type.getDescriptor(classOf[ModelBase])))
    constructor.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, ByteClassLoader.choiceObf("modelLeggings", "field_177189_c"), Type.getDescriptor(classOf[ModelBase])))
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new VarInsnNode(ALOAD, 2))
    constructor.instructions.add(new FieldInsnNode(GETFIELD, Type.getInternalName(classOf[LayerArmorBase[_]]), ByteClassLoader.choiceObf("modelArmor", "field_177186_d"), Type.getDescriptor(classOf[ModelBase])))
    constructor.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, ByteClassLoader.choiceObf("modelArmor", "field_177186_d"), Type.getDescriptor(classOf[ModelBase])))
    constructor.instructions.add(new InsnNode(RETURN))
    cn.methods.add(constructor)

    className -> toBytes(cn)
  }
}
