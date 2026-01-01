/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.akashic_goggles.mod.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import git.jbredwards.akashic_goggles.Tags;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class ModelHeadwear implements IModel
{
    @Nonnull
    public static final ModelHeadwear DUMMY = new ModelHeadwear(
            new ResourceLocation(ModelLoader.MODEL_MISSING.getNamespace(), ModelLoader.MODEL_MISSING.getPath()),
            new ResourceLocation(ModelLoader.MODEL_MISSING.getNamespace(), ModelLoader.MODEL_MISSING.getPath()));

    @Nonnull
    public final ResourceLocation baseLocation, headLocation;
    public ModelHeadwear(@Nonnull final ResourceLocation baseLocationIn, @Nonnull final ResourceLocation headLocationIn) {
        baseLocation = baseLocationIn;
        headLocation = headLocationIn;
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of(baseLocation, headLocation);
    }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull final IModelState state, @Nonnull final VertexFormat format, @Nonnull final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new Baked(bakePart(baseLocation, state, format, bakedTextureGetter), bakePart(headLocation, state, format, bakedTextureGetter));
    }

    @Nonnull
    protected static IBakedModel bakePart(@Nonnull final ResourceLocation location, @Nonnull final IModelState state, @Nonnull final VertexFormat format, @Nonnull final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        @Nonnull final IModel model = ModelLoaderRegistry.getModelOrLogError(location, "Couldn't load ModelHeadwear dependency: " + location);
        return model.bake(new ModelStateComposition(state, model.getDefaultState()), format, bakedTextureGetter);
    }

    @SideOnly(Side.CLIENT)
    public static class Baked extends BakedModelWrapper<IBakedModel>
    {
        @Nonnull
        public final IBakedModel headwearModel;
        public Baked(@Nonnull final IBakedModel baseModelIn, @Nonnull final IBakedModel headwearModelIn) {
            super(baseModelIn);
            headwearModel = headwearModelIn;
        }

        @Nonnull
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull final ItemCameraTransforms.TransformType cameraTransformType) {
            return (cameraTransformType == ItemCameraTransforms.TransformType.HEAD ? headwearModel : originalModel).handlePerspective(cameraTransformType);
        }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return new ItemOverrideList(Collections.emptyList()) {
                @Nonnull
                @Override
                public IBakedModel handleItemState(@Nonnull final IBakedModel wrapper, @Nonnull final ItemStack stack, @Nullable final World world, @Nullable final EntityLivingBase entity) {
                    return new Baked(originalModel.getOverrides().handleItemState(originalModel, stack, world, entity), headwearModel.getOverrides().handleItemState(headwearModel, stack, world, entity));
                }
            };
        }
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull final ImmutableMap<String, String> customData) {
        if(customData.containsKey("base") && customData.containsKey("head")) {
            @Nonnull final ResourceLocation base = new ModelResourceLocation(JsonUtils.getString(new JsonParser().parse(customData.get("base")), "base"));
            @Nonnull final ResourceLocation head = new ModelResourceLocation(JsonUtils.getString(new JsonParser().parse(customData.get("head")), "head"));
            return new ModelHeadwear(base, head);
        }

        return DUMMY;
    }

    @SideOnly(Side.CLIENT)
    public enum Loader implements ICustomModelLoader
    {
        INSTANCE;

        @Override
        public void onResourceManagerReload(@Nonnull final IResourceManager manager) {}

        @Override
        public boolean accepts(@Nonnull final ResourceLocation modelLocation) {
            return modelLocation.getNamespace().equals(Tags.MOD_ID)
                    && modelLocation.getPath().endsWith("builtin");
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull final ResourceLocation modelLocation) { return DUMMY; }
    }
}
