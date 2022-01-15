package hohserg.soulkeeper.utils

import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.common.asm.transformers._
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.objectweb.asm.tree._

import scala.collection.JavaConverters._
import scala.language.implicitConversions

class ByteClassLoader(classes: Map[String, Array[Byte]]) extends ClassLoader(ByteClassLoader.getClass.getClassLoader) {
  private val cached = classes.map { case (name, a) => name -> new LazyRef[Class[_]](defineClass(name, a, 0, a.length)) }

  override def findClass(name: String): Class[_] = {
    cached.get(name)
      .map(_.value)
      .getOrElse(super.findClass(name))
  }
}

object ByteClassLoader {

  val obfClassNames = Map(
    "net.minecraft.client.renderer.RenderItem" -> "bzw",
    "net.minecraft.client.renderer.entity.layers.LayerArmorBase" -> "cbp",
    "net.minecraft.client.renderer.entity.layers.LayerElytra" -> "cbw"
  )

  def getBytesOfExistingClass(cl: Class[_]): Array[Byte] = {
    val loader = ByteClassLoader.getClass.getClassLoader.asInstanceOf[LaunchClassLoader]

    val deobfName = cl.getName
    val obfName = obfClassNames.getOrElse(deobfName, deobfName)
    if (FMLLaunchHandler.isDeobfuscatedEnvironment)
      loader.getClassBytes(deobfName)
    else
      loader.getTransformers.asScala
        .filter(t => t.isInstanceOf[PatchingTransformer] || t.isInstanceOf[SideTransformer] || t.isInstanceOf[DeobfuscationTransformer] || t.isInstanceOf[AccessTransformer] || t.isInstanceOf[ModAccessTransformer])
        .foldLeft(
          loader.getClassBytes(obfName)
        ) { case (b, t) => t.transform(obfName, deobfName, b) }
  }

  def toClassNode(cl: Class[_]): ClassNode = {
    val cn = new ClassNode(ASM5)
    new ClassReader(getBytesOfExistingClass(cl))
      .accept(cn, ClassReader.EXPAND_FRAMES)
    cn
  }

  def template: ClassNode = {
    val cn = new ClassNode(ASM5)
    cn.version = V1_8
    cn.access = ACC_PUBLIC | ACC_SUPER
    cn.superName = "java/lang/Object"
    cn
  }

  def toBytes(cn: ClassNode): Array[Byte] = {
    val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
    cn.accept(cw)
    cw.toByteArray
  }

  def canonical(className: String): String = className.replace('.', '/')

  implicit def insnList2Seq(l: InsnList): Seq[AbstractInsnNode] =
    l.toArray.toSeq

  implicit def seq2InsnList(s: Seq[AbstractInsnNode]): InsnList =
    s.foldLeft(new InsnList) { case (list, insn) => list.add(insn); list }

  def choiceObf(deobf: String, obf: String): String =
    if (FMLLaunchHandler.isDeobfuscatedEnvironment) deobf else obf
}
