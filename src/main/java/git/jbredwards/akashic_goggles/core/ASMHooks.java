package git.jbredwards.akashic_goggles.core;

import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public final class ASMHooks
{
    public static final boolean HAS_BAUBLES = Loader.isModLoaded("baubles");

    @Nonnull
    public static ItemStack getMatchingGoggles(@Nonnull final EntityLivingBase entity, @Nonnull final Item target) {
        return AkashicGogglesUtil.getMatchingGoggles(entity, new ItemStack(target));
    }

    @Nonnull
    public static ItemStack getMatchingGogglesBaubles(@Nonnull final EntityLivingBase entity, @Nonnull final ItemStack target) {
        @Nullable final IBaublesItemHandler baubles = entity.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if(baubles != null) {
            baubles.setPlayer(entity);
            for(int slot = 0; slot < baubles.getSlots(); slot++) {
                @Nonnull final ItemStack bauble = baubles.getStackInSlot(slot);
                if(target.isItemEqualIgnoreDurability(bauble)) return bauble;

                @Nonnull final Optional<ItemStack> matching = AkashicGogglesUtil.getContainedGoggles(bauble).filter(target::isItemEqualIgnoreDurability).findFirst();
                if(matching.isPresent()) return matching.get();
            }
        }

        return ItemStack.EMPTY;
    }

    public static void registerGoggles(@Nonnull final Item item) {
        InventoryAkashicGoggles.SUPPORTED_GOGGLES.put(item, 0);
    }
}
