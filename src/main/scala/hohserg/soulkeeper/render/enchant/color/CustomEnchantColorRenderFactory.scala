package hohserg.soulkeeper.render.enchant.color

import hohserg.soulkeeper.utils.ByteClassLoader
import hohserg.soulkeeper.utils.ByteClassLoader._
import net.minecraft.client.renderer.RenderItem
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.entity.layers.LayerArmorBase
import net.minecraft.item.ItemStack
import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.objectweb.asm.tree._

import scala.collection.JavaConverters._

object CustomEnchantColorRenderFactory {

  var instance: ICustomEnchantColorRender = _

  def init(cl: ByteClassLoader)(renderItem: RenderItem): Unit =
    instance = cl.findClass(className)
      .getDeclaredConstructor(classOf[RenderItem])
      .newInstance(renderItem).asInstanceOf[ICustomEnchantColorRender]

  val className = "hohserg.soulkeeper.render.enchant.color.CustomEnchantColorRender"

  val renderItemFieldName = "baseRenderItem"
  val colorFieldName = "color"

  def bytes(): (String, Array[Byte]) = {
    val cn = template

    cn.name = canonical(className)
    cn.interfaces.add(canonical(classOf[ICustomEnchantColorRender].getName))
    cn.methods.clear()

    addFields(cn)

    cn.methods.add(getConstructor(cn))

    getRenderItemMethods(cn).foreach(cn.methods.add)

    getShinyGlintMethods(cn).foreach(cn.methods.add)

    className -> toBytes(cn)
  }

