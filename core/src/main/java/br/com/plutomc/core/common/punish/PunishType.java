package br.com.plutomc.core.common.punish;

public enum PunishType {
   BAN("banido"),
   MUTE("mutado"),
   KICK("expulso");

   private String descriminator;

   private PunishType(String descriminator) {
      this.descriminator = descriminator;
   }

   public String getDescriminator() {
      return this.descriminator;
   }
}
