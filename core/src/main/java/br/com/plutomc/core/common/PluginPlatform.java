package br.com.plutomc.core.common;

import java.util.UUID;
import java.util.logging.Logger;

public interface PluginPlatform {
   UUID getUniqueId(String var1);

   String getName(UUID var1);

   void runAsync(Runnable var1);

   void runAsync(Runnable var1, long var2);

   void runAsync(Runnable var1, long var2, long var4);

   void run(Runnable var1, long var2);

   void run(Runnable var1, long var2, long var4);

   void shutdown(String var1);

   Logger getLogger();

   void dispatchCommand(String var1);

   void broadcast(String var1);

   void broadcast(String var1, String var2);
}
