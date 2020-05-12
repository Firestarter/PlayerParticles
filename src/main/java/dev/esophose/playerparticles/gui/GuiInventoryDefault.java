package dev.esophose.playerparticles.gui;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.manager.ConfigurationManager.GuiIcon;
import dev.esophose.playerparticles.manager.GuiManager;
import dev.esophose.playerparticles.manager.LocaleManager;
import dev.esophose.playerparticles.manager.ParticleGroupPresetManager;
import dev.esophose.playerparticles.manager.PermissionManager;
import dev.esophose.playerparticles.particles.PPlayer;
import dev.esophose.playerparticles.particles.ParticleEffect.ParticleProperty;
import dev.esophose.playerparticles.particles.ParticleGroup;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.util.ParticleUtils;
import dev.esophose.playerparticles.util.StringPlaceholders;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

@SuppressWarnings("deprecation")
public class GuiInventoryDefault extends GuiInventory {

    public GuiInventoryDefault(PPlayer pplayer) {
        super(pplayer, Bukkit.createInventory(pplayer.getPlayer(), 27, "Particle Effects"));
        INVENTORY_SIZE = 27;

        GuiManager guiManager = PlayerParticles.getInstance().getManager(GuiManager.class);

        this.fillColor(BorderColor.WHITE, 27);
        this.fillBorderAlternating(BorderColor.LIGHT_BLUE, BorderColor.BLUE);

        // Back button to perks menu
        GuiActionButton backButton = new GuiActionButton(
                10, Material.PAPER,
                ChatColor.translateAlternateColorCodes('&', "&b&lBack"), new String[0],
                (button, isShiftClick) -> Bukkit.getScheduler().runTask(PlayerParticles.getInstance(), () -> pplayer.getPlayer().chat("/perks"))
        );
        this.actionButtons.add(backButton);

        // Manage Your Particles button
        GuiActionButton manageYourParticlesButton = new GuiActionButton(
                14, Material.NETHER_STAR,
                ChatColor.translateAlternateColorCodes('&', "&f&lManage Particles"),
                new String[]{
                        ChatColor.GRAY + "Create, edit, and",
                        ChatColor.GRAY + "delete your particles."
                },
                (button, isShiftClick) -> guiManager.transition(new GuiInventoryManageParticles(pplayer)));
        manageYourParticlesButton.addEnchant();
        this.actionButtons.add(manageYourParticlesButton);

        final ParticlePair editingParticle = pplayer.getPrimaryParticle();
        boolean canEditPrimaryStyleAndData = pplayer.getActiveParticle(1) != null;

        // Edit Primary Effect
        GuiActionButton editPrimaryEffect = new GuiActionButton(
                12,
                Material.WHITE_TULIP,
                ChatColor.translateAlternateColorCodes('&', "&f&lEdit Primary Effect"),
                new String[]{
                        ChatColor.GRAY + "Change the particle effect",
                        ChatColor.GRAY + "of your primary particle."
                },
                (button, isShiftClick) -> {
                    List<Runnable> callbacks = new ArrayList<>();
                    callbacks.add(() -> guiManager.transition(new GuiInventoryDefault(pplayer)));
                    callbacks.add(() -> guiManager.transition(new GuiInventoryEditEffect(pplayer, editingParticle, 1, callbacks, 1)));
                    callbacks.add(() -> {
                        ParticleGroup group = pplayer.getActiveParticleGroup();
                        if (canEditPrimaryStyleAndData) {
                            for (ParticlePair particle : group.getParticles().values()) {
                                if (particle.getId() == editingParticle.getId()) {
                                    particle.setEffect(editingParticle.getEffect());
                                    break;
                                }
                            }
                        } else {
                            group.getParticles().put(editingParticle.getId(), editingParticle);
                        }
                        PlayerParticlesAPI.getInstance().savePlayerParticleGroup(pplayer.getPlayer(), group);

                        guiManager.transition(new GuiInventoryDefault(pplayer));
                    });

                    callbacks.get(1).run();
                });
        editPrimaryEffect.addEnchant();
        this.actionButtons.add(editPrimaryEffect);

        // Edit Primary Style
        String[] editPrimaryStyleLore;
        if (canEditPrimaryStyleAndData) {
            editPrimaryStyleLore =  new String[]{
                    ChatColor.GRAY + "Change the particle style",
                    ChatColor.GRAY + "of your primary particle."
            };
        } else {
            editPrimaryStyleLore =  new String[]{
                    ChatColor.GRAY + "Change the particle style",
                    ChatColor.GRAY + "of your primary particle.",
                    ChatColor.LIGHT_PURPLE + "Select an effect first."
            };
        }
        GuiActionButton editPrimaryStyle = new GuiActionButton(
                13,
                Material.LILAC,
                ChatColor.translateAlternateColorCodes('&', "&f&lEdit Primary Style"),
                editPrimaryStyleLore,
                (button, isShiftClick) -> {
                    if (!canEditPrimaryStyleAndData) return;

                    List<Runnable> callbacks = new ArrayList<>();
                    callbacks.add(() -> guiManager.transition(new GuiInventoryDefault(pplayer)));
                    callbacks.add(() -> guiManager.transition(new GuiInventoryEditStyle(pplayer, editingParticle, 1, callbacks, 1)));
                    callbacks.add(() -> {
                        ParticleGroup group = pplayer.getActiveParticleGroup();
                        for (ParticlePair particle : group.getParticles().values()) {
                            if (particle.getId() == editingParticle.getId()) {
                                particle.setStyle(editingParticle.getStyle());
                                break;
                            }
                        }
                        PlayerParticlesAPI.getInstance().savePlayerParticleGroup(pplayer.getPlayer(), group);

                        guiManager.transition(new GuiInventoryDefault(pplayer));
                    });

                    callbacks.get(1).run();
                });
        editPrimaryStyle.addEnchant();
        this.actionButtons.add(editPrimaryStyle);

        // Edit Primary Data
        /*String[] editPrimaryDataLore;
        if (canEditPrimaryStyleAndData && doesEffectUseData) {
            editPrimaryDataLore = new String[]{localeManager.getLocaleMessage("gui-color-info") + localeManager.getLocaleMessage("gui-edit-primary-data-description")};
        } else if (canEditPrimaryStyleAndData) {
            editPrimaryDataLore = new String[]{
                    localeManager.getLocaleMessage("gui-color-info") + localeManager.getLocaleMessage("gui-edit-primary-data-description"),
                    localeManager.getLocaleMessage("gui-color-unavailable") + localeManager.getLocaleMessage("gui-edit-primary-data-unavailable")
            };
        } else {
            editPrimaryDataLore = new String[]{
                    localeManager.getLocaleMessage("gui-color-info") + localeManager.getLocaleMessage("gui-edit-primary-data-description"),
                    localeManager.getLocaleMessage("gui-color-unavailable") + localeManager.getLocaleMessage("gui-edit-primary-data-missing-effect")
            };
        }
        GuiActionButton editPrimaryData = new GuiActionButton(
                42,
                GuiIcon.EDIT_DATA.get(),
                localeManager.getLocaleMessage("gui-color-icon-name") + localeManager.getLocaleMessage("gui-edit-primary-data"),
                editPrimaryDataLore,
                (button, isShiftClick) -> {
                    if (!canEditPrimaryStyleAndData || !doesEffectUseData) return;

                    List<Runnable> callbacks = new ArrayList<>();
                    callbacks.add(() -> guiManager.transition(new GuiInventoryDefault(pplayer)));
                    callbacks.add(() -> guiManager.transition(new GuiInventoryEditData(pplayer, editingParticle, 1, callbacks, 1)));
                    callbacks.add(() -> {
                        ParticleGroup group = pplayer.getActiveParticleGroup();
                        for (ParticlePair particle : group.getParticles().values()) {
                            if (particle.getId() == editingParticle.getId()) {
                                particle.setColor(editingParticle.getColor());
                                particle.setNoteColor(editingParticle.getNoteColor());
                                particle.setItemMaterial(editingParticle.getItemMaterial());
                                particle.setBlockMaterial(editingParticle.getBlockMaterial());
                                break;
                            }
                        }
                        PlayerParticlesAPI.getInstance().savePlayerParticleGroup(pplayer.getPlayer(), group);

                        guiManager.transition(new GuiInventoryDefault(pplayer));
                    });

                    callbacks.get(1).run();
                });
        this.actionButtons.add(editPrimaryData);*/

        this.populate();
    }
}
