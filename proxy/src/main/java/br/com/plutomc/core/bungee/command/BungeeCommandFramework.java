package br.com.plutomc.core.bungee.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.account.Account;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BungeeCommandFramework implements CommandFramework {
   private final Map<String, Entry<Method, Object>> commandMap = new HashMap<>();
   private final Map<String, Entry<Method, Object>> completers = new HashMap<>();
   private final Plugin plugin;

   public BungeeCommandFramework(Plugin plugin) {
      this.plugin = plugin;
      this.plugin.getProxy().getPluginManager().registerListener(plugin, new BungeeCompleter());
   }

   public boolean handleCommand(final CommandSender sender, final String label, final String[] args) {
      for(int i = args.length; i >= 0; --i) {
         StringBuilder buffer = new StringBuilder();
         buffer.append(label.toLowerCase());

         for(int x = 0; x < i; ++x) {
            buffer.append(".").append(args[x].toLowerCase());
         }

         final String cmdLabel = buffer.toString();
         if (this.commandMap.containsKey(cmdLabel)) {
            final Entry<Method, Object> entry = this.commandMap.get(cmdLabel);
            Command command = entry.getKey().getAnnotation(Command.class);
            if (sender instanceof ProxiedPlayer) {
               ProxiedPlayer p = (ProxiedPlayer)sender;
               Account account = CommonPlugin.getInstance().getAccountManager().getAccount(p.getUniqueId());
               if (account == null) {
                  p.disconnect(TextComponent.fromLegacyText("ERRO"));
                  return true;
               }

               if (!command.permission().isEmpty() && !account.hasPermission(command.permission())) {
                  account.sendMessage(account.getLanguage().t("no-permission"));
                  return true;
               }
            } else if (!command.console()) {
               sender.sendMessage(CommonPlugin.getInstance().getPluginInfo().translate("command-only-for-player"));
               return true;
            }

            if (command.runAsync()) {
               this.plugin.getProxy().getScheduler().runAsync(this.plugin, new Runnable() {
                  @Override
                  public void run() {
                     try {
                        entry.getKey().invoke(entry.getValue(), new BungeeCommandArgs(sender, label.replace(".", " "), args, cmdLabel.split("\\.").length - 1));
                     } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException var2) {
                        var2.printStackTrace();
                     }
                  }
               });
            } else {
               try {
                  entry.getKey().invoke(entry.getValue(), new BungeeCommandArgs(sender, label, args, cmdLabel.split("\\.").length - 1));
               } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException var11) {
                  var11.printStackTrace();
               }
            }

            return true;
         }
      }

      this.defaultCommand(new BungeeCommandArgs(sender, label, args, 0));
      return true;
   }

   @Override
   public void registerCommands(CommandClass cls) {
      for(Method m : cls.getClass().getMethods()) {
         if (m.getAnnotation(Command.class) != null) {
            Command command = m.getAnnotation(Command.class);
            if (m.getParameterTypes().length <= 1 && m.getParameterTypes().length > 0 && CommandArgs.class.isAssignableFrom(m.getParameterTypes()[0])) {
               this.registerCommand(command, command.name(), m, cls);

               for(String alias : command.aliases()) {
                  this.registerCommand(command, alias, m, cls);
               }
            } else {
               System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
            }
         } else if (m.getAnnotation(Completer.class) != null) {
            Completer comp = m.getAnnotation(Completer.class);
            if (m.getParameterTypes().length <= 1 && m.getParameterTypes().length > 0 && CommandArgs.class.isAssignableFrom(m.getParameterTypes()[0])) {
               if (m.getReturnType() != List.class) {
                  System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
               } else {
                  this.registerCompleter(comp.name(), m, cls);

                  for(String alias : comp.aliases()) {
                     this.registerCompleter(alias, m, cls);
                  }
               }
            } else {
               System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
            }
         }
      }
   }

   private void registerCommand(Command command, String label, Method m, Object obj) {
      Entry<Method, Object> entry = new SimpleEntry<>(m, obj);
      this.commandMap.put(label.toLowerCase(), entry);
      String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
      this.plugin.getProxy().getPluginManager().registerCommand(this.plugin, new BungeeCommand(cmdLabel));
   }

   private void registerCompleter(String label, Method m, Object obj) {
      this.completers.put(label, new SimpleEntry<>(m, obj));
   }

   private void defaultCommand(CommandArgs args) {
      args.getSender().sendMessage("§cComando do bungeecord inacessível!");
   }

   @Override
   public Class<?> getJarClass() {
      return this.plugin.getClass();
   }

   class BungeeCommand extends net.md_5.bungee.api.plugin.Command {
      protected BungeeCommand(String label) {
         super(label);
      }

      protected BungeeCommand(String label, String permission) {
         super(label, permission, new String[0]);
      }

      public void execute(CommandSender sender, String[] args) {
         BungeeCommandFramework.this.handleCommand(sender, this.getName(), args);
      }
   }

   public class BungeeCompleter implements Listener {
      @EventHandler
      public void onTabComplete(TabCompleteEvent event) {
         if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer)event.getSender();
            String[] split = event.getCursor().replaceAll("\\s+", " ").split(" ");
            if (split.length != 0) {
               String[] args = new String[split.length - 1];

               for(int i = 1; i < split.length; ++i) {
                  args[i - 1] = split[i];
               }

               String label = split[0].substring(1);

               for(int i = args.length; i >= 0; --i) {
                  StringBuilder buffer = new StringBuilder();
                  buffer.append(label.toLowerCase());

                  for(int x = 0; x < i; ++x) {
                     buffer.append(".").append(args[x].toLowerCase());
                  }

                  String cmdLabel = buffer.toString();
                  if (BungeeCommandFramework.this.completers.containsKey(cmdLabel)) {
                     Entry<Method, Object> entry = BungeeCommandFramework.this.completers.get(cmdLabel);

                     try {
                        event.getSuggestions().clear();
                        List<String> list = (List)entry.getKey()
                           .invoke(entry.getValue(), new BungeeCommandArgs(player, label, args, cmdLabel.split("\\.").length - 1));
                        event.getSuggestions().addAll(list);
                     } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException var11) {
                        var11.printStackTrace();
                     }
                  }
               }
            }
         }
      }
   }
}
