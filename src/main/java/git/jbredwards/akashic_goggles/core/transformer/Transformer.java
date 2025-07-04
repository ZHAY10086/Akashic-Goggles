package git.jbredwards.akashic_goggles.core.transformer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
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
            // New: ASMHooks.registerGoggles(this)
            builder.put(map("net/minecraft/item/ItemArmor <init>(Lnet/minecraft/item/ItemArmor$ArmorMaterial;ILnet/minecraft/inventory/EntityEquipmentSlot;)V"), (instructions, insn) -> {
                instructions.insert(insn, hook("registerGoggles", "(Lnet/minecraft/item/Item;)V"));
                instructions.insert(insn, new VarInsnNode(ALOAD, 0));
            });
        });
    }

    private static void addMapping(@Nonnull final String targetMethod, @Nonnull final Consumer<ImmutableMap.Builder<ASMPredicate, ASMConsumer>> mapper) {
        @Nonnull final ImmutableMap.Builder<ASMPredicate, ASMConsumer> builder = ImmutableMap.builder();
        mapper.accept(builder);

        @Nonnull final String[] mapping = targetMethod.split(" ");
        MAPPINGS.computeIfAbsent(mapping[0].replace('/', '.'), key -> new HashMap<>()).put(mapping[1], new HashMap<>(builder.build()));
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
                actionMappings.entrySet().stream().filter(e -> e.getKey().test(method, insn)).forEach(e -> e.getValue().accept(method.instructions, insn));
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
        return (method, insn) -> {
            if(!(insn instanceof VarInsnNode)) return false;
            @Nonnull final OptionalInt index = method.localVariables.stream()
                    .filter(local -> local.name.equals(var))
                    .mapToInt(local -> local.index).findFirst();
            return index.isPresent() && ((VarInsnNode)insn).var == index.getAsInt();
        };
    }

    // -----------
    // ASM Utility
    // -----------

    @Nonnull
    private static MethodInsnNode hook(@Nonnull final String name, @Nonnull final String desc) {
        return new MethodInsnNode(INVOKESTATIC, "git/jbredwards/akashic_goggles/core/ASMHooks", name, desc, false);
    }
}
