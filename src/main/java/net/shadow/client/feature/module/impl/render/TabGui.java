/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.feature.module.impl.render;

import lombok.Data;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.shadow.client.feature.gui.clickgui.theme.ThemeManager;
import net.shadow.client.feature.module.Module;
import net.shadow.client.feature.module.ModuleRegistry;
import net.shadow.client.feature.module.ModuleType;
import net.shadow.client.helper.event.EventType;
import net.shadow.client.helper.event.Events;
import net.shadow.client.helper.event.events.KeyboardEvent;
import net.shadow.client.helper.font.FontRenderers;
import net.shadow.client.helper.render.Renderer;
import net.shadow.client.helper.util.Transitions;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.function.BooleanSupplier;

public class TabGui extends Module {
    @Getter
    Stack<TabPane> tabStack = new Stack<>();

    public TabGui() {
        super("TabGui", "Renders a small module manager top left", ModuleType.RENDER);
        Events.registerEventHandler(EventType.KEYBOARD, event -> {
            if (!this.isEnabled()) return;
            KeyboardEvent me = (KeyboardEvent) event;
            handleMouse(me);
        });
    }

    @Override
    public void onFastTick() {
        for (TabPane tabPane : tabStack) {
            tabPane.smoothCursor = Transitions.transition(tabPane.smoothCursor, tabPane.cursor, 7, 0);
        }
    }

    @Override
    public void postInit() {
        if (tabStack.isEmpty()) {
            TabPane tbp = new TabPane();
            for (ModuleType value : ModuleType.values()) {
                GuiEntry ge = new GuiEntry(value.getName(), () -> false, () -> {
                    TabPane modules = new TabPane();
                    for (Module module : ModuleRegistry.getModules()) {
                        if (module.getModuleType() != value) continue;
                        GuiEntry ge1 = new GuiEntry(module.getName(), module::isEnabled, module::toggle, () -> tabStack.pop(), FontRenderers.getRenderer().getStringWidth(module.getName()), FontRenderers.getRenderer().getMarginHeight());
                        modules.entries.add(ge1);
                    }
                    if (modules.entries.isEmpty()) return;
                    tabStack.add(modules);
                }, () -> {
                }, FontRenderers.getRenderer().getStringWidth(value.getName()), FontRenderers.getRenderer().getMarginHeight());
                tbp.entries.add(ge);
            }
            tabStack.add(tbp);
        }
    }

    int makeSureInBounds(int index, int size) {
        index %= size;
        if (index < 0) {
            index = size + index;
        }
        return index;
    }

    void handleMouse(KeyboardEvent me) {
        if (me.getType() == 0) return;
        if (tabStack.isEmpty()) return;
        TabPane tbp = tabStack.peek();
        switch (me.getKeycode()) {
            case GLFW.GLFW_KEY_DOWN -> tbp.cursor++;
            case GLFW.GLFW_KEY_UP -> tbp.cursor--;
            case GLFW.GLFW_KEY_RIGHT -> tbp.entries.get(tbp.cursor).onClick().run();
            case GLFW.GLFW_KEY_LEFT -> tbp.entries.get(tbp.cursor).onClose().run();
        }
        tbp.cursor = makeSureInBounds(tbp.cursor, tbp.entries.size());
    }

    @Override
    public void onHudRender() {
    }

    public void render(MatrixStack stack) {
        if (!this.isEnabled()) return;
        for (TabPane tabPane : tabStack) {
            GuiEntry widest = tabPane.entries.stream().max(Comparator.comparingDouble(value -> value.width)).orElseThrow();
            double padOuter = 2;
            double scrollerWidth = 1.5;
            double yOffset = padOuter;
            double oneHeight = FontRenderers.getRenderer().getMarginHeight();
            double scrollerYOffset = tabPane.smoothCursor * oneHeight;
            double scrollerYEnd = (tabPane.smoothCursor + 1) * oneHeight;
            double height = tabPane.entries.size() * oneHeight + padOuter * 2;

            double width = padOuter + scrollerWidth + 2 + Math.ceil(widest.width + 1) + 3;
            Renderer.R2D.renderRoundedQuad(stack, ThemeManager.getMainTheme().getConfig(), 0, 0, width, height, 3, 20);
            Renderer.R2D.renderRoundedQuad(stack, ThemeManager.getMainTheme().getAccent(), padOuter, yOffset + scrollerYOffset, padOuter + scrollerWidth, yOffset + scrollerYEnd, scrollerWidth / 2d, 20);

            double lastEnabledStackHeight = 0;
            double lastEnabledStackY = 0;
            double yOff = yOffset;
            for (GuiEntry ge : tabPane.entries) {
                if (ge.isEnabled.getAsBoolean()) {
                    if (lastEnabledStackHeight == 0) {
                        lastEnabledStackY = yOff;
                    }
                    lastEnabledStackHeight += oneHeight;
                } else {
                    if (lastEnabledStackHeight != 0) {
                        Renderer.R2D.renderRoundedQuad(stack, new Color(40, 40, 40, 200), scrollerWidth + padOuter + 1, lastEnabledStackY, width - 2, lastEnabledStackY + lastEnabledStackHeight, 3, 20);
                    }
                    lastEnabledStackHeight = 0;
                    lastEnabledStackY = 0;
                }
                yOff += oneHeight;
            }

            for (GuiEntry entry : tabPane.entries) {
                FontRenderers.getRenderer().drawString(stack, entry.text, scrollerWidth + padOuter + 2, yOffset, entry.isEnabled.getAsBoolean() ? 0xFFFFFF : 0xBBBBBB);
                yOffset += oneHeight;
            }
            stack.translate(width + 5, 0, 0); // x offset
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Data
    public static class TabPane {
        int cursor = 0;
        double smoothCursor = 0;
        List<GuiEntry> entries = new ArrayList<>();
    }

    record GuiEntry(String text, BooleanSupplier isEnabled, Runnable onClick, Runnable onClose, double width,
                    double height) {
    }


}
