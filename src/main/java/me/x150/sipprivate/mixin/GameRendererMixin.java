package me.x150.sipprivate.mixin;

import me.x150.sipprivate.SipoverPrivate;
import me.x150.sipprivate.feature.module.Module;
import me.x150.sipprivate.feature.module.ModuleRegistry;
import me.x150.sipprivate.helper.ImGuiManager;
import me.x150.sipprivate.helper.render.Renderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class) public class GameRendererMixin {

    Module noRender;
    private boolean vb;
    private boolean dis;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void atomic_dispatchWorldRender(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Renderer.R3D.setLastRenderStack(matrix);
        if (vb) {
            SipoverPrivate.client.options.bobView = true;
            vb = false;
        }
        for (Module module : ModuleRegistry.getModules()) {
            if (module.isEnabled()) {
                module.onWorldRender(matrix);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD")) void atomic_initShit(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!ImGuiManager.isInitialized()) {
            ImGuiManager.init();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderWorld") private void atomic_preRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        dis = true;
    }

    @Inject(at = @At("HEAD"), method = "bobView", cancellable = true) private void atomic_stopCursorBob(MatrixStack matrices, float f, CallbackInfo ci) {
        if (SipoverPrivate.client.options.bobView && dis) {
            vb = true;
            SipoverPrivate.client.options.bobView = false;
            dis = false;
            ci.cancel();
        }
    }
}