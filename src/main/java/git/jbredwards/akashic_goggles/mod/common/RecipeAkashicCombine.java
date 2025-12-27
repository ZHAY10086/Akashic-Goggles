package git.jbredwards.akashic_goggles.mod.common;

import git.jbredwards.akashic_goggles.mod.AkashicGoggles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.GameData;
import vazkii.arl.interf.IDropInItem;
import vazkii.arl.recipe.ModRecipe;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
public class RecipeAkashicCombine extends ModRecipe
{
    public RecipeAkashicCombine() {
        super(GameData.checkPrefix("combine", true));
    }

    @Override
    public boolean matches(@Nonnull final InventoryCrafting inv, @Nonnull final World worldIn) {
        return !getCraftingResult(inv).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
        int gogglesSlot = -1;

        @Nonnull ItemStack goggles = ItemStack.EMPTY;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(inv.getStackInSlot(i).getItem() == AkashicGoggles.GOGGLES) {
                goggles = ItemHandlerHelper.copyStackWithSize(inv.getStackInSlot(gogglesSlot = i), 1);
                if(!ItemNBTHelper.getBoolean(goggles, InventoryAkashicGoggles.NBT_MUTABLE, true)) return ItemStack.EMPTY;
                break;
            }
        }

        if(goggles.isEmpty()) return ItemStack.EMPTY;

        InventoryAkashicGoggles.setValid(goggles); // Only "valid" goggles have the capability, so temporarily mark the result as valid.
        @Nonnull final IDropInItem gogglesHandler = Objects.requireNonNull(goggles.getCapability(IDropInItem.DROP_IN_CAPABILITY, null));
        InventoryAkashicGoggles.setInvalid(goggles); // Ensure players can't put items into the "result" Akashic Goggles before crafting.

        @Nullable final EntityPlayer player = ForgeHooks.getCraftingPlayer();
        boolean nonEmpty = false;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(i == gogglesSlot) continue;

            @Nonnull final ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                nonEmpty = true;

                if(gogglesHandler.canDropItemIn(player, goggles, stack)) goggles = gogglesHandler.dropItemIn(player, goggles, stack.copy());
                else return ItemStack.EMPTY;
            }
        }

        return nonEmpty ? goggles : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(final int width, final int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull final InventoryCrafting inv) {
        // Play insert sound after "combine via craft".
        @Nullable final EntityPlayer player = ForgeHooks.getCraftingPlayer();
        if(player != null && !(player instanceof FakePlayer))
            player.world.playSound(null, player.posX, player.posY, player.posZ, AkashicGoggles.ITEM_GOGGLES_INSERT, player.getSoundCategory(), 1, 1);

        // Always consume all items.
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }
}
