package br.com.plutomc.core.bukkit.utils.permission;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.utils.permission.injector.PermissionMatcher;
import br.com.plutomc.core.bukkit.utils.permission.injector.RegExpMatcher;
import br.com.plutomc.core.bukkit.utils.permission.injector.regexperms.RegexPermissions;
import org.bukkit.Server;
import org.bukkit.event.Listener;

public class PermissionManager {
   private BukkitCommon plugin;
   private RegexPermissions regexPerms;
   protected PermissionMatcher matcher = new RegExpMatcher();

   public PermissionManager(BukkitCommon plugin) {
      this.plugin = plugin;
      this.regexPerms = new RegexPermissions(this);
   }

   public void onDisable() {
      if (this.regexPerms != null) {
         this.regexPerms.onDisable();
         this.regexPerms = null;
      }
   }

   public Server getServer() {
      return this.plugin.getServer();
   }

   public void registerListener(Listener listener) {
      this.getServer().getPluginManager().registerEvents(listener, this.plugin);
   }

   public RegexPermissions getRegexPerms() {
      return this.regexPerms;
   }

   public PermissionMatcher getPermissionMatcher() {
      return this.matcher;
   }

   public BukkitCommon getPlugin() {
      return this.plugin;
   }

   public PermissionMatcher getMatcher() {
      return this.matcher;
   }
}
