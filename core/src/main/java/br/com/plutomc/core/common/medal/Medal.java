package br.com.plutomc.core.common.medal;

import java.util.List;
import net.md_5.bungee.api.ChatColor;

public class Medal {
   private String medalName;
   private String symbol;
   private String chatColor;
   private List<String> aliases;

   public ChatColor getChatColor() {
      return ChatColor.valueOf(this.chatColor);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof Medal) {
         Medal medal = (Medal)obj;
         return medal.getMedalName().equals(this.getMedalName());
      } else {
         return super.equals(obj);
      }
   }

   public String getMedalName() {
      return this.medalName;
   }

   public String getSymbol() {
      return this.symbol;
   }

   public List<String> getAliases() {
      return this.aliases;
   }

   public Medal(String medalName, String symbol, String chatColor, List<String> aliases) {
      this.medalName = medalName;
      this.symbol = symbol;
      this.chatColor = chatColor;
      this.aliases = aliases;
   }
}
