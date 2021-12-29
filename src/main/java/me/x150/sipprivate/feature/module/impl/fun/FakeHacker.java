package me.x150.sipprivate.feature.module.impl.fun;

import me.x150.sipprivate.CoffeeClientMain;
import me.x150.sipprivate.feature.gui.notifications.Notification;
import me.x150.sipprivate.feature.module.Module;
import me.x150.sipprivate.feature.module.ModuleType;
import me.x150.sipprivate.helper.event.EventType;
import me.x150.sipprivate.helper.event.Events;
import me.x150.sipprivate.helper.event.events.MouseEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class FakeHacker extends Module {
    PlayerEntity target = null;
    public FakeHacker() {
        super("FakeHacker", "Makes it seem like another user is hacking", ModuleType.FUN);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled()) {
                return;
            }
            if (CoffeeClientMain.client.player == null || CoffeeClientMain.client.world == null) {
                return;
            }
            if (CoffeeClientMain.client.currentScreen != null) {
                return;
            }
            MouseEvent me = (MouseEvent) event;
            if (me.getAction() == 1 && me.getButton() == 2) {
                HitResult hr = CoffeeClientMain.client.crosshairTarget;
                if (hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity pe) {
                    target = pe;
                }
            }
        });
    }

    @Override public void tick() {
        if (target != null) {
            Iterable<Entity> entities = CoffeeClientMain.client.world.getEntities();
            List<Entity> entities1 = new ArrayList<>(StreamSupport.stream(entities.spliterator(),false).toList());
            Collections.shuffle(entities1);
            for (Entity entity : entities1) {
                if (entity.equals(target)) continue;
                if (entity.isAttackable() && entity.distanceTo(target) < 4) {
                    target.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES,entity.getPos().add(0,entity.getHeight()/2,0));
                    target.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    @Override public void enable() {
        target = null;
        Notification.create(6000, "", true, "Middle click a player to select them");
    }

    @Override public void disable() {

    }

    @Override public String getContext() {
        return target==null?null:target.getEntityName();
    }

    @Override public void onWorldRender(MatrixStack matrices) {

    }

    @Override public void onHudRender() {

    }
}
