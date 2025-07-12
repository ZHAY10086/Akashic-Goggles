package git.jbredwards.akashic_goggles.core.transformer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.launchwrapper.IClassTransformer;
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
        {
            addMapping("vazkii/arl/util/DropInHandler onRightClick(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V", builder -> {
                // Slot.getSlotIndex() is usually wrong, at least when using that value to find a slot.
                // Old: new MessageDropIn(under.getSlotIndex(), held)
                // New: new MessageDropIn(ASKHooks.getSlotIndex(under), held)
                builder.put(map("net/minecraft/inventory/Slot getSlotIndex()I"), (instructions, insn) -> {
                    instructions.insert(insn, hook("getSlotIndex", "(Lnet/minecraft/inventory/Slot;)I"));
                    instructions.remove(insn);
                });
            });
        }
        // ===========
        // BiblioCraft
        // ===========
        {
            addSupport("jds/bibliocraft/items/ItemReadingGlasses <init>()V", ASMConsumer.identity());
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
        }
        // =======
        // Botania
        // =======
        {
            addSupport("vazkii/botania/common/item/equipment/bauble/ItemMonocle <init>()V", ASMConsumer.identity());
            addOverwrite("vazkii/botania/common/item/equipment/bauble/ItemMonocle hasMonocle(Lnet/minecraft/entity/player/EntityPlayer;)Z", adapter -> {
                // Account for Akashic Goggles.
                // Old: { ... }
                // New: { return ASMHooks.hasMonocle(player) }
                adapter.visitVarInsn(ALOAD, 0);
                hook(adapter, "hasMonocle", "(Lnet/minecraft/entity/player/EntityPlayer;)Z");
            });
        }
        // ======
        // Embers
        // ======
        {
            addSupport("teamroots/embers/item/ItemAshenCloak <init>(Lnet/minecraft/item/ItemArmor$ArmorMaterial;ILnet/minecraft/inventory/EntityEquipmentSlot;)V",
            addMethod("canDropInAkashic", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", adapter -> {
                // Only allow goggles to be dropped into Akashic Goggles.
                // Old: N/A
                // New: { return ASMHooks.isHelmet(stack) }
                adapter.visitVarInsn(ALOAD, 2);
                hook(adapter, "isHelmet", "(Lnet/minecraft/item/ItemStack;)Z");
            }));
            addOverwrite("teamroots/embers/proxy/ClientProxy isGoggles(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/inventory/EntityEquipmentSlot;)Z", adapter -> {
                // Account for Akashic Googles and Baubles slots.
                // Old: { ... }
                // New: { return ASMHooks.isGoggles(player, slot) }
                adapter.visitVarInsn(ALOAD, 1);
                adapter.visitVarInsn(ALOAD, 2);
                hook(adapter, "isGoggles", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/inventory/EntityEquipmentSlot;)Z");
            });
        }
        // =============
        // Nature's Aura
        // =============
        {
            addSupport("de/ellpeck/naturesaura/items/ItemEye <init>(Ljava/lang/String;)V", ASMConsumer.identity());
            addMapping("de/ellpeck/naturesaura/events/ClientEvents onClientTick(Lnet/minecraftforge/fml/common/gameevent/TickEvent$ClientTickEvent;)V", builder -> {
                // Account for Akashic Googles.
                // Old: if (Compat.baubles)
                // New: if (ASMHooks.getEyes(Compat.baubles))
                builder.put(map("de/ellpeck/naturesaura/compat/Compat baubles"), (instructions, insn) -> {
                    instructions.insert(insn, hook("getEyes", "(Z)Z"));
                });
            });
        }
        // =========
        // Railcraft
        // =========
        {
            addSupport("mods/railcraft/common/items/ItemGoggles <init>()V",
            addMethod("compareDuringAkashicDropIn", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", adapter -> {
                // Allow Akashic Goggles to hold multiple railman goggles, if they have different auras.
                // Old: N/A
                // New: { return ASMHooks.isSameAura(stack, other) }
                adapter.visitVarInsn(ALOAD, 2);
                adapter.visitVarInsn(ALOAD, 3);
                hook(adapter, "isSameAura", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z");
            }));
            addMapping("mods/railcraft/client/render/world/GoggleAuraWorldRenderer onWorldRender(Lnet/minecraftforge/client/event/RenderWorldLastEvent;)V", builder -> {
                // Check for specific GoggleAura, so the player may wear multiple goggles with different auras at once.
                // Old: if (ItemGoggles.isPlayerWearing(entityPlayerSP))
                // New: if (ASMHooks.isPlayerWearing(entityPlayerSP, ItemGoggles.GoggleAura.SHUNTING))
                builder.put(map("mods/railcraft/common/items/ItemGoggles isPlayerWearing(Lnet/minecraft/entity/player/EntityPlayer;)Z"), (instructions, insn) -> {
                    instructions.insert(insn, hook("isPlayerWearing", "(Lnet/minecraft/entity/player/EntityPlayer;Lmods/railcraft/common/items/ItemGoggles$GoggleAura;)Z"));
                    instructions.insert(insn, new FieldInsnNode(GETSTATIC, "mods/railcraft/common/items/ItemGoggles$GoggleAura", "SHUNTING", "Lmods/railcraft/common/items/ItemGoggles$GoggleAura;"));
                    instructions.remove(insn);
                });
                // No longer needed.
                // Old: ItemStack goggles = ItemGoggles.getGoggles(entityPlayerSP)
                // New: ItemStack goggles = ItemStack.EMPTY
                builder.put(map("mods/railcraft/common/items/ItemGoggles getGoggles(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), (instructions, insn) -> {
                    instructions.insert(insn, new FieldInsnNode(GETSTATIC, "net/minecraft/item/ItemStack", obfuscate("EMPTY", "field_190927_a"), "Lnet/minecraft/item/ItemStack;"));
                    instructions.remove(insn.getPrevious());
                    instructions.remove(insn);
                });
                // No longer needed.
                // Old: ItemGoggles.GoggleAura aura = ItemGoggles.getCurrentAura(goggles)
                // New: ItemGoggles.GoggleAura aura = ItemGoggles.GoggleAura.SHUNTING
                builder.put(map("mods/railcraft/common/items/ItemGoggles getCurrentAura(Lnet/minecraft/item/ItemStack;)Lmods/railcraft/common/items/ItemGoggles$GoggleAura;"), (instructions, insn) -> {
                    instructions.insert(insn, new FieldInsnNode(GETSTATIC, "mods/railcraft/common/items/ItemGoggles$GoggleAura", "SHUNTING", "Lmods/railcraft/common/items/ItemGoggles$GoggleAura;"));
                    instructions.remove(insn.getPrevious());
                    instructions.remove(insn);
                });
            });
            addOverwrite("mods/railcraft/common/items/ItemGoggles getGoggles(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", adapter -> {
                // Check for specific GoggleAura, so the player may wear multiple goggles with different auras at once.
                // Old: { ... }
                // New: { return ASMHooks.isGoggleAuraActive(aura) }
                adapter.visitVarInsn(ALOAD, 0);
                hook(adapter, "isGoggleAuraActive", "(Lmods/railcraft/common/items/ItemGoggles$GoggleAura;)Z");
            });
            addMapping("mods/railcraft/common/carts/ShuntingAuraTickHandler tick(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingUpdateEvent;)V", builder -> {
                // Check for specific GoggleAura, so the player may wear multiple goggles with different auras at once.
                // Old: if (ItemGoggles.isPlayerWearing(player))
                // New: if (ASMHooks.isPlayerWearing(player, ItemGoggles.GoggleAura.SHUNTING))
                builder.put(map("mods/railcraft/common/items/ItemGoggles isPlayerWearing(Lnet/minecraft/entity/player/EntityPlayer;)Z"), (instructions, insn) -> {
                    instructions.insert(insn, hook("isPlayerWearing", "(Lnet/minecraft/entity/player/EntityPlayer;Lmods/railcraft/common/items/ItemGoggles$GoggleAura;)Z"));
                    instructions.insert(insn, new FieldInsnNode(GETSTATIC, "mods/railcraft/common/items/ItemGoggles$GoggleAura", "SHUNTING", "Lmods/railcraft/common/items/ItemGoggles$GoggleAura;"));
                    instructions.remove(insn);
                });
                // No longer needed.
                // Old: ItemStack goggles = ItemGoggles.getGoggles(player)
                // New: ItemStack goggles = ItemStack.EMPTY
                builder.put(map("mods/railcraft/common/items/ItemGoggles getGoggles(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), (instructions, insn) -> {
                    instructions.insert(insn, new FieldInsnNode(GETSTATIC, "net/minecraft/item/ItemStack", obfuscate("EMPTY", "field_190927_a"), "Lnet/minecraft/item/ItemStack;"));
                    instructions.remove(insn.getPrevious());
                    instructions.remove(insn);
                });
                // No longer needed.
                // Old: ItemGoggles.GoggleAura aura = ItemGoggles.getCurrentAura(goggles)
                // New: ItemGoggles.GoggleAura aura = ItemGoggles.GoggleAura.SHUNTING
                builder.put(map("mods/railcraft/common/items/ItemGoggles getCurrentAura(Lnet/minecraft/item/ItemStack;)Lmods/railcraft/common/items/ItemGoggles$GoggleAura;"), (instructions, insn) -> {
                    instructions.insert(insn, new FieldInsnNode(GETSTATIC, "mods/railcraft/common/items/ItemGoggles$GoggleAura", "SHUNTING", "Lmods/railcraft/common/items/ItemGoggles$GoggleAura;"));
                    instructions.remove(insn.getPrevious());
                    instructions.remove(insn);
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
        // ----------
        // Thaumcraft
        // ----------
        {
            addSupport("thaumcraft/common/items/armor/ItemGoggles <init>()V", ASMConsumer.identity());
        }
    }

    private static void addMapping(@Nonnull final String targetMethod, @Nonnull final Consumer<ImmutableMap.Builder<ASMPredicate, ASMConsumer>> mapper) {
        @Nonnull final ImmutableMap.Builder<ASMPredicate, ASMConsumer> builder = ImmutableMap.builder();
        mapper.accept(builder);

        @Nonnull final String[] mapping = targetMethod.split(" ");
        MAPPINGS.computeIfAbsent(mapping[0].replace('/', '.'), key -> new HashMap<>()).put(mapping[1], new HashMap<>(builder.build()));
    }

    @Nonnull
    private static ASMConsumer addMethod(@Nonnull final String name, @Nonnull final String desc, @Nonnull final Consumer<GeneratorAdapter> generator) {
        return ASMConsumer.advanced((classNode, unused, insn) -> {
            @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
            classNode.methods.add(method);

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            generator.accept(adapter);
            adapter.returnValue();
            adapter.endMethod();
        });
    }

    private static void addOverwrite(@Nonnull final String targetMethod, @Nonnull final Consumer<GeneratorAdapter> generator) {
        addMapping(targetMethod, builder -> {
            builder.put((method, insn) -> method.instructions.getFirst() == insn,
                    ASMConsumer.advanced((classNode, method, insn) -> {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables.clear();

                @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
                generator.accept(adapter);
                adapter.returnValue();
                adapter.endMethod();
            }));
        });
    }

    private static void addSupport(@Nonnull final String targetMethod, @Nonnull final ASMConsumer consumer) {
        addMapping(targetMethod, builder -> {
            builder.put((method, insn) -> method.instructions.getFirst() == insn,
                    ASMConsumer.advanced((classNode, method, insn) -> {
                classNode.interfaces.add("git/jbredwards/akashic_goggles/api/IAkashicGoggles");
                consumer.accept(classNode, method, insn);
            }));
        });
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
                actionMappings.entrySet().stream().filter(e -> e.getKey().test(method, insn)).forEach(e -> e.getValue().accept(classNode, method, insn));
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
        return map(String.format(key, obfuscate(deobfName, obfName)));
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

    @Nonnull
    private static String obfuscate(@Nonnull final String deobfName, @Nonnull final String obfName) {
        return /*FMLLaunchHandler.isDeobfuscatedEnvironment() ? deobfName :*/ obfName;
    }
}
