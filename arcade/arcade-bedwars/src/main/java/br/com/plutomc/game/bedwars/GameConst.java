package br.com.plutomc.game.bedwars;

import java.util.HashMap;
import java.util.Map;
import br.com.plutomc.game.bedwars.menu.FinderInventory;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameConst {
   public static final Map<Material, Double> DAMAGE_PER_ITEM = new HashMap<>();
   public static final int STARTING_TIME = 60;
   public static final int DEFAULT_UPGRADE = 360;
   public static final String INVENCIBILITY_METADATA = "invencibility";
   public static final String BED_METADATA = "bed-island";
   public static final String CHEST_METADATA = "chest-island";
   public static final String PLAYER_TARGET_METADATA = "player-target";
   public static final String PLAYER_ARMOR_METADATA = "player-armor";
   public static final int REWARD_FOR_BREAKBED = 20;
   public static final int REWARD_FOR_ENDKILL = 10;
   public static final int REWARD_FOR_KILL = 5;
   public static final int REWARD_FOR_WIN = 50;
   public static final int MULTIPLER = 1;
   public static final double PLAYER_YBOOST_IF_OFFGROUND = 0.8;
   public static final double YBOOST_IF_ONGROUND = 1.2;
   public static final double PLAYER_YBOOST_IF_ONGROUND = 0.6;
   public static final double YBOOST_IF_OFFGROUND = 0.3;
   public static final double BOOST_IF_OFFGROUND = 1.0;
   public static final double BOOST_IF_ONGROUND = 0.5;
   public static final double PLAYER_BOOST_IF_OFFGROUND = 2.8;
   public static final double PLAYER_BOOST_IF_ONGROUND = 5.8;
   public static final double TNT_BOOST_MULTIPLIER_ONGROUND = 1.5;
   public static final double TNT_BOOST_MULTIPLIER_OFFGROUND = 1.5;
   public static final double TNT_BOOST_Y_MULTIPLIER_ONGROUND = 0.5;
   public static final double TNT_BOOST_Y_MULTIPLIER_OFFGROUND = 0.9;
   public static final ActionItemStack FINDER = new ActionItemStack(new ItemBuilder().type(Material.COMPASS).build(), new ActionItemStack.Interact() {
      @Override
      public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
         new FinderInventory(player);
         return false;
      }
   });
   public static final String SHOP_NPC = "unidade";
   public static final String UPGRADE_NPC = "staack";
}
