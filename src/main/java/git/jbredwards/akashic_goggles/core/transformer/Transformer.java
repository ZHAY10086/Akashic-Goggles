package git.jbredwards.akashic_goggles.core.transformer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 *
 * @author jbred
 *
 */
public final class Transformer implements IClassTransformer, Opcodes
{
    @Nonnull
    public static final Map<String, Map<String, Map<ASMPredicate, ASMConsumer>>> MAPPINGS = new HashMap<>();
    static {
        // ==========
        // AutoRegLib
        // ==========
        addMapping("vazkii/arl/util/DropInHandler onRightClick(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V", builder -> {
            // Slot.getSlotIndex() is usually wrong, at least when using that value to find a slot.
            // Old: new MessageDropIn(under.getSlotIndex(), held)
            // New: new MessageDropIn(ASKHooks.getSlotIndex(under), held)
            builder.put(map("net/minecraft/inventory/Slot getSlotIndex()I"), (instructions, insn) -> {
                instructions.insert(insn, hook("getSlotIndex", "(Lnet/minecraft/inventory/Slot;)I"));
                instructions.remove(insn);
            });
        });
        // ===========
        // BiblioCraft
        // ===========
        addMapping("jds/bibliocraft/events/EventBlockMarkerHighlight DrawBlockHighlightEvent(Lnet/minecraftforge/client/event/DrawBlockHighlightEvent;)V", builder -> {
            // Account for Akashic Googles and Baubles slots.
            // Old: ItemStack headArmor = event.getPlayer().inventory.armorItemInSlot(3)
            // New: ItemStack headArmor = ASMHooks.getReadingGlasses()
            builder.put(map("net/minecraft/entity/player/InventoryPlayer %s(I)Lnet/minecraft/item/ItemStack;", "armorItemInSlot", "func_70440_f"), (instructions, insn) -> {
                instructions.insert(insn, hook("getReadingGlasses", "()Lnet/minecraft/item/ItemStack;"));
                instructions.insert(insn, new InsnNode(POP2));
                instructions.remove(insn);
            });
            // This is now checked through the hooked method above. All that's needed here now is an !isEmpty check.
            // Old: if (EventBlockMarkerHighlight.canHeadArmorRead(headArmor))
            // New: if (ASMHooks.isNonEmpty(headArmor))
            builder.put(map("jds/bibliocraft/events/EventBlockMarkerHighlight canHeadArmorRead(Lnet/minecraft/item/ItemStack;)Z"), (instructions, insn) -> {
                instructions.insert(insn, hook("isNonEmpty", "(Lnet/minecraft/item/ItemStack;)Z"));
                instructions.remove(insn);
            });
            // Store text data in a static field, to prevent having to modify the stack on the client.
            // Old: NBTTagCompound armorTags = headArmor.getTagCompound()
            // New: NBTTagCompound armorTags = ASMHooks.CLIENT_READING_DATA
            builder.put(map("net/minecraft/item/ItemStack %s()Lnet/minecraft/nbt/NBTTagCompound;", "getTagCompound", "func_77978_p").andPrev(var("headArmor")), (instructions, insn) -> {
                instructions.insert(insn, new FieldInsnNode(GETSTATIC, "git/jbredwards/akashic_goggles/core/ASMHooks", "CLIENT_READING_DATA", "Lnet/minecraft/nbt/NBTTagCompound;"));
                instructions.remove(insn.getPrevious());
                instructions.remove(insn);
            });
            // Don't modify the stack on the client.
            // Old: headArmor.setTagCompound(armorTags)
            // New: N/A
            builder.put(map("net/minecraft/item/ItemStack %s(Lnet/minecraft/nbt/NBTTagCompound;)V", "setTagCompound", "func_77982_d"), (instructions, insn) -> {
                instructions.remove(insn.getPrevious());
                instructions.remove(insn.getPrevious());
                instructions.remove(insn);
            });
            // Don't modify the inventory on the client.
            // Old: event.getPlayer().inventory.armorInventory.set(3, headArmor)
            // New: N/A
            builder.put(map("net/minecraft/util/NonNullList set(ILjava/lang/Object;)Ljava/lang/Object;"), (instructions, insn) -> {
                instructions.remove(insn.getPrevious());
                instructions.remove(insn.getPrevious());
                instructions.remove(insn);
            });
        });
        addMapping("jds/bibliocraft/events/GuiBiblioOverlay RenderGameOverlayEvent(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$Post;)V", builder -> {
            // Account for Akashic Googles and Baubles slots.
            // Old: ItemStack headArmor = event.getPlayer().inventory.armorItemInSlot(3)
            // New: ItemStack headArmor = ASMHooks.getReadingGlasses()
            builder.put(map("net/minecraft/entity/player/InventoryPlayer %s(I)Lnet/minecraft/item/ItemStack;", "armorItemInSlot", "func_70440_f"), (instructions, insn) -> {
                instructions.insert(insn, hook("getReadingGlasses", "()Lnet/minecraft/item/ItemStack;"));
                instructions.insert(insn, new InsnNode(POP2));
                instructions.remove(insn);
            });
            // This is now checked through the hooked method above. All that's needed here now is an !isEmpty check.
            // Old: if (EventBlockMarkerHighlight.canHeadArmorRead(headArmor))
            // New: if (ASMHooks.isNonEmpty(headArmor))
            builder.put(map("jds/bibliocraft/events/EventBlockMarkerHighlight canHeadArmorRead(Lnet/minecraft/item/ItemStack;)Z"), (instructions, insn) -> {
                instructions.insert(insn, hook("isNonEmpty", "(Lnet/minecraft/item/ItemStack;)Z"));
                instructions.remove(insn);
            });
            // Store text data in a static field, to prevent having to modify the stack on the client.
            // Old: NBTTagCompound armorTags = headArmor.getTagCompound()
            // New: NBTTagCompound armorTags = ASMHooks.CLIENT_READING_DATA
            builder.put(map("net/minecraft/item/ItemStack %s()Lnet/minecraft/nbt/NBTTagCompound;", "getTagCompound", "func_77978_p").andPrev(var("headArmor")), (instructions, insn) -> {
                instructions.insert(insn, new FieldInsnNode(GETSTATIC, "git/jbredwards/akashic_goggles/core/ASMHooks", "CLIENT_READING_DATA", "Lnet/minecraft/nbt/NBTTagCompound;"));
                instructions.remove(insn.getPrevious());
                instructions.remove(insn);
            });
        });
        addMapping("jds/bibliocraft/items/ItemReadingGlasses <init>()V", builder -> {
            // Add all reading glasses to list of supported items for Akashic Goggles.
            // Old: N/A
            // New: ASMHooks.registerGlasses(this)
            builder.put(map("net/minecraft/item/ItemArmor <init>(Lnet/minecraft/item/ItemArmor$ArmorMaterial;ILnet/minecraft/inventory/EntityEquipmentSlot;)V"), (instructions, insn) -> {
                instructions.insert(insn, hook("registerGlasses", "(Lnet/minecraft/item/Item;)V"));
                instructions.insert(insn, new VarInsnNode(ALOAD, 0));
            });
        });
        // =======
        // Botania
        // =======
        addMapping("vazkii/botania/common/item/equipment/bauble/ItemMonocle <init>()V", builder -> {
            // Add all monocles to list of supported items for Akashic Goggles.
            // Old: N/A
            // New: ASMHooks.registerGoggles(this)
            builder.put(map("vazkii/botania/common/item/equipment/bauble/ItemBauble <init>(Ljava/lang/String;)V"), (instructions, insn) -> {
                instructions.insert(insn, hook("registerGoggles", "(Lnet/minecraft/item/Item;)V"));
                instructions.insert(insn, new VarInsnNode(ALOAD, 0));
            });
        });
        addOverwrite("vazkii/botania/common/item/equipment/bauble/ItemMonocle hasMonocle(Lnet/minecraft/entity/player/EntityPlayer;)Z", adapter -> {
            // Account for Akashic Goggles.
            // Old: { ... }
            // New: { return ASMHooks.hasMonocle(player) }
            adapter.visitVarInsn(ALOAD, 0);
            hook(adapter, "hasMonocle", "(Lnet/minecraft/entity/player/EntityPlayer;)Z");
        });
        // ======
        // Embers
        // ======
        addMapping("teamroots/embers/item/ItemAshenCloak <init>(Lnet/minecraft/item/ItemArmor$ArmorMaterial;ILnet/minecraft/inventory/EntityEquipmentSlot;)V", builder -> {
            // Add all ash goggles to list of supported items for Akashic Goggles.
            // Old: N/A
            // New: ASMHooks.registerGoggles(this)
            builder.put(map("teamroots/embers/item/ItemArmorBase <init>(Lnet/minecraft/item/ItemArmor$ArmorMaterial;ILnet/minecraft/inventory/EntityEquipmentSlot;Ljava/lang/String;Z)V"), (instructions, insn) -> {
                instructions.insert(insn, hook("registerGoggles", "(Lnet/minecraft/item/ItemArmor;)V"));
                instructions.insert(insn, new VarInsnNode(ALOAD, 0));
            });
        });
        addOverwrite("teamroots/embers/proxy/ClientProxy isGoggles(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/inventory/EntityEquipmentSlot;)Z", adapter -> {
            // Account for Akashic Googles and Baubles slots.
            // Old: { ... }
            // New: { return ASMHooks.isGoggles(player, slot) }
            adapter.visitVarInsn(ALOAD, 1);
            adapter.visitVarInsn(ALOAD, 2);
            hook(adapter, "isGoggles", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/inventory/EntityEquipmentSlot;)Z");
        });
        // =========
        // Railcraft
        // =========
        addMapping("mods/railcraft/common/items/ItemGoggles <init>()V", builder -> {
            // Add all railman goggles to list of supported items for Akashic Goggles.
            // Old: N/A
            // New: ASMHooks.registerGoggles(this)
            builder.put(map("mods/railcraft/common/items/ItemRailcraftArmor <init>(Lnet/minecraft/item/ItemArmor$ArmorMaterial;ILnet/minecraft/inventory/EntityEquipmentSlot;)V"), (instructions, insn) -> {
                instructions.insert(insn, hook("registerGoggles", "(Lnet/minecraft/item/ItemArmor;)V"));
                instructions.insert(insn, new VarInsnNode(ALOAD, 0));
            });
        });
        addOverwrite("mods/railcraft/common/items/ItemGoggles getGoggles(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", adapter -> {
            // Account for Akashic Googles and Baubles slots.
            // Old: { ... }
            // New: { return ASMHooks.getGoggles(player, null) }
            adapter.visitVarInsn(ALOAD, 0);
            adapter.visitInsn(ACONST_NULL);
            hook(adapter, "getGoggles", "(Lnet/minecraft/entity/player/EntityPlayer;Lmods/railcraft/common/items/ItemGoggles$GoggleAura;)Lnet/minecraft/item/ItemStack;");
        });
        addOverwrite("mods/railcraft/common/items/ItemGoggles isPlayerWearing(Lnet/minecraft/entity/player/EntityPlayer;)Z", adapter -> {
            // Account for Akashic Googles and Baubles slots.
            // Old: { ... }
            // New: { return ASMHooks.isPlayerWearing(player, null) }
            adapter.visitVarInsn(ALOAD, 0);
            adapter.visitInsn(ACONST_NULL);
            hook(adapter, "isPlayerWearing", "(Lnet/minecraft/entity/player/EntityPlayer;Lmods/railcraft/common/items/ItemGoggles$GoggleAura;)Z");
        });
    }

