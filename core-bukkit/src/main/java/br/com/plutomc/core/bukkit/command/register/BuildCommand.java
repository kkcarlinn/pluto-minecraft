package br.com.plutomc.core.bukkit.command.register;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.server.ServerType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandClass {
   @CommandFramework.Command(
      name = "build",
      aliases = {"b"},
      permission = "command.build",
      console = false
   )
   public void buildCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length != 0) {
         if (sender.hasPermission("command.build-bypass")) {
            BukkitMember player = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[0], BukkitMember.class);
            if (player == null) {
               sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[0]));
               return;
            }

            player.setBuildEnabled(!player.isBuildEnabled());
            sender.sendMessage(
               sender.getLanguage().t("command-build-target-" + (player.isBuildEnabled() ? "enabled" : "disabled"), "%target%", player.getName())
            );
            player.sendMessage("§%command-build-" + (player.isBuildEnabled() ? "enabled" : "disabled") + "%§");
         } else {
            sender.sendMessage("§cVocê não tem acesso a esse comando neste servidor no momento.");
         }
      } else {
         if (CommonPlugin.getInstance().getServerType() != ServerType.BUILD && !sender.hasPermission("command.build-bypass")) {
            sender.sendMessage("§cVocê não tem acesso a esse comando neste servidor no momento.");
         } else {
            BukkitMember player = cmdArgs.getSenderAsMember(BukkitMember.class);
            player.setBuildEnabled(!player.isBuildEnabled());
            player.sendMessage("§%command-build-" + (player.isBuildEnabled() ? "enabled" : "disabled") + "%§");
         }
      }
   }

   @CommandFramework.Command(
      name = "wand",
      permission = "command.build"
   )
   public void wandCommand(CommandArgs cmdArgs) {
      if (cmdArgs.isPlayer()) {
         BukkitCommon.getInstance().getBlockManager().giveWand(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
         cmdArgs.getSender().sendMessage(" §a* §fVocê recebeu a varinha do §aWorldedit§f!");
      }
   }

   @CommandFramework.Command(
      name = "set",
      permission = "command.build"
   )
   public void setCommand(CommandArgs cmdArgs) {
      if (cmdArgs.isPlayer()) {
         Player player = ((BukkitMember)cmdArgs.getSender()).getPlayer();
         String[] args = cmdArgs.getArgs();
         if (args.length == 0) {
            player.sendMessage(" §eUse §b/set <material:id>§e para setar um bloco.");
         } else {
            Material blockMaterial = null;
            byte blockId = 0;
            if (args[0].contains(":")) {
               blockMaterial = Material.getMaterial(args[0].split(":")[0].toUpperCase());
               if (blockMaterial == null) {
                  try {
                     blockMaterial = Material.getMaterial(Integer.valueOf(args[0].split(":")[0]));
                  } catch (NumberFormatException var14) {
                     player.sendMessage("§cNão foi possível encontrar esse bloco!");
                     return;
                  }
               }

               try {
                  blockId = Byte.valueOf(args[0].split(":")[1]);
               } catch (Exception var13) {
                  player.sendMessage("§cO bloco " + args[0] + " não existe!");
                  return;
               }
            } else {
               blockMaterial = Material.getMaterial(args[0]);
               if (blockMaterial == null) {
                  try {
                     blockMaterial = Material.getMaterial(Integer.valueOf(args[0]));
                  } catch (NumberFormatException var12) {
                     player.sendMessage("§cNão foi possível encontrar esse bloco!");
                     return;
                  }
               }
            }

            if (blockMaterial == null) {
               player.sendMessage(" §cNão foi possível encontrar o bloco " + args[0] + "!");
            } else if (!BukkitCommon.getInstance().getBlockManager().hasFirstPosition(player)) {
               player.sendMessage("§cA primeira posição não foi setada!");
            } else if (!BukkitCommon.getInstance().getBlockManager().hasSecondPosition(player)) {
               player.sendMessage("§cA segunda posição não foi setada!");
            } else {
               Location first = BukkitCommon.getInstance().getBlockManager().getFirstPosition(player);
               Location second = BukkitCommon.getInstance().getBlockManager().getSecondPosition(player);
               Map<Location, BlockState> map = new HashMap<>();
               int amount = 0;

               for(Location location : BukkitCommon.getInstance().getBlockManager().getLocationsFromTwoPoints(first, second)) {
                  map.put(location.clone(), location.getBlock().getState());
                  if (location.getBlock().getType() != blockMaterial || location.getBlock().getData() != blockId) {
                     BukkitCommon.getInstance().getBlockManager().setBlockFast(location.getWorld(), location, blockMaterial.getId(), blockId);
                     ++amount;
                  }
               }

               BukkitCommon.getInstance().getBlockManager().addUndo(player, map);
               player.sendMessage("§dVocê colocou " + amount + " blocos!");
            }
         }
      }
   }

   @CommandFramework.Command(
      name = "undo",
      permission = "command.build"
   )
   public void undoCommand(CommandArgs cmdArgs) {
      if (cmdArgs.isPlayer()) {
         Player player = ((BukkitMember)cmdArgs.getSender()).getPlayer();
         if (!BukkitCommon.getInstance().getBlockManager().hasUndoList(player)) {
            player.sendMessage("§cVocê não tem nada para desfazer");
         } else {
            Map<Location, BlockState> map = BukkitCommon.getInstance()
               .getBlockManager()
               .getUndoList(player)
               .get(BukkitCommon.getInstance().getBlockManager().getUndoList(player).size() - 1);
            int amount = 0;

            for(Entry<Location, BlockState> entry : map.entrySet()) {
               BukkitCommon.getInstance()
                  .getBlockManager()
                  .setBlockFast(entry.getKey().getWorld(), entry.getKey(), entry.getValue().getType().getId(), entry.getValue().getData().getData());
               ++amount;
            }

            BukkitCommon.getInstance().getBlockManager().removeUndo(player, map);
            player.sendMessage("§dVocê colocou " + amount + " blocos!");
         }
      }
   }
}
