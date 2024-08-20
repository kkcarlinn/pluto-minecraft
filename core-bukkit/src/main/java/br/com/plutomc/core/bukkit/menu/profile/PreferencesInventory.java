package br.com.plutomc.core.bukkit.menu.profile;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.MenuClickHandler;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PreferencesInventory {
   private boolean message = false;

   public PreferencesInventory(Player player, int page, long wait) {
      MenuInventory menuInventory = new MenuInventory("§7§%inventory-preferences%§", 4);
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      this.create(
         player,
         "Bate-papo",
         "Receber mensagens no bate-papo do servidor",
         Material.PAPER,
         member.getMemberConfiguration().isSeeingChat(),
         11,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               if (!this.message) {
                  this.message = true;
                  member.sendMessage("§cVocê precisa esperar para mudar uma configuração.");
               }
            } else {
               member.getMemberConfiguration().setSeeingChat(!member.getMemberConfiguration().isSeeingChat());
               new PreferencesInventory(player, page, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Conversa privada",
         "Receber mensagens privadas no servidor",
         Material.PAPER,
         member.getMemberConfiguration().isTellEnabled(),
         12,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               if (!this.message) {
                  this.message = true;
                  member.sendMessage("§cVocê precisa esperar para mudar uma configuração.");
               }
            } else {
               member.getMemberConfiguration().setTellEnabled(!member.getMemberConfiguration().isTellEnabled());
               new PreferencesInventory(player, page, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Convite de party",
         "Receber convites para party",
         Material.PAPER,
         member.getMemberConfiguration().isPartyInvites(),
         13,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               if (!this.message) {
                  this.message = true;
                  member.sendMessage("§cVocê precisa esperar para mudar uma configuração.");
               }
            } else {
               member.getMemberConfiguration().setPartyInvites(!member.getMemberConfiguration().isPartyInvites());
               new PreferencesInventory(player, page, System.currentTimeMillis() + 500L);
            }
         }
      );
      menuInventory.open(player);
      player.updateInventory();
   }

   public PreferencesInventory(Player player) {
      new PreferencesInventory(player, 1, -1L);
   }

   public void create(
      Player player, String name, String description, Material material, Boolean active, int slot, MenuInventory menuInventory, MenuClickHandler handler
   ) {
      menuInventory.setItem(slot, new ItemBuilder().name((active ? "§a" : "§c") + name).type(material).lore("§7" + description).build(), handler);
      menuInventory.setItem(
         slot + 9,
         new ItemBuilder()
            .name((active ? "§a" : "§c") + name)
            .type(Material.INK_SACK)
            .durability(active ? 10 : 8)
            .lore(active ? "§7Clique para desativar." : "§7Clique para ativar.")
            .build(),
         handler
      );
   }
}