  private def addFields(cn: ClassNode) = {
    cn.fields.add(new FieldNode(ACC_PRIVATE, renderItemFieldName, Type.getDescriptor(classOf[RenderItem]), null, null))

    cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, colorFieldName, Type.INT_TYPE.getDescriptor, null, null))

    cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, "r", Type.FLOAT_TYPE.getDescriptor, null, null))
    cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, "g", Type.FLOAT_TYPE.getDescriptor, null, null))
    cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, "b", Type.FLOAT_TYPE.getDescriptor, null, null))
  }

  private def getConstructor(cn: ClassNode) = {
    val constructor = new MethodNode(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[RenderItem])), null, null)
    constructor.instructions = new InsnList
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false))
    constructor.instructions.add(new VarInsnNode(ALOAD, 0))
    constructor.instructions.add(new VarInsnNode(ALOAD, 1))
    constructor.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, renderItemFieldName, Type.getDescriptor(classOf[RenderItem])))
    constructor.instructions.add(new InsnNode(RETURN))
    constructor
  }

  def getRenderItemMethods(cn: ClassNode): Seq[MethodNode] = {

    val RenderItemCN = toClassNode(classOf[RenderItem])

    val renderEffectMN = RenderItemCN.methods.asScala.find(mn => mn.name == "renderEffect" || mn.name == "func_191966_a").get
    renderEffectMN.instructions =
      renderEffectMN.instructions.flatMap {
        case insnMethod: MethodInsnNode if insnMethod.name == "renderModel" || insnMethod.name == "func_191965_a" =>
          Seq(
            new InsnNode(POP),
            new InsnNode(POP),
            new InsnNode(POP),
            new VarInsnNode(ALOAD, 0),
            new FieldInsnNode(GETFIELD, cn.name, renderItemFieldName, Type.getDescriptor(classOf[RenderItem])),
            new VarInsnNode(ALOAD, 1),
            new FieldInsnNode(GETSTATIC, cn.name, colorFieldName, Type.INT_TYPE.getDescriptor),
            insnMethod
          )
        case insnField: FieldInsnNode if insnField.name == "textureManager" || insnField.name == "field_175057_n" =>
          Seq(
            new FieldInsnNode(GETFIELD, cn.name, renderItemFieldName, Type.getDescriptor(classOf[RenderItem])),
            insnField
          )
        case other => Seq(other)
      }

    val renderItemMN = RenderItemCN.methods.asScala.find(mn => (mn.name == "renderItem" || mn.name == "func_180454_a") && mn.desc == Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[ItemStack]), Type.getType(classOf[IBakedModel]))).get
    renderItemMN.name = "renderShinyItem"
    renderItemMN.desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[ItemStack]), Type.getType(classOf[IBakedModel]), Type.INT_TYPE)
    renderItemMN.instructions =
      renderItemMN.instructions.map {
        case insnMethod: MethodInsnNode if insnMethod.owner == Type.getInternalName(classOf[RenderItem]) && insnMethod.getOpcode == INVOKESPECIAL =>
          new MethodInsnNode(INVOKEVIRTUAL, insnMethod.owner, insnMethod.name, insnMethod.desc, insnMethod.itf)
        case other => other
      }.flatMap {
        case insnMethod: MethodInsnNode if insnMethod.name == "renderModel" || insnMethod.name == "func_191961_a" =>
          Seq(
            new InsnNode(POP),
            new InsnNode(POP),
            new InsnNode(POP),
            new VarInsnNode(ALOAD, 0),
            new FieldInsnNode(GETFIELD, cn.name, renderItemFieldName, Type.getDescriptor(classOf[RenderItem])),
            new VarInsnNode(ALOAD, 2),
            new VarInsnNode(ALOAD, 1),
            insnMethod
          )
        case insnMethod: MethodInsnNode if insnMethod.name == renderEffectMN.name =>
          Seq(
            new VarInsnNode(ILOAD, 3),
            new FieldInsnNode(PUTSTATIC, cn.name, colorFieldName, Type.INT_TYPE.getDescriptor),
            new MethodInsnNode(INVOKESPECIAL, cn.name, renderEffectMN.name, renderEffectMN.desc, false)
          )
        case other => Seq(other)
      }

    Seq(renderEffectMN, renderItemMN)
  }

  def getShinyGlintMethods(cn: ClassNode): Seq[MethodNode] = {

    val LayerArmorBaseCN = toClassNode(classOf[LayerArmorBase[_]])
    val renderEnchantedGlintMN = LayerArmorBaseCN.methods.asScala.find(mn => mn.name == "renderEnchantedGlint" || mn.name == "func_188364_a").get
    renderEnchantedGlintMN.instructions =
      renderEnchantedGlintMN.instructions.flatMap {
        case insnMethod: MethodInsnNode if insnMethod.name == "color" || insnMethod.name == "func_179131_c" =>
          Seq(
            new InsnNode(POP),
            new InsnNode(POP),
            new InsnNode(POP),
            new InsnNode(POP),
            new FieldInsnNode(GETSTATIC, cn.name, "r", Type.FLOAT_TYPE.getDescriptor),
            new FieldInsnNode(GETSTATIC, cn.name, "g", Type.FLOAT_TYPE.getDescriptor),
            new FieldInsnNode(GETSTATIC, cn.name, "b", Type.FLOAT_TYPE.getDescriptor),
            new InsnNode(FCONST_1),
            insnMethod
          )
        case other => Seq(other)
      }

    val renderShinyGlintMN = new MethodNode(ACC_PUBLIC, "renderShinyGlint", Type.getMethodDescriptor(classOf[ICustomEnchantColorRender].getDeclaredMethods.find(m => m.getName == "renderShinyGlint").get), null, null)
    renderShinyGlintMN.instructions.add(new VarInsnNode(FLOAD, 11))
    renderShinyGlintMN.instructions.add(new FieldInsnNode(PUTSTATIC, cn.name, "r", Type.FLOAT_TYPE.getDescriptor))
    renderShinyGlintMN.instructions.add(new VarInsnNode(FLOAD, 12))
    renderShinyGlintMN.instructions.add(new FieldInsnNode(PUTSTATIC, cn.name, "g", Type.FLOAT_TYPE.getDescriptor))
    renderShinyGlintMN.instructions.add(new VarInsnNode(FLOAD, 13))
    renderShinyGlintMN.instructions.add(new FieldInsnNode(PUTSTATIC, cn.name, "b", Type.FLOAT_TYPE.getDescriptor))
    for (i <- 1 to 3)
      renderShinyGlintMN.instructions.add(new VarInsnNode(ALOAD, i))
    for (i <- 4 to 10)
      renderShinyGlintMN.instructions.add(new VarInsnNode(FLOAD, i))
    renderShinyGlintMN.instructions.add(new MethodInsnNode(INVOKESTATIC, cn.name, renderEnchantedGlintMN.name, renderEnchantedGlintMN.desc, false))
    renderShinyGlintMN.instructions.add(new InsnNode(RETURN))

    Seq(renderShinyGlintMN, renderEnchantedGlintMN)
  }
}
