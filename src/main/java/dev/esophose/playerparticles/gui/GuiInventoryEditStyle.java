package dev.esophose.playerparticles.gui;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.manager.ConfigurationManager.GuiIcon;
import dev.esophose.playerparticles.manager.GuiManager;
import dev.esophose.playerparticles.manager.LocaleManager;
import dev.esophose.playerparticles.manager.PermissionManager;
import dev.esophose.playerparticles.particles.PPlayer;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.styles.ParticleStyle;
import dev.esophose.playerparticles.util.ParticleUtils;
import dev.esophose.playerparticles.util.StringPlaceholders;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class GuiInventoryEditStyle extends GuiInventory {

    public GuiInventoryEditStyle(PPlayer pplayer, ParticlePair editingParticle, int pageNumber, List<Runnable> callbackList, int callbackListPosition) {
        super(pplayer, Bukkit.createInventory(pplayer.getPlayer(), 54, PlayerParticles.getInstance().getManager(LocaleManager.class).getLocaleMessage("gui-select-style")));

        LocaleManager localeManager = PlayerParticles.getInstance().getManager(LocaleManager.class);
        GuiManager guiManager = PlayerParticles.getInstance().getManager(GuiManager.class);

        this.fillBorderAlternating(BorderColor.LIME, BorderColor.GREEN);

        // Select Style Buttons
        List<ParticleStyle> stylesUserHasPermissionFor = PlayerParticles.getInstance().getManager(PermissionManager.class).getStylesUserHasPermissionFor(pplayer);
        int numberOfItems = stylesUserHasPermissionFor.size();
        int itemsPerPage = 28;
        int maxPages = (int) Math.max(1, Math.ceil((double) numberOfItems / itemsPerPage));
        int slot = 10;
        int nextWrap = 17;
        int maxSlot = 43;

        for (int i = (pageNumber - 1) * itemsPerPage; i < numberOfItems; i++) {
            ParticleStyle style = stylesUserHasPermissionFor.get(i);
            GuiActionButton selectButton = new GuiActionButton(
                    slot,
                    style.getGuiIconMaterial(),
                    ChatColor.translateAlternateColorCodes('&', String.format("&f&l%s", ParticleUtils.formatName(style.getName()))),
                    new String[]{},
                    (button, isShiftClick) -> {
                        editingParticle.setStyle(style);
                        callbackList.get(callbackListPosition + 1).run();
                    });
            this.actionButtons.add(selectButton);

            slot++;
            if (slot == nextWrap) { // Loop around border
                nextWrap += 9;
                slot += 2;
            }
            if (slot > maxSlot) break; // Overflowed the available space
        }

        // Back Button
        GuiActionButton backButton = new GuiActionButton(
                INVENTORY_SIZE - 1,
                Material.PAPER,
                ChatColor.translateAlternateColorCodes('&', "&b&lBack"),
                new String[]{},
                (button, isShiftClick) -> callbackList.get(callbackListPosition - 1).run());
        this.actionButtons.add(backButton);

        // Previous page button
        if (pageNumber != 1) {
            GuiActionButton previousPageButton = new GuiActionButton(
                    INVENTORY_SIZE - 6,
                    GuiIcon.PREVIOUS_PAGE.get(),
                    ChatColor.translateAlternateColorCodes('&', "&f&lPrevious"),
                    new String[]{},
                    (button, isShiftClick) -> guiManager.transition(new GuiInventoryEditStyle(pplayer, editingParticle, pageNumber - 1, callbackList, callbackListPosition)));
            this.actionButtons.add(previousPageButton);
        }

        // Next page button
        if (pageNumber != maxPages) {
            GuiActionButton nextPageButton = new GuiActionButton(
                    INVENTORY_SIZE - 4,
                    GuiIcon.NEXT_PAGE.get(),
                    ChatColor.translateAlternateColorCodes('&', "&f&lNext"),
                    new String[]{},
                    (button, isShiftClick) -> guiManager.transition(new GuiInventoryEditStyle(pplayer, editingParticle, pageNumber + 1, callbackList, callbackListPosition)));
            this.actionButtons.add(nextPageButton);
        }

        this.populate();
    }

}
