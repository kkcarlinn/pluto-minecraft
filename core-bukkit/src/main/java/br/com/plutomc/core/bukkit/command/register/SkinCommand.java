package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.menu.profile.SkinInventory;
import br.com.plutomc.core.bukkit.utils.player.PlayerAPI;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import java.util.Optional;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.member.configuration.LoginConfiguration;
import br.com.plutomc.core.common.packet.types.skin.SkinChange;
import br.com.plutomc.core.common.utils.skin.Skin;

public class SkinCommand implements CommandClass {
   @CommandFramework.Command(
      name = "skin.#",
      runAsync = true,
      console = false
   )
   public void skinresetCommand(CommandArgs cmdArgs) {
      BukkitMember sender = cmdArgs.getSenderAsMember(BukkitMember.class);
      if (!sender.isCustomSkin()) {
         sender.sendMessage("§cVocê não está usando uma skin customizada.");
      } else {
         Skin skin = null;
         if (sender.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.PREMIUM) {
            WrappedSignedProperty changePlayerSkin = PlayerAPI.changePlayerSkin(sender.getPlayer(), sender.getName(), sender.getUniqueId(), true);
            skin = new Skin(sender.getName(), sender.getUniqueId(), changePlayerSkin.getValue(), changePlayerSkin.getSignature());
         } else {
            WrappedSignedProperty changePlayerSkin = PlayerAPI.changePlayerSkin(
               sender.getPlayer(), CommonConst.DEFAULT_SKIN.getValue(), CommonConst.DEFAULT_SKIN.getSignature(), true
            );
            skin = new Skin(sender.getName(), sender.getUniqueId(), changePlayerSkin.getValue(), changePlayerSkin.getSignature());
         }

         sender.setSkin(skin, false);
         sender.sendMessage("§aSua skin foi resetada com sucesso..");
         sender.putCooldown("command.skin", 120L);
         CommonPlugin.getInstance().getServerData().sendPacket(new SkinChange(sender));
      }
   }

   @CommandFramework.Command(
      name = "skin",
      runAsync = true,
      console = false
   )
   public void skinCommand(CommandArgs cmdArgs) {
      BukkitMember sender = cmdArgs.getSenderAsMember(BukkitMember.class);
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         new SkinInventory(sender.getPlayer());
      } else if (!sender.hasPermission("command.skin")) {
         sender.sendMessage("§cVocê não tem permissão para usar skins fora do catálogo de skins.");
      } else if (sender.hasCooldown("command.skin") && !sender.hasPermission("command.admin")) {
         sender.sendMessage("§cVocê precisa esperar " + sender.getCooldownFormatted("command.skin") + " para usar eses comando novamente.");
      } else {
         String playerName = args[0];
         if (!PlayerAPI.validateName(playerName)) {
            sender.sendMessage("§cO nome inserido é inválido.");
         } else {
            Optional<Skin> optional = CommonPlugin.getInstance().getSkinData().loadData(playerName);
            if (!optional.isPresent()) {
               sender.sendMessage("§cO jogador não possui conta na mojang.");
            } else {
               Skin skin = optional.get();
               PlayerAPI.changePlayerSkin(sender.getPlayer(), skin.getValue(), skin.getSignature(), true);
               sender.setSkin(skin, true);
               sender.sendMessage("§aSua skin foi alterada para " + playerName + ".");
               sender.putCooldown("command.skin", 120L);
               CommonPlugin.getInstance().getServerData().sendPacket(new SkinChange(sender));
            }
         }
      }
   }
}
