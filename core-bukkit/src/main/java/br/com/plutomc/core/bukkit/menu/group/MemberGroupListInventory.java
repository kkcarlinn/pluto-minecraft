package br.com.plutomc.core.bukkit.menu.group;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.permission.GroupInfo;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MemberGroupListInventory {
   private Player player;
   private Group group;
   private List<MenuItem> items;
   private int page = 1;

   public MemberGroupListInventory(Player player, Group group, List<Member> memberList) {
      this.player = player;
      this.group = group;
      boolean skipMember = group != CommonPlugin.getInstance().getPluginInfo().getDefaultGroup();
      this.items = memberList.stream()
         .sorted((o1, o2) -> o1.getPlayerName().compareTo(o2.getPlayerName()))
         .map(
            member -> {
               ItemBuilder itemBuilder = new ItemBuilder()
                  .name(member.getDefaultTag().getRealPrefix() + member.getPlayerName())
                  .type(Material.SKULL_ITEM)
                  .durability(3)
                  .skin(member.getName())
                  .lore("");
      
               for(Entry<Group, GroupInfo> entry : member.getGroups()
                  .entrySet()
                  .stream()
                  .map(e -> new SimpleEntry<>(CommonPlugin.getInstance().getPluginInfo().getGroupByName(e.getKey()), e.getValue()))
                  .sorted((o1, o2) -> (o1.getKey().getId() - o2.getKey().getId()) * -1)
                  .collect(Collectors.toList())) {
                  if (!skipMember || entry.getKey() != CommonPlugin.getInstance().getPluginInfo().getDefaultGroup()) {
                     itemBuilder.lore(
                        "§7Grupo: " + CommonPlugin.getInstance().getPluginInfo().getTagByName(entry.getKey().getGroupName()).getTagPrefix(),
                        "§7Desde de: §f" + CommonConst.DATE_FORMAT.format(entry.getValue().getGivenDate()),
                        "§7Autor: §f" + entry.getValue().getAuthorName()
                     );
                     if (!entry.getValue().isPermanent()) {
                        if (entry.getValue().hasExpired()) {
                           itemBuilder.lore("§cO cargo expirou.");
                        } else {
                           itemBuilder.lore("§7Expira em: §f" + DateUtils.getTime(Language.getLanguage(player.getUniqueId()), entry.getValue().getExpireTime()));
                        }
                     }
      
                     itemBuilder.lore("");
                  }
               }
      
               return new MenuItem(itemBuilder.build(), (p, inv, type, stack, slot) -> new MemberGroupInventory(player, member, group, this.items, this.page));
            }
         )
         .collect(Collectors.toList());
      this.create();
   }

   public MemberGroupListInventory(Player player, Group group, List<MenuItem> items, int page) {
      this.player = player;
      this.group = group;
      this.items = items;
      this.page = page;
      this.create();
   }

   public void create() {
      MenuInventory menuInventory = new MenuInventory("§7Listando " + this.group.getGroupName() + " (" + this.items.size() + ")", 5);
      int pageStart = 0;
      int pageEnd = 21;
      if (this.page > 1) {
         pageStart = (this.page - 1) * 21;
         pageEnd = this.page * 21;
      }

      if (pageEnd > this.items.size()) {
         pageEnd = this.items.size();
      }

      int w = 10;

      for(int i = pageStart; i < pageEnd; ++i) {
         MenuItem item = this.items.get(i);
         menuInventory.setItem(item, w);
         if (w % 9 == 7) {
            w += 3;
         } else {
            ++w;
         }
      }

      if (this.page != 1) {
         menuInventory.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (this.page - 1)).build(),
               (p, inv, type, stack, s) -> new MemberGroupListInventory(this.player, this.group, this.items, this.page - 1)
            ),
            39
         );
      }

      if (Math.ceil((double)(this.items.size() / 21)) + 1.0 > (double)this.page) {
         menuInventory.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (this.page + 1)).build(),
               (p, inventory, clickType, itemx, slot) -> new MemberGroupListInventory(this.player, this.group, this.items, this.page + 1)
            ),
            41
         );
      }

      menuInventory.open(this.player);
   }
}
