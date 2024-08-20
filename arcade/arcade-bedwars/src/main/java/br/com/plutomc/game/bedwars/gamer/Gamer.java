package br.com.plutomc.game.bedwars.gamer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.store.ShopCategory;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.game.bedwars.event.PlayerLevelUpEvent;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.common.member.status.Status;
import br.com.plutomc.core.common.member.status.StatusType;
import br.com.plutomc.core.common.member.status.types.BedwarsCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class Gamer extends br.com.plutomc.game.engine.gamer.Gamer {
   private Map<ShopCategory, Set<Integer>> favoriteMap = new HashMap<>();
   private transient boolean alive;
   private transient boolean spectator;
   private transient boolean shears;
   private transient SwordLevel swordLevel;
   private transient PickaxeLevel pickaxeLevel;
   private transient AxeLevel axeLevel;
   private transient ArmorLevel armorLevel;
   private transient int kills;
   private transient int finalKills;
   private transient int brokenBeds;
   private boolean teamChat;

   public Gamer(String playerName, UUID uniqueId) {
      super(playerName, uniqueId);
      this.loadGamer();
   }

   @Override
   public void loadGamer() {
      super.loadGamer();
      this.alive = false;
      this.spectator = false;
      this.shears = false;
      this.swordLevel = SwordLevel.WOOD;
      this.pickaxeLevel = PickaxeLevel.NONE;
      this.axeLevel = AxeLevel.NONE;
      this.armorLevel = ArmorLevel.LEATHER;
      this.kills = 0;
      this.finalKills = 0;
      this.brokenBeds = 0;
   }

   public void addBedBroken() {
      ++this.brokenBeds;
   }

   public void addKills(boolean finalKill) {
      ++this.kills;
      if (finalKill) {
         ++this.finalKills;
      }
   }

   public boolean isAlive() {
      return !this.spectator && this.alive;
   }

   public boolean addFavorite(ShopCategory storeCategory, int indexOf) {
      Set<Integer> set = this.favoriteMap.computeIfAbsent(storeCategory, v -> new HashSet());
      int integer = 0;

      for(Entry<ShopCategory, Set<Integer>> entry : this.favoriteMap.entrySet()) {
         integer += entry.getValue().size();
      }

      if (integer < 21) {
         if (!set.contains(indexOf)) {
            set.add(indexOf);
            this.save("favoriteMap");
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean removeFavorite(ShopCategory.ShopItem block) {
      for(ShopCategory shopCategory : ShopCategory.values()) {
         List<ShopCategory.ShopItem> list = shopCategory.getShopItem();
         Set<Integer> set = this.favoriteMap.computeIfAbsent(shopCategory, v -> new HashSet());
         int indexOf = list.indexOf(block);
         if (set.contains(indexOf)) {
            set.remove(list.indexOf(block));
            if (set.isEmpty()) {
               this.favoriteMap.remove(shopCategory);
               this.save("favoriteMap");
            }

            return true;
         }
      }

      return false;
   }

   public void checkLevel() {
      Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(this.getUniqueId(), StatusType.BEDWARS);
      int level = status.getInteger(BedwarsCategory.BEDWARS_LEVEL);
      int points = status.getInteger(BedwarsCategory.BEDWARS_POINTS);
      int maxPoints = GameMain.getInstance().getMaxPoints(level);
      if (points > maxPoints) {
         status.setInteger(BedwarsCategory.BEDWARS_LEVEL, level + 1);
         status.setInteger(BedwarsCategory.BEDWARS_POINTS, 0);
         status.save();
         Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(this.getPlayer(), this, level));
      }
   }

   public Map<ShopCategory, Set<Integer>> getFavoriteMap() {
      return this.favoriteMap;
   }

   public boolean isSpectator() {
      return this.spectator;
   }

   public boolean isShears() {
      return this.shears;
   }

   public SwordLevel getSwordLevel() {
      return this.swordLevel;
   }

   public PickaxeLevel getPickaxeLevel() {
      return this.pickaxeLevel;
   }

   public AxeLevel getAxeLevel() {
      return this.axeLevel;
   }

   public ArmorLevel getArmorLevel() {
      return this.armorLevel;
   }

   public int getKills() {
      return this.kills;
   }

   public int getFinalKills() {
      return this.finalKills;
   }

   public int getBrokenBeds() {
      return this.brokenBeds;
   }

   public boolean isTeamChat() {
      return this.teamChat;
   }

   public void setFavoriteMap(Map<ShopCategory, Set<Integer>> favoriteMap) {
      this.favoriteMap = favoriteMap;
   }

   public void setAlive(boolean alive) {
      this.alive = alive;
   }

   public void setSpectator(boolean spectator) {
      this.spectator = spectator;
   }

   public void setShears(boolean shears) {
      this.shears = shears;
   }

   public void setSwordLevel(SwordLevel swordLevel) {
      this.swordLevel = swordLevel;
   }

   public void setPickaxeLevel(PickaxeLevel pickaxeLevel) {
      this.pickaxeLevel = pickaxeLevel;
   }

   public void setAxeLevel(AxeLevel axeLevel) {
      this.axeLevel = axeLevel;
   }

   public void setArmorLevel(ArmorLevel armorLevel) {
      this.armorLevel = armorLevel;
   }

   public void setKills(int kills) {
      this.kills = kills;
   }

   public void setFinalKills(int finalKills) {
      this.finalKills = finalKills;
   }

   public void setBrokenBeds(int brokenBeds) {
      this.brokenBeds = brokenBeds;
   }

   public void setTeamChat(boolean teamChat) {
      this.teamChat = teamChat;
   }

   public static enum ArmorLevel {
      LEATHER,
      CHAINMAIL,
      IRON,
      DIAMOND;
   }

   public static enum AxeLevel {
      NONE(new ItemBuilder().build()),
      FIRST(new ItemBuilder().type(Material.WOOD_AXE).enchantment(Enchantment.DIG_SPEED).build()),
      SECOND(new ItemBuilder().type(Material.STONE_AXE).enchantment(Enchantment.DIG_SPEED).build()),
      THIRD(new ItemBuilder().type(Material.IRON_AXE).enchantment(Enchantment.DIG_SPEED, 2).build()),
      FOURTH(new ItemBuilder().type(Material.GOLD_AXE).enchantment(Enchantment.DIG_SPEED, 3).build()),
      FIFTH(new ItemBuilder().type(Material.DIAMOND_AXE).enchantment(Enchantment.DIG_SPEED, 3).build());

      private ItemStack itemStack;

      public AxeLevel getNext() {
         return this == values()[values().length - 1] ? this : values()[this.ordinal() + 1];
      }

      public AxeLevel getPrevious() {
         return this == values()[0] ? this : values()[this.ordinal() - 1];
      }

      public boolean isLastLevel() {
         return this == values()[values().length - 1];
      }

      public ShopCategory.ShopItem getAsShopItem() {
         return new ShopCategory.ShopItem(
            ItemBuilder.fromStack(this.getItemStack())
               .lore("§7Nível: §e" + this.ordinal(), "", "§7Você sempre renascerá com ao mínimo o primeiro nível.")
               .build(),
            new ShopCategory.ShopPrice(
               this.ordinal() >= FOURTH.ordinal() ? Material.GOLD_INGOT : Material.IRON_INGOT,
               this.ordinal() >= FOURTH.ordinal() ? 3 * (this.ordinal() - FOURTH.ordinal() + 1) : 10
            )
         );
      }

      public ItemStack getItemStack() {
         return this.itemStack;
      }

      private AxeLevel(ItemStack itemStack) {
         this.itemStack = itemStack;
      }
   }

   public static enum PickaxeLevel {
      NONE(new ItemBuilder().build()),
      FIRST(new ItemBuilder().type(Material.WOOD_PICKAXE).enchantment(Enchantment.DIG_SPEED).build()),
      SECOND(new ItemBuilder().type(Material.STONE_PICKAXE).enchantment(Enchantment.DIG_SPEED).build()),
      THIRD(new ItemBuilder().type(Material.IRON_PICKAXE).enchantment(Enchantment.DIG_SPEED, 2).build()),
      FOURTH(new ItemBuilder().type(Material.GOLD_PICKAXE).enchantment(Enchantment.DIG_SPEED, 3).build()),
      FIFTH(new ItemBuilder().type(Material.DIAMOND_PICKAXE).enchantment(Enchantment.DIG_SPEED, 3).build());

      private ItemStack itemStack;

      public PickaxeLevel getNext() {
         return this == values()[values().length - 1] ? this : values()[this.ordinal() + 1];
      }

      public PickaxeLevel getPrevious() {
         return this == values()[0] ? this : values()[this.ordinal() - 1];
      }

      public boolean isLastLevel() {
         return this == values()[values().length - 1];
      }

      public ShopCategory.ShopItem getAsShopItem() {
         return new ShopCategory.ShopItem(
            ItemBuilder.fromStack(this.getItemStack())
               .lore("§7Nível: §e" + this.ordinal())
               .lore("")
               .lore("§7Você sempre renascerá com ao mínimo o primeiro nível.")
               .build(),
            new ShopCategory.ShopPrice(
               this.ordinal() >= FOURTH.ordinal() ? Material.GOLD_INGOT : Material.IRON_INGOT,
               this.ordinal() >= FOURTH.ordinal() ? 3 * (this.ordinal() - FOURTH.ordinal() + 1) : 10
            )
         );
      }

      public ItemStack getItemStack() {
         return this.itemStack;
      }

      private PickaxeLevel(ItemStack itemStack) {
         this.itemStack = itemStack;
      }
   }

   public static enum SwordLevel {
      WOOD,
      STONE,
      IRON,
      DIAMOND;

      public SwordLevel getPrevious() {
         return this == WOOD ? WOOD : values()[this.ordinal() - 1];
      }
   }
}
