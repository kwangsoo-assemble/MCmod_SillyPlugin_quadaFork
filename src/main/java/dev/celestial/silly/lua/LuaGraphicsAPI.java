package dev.celestial.silly.lua;

//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.annotations.Alias;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.LivingEntityAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.luaj.vm2.LuaError;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(name = "LuaGraphicsAPI", value = "silly.lua_graphics")
public class LuaGraphicsAPI implements AutoCloseable {
    private final GuiGraphics graphics;
//    private Deque<Integer> tintStack = new ArrayDeque<>();
    private int pushedPoses = 0;
    private int scissors = 0;
    private boolean valid = true;
    public LuaGraphicsAPI(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public void _pushPose() {
        //? if >=1.21.6 {
        graphics.pose().pushMatrix();
        //?} else {
        /*graphics.pose().pushPose();
        *///?}
        pushedPoses++;
    }

    public void _popPose() {
        if (pushedPoses <= 0)
            throw new LuaError("Tried to pop pose when no poses were pushed! (mismatched pop/push?)");
        //? if >=1.21.6 {
        graphics.pose().popMatrix();
        //?} else {
        /*graphics.pose().popPose();
        *///?}
        pushedPoses--;
    }

    public void _ensureValid() {
        if (!valid) throw new LuaError("Attempted to use a revoked LuaGraphics instance!");
    }

    public void exit() {
        while (pushedPoses > 0) {
            //? if >=1.21.6 {
            graphics.pose().popMatrix();
            //?} else {
            /*graphics.pose().popPose();
            *///?}
            pushedPoses--;
        }
        while (scissors > 0) {
            graphics.disableScissor();
            scissors--;
        }
        valid = false;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.is_revoked")
    public Boolean isRevoked() {
        return !valid;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.set_scissors",
        overloads = {
                @LuaMethodOverload(
                        argumentTypes = { FiguraVec4.class },
                        argumentNames = { "region" },
                        returnType = LuaGraphicsAPI.class
                ),
                @LuaMethodOverload(
                        argumentNames = {"x", "y", "z", "w"},
                        argumentTypes = { Integer.class, Integer.class, Integer.class, Integer.class },
                        returnType = LuaGraphicsAPI.class
                ),
                @LuaMethodOverload(
                        argumentNames = {},
                        argumentTypes = {},
                        returnType = LuaGraphicsAPI.class
                )
        }
    )
    public LuaGraphicsAPI setScissors(Object x, Integer y, Integer z, Integer w) {
        _ensureValid();
        FiguraVec4 v = LuaUtils.parseVec4("setScissors", x, y, z, w, 0, 0, 0, 0);
        if (v.length() == 0) {
            scissors--;
            graphics.disableScissor();
            return this;
        }
        graphics.enableScissor((int)v.x, (int)v.y, (int)v.z, (int)v.w);
        scissors++;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "texture", "pos", "region" },
                            argumentTypes = { FiguraTexture.class, FiguraVec2.class, FiguraVec4.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "texture", "pos" },
                            argumentTypes = { FiguraTexture.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "resourceLocation", "pos", "region" },
                            argumentTypes = { String.class, FiguraVec2.class, FiguraVec4.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "resourceLocation", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blit(@LuaNotNil Object texture, @LuaNotNil FiguraVec2 pos, FiguraVec4 region) {
        _ensureValid();

        if (texture instanceof FiguraTexture tx) {
            region = region != null ? region : FiguraVec4.of(0,0,tx.getWidth(), tx.getHeight());
            //? if >=1.21.5 {
            graphics.blit(
                    net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                    tx.getLocation(),
                    (int)pos.x,
                    (int)pos.y,
                    (float)region.x,
                    (float)region.y,
                    (int)region.z,
                    (int)region.w,
                    tx.getWidth(),
                    tx.getHeight()
            );
            //?} else if >=1.21.2 {
            /*graphics.blit(
                    RenderType::guiTextured,
                    tx.getLocation(),
                    (int)pos.x,
                    (int)pos.y,
                    (float)region.x,
                    (float)region.y,
                    (int)region.z,
                    (int)region.w,
                    tx.getWidth(),
                    tx.getHeight()
            );
            *///?} else {
            /*graphics.blit(
                    tx.getLocation(),
                    (int)pos.x,
                    (int)pos.y,
                    0,
                    (int)region.x,
                    (int)region.y,
                    (int)region.z,
                    (int)region.w,
                    tx.getWidth(),
                    tx.getHeight());
            *///?}
        } else if (texture instanceof String str) {
            //? if >=1.21 {
            ResourceLocation loc = ResourceLocation.tryParse(str);
            //?} else {
            /*ResourceLocation loc = new ResourceLocation(str);
            *///?}

            FiguraVec2 size = cachedTextureSizes.computeIfAbsent(loc, (l) -> {
                var s = new FiguraVec2();
                var img = SillyUtil.getImage(l);
                s.x = img.getWidth();
                s.y = img.getHeight();
                return s;
            });
            region = region != null ? region : FiguraVec4.of(0,0,size.x, size.y);

            //? if >=1.21.5 {
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, loc, (int)pos.x, (int)pos.y, (int)region.x, (int)region.y, (int)region.z, (int)region.w,(int)size.x,(int)size.y);
            //?} else if >=1.21.2 {
            /*graphics.blit(RenderType::guiTextured, loc, (int)pos.x, (int)pos.y, (int)region.x, (int)region.y, (int)region.z, (int)region.w,(int)size.x,(int)size.y);
            *///?} else {
            /*graphics.blit(loc, (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, (int)size.x,(int)size.y);
            *///?}

        } else {
            throw new LuaError("LuaGraphics.blit, expected FiguraTexture or String, got " + texture.getClass().getSimpleName());
        }

        return this;
    }

    public static Map<ResourceLocation, FiguraVec2> cachedTextureSizes = new HashMap<>();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_string",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos", "width" },
                            argumentTypes = { String.class, FiguraVec2.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y", "width" },
                            argumentTypes = { String.class, Integer.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitString(@LuaNotNil String string, @LuaNotNil Object x, Integer y, Integer width) {
        _ensureValid();
        if (width == null) width = graphics.guiWidth();
        Integer finalX = null;
        Integer finalY = null;
        if (x instanceof FiguraVec2 p) {
            finalX = ((Number)p.x).intValue();
            finalY = ((Number)p.y).intValue();
            if (y != null)
                //noinspection SuspiciousNameCombination
                width = y;
        } else {
            finalX = ((Number)x).intValue();
            finalY = y;
        }
        Component comp = TextUtils.tryParseJson(string);
        Emojis.applyEmojis(comp);

//        comp = TextUtils.formatInBounds(comp, Minecraft.getInstance().font, width, )
        graphics.drawWordWrap(Minecraft.getInstance().font, comp, finalX, finalY, width, 0xFFFFFFFF);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_item",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "stackString", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "stackString", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "itemStack", "pos" },
                            argumentTypes = { ItemStackAPI.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "itemStack", "x", "y" },
                            argumentTypes = { ItemStackAPI.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitItem(@LuaNotNil Object string, @LuaNotNil Object x, Integer y) {
        _ensureValid();
        FiguraVec2 pos = LuaUtils.parseVec2("blitItem", x, y);
        ItemStack stack = LuaUtils.parseItemStack("blitItem", string);
        graphics.renderFakeItem(stack, (int)pos.x, (int)pos.y);
        return this;
    }

//    @LuaWhitelist
//    public LuaGraphics pushTint(@LuaNotNil FiguraVec4 col) {
//        _ensureValid();
//        Vector4i colorInt = new Vector4i((int) (col.x * 255), (int) (col.y * 255), (int) (col.z * 255), (int) (col.w * 255));
//        if (colorInt.x() > 255 || colorInt.y() > 255 || colorInt.z() > 255 || colorInt.w() > 255 | colorInt.x() < 0 || colorInt.y() < 0 || colorInt.z() < 0 | colorInt.w() < 0)
//            throw new LuaError("Invalid color: " + colorInt.toString());
//        int finalInt = colorInt.z << 24 | colorInt.x << 16 | colorInt.y << 8 | colorInt.z;
//        tintStack.push(finalInt);
//
//        return this;
//    }

//    @LuaWhitelist
//    public LuaGraphics popTint() {
//        _ensureValid();
//        if (tintStack.isEmpty()) throw new LuaError("Tried to pop tint, but it was empty! (mismatched pop/push?)");
//        tintStack.pop();
//        return this;
//    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.push_pose_matrix",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "matrix" },
                            argumentTypes = { FiguraMat4.class },
                            returnType = LuaGraphicsAPI.class
                    )
            },
            aliases = { "pushPose" }
    )
    public LuaGraphicsAPI pushPoseMatrix(FiguraMat4 matrix) {
        _ensureValid();
        _pushPose();

        //? if >=1.21.6 {
        // 1.21.6+ GUI 포즈는 2D(Matrix3x2f) — 4x4 행렬의 2D 아핀 성분만 적용
        org.joml.Matrix4f m4 = matrix.toMatrix4f();
        graphics.pose().mul(new org.joml.Matrix3x2f(m4.m00(), m4.m01(), m4.m10(), m4.m11(), m4.m30(), m4.m31()));
        //?} else if >=1.21 {
        /*graphics.pose().mulPose(matrix.toMatrix4f());
        *///?} else {
        /*graphics.pose().mulPoseMatrix(matrix.toMatrix4f());
        *///?}
        return this;
    }

    @LuaWhitelist @Alias
    public LuaGraphicsAPI pushPose(FiguraMat4 matrix) { return pushPoseMatrix(matrix); }

    @LuaWhitelist
    public LuaGraphicsAPI fill(FiguraVec3 color, FiguraVec4 region) {
        _ensureValid();
        Vec3i colorInt = new Vec3i((int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));
        if (colorInt.getX() > 255 || colorInt.getY() > 255 || colorInt.getZ() > 255 | colorInt.getX() < 0 || colorInt.getY() < 0 || colorInt.getZ() < 0)
            throw new LuaError("Invalid color: " + colorInt.toShortString());
        graphics.fill((int) region.x, (int) region.y, (int) (region.z + region.x), (int) (region.w + region.y), 0xFF000000 | (colorInt.getX() << 16) | (colorInt.getY() << 8) | (colorInt.getZ()));
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.pop_pose",
            overloads =
                    {@LuaMethodOverload(returnType = LuaGraphicsAPI.class)}
    )
    public LuaGraphicsAPI popPose() {
        _ensureValid();
        _popPose();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_entity",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { LivingEntityAPI.class, FiguraVec2.class, FiguraVec3.class },
                            argumentNames = { "entity", "pos", "rot" },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitEntity(@LuaNotNil LivingEntityAPI<? extends LivingEntity> entity, @LuaNotNil FiguraVec2 pos, @LuaNotNil FiguraVec3 rot) {
        _ensureValid();
        FiguraVec3 rot2 = rot.copy();
        rot2.scale(MathUtils.DEG_TO_RAD);
        Quaternionf quat = new Quaternionf().rotationZYX((float)rot2.x, (float)rot2.y, (float)rot2.z);
        //? if >=1.21.6 {
        // 1.21.6+ PiP 방식: 3D 포즈 변환 불가 → 좌표 박스 기반 호출로 대체 (기존 스케일 2*16=32 근사)
        int px = (int) pos.x;
        int py = (int) pos.y;
        // poked around in the code and supposedly this hides the nameplate.
        UIHelper.paperdoll = true;
        InventoryScreen.renderEntityInInventory(graphics, px - 32, py - 64, px + 32, py, 32f,
                new Vector3f(0f, entity.getEntity().getBbHeight() / 2f, 0f), quat, null, entity.getEntity());
        UIHelper.paperdoll = false;
        //?} else if >=1.21 {
        /*graphics.pose().pushPose();
        graphics.pose().translate(pos.x, pos.y, 0);
        graphics.pose().scale(2,-2,2);
        graphics.pose().pushPose();
        graphics.pose().scale(16,16,16);

        // poked around in the code and supposedly this hides the nameplate.
        UIHelper.paperdoll = true;
        InventoryScreen.renderEntityInInventory(graphics, 0, 0, 1, new Vector3f(), quat, null, entity.getEntity());
        UIHelper.paperdoll = false;

        graphics.pose().popPose();
        graphics.pose().popPose();
        *///?} else {
        /*graphics.pose().pushPose();
        graphics.pose().translate(pos.x, pos.y, 0);
        graphics.pose().scale(2,-2,2);
        graphics.pose().pushPose();
        graphics.pose().scale(16,16,16);

        // poked around in the code and supposedly this hides the nameplate.
        UIHelper.paperdoll = true;
        InventoryScreen.renderEntityInInventory(graphics, 0, 0, 1, quat, null, entity.getEntity());
        UIHelper.paperdoll = false;

        graphics.pose().popPose();
        graphics.pose().popPose();
        *///?}
        return this;
    }

    @Override
    public void close() throws Exception {
        exit();
    }
}
