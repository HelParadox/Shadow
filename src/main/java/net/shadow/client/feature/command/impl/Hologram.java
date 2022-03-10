

package net.shadow.client.feature.command.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shadow.client.ShadowMain;
import net.shadow.client.feature.command.Command;
import net.shadow.client.helper.manager.HologramManager;

import java.util.Arrays;
import java.util.Objects;

public class Hologram extends Command {

    public Hologram() {
        super("Hologram", "Generates a hologram without needing op (requires creative)", "hologram", "holo", "hlg");
    }

    @Override
    public String[] getSuggestions(String fullCommand, String[] args) {
        if (args.length == 1) {
            return new String[]{"(flags)"};
        }
        if (args.length == 2) {
            return new String[]{"(message)"};
        }
        return super.getSuggestions(fullCommand, args);
    }

    @Override
    public void onExecute(String[] args) {
        if (args.length < 2) {
            message("i need options and text pls. example: \".hologram eb your text\". specify option \"h\" to show help");
            return;
        }
        String options = args[0].toLowerCase();
        boolean generateAsBaby = false;
        boolean generateAsEgg = false;
        boolean makeGravity = false;
        boolean makeVisible = false;
        boolean marker = true;
        for (char c : options.toCharArray()) {
            switch (c) {
                case 'e' -> generateAsEgg = true;
                case 'b' -> generateAsBaby = true;
                case 'g' -> makeGravity = true;
                case 'v' -> makeVisible = true;
                case 'm' -> marker = false;
                case 'n' -> {
                }
                default -> {
                    error("Unknown option \"" + c + "\". Valid options:");
                    message("  N = None (Placeholder)");
                    message("  E = Makes a spawn egg instead of an armor stand item");
                    message("  B = Makes the hologram entity small");
                    message("  G = Makes the hologram have gravity");
                    message("  V = Makes the hologram entity visible");
                    message("  M = Makes the hologram entity not a marker (enable interactions and hitbox)");
                    return;
                }
            }
        }
        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Vec3d pos = Objects.requireNonNull(ShadowMain.client.player).getPos();
        BlockPos displayable = ShadowMain.client.player.getBlockPos();
        message("Armor stand config:");
        message("  Text: " + text);
        message("  Is baby: " + (generateAsBaby ? "Yes" : "No"));
        message("  Is egg: " + (generateAsEgg ? "Yes" : "No"));
        message("  Is invisible: " + (!makeVisible ? "Yes" : "No"));
        message("  Has gravity: " + (makeGravity ? "Yes" : "No"));
        message("  Is marker: " + (marker ? "Yes" : "No"));
        message("  Pos: " + displayable.getX() + ", " + displayable.getY() + ", " + displayable.getZ());
        HologramManager.Hologram h = HologramManager.generateDefault(text, pos).isEgg(generateAsEgg).isSmall(generateAsBaby).hasGravity(makeGravity).isVisible(makeVisible).isMarker(marker);
        ItemStack stack = h.generate();
        message("Dont forget to open your inventory before placing");
        ShadowMain.client.player.getInventory().addPickBlock(stack);
    }
}
