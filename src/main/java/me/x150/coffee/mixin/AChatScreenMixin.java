package me.x150.coffee.mixin;

import com.google.gson.JsonObject;
import me.x150.coffee.CoffeeClientMain;
import me.x150.coffee.feature.command.CommandRegistry;
import me.x150.coffee.feature.gui.screen.CoffeeConsoleScreen;
import me.x150.coffee.feature.module.ModuleRegistry;
import me.x150.coffee.feature.module.impl.misc.InfChatLength;
import me.x150.coffee.helper.ManagerSocket;
import me.x150.coffee.helper.font.FontRenderers;
import me.x150.coffee.helper.util.Utils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class AChatScreenMixin extends Screen {

    @Shadow
    protected TextFieldWidget chatField;

    protected AChatScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;)V"))
    void atomic_interceptChatMessage(ChatScreen instance, String s) {
        if (s.startsWith(".")) { // filter all messages starting with .
            CoffeeClientMain.client.inGameHud.getChatHud().addToMessageHistory(s);
            if (s.equalsIgnoreCase(".console")) {
                Utils.TickManager.runInNTicks(2, () -> CoffeeClientMain.client.setScreen(CoffeeConsoleScreen.instance()));
            } else CommandRegistry.execute(s.substring(1)); // cut off prefix
        } else if (s.startsWith("#")) {
            CoffeeClientMain.client.inGameHud.getChatHud().addToMessageHistory(s);
            if (!CoffeeClientMain.sman.getSocket().allowOnlineFeatures()) {
                Utils.Logging.error("IRC is not available");
            } else {
                String cmsg = s.substring(1).trim();
                JsonObject pd = new JsonObject();
                pd.addProperty("message", cmsg);
                CoffeeClientMain.sman.getSocket().send(new ManagerSocket.Packet("chatIrc", pd));
            }

        } else {
            instance.sendMessage(s); // else, go
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    void renderText(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String t = chatField.getText();
        if (t.startsWith(".")) {
            String note = "If you need a bigger console, do \".console\"";
            double len = FontRenderers.getRenderer().getStringWidth(note) + 1;
            FontRenderers.getRenderer().drawString(matrices, note, width - len - 2, height - 15 - FontRenderers.getRenderer().getMarginHeight(), 0xFFFFFF);
        }
    }

    @Inject(method = "onChatFieldUpdate", at = @At("HEAD"))
    public void atomic_preChatFieldUpdate(String chatText, CallbackInfo ci) {
        chatField.setMaxLength((ModuleRegistry.getByClass(InfChatLength.class).isEnabled()) ? Integer.MAX_VALUE : 256);
    }
}
