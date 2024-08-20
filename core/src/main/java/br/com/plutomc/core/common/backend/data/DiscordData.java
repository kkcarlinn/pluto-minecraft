package br.com.plutomc.core.common.backend.data;

public interface DiscordData {
   String getNameByCode(String var1, boolean var2);

   String getCodeOrCreate(String var1, String var2);
}
