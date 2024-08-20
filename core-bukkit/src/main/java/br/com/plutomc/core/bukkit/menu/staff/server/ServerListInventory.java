package br.com.plutomc.core.bukkit.menu.staff.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ServerListInventory extends MenuInventory {
   private Player player;
   private int page;
   private List<ProxiedServer> serverList = new ArrayList<>();
   private boolean loading;
   private ServerOrdenator ordenator = ServerOrdenator.ALPHABETIC;
   private boolean asc;
   private long wait;

   public ServerListInventory(Player player, int page) {
      super("§7Lista de servidores", 5);
      this.player = player;
      this.page = page;
      if (!BukkitCommon.getInstance().isServerLog()) {
         this.loading = true;
      }

      this.handleItems();
      this.open(player);
   }

   private void handleItems() {
      if (this.loading) {
         this.setItem(
            13, new ItemBuilder().name("§aCarregando...").type(Material.BARRIER).lore("§7Estamos carregando as informações do servidores, aguarde...").build()
         );
      } else {
         this.removeItem(13);
         List<MenuItem> items = new ArrayList<>();

         for(ProxiedServer server : this.getServerList()
            .stream()
            .sorted((o1, o2) -> this.ordenator.compare(o1, o2) * (this.asc ? 1 : -1))
            .collect(Collectors.toList())) {
            items.add(
               new MenuItem(
                  new ItemBuilder()
                     .name("§a" + server.getServerId())
                     .type(Material.BOOK)
                     .lore(
                        "",
                        "§fTipo: §7" + server.getServerType().name(),
                        "",
                        "§fPlayers: §7" + server.getOnlinePlayers(),
                        "§fMáximo de players: §7" + server.getPlayersRecord(),
                        "§fPing médio: §70ms",
                        "§fLigado há: §7"
                           + DateUtils.formatDifference(
                              Language.getLanguage(this.player.getUniqueId()), (System.currentTimeMillis() - server.getStartTime()) / 1000L
                           ),
                        "",
                        "§aClique para executar ações."
                     )
                     .build(),
                  (p, inv, type, stack, slot) -> new ServerInfoInventory(this.player, server, this)
               )
            );
         }

         int pageStart = 0;
         int pageEnd = 21;
         if (this.page > 1) {
            pageStart = (this.page - 1) * 21;
            pageEnd = this.page * 21;
         }

         if (pageEnd > items.size()) {
            pageEnd = items.size();
         }

         int w = 10;

         for(int i = pageStart; i < pageEnd; ++i) {
            MenuItem item = items.get(i);
            this.setItem(item, w);
            if (w % 9 == 7) {
               w += 3;
            } else {
               ++w;
            }
         }

         this.setItem(
            40,
            new ItemBuilder()
               .name("§a§%server.order." + this.ordenator.name().toLowerCase().replace("_", "-") + "-name%§")
               .type(Material.ITEM_FRAME)
               .lore(
                  "§7§%server.order." + this.ordenator.name().toLowerCase().replace("_", "-") + "-description%§",
                  this.asc ? "§7Ordem crescente." : "§7Ordem decrescente."
               )
               .build(),
            (p, inv, type, stack, s) -> {
               if (this.wait > System.currentTimeMillis()) {
                  p.sendMessage("§cAguarde para mudar a ordenação novamente.");
               } else {
                  this.wait = System.currentTimeMillis() + 500L;
                  if (type != ClickType.RIGHT && type != ClickType.SHIFT) {
                     this.ordenator = ServerOrdenator.values()[this.ordenator.ordinal() == ServerOrdenator.values().length - 1
                        ? 0
                        : this.ordenator.ordinal() + 1];
                  } else {
                     this.asc = !this.asc;
                  }
   
                  this.handleItems();
               }
            }
         );
         if (this.page == 1) {
            this.removeItem(39);
         } else {
            this.setItem(new MenuItem(new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (this.page - 1)).build(), (p, inv, type, stack, s) -> {
               --this.page;
               this.handleItems();
            }), 39);
         }

         if (Math.ceil((double)(items.size() / 21)) + 1.0 > (double)this.page) {
            this.setItem(
               new MenuItem(new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (this.page + 1)).build(), (p, inventory, clickType, itemx, slot) -> {
                  ++this.page;
                  this.handleItems();
               }), 41
            );
         } else {
            this.removeItem(41);
         }
      }
   }

   public Collection<ProxiedServer> getServerList() {
      return (Collection<ProxiedServer>)(BukkitCommon.getInstance().isServerLog()
         ? BukkitCommon.getInstance().getServerManager().getActiveServers().values()
         : this.serverList);
   }
}