    private static void addMapping(@Nonnull final String targetMethod, @Nonnull final Consumer<ImmutableMap.Builder<ASMPredicate, ASMConsumer>> mapper) {
        @Nonnull final ImmutableMap.Builder<ASMPredicate, ASMConsumer> builder = ImmutableMap.builder();
        mapper.accept(builder);

        @Nonnull final String[] mapping = targetMethod.split(" ");
        MAPPINGS.computeIfAbsent(mapping[0].replace('/', '.'), key -> new HashMap<>()).put(mapping[1], new HashMap<>(builder.build()));
    }

    private static void addOverwrite(@Nonnull final String targetMethod, @Nonnull final Consumer<GeneratorAdapter> generator) {
        addMapping(targetMethod, builder -> builder.put((method, insn) -> method.instructions.getFirst() == insn, ASMConsumer.method((method, insn) -> {
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables.clear();

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            generator.accept(adapter);
            adapter.returnValue();
            adapter.endMethod();
        })));
    }

    @Nullable
    @Override
    public byte[] transform(@Nullable final String name, @Nullable final String transformedName, @Nullable final byte[] basicClass) {
        if(basicClass == null || transformedName == null) return basicClass;

        @Nullable final Map<String, Map<ASMPredicate, ASMConsumer>> methodMappings = MAPPINGS.get(transformedName);
        if(methodMappings == null) return basicClass;

        @Nonnull final ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        for(@Nonnull final MethodNode method : classNode.methods) {
            @Nullable final Map<ASMPredicate, ASMConsumer> actionMappings = methodMappings.get(method.name + method.desc);
            if(actionMappings != null) for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                actionMappings.entrySet().stream().filter(e -> e.getKey().test(method, insn)).forEach(e -> e.getValue().acceptMethod(method, insn));
            }
        }

