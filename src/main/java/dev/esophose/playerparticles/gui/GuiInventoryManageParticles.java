package dev.esophose.playerparticles.gui;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.manager.ConfigurationManager.GuiIcon;
import dev.esophose.playerparticles.manager.GuiManager;
import dev.esophose.playerparticles.manager.LocaleManager;
import dev.esophose.playerparticles.manager.PermissionManager;
import dev.esophose.playerparticles.particles.PPlayer;
import dev.esophose.playerparticles.particles.ParticleEffect.ParticleProperty;
import dev.esophose.playerparticles.particles.ParticleGroup;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.util.ParticleUtils;
import dev.esophose.playerparticles.util.StringPlaceholders;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class GuiInventoryManageParticles extends GuiInventory {

    public GuiInventoryManageParticles(PPlayer pplayer) {
        super(pplayer, Bukkit.createInventory(pplayer.getPlayer(), 54, "Manage Particles"));

        LocaleManager localeManager = PlayerParticles.getInstance().getManager(LocaleManager.class);
        GuiManager guiManager = PlayerParticles.getInstance().getManager(GuiManager.class);

        this.fillBorderAlternating(BorderColor.ORANGE, BorderColor.RED);

        // Manage/Delete Particle Buttons
        List<ParticlePair> particles = new ArrayList<>(pplayer.getActiveParticles());
        particles.sort(Comparator.comparingInt(ParticlePair::getId));

        int index = 10;
        int nextWrap = 17;
        int maxIndex = 35;
        for (ParticlePair particle : particles) {
            GuiActionButton selectButton = new GuiActionButton(
                    index,
                    Material.NETHER_STAR,
                    ChatColor.translateAlternateColorCodes('&', String.format("&f&lParticle %s", particle.getId())),
                    new String[] {
                            ChatColor.GRAY + "Effect: " + ChatColor.LIGHT_PURPLE + ParticleUtils.formatName(particle.getEffect().getName()),
                            ChatColor.GRAY + "Style: " + ChatColor.LIGHT_PURPLE + ParticleUtils.formatName(particle.getStyle().getName()),
                            ChatColor.AQUA + "Shift-click to delete."
                    },
                    (button, isShiftClick) -> {
                        if (!isShiftClick) {
                            guiManager.transition(new GuiInventoryEditParticle(pplayer, particle));
                        } else {
                            // Delete particle
                            ParticleGroup activeGroup = pplayer.getActiveParticleGroup();
                            activeGroup.getParticles().remove(particle.getId());
                            PlayerParticlesAPI.getInstance().savePlayerParticleGroup(pplayer.getPlayer(), activeGroup);

                            // Update inventory to reflect deletion
                            this.actionButtons.remove(button);
                            this.inventory.setItem(button.getSlot(), null);
                        }
                    });
            this.actionButtons.add(selectButton);

            index++;
            if (index == nextWrap) { // Loop around border
                nextWrap += 9;
                index += 2;
            }
            if (index > maxIndex) break; // Overflowed the available space
        }

        // Create New Particle Button
        boolean canCreate = pplayer.getActiveParticles().size() < PlayerParticles.getInstance().getManager(PermissionManager.class).getMaxParticlesAllowed(pplayer);
        GuiActionButton createNewParticle = new GuiActionButton(
                INVENTORY_SIZE - 5,
                GuiIcon.CREATE.get(),
                ChatColor.translateAlternateColorCodes('&', "&a&lNew Particle"),
                canCreate ? new String[]{} : new String[]{
                        ChatColor.RED + "You've already used all",
                        ChatColor.RED + "of your particle slots."
                },
                (button, isShiftClick) -> {
                    if (!canCreate) return;
                    ParticlePair editingParticle = ParticlePair.getNextDefault(pplayer);
                    List<Runnable> callbacks = new ArrayList<>();
                    callbacks.add(() -> guiManager.transition(new GuiInventoryManageParticles(pplayer)));
                    callbacks.add(() -> guiManager.transition(new GuiInventoryEditEffect(pplayer, editingParticle, 1, callbacks, 1)));
                    callbacks.add(() -> guiManager.transition(new GuiInventoryEditStyle(pplayer, editingParticle, 1, callbacks, 2)));
                    callbacks.add(() -> {
                        if (editingParticle.getEffect().hasProperty(ParticleProperty.COLORABLE) || editingParticle.getEffect().hasProperty(ParticleProperty.REQUIRES_MATERIAL_DATA)) {
                            guiManager.transition(new GuiInventoryEditData(pplayer, editingParticle, 1, callbacks, 3));
                        } else {
                            callbacks.get(4).run();
                        }
                    });
                    callbacks.add(() -> {
                        // Save new particle
                        ParticleGroup group = pplayer.getActiveParticleGroup();
                        group.getParticles().put(editingParticle.getId(), editingParticle);
                        PlayerParticlesAPI.getInstance().savePlayerParticleGroup(pplayer.getPlayer(), group);

                        // Reopen the manage particle inventory
                        guiManager.transition(new GuiInventoryManageParticles(pplayer));
                    });
                    callbacks.get(1).run();
                });
        this.actionButtons.add(createNewParticle);

        // Reset Particles Button
        GuiActionButton resetParticles = new GuiActionButton(
                INVENTORY_SIZE - 4,
                GuiIcon.RESET.get(),
                ChatColor.translateAlternateColorCodes('&', "&c&lReset Particles"),
                new String[]{},
                (button, isShiftClick) -> {
                    // Reset particles
                    PlayerParticlesAPI.getInstance().savePlayerParticleGroup(pplayer.getPlayer(), ParticleGroup.getDefaultGroup());

                    // Reopen this same inventory to refresh it
                    guiManager.transition(new GuiInventoryManageParticles(pplayer));
                });
        this.actionButtons.add(resetParticles);

        // Back Button
        GuiActionButton backButton = new GuiActionButton(
                INVENTORY_SIZE - 6,
                Material.PAPER,
                ChatColor.translateAlternateColorCodes('&', "&b&lBack"),
                new String[]{},
                (button, isShiftClick) -> guiManager.transition(new GuiInventoryDefault(pplayer)));
        this.actionButtons.add(backButton);

        this.populate();
    }

}
