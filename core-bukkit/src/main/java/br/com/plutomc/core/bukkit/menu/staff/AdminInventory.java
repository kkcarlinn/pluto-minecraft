package br.com.plutomc.core.bukkit.menu.staff;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.MenuClickHandler;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.configuration.AccountConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AdminInventory {
   public AdminInventory(Player player) {
      this(player, 0L);
   }

   public AdminInventory(Player player, long wait) {
      MenuInventory menuInventory = new MenuInventory("§7Admin Config", 4);
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      MenuClickHandler handler = (p, inv, type, stack, s) -> {
         if (wait > System.currentTimeMillis()) {
            player.sendMessage("§cAguarde para alterar outra configuração.");
         } else {
            int code = account.getAccountConfiguration().getAdminModeJoin();
            if (code == 2) {
               code = 0;
            } else {
               ++code;
            }

            account.getAccountConfiguration().setAdminModeJoin(code);
            new AdminInventory(player, System.currentTimeMillis() + 500L);
            p.updateInventory();
         }
      };
      String color = account.getAccountConfiguration().getAdminModeJoin() == 0 ? "§c" : (account.getAccountConfiguration().getAdminModeJoin() == 1 ? "§a" : "§e");
      menuInventory.setItem(
         10,
         new ItemBuilder().name(color + "Entrar no admin").type(Material.PAPER).lore("§7Entre automaticamente no modo admin ao entrar no servidor.").build(),
         handler
      );
      menuInventory.setItem(
         19,
         new ItemBuilder()
            .name(color + "Entrar no admin")
            .type(Material.INK_SACK)
            .durability(account.getAccountConfiguration().getAdminModeJoin() == 0 ? 8 : (account.getAccountConfiguration().getAdminModeJoin() == 1 ? 10 : 11))
            .lore(
               account.getAccountConfiguration().getAdminModeJoin() == 0
                  ? "§7Clique para ativar"
                  : (account.getAccountConfiguration().getAdminModeJoin() == 1 ? "§7Clique para mudar para o modo alternado" : "§7Clique para desativar")
            )
            .build(),
         handler
      );
      this.create(
         player,
         "Items do admin",
         "Remover items ao entrar no modo admin.",
         Material.PAPER,
         account.getAccountConfiguration().isAdminRemoveItems(),
         11,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               player.sendMessage("§cAguarde para alterar outra configuração.");
            } else {
               account.getAccountConfiguration().setAdminRemoveItems(!account.getAccountConfiguration().isAdminRemoveItems());
               new AdminInventory(player, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Staffchat",
         "Visualizar mensagens do staffchat.",
         Material.PAPER,
         account.getAccountConfiguration().isSeeingStaffChat(),
         12,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               player.sendMessage("§cAguarde para alterar outra configuração.");
            } else {
               account.getAccountConfiguration().setSeeingStaffChat(!account.getAccountConfiguration().isSeeingStaffChat());
               new AdminInventory(player, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Stafflog",
         "Visualizar as logs da staff e monitorar o que estão fazendo no servidor",
         Material.PAPER,
         account.getAccountConfiguration().isSeeingLogs(),
         13,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               player.sendMessage("§cAguarde para alterar outra configuração.");
            } else {
               account.getAccountConfiguration().setSeeingLogs(!account.getAccountConfiguration().isSeeingLogs());
               new AdminInventory(player, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Espectadores",
         "Ver os espectadores das partidas",
         Material.PAPER,
         account.getAccountConfiguration().isSpectatorsEnabled(),
         14,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               player.sendMessage("§cAguarde para alterar outra configuração.");
            } else {
               account.getAccountConfiguration().setSpectatorsEnabled(!account.getAccountConfiguration().isSpectatorsEnabled());
               BukkitCommon.getInstance().getVanishManager().updateVanishToPlayer(p);
               new AdminInventory(player, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Reports",
         "Receba um aviso no chat sempre que um report for feito",
         Material.PAPER,
         account.getAccountConfiguration().isReportsEnabled(),
         15,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               player.sendMessage("§cAguarde para alterar outra configuração.");
            } else {
               account.getAccountConfiguration().setReportsEnabled(!account.getAccountConfiguration().isReportsEnabled());
               new AdminInventory(player, System.currentTimeMillis() + 500L);
            }
         }
      );
      this.create(
         player,
         "Anticheat",
         "Receba um aviso no chat sempre que um report for feito",
         Material.PAPER,
         account.getAccountConfiguration().isAnticheatEnabled(),
         16,
         menuInventory,
         (p, inv, type, stack, s) -> {
            if (wait > System.currentTimeMillis()) {
               player.sendMessage("§cAguarde para alterar outra configuração.");
            } else {
               account.getAccountConfiguration()
                  .setCheatState(
                     account.getAccountConfiguration().isAnticheatEnabled() ? AccountConfiguration.CheatState.DISABLED : AccountConfiguration.CheatState.ENABLED
                  );
               new AdminInventory(player, System.currentTimeMillis() + 500L);
            }
         }
      );
      menuInventory.open(player);
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
