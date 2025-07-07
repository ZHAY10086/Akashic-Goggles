package git.jbredwards.akashic_goggles.mod.common.baubles;

import baubles.api.cap.BaublesCapabilities;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class LayerBaublesArmor extends LayerBipedArmor
{
    @Nonnull
    protected static final EntityLiving FAKE_WEARER = new EntityLiving(null) {}; // World may be null.
    public LayerBaublesArmor(@Nonnull final RenderLivingBase<?> rendererIn) { super(rendererIn); }

    @Override
    protected void initArmor() { modelArmor = new ModelBiped(0.25f); /* Use a size of 0.25 to render under helmets. */ }
    protected static boolean shouldRender(@Nonnull final ItemStack stack) { return CompatBaubles.LOADED_HANDLERS.stream().anyMatch(ch -> ch.condition.test(stack)); }

    @Override
    public void doRenderLayer(@Nonnull final EntityLivingBase entity, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        if(entity.isPotionActive(MobEffects.INVISIBILITY) || Iterables.any(entity.getArmorInventoryList(), LayerBaublesArmor::shouldRender)) return;
        // A hack to render any armor item, regardless of what the entity actually has equipped.
        @Nullable final IItemHandler inventory = entity.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if(inventory != null) IntStream.range(0, inventory.getSlots()).mapToObj(inventory::getStackInSlot).filter(LayerBaublesArmor::shouldRender).findFirst().ifPresent(stack -> {
            FAKE_WEARER.setItemStackToSlot(EntityLiving.getSlotForItemStack(stack), stack);
            FAKE_WEARER.setSneaking(entity.isSneaking());
            FAKE_WEARER.ticksExisted = entity.ticksExisted;
            super.doRenderLayer(FAKE_WEARER, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        });
    }
}
