package br.com.plutomc.core.bukkit.anticheat.hack;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.anticheat.StormCore;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;

public interface Verify extends Listener {
   HackType getHackType();

   default boolean isPlayerBypass(Player player) {
      return CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId(), BukkitMember.class).isAnticheatBypass();
   }

   default boolean isIgnore(Player player) {
      if (player.hasMetadata("anticheat-ignore")) {
         MetadataValue metadataValue = player.getMetadata("anticheat-ignore").stream().findFirst().orElse(null);
         if (metadataValue.asLong() > System.currentTimeMillis()) {
            return true;
         }

         player.removeMetadata("anticheat-ignore", BukkitCommon.getInstance());
      }

      return false;
   }

   default boolean ignore(Player player, double seconds) {
      StormCore.getInstance().ignore(player, seconds);
      return true;
   }

   default UserData getUserData(Player player) {
      return CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId(), BukkitMember.class).getUserData();
   }

   default void alert(Player player) {
      this.alert(player, "");
   }

   default void alert(Player player, String message) {
      UserData userData = this.getUserData(player);
      if (player != null && userData != null) {
         userData.pulse(this.getHackType(), message);
      }
   }
}
