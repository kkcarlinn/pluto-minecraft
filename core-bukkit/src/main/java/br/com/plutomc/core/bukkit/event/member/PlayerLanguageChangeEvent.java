package br.com.plutomc.core.bukkit.event.member;

import br.com.plutomc.core.bukkit.event.PlayerEvent;
import lombok.NonNull;
import br.com.plutomc.core.common.language.Language;
import org.bukkit.entity.Player;

public class PlayerLanguageChangeEvent extends PlayerEvent {
   private Language language;

   public PlayerLanguageChangeEvent(@NonNull Player player, @NonNull Language language) {
      super(player);
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else if (language == null) {
         throw new NullPointerException("language is marked non-null but is null");
      } else {
         this.language = language;
      }
   }

   public Language getLanguage() {
      return this.language;
   }
}