        @Nonnull final ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    // -----------
    // ASM Targets
    // -----------

    @Nonnull
    private static ASMPredicate map(@Nonnull final String key, @Nonnull final String deobfName, @Nonnull final String obfName) {
        return map(String.format(key, /*FMLLaunchHandler.isDeobfuscatedEnvironment() ? deobfName :*/ obfName));
    }

    @Nonnull
    private static ASMPredicate map(@Nonnull final String key) {
        return (method, insn) -> {
            if(insn instanceof MethodInsnNode) return key.equals(((MethodInsnNode)insn).owner + ' ' + ((MethodInsnNode)insn).name + ((MethodInsnNode)insn).desc);
            else if(insn instanceof FieldInsnNode) return key.equals(((FieldInsnNode)insn).owner + ' ' + ((FieldInsnNode)insn).name);
            else return false;
        };
    }

    @Nonnull
    private static ASMPredicate var(@Nonnull final String var) {
        return new ASMPredicate() {
            @Nullable
            OptionalInt index;

            @Override
            public boolean test(@Nonnull final MethodNode method, @Nonnull final AbstractInsnNode insn) {
                if(index == null) index = method.localVariables.stream().filter(local -> local.name.equals(var)).mapToInt(local -> local.index).findFirst();
                return index.isPresent() && insn instanceof VarInsnNode && ((VarInsnNode)insn).var == index.getAsInt();
            }
        };
    }

    // -----------
    // ASM Utility
    // -----------

    @Nonnull
    private static MethodInsnNode hook(@Nonnull final String name, @Nonnull final String desc) {
        return new MethodInsnNode(INVOKESTATIC, "git/jbredwards/akashic_goggles/core/ASMHooks", name, desc, false);
    }

    private static void hook(@Nonnull final MethodVisitor visitor, @Nonnull final String name, @Nonnull final String desc) {
        visitor.visitMethodInsn(INVOKESTATIC, "git/jbredwards/akashic_goggles/core/ASMHooks", name, desc, false);
    }
}
