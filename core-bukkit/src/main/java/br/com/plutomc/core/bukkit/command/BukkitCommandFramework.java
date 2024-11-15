package br.com.plutomc.core.bukkit.command;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.server.ServerType;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.CustomTimingsHandler;

public class BukkitCommandFramework implements CommandFramework {
   public static final BukkitCommandFramework INSTANCE = new BukkitCommandFramework(BukkitCommon.getInstance());
   private Plugin plugin;
   private final Map<String, Entry<Method, Object>> commandMap = new HashMap<>();
   private CommandMap map;
   private Map<String, org.bukkit.command.Command> knownCommands;

   /** @deprecated */
   public BukkitCommandFramework(Plugin plugin) {
      this.plugin = plugin;
      if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
         SimplePluginManager manager = (SimplePluginManager)plugin.getServer().getPluginManager();

         try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            this.map = (CommandMap)field.get(manager);
         } catch (NoSuchFieldException | IllegalAccessException | SecurityException | IllegalArgumentException var5) {
            var5.printStackTrace();
         }

         try {
            Field field = this.map.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            this.knownCommands = (HashMap)field.get(this.map);
         } catch (NoSuchFieldException | IllegalAccessException | SecurityException | IllegalArgumentException var4) {
            var4.printStackTrace();
         }
      }
   }

   public boolean handleCommand(final CommandSender sender, final String label, org.bukkit.command.Command cmd, final String[] args) {
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
            if (!command.console() && !(sender instanceof Player)) {
               sender.sendMessage("§%command-only-for-players%§");
               return true;
            }

            if (command.runAsync() && Bukkit.isPrimaryThread()) {
               (new BukkitRunnable() {
                  @Override
                  public void run() {
                     try {
                        entry.getKey().invoke(entry.getValue(), new BukkitCommandArgs(sender, label, args, cmdLabel.split("\\.").length - 1));
                     } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException var2) {
                        var2.printStackTrace();
                     }
                  }
               }).runTaskAsynchronously(this.plugin);
            } else {
               try {
                  entry.getKey().invoke(entry.getValue(), new BukkitCommandArgs(sender, label, args, cmdLabel.split("\\.").length - 1));
               } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException var11) {
                  var11.printStackTrace();
               }
            }

            return true;
         }
      }

      sender.sendMessage("§cO comando está inacessível no momento!");
      return true;
   }

   @Override
   public void registerCommands(CommandClass commandClass) {
      for(Method m : commandClass.getClass().getMethods()) {
         if (m.getAnnotation(Command.class) != null) {
            Command command = m.getAnnotation(Command.class);
            if (m.getParameterTypes().length <= 1 && m.getParameterTypes().length > 0 && CommandArgs.class.isAssignableFrom(m.getParameterTypes()[0])) {
               this.registerCommand(command, command.name(), m, commandClass);

               for(String alias : command.aliases()) {
                  this.registerCommand(command, alias, m, commandClass);
               }
            } else {
               System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
            }
         }
      }

      for(Method m : commandClass.getClass().getMethods()) {
         if (m.getAnnotation(Completer.class) != null) {
            Completer comp = m.getAnnotation(Completer.class);
            if (m.getParameterTypes().length <= 1 && m.getParameterTypes().length != 0 && m.getParameterTypes()[0] == CommandArgs.class) {
               if (m.getReturnType() != List.class) {
                  System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
               } else {
                  this.registerCompleter(comp.name(), m, commandClass);

                  for(String alias : comp.aliases()) {
                     this.registerCompleter(alias, m, commandClass);
                  }
               }
            } else {
               System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
            }
         }
      }
   }

   public void registerHelp() {
      Set<HelpTopic> help = new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance());

      for(String s : this.commandMap.keySet()) {
         if (!s.contains(".")) {
            org.bukkit.command.Command cmd = this.map.getCommand(s);
            HelpTopic topic = new GenericCommandHelpTopic(cmd);
            help.add(topic);
         }
      }

      IndexHelpTopic topic = new IndexHelpTopic(
         this.plugin.getName(), "All commands for " + this.plugin.getName(), null, help, "Below is a list of all " + this.plugin.getName() + " commands:"
      );
      Bukkit.getServer().getHelpMap().addTopic(topic);
   }

   private void registerCommand(Command command, String label, Method m, Object obj) {
      Entry<Method, Object> entry = new SimpleEntry<>(m, obj);
      this.commandMap.put(label.toLowerCase(), entry);
      String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
      if (this.map.getCommand(cmdLabel) == null) {
         org.bukkit.command.Command cmd = new BukkitCommand(command.name(), cmdLabel, this.plugin, command.permission());
         this.knownCommands.put(cmdLabel, cmd);
      } else if (this.map.getCommand(cmdLabel) instanceof BukkitCommand) {
         BukkitCommand bukkitCommand = (BukkitCommand)this.map.getCommand(cmdLabel);
         bukkitCommand.setPermission(command.permission());
      }

      if (!command.description().equalsIgnoreCase("") && cmdLabel == label) {
         this.map.getCommand(cmdLabel).setDescription(command.description());
      }

      if (!command.usage().equalsIgnoreCase("") && cmdLabel == label) {
         this.map.getCommand(cmdLabel).setUsage(command.usage());
      }
   }

   private void registerCompleter(String label, Method m, Object obj) {
      String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
      if (this.map.getCommand(cmdLabel) == null) {
         org.bukkit.command.Command command = new BukkitCommand(cmdLabel, cmdLabel, this.plugin, "");
         this.knownCommands.put(cmdLabel, command);
      }

      if (this.map.getCommand(cmdLabel) instanceof BukkitCommand) {
         BukkitCommand command = (BukkitCommand)this.map.getCommand(cmdLabel);
         if (command.getCompleter() == null) {
            command.setCompleter(new BukkitCompleter());
         }

         command.getCompleter().addCompleter(label, m, obj);
      } else if (this.map.getCommand(cmdLabel) instanceof PluginCommand) {
         try {
            Object command = this.map.getCommand(cmdLabel);
            Field field = command.getClass().getDeclaredField("completer");
            field.setAccessible(true);
            if (field.get(command) == null) {
               BukkitCompleter completer = new BukkitCompleter();
               completer.addCompleter(label, m, obj);
               field.set(command, completer);
            } else if (field.get(command) instanceof BukkitCompleter) {
               BukkitCompleter completer = (BukkitCompleter)field.get(command);
               completer.addCompleter(label, m, obj);
            } else {
               System.out.println("Unable to register tab completer " + m.getName() + ". A tab completer is already registered for that command!");
            }
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }
   }

   public void unregisterCommands(String... commands) {
      try {
         Field f1 = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         f1.setAccessible(true);
         CommandMap commandMap = (CommandMap)f1.get(Bukkit.getServer());
         Field f2 = commandMap.getClass().getDeclaredField("knownCommands");
         f2.setAccessible(true);

         for(String command : commands) {
            if (this.knownCommands.containsKey(command)) {
               this.knownCommands.remove(command);
               List<String> aliases = new ArrayList<>();

               for(String key : this.knownCommands.keySet()) {
                  if (key.contains(":")) {
                     String substr = key.substring(key.indexOf(":") + 1);
                     if (substr.equalsIgnoreCase(command)) {
                        aliases.add(key);
                     }
                  }
               }

               for(String alias : aliases) {
                  this.knownCommands.remove(alias);
               }
            }
         }

         Iterator<Entry<String, org.bukkit.command.Command>> iterator = this.knownCommands.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<String, org.bukkit.command.Command> entry = iterator.next();
            if (entry.getKey().contains(":")) {
               entry.getValue().unregister(commandMap);
               iterator.remove();
            }
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }
   }

   @Override
   public Class<?> getJarClass() {
      return this.plugin.getClass();
   }

   public class BukkitCommand extends org.bukkit.command.Command {
      private Plugin owningPlugin;
      private CommandExecutor executor;
      private BukkitCompleter completer;
      private String permission;

      public BukkitCommand(String fallbackPrefix, String label, Plugin owner, String permission) {
         super(label);
         this.executor = owner;
         this.owningPlugin = owner;
         this.usageMessage = "";
         this.permission = permission;
            try {
               Class<?> timingsClass = Class.forName("co.aikar.timings.Timings");
               Method method = timingsClass.getDeclaredMethod("ofSafe", String.class);
               method.setAccessible(true);
               Field field = org.bukkit.command.Command.class.getDeclaredField("timings");
               field.setAccessible(true);
               field.set(this, method.invoke(null, "** Command: " + this.getName()));
            } catch (NoSuchMethodException | ClassNotFoundException var9) {
               var9.printStackTrace();
            } catch (NoSuchFieldException var10) {
               throw new RuntimeException(var10);
            } catch (InvocationTargetException var11) {
               throw new RuntimeException(var11);
            } catch (IllegalAccessException var12) {
               throw new RuntimeException(var12);
            }
      }

      @Override
      public boolean execute(CommandSender sender, String commandLabel, String[] args) {
         boolean success = false;
         if (!this.owningPlugin.isEnabled()) {
            return false;
         } else if (!this.testPermission(sender)) {
            return true;
         } else {
            try {
               success = BukkitCommandFramework.this.handleCommand(sender, commandLabel, this, args);
            } catch (Throwable var9) {
               throw new CommandException(
                  "Unhandled exception executing command '" + commandLabel + "' in plugin " + this.owningPlugin.getDescription().getFullName(), var9
               );
            }

            if (!success && this.usageMessage.length() > 0) {
               for(String line : this.usageMessage.replace("<command>", commandLabel).split("\n")) {
                  sender.sendMessage(line);
               }
            }

            return success;
         }
      }

      @Override
      public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws CommandException, IllegalArgumentException {
         Validate.notNull(sender, "Sender cannot be null");
         Validate.notNull(args, "Arguments cannot be null");
         Validate.notNull(alias, "Alias cannot be null");
         List<String> completions = null;

         try {
            if (this.completer != null) {
               completions = this.completer.onTabComplete(sender, this, alias, args);
            }

            if (completions == null && this.executor instanceof TabCompleter) {
               completions = ((TabCompleter)this.executor).onTabComplete(sender, this, alias, args);
            }
         } catch (Throwable var11) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

            for(String arg : args) {
               message.append(arg).append(' ');
            }

            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(this.owningPlugin.getDescription().getFullName());
            throw new CommandException(message.toString(), var11);
         }

         return completions == null ? super.tabComplete(sender, alias, args) : completions;
      }

      @Override
      public boolean testPermission(CommandSender target) {
         if (this.testPermissionSilent(target)) {
            return true;
         } else {
            target.sendMessage("§%no-permission%§");
            return false;
         }
      }

      @Override
      public boolean testPermissionSilent(CommandSender target) {
         if (this.getPermission().isEmpty()) {
            return true;
         } else {
            return target instanceof Player ? target.hasPermission(this.getPermission()) : true;
         }
      }

      public void setCompleter(BukkitCompleter completer) {
         this.completer = completer;
      }

      public BukkitCompleter getCompleter() {
         return this.completer;
      }

      @Override
      public String getPermission() {
         return this.permission;
      }
   }

   public class BukkitCompleter implements TabCompleter {
      private final Map<String, Entry<Method, Object>> completers = new HashMap<>();

      public void addCompleter(String label, Method m, Object obj) {
         this.completers.put(label, new SimpleEntry<>(m, obj));
      }

      @Override
      public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
         for(int i = args.length; i >= 0; --i) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for(int x = 0; x < i; ++x) {
               if (!args[x].equals("") && !args[x].equals(" ")) {
                  buffer.append(".").append(args[x].toLowerCase());
               }
            }

            String cmdLabel = buffer.toString();
            if (this.completers.containsKey(cmdLabel)) {
               Entry<Method, Object> entry = this.completers.get(cmdLabel);

               try {
                  return (List<String>)entry.getKey().invoke(entry.getValue(), new BukkitCommandArgs(sender, label, args, cmdLabel.split("\\.").length - 1));
               } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException var10) {
                  var10.printStackTrace();
               }
            }
         }

         return null;
      }
   }
}
