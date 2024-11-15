package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.menu.staff.LanguageInventory;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import net.md_5.bungee.api.ChatColor;

public class LanguageCommand implements CommandClass {
   @CommandFramework.Command(
      name = "language",
      aliases = {"lang", "lingua", "linguagem", "idioma", "idiomas"},
      permission = "command.language"
   )
   public void languageCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         if (cmdArgs.isPlayer()) {
            new br.com.plutomc.core.bukkit.menu.LanguageInventory(((BukkitAccount)sender).getPlayer());
         } else {
            sender.sendMessage(sender.getLanguage().t("command-language-usage", "%label%", cmdArgs.getLabel()));
         }
      } else {
         Language language = Language.getLanguageByName(args[0]);
         if (language == null) {
            sender.sendMessage(sender.getLanguage().t("language-not-found", "%language%", args[0]));
         } else {
            if (cmdArgs.isPlayer()) {
               ((Account)sender).setLanguage(language);
            } else {
               CommonPlugin.getInstance().getPluginInfo().setDefaultLanguage(language);
            }

            sender.sendMessage(sender.getLanguage().t("command-language-changed", "%language%", language.getLanguageName()));
         }
      }
   }

   @CommandFramework.Command(
      name = "translate",
      permission = "command.translate"
   )
   public void translateCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage(sender.getLanguage().t("command-translate-usage", "%label%", cmdArgs.getLabel()));
         if (cmdArgs.isPlayer()) {
            new LanguageInventory(((BukkitAccount)sender).getPlayer());
         }
      } else {
         Language language = Language.getLanguageByName(args[0]);
         if (language == null) {
            sender.sendMessage(sender.getLanguage().t("language-not-found", "%language%", args[0]));
         } else if (args.length == 1) {
            sender.sendMessage("  §aIdioma " + language.getLanguageName());
            sender.sendMessage("    §fTotal de traduções: §7" + CommonPlugin.getInstance().getPluginInfo().getLanguageMap().get(language).size());
            sender.sendMessage(
               "    §fTotal de traduções incompletas: §7"
                  + CommonPlugin.getInstance()
                     .getPluginInfo()
                     .getLanguageMap()
                     .get(language)
                     .entrySet()
                     .stream()
                     .filter(entry -> entry.getValue().startsWith("[NOT FOUND: "))
                     .count()
            );

            for(Entry<String, String> entry : CommonPlugin.getInstance().getPluginInfo().getLanguageMap().get(language).entrySet()) {
               if (entry.getValue().startsWith("[NOT FOUND: ")) {
                  sender.sendMessage("      §f- " + (String)entry.getKey());
               }
            }
         } else {
            String translateKey = args[1];
            if (args.length == 2) {
               sender.sendMessage("  §aTradução da " + translateKey + " no idioma " + language.getLanguageName());
               sender.sendMessage("    §f- " + language.t(translateKey));
            } else {
               String translate = Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length)).replace("|n", "\n");
               CommonPlugin.getInstance().getPluginInfo().addTranslate(language, translateKey, translate);
               sender.sendMessage(
                  "§aA tradução do idioma "
                     + language.name()
                     + " para do key "
                     + translateKey
                     + " foi alterada para "
                     + ChatColor.translateAlternateColorCodes('&', translate)
               );
               CommonPlugin.getInstance().saveConfig();
            }
         }
      }
   }

   @CommandFramework.Completer(
      name = "language",
      aliases = {"lang", "lingua", "linguagem", "idioma", "idiomas"}
   )
   public List<String> languageCompleter(CommandArgs cmdArgs) {
      List<String> languageList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         if (cmdArgs.getArgs()[0].isEmpty()) {
            for(Language language : Language.values()) {
               languageList.add(language.name());
            }
         } else {
            for(Language language : Language.values()) {
               if (language.name().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  languageList.add(language.name());
               }
            }
         }
      }

      return languageList;
   }

   @CommandFramework.Completer(
      name = "translate"
   )
   public List<String> translateCompleter(CommandArgs cmdArgs) {
      List<String> translateList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         if (cmdArgs.getArgs()[0].isEmpty()) {
            for(Language language : Language.values()) {
               translateList.add(language.name());
            }
         } else {
            for(Language language : Language.values()) {
               if (language.name().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  translateList.add(language.name());
               }
            }
         }
      } else if (cmdArgs.getArgs().length == 2) {
         Language language = Language.getLanguageByName(cmdArgs.getArgs()[0]);
         if (language != null) {
            for(Entry<String, String> entry : CommonPlugin.getInstance()
               .getPluginInfo()
               .getLanguageMap()
               .computeIfAbsent(language, v -> new HashMap<>())
               .entrySet()
               .stream()
               .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
               .collect(Collectors.toList())) {
               if (entry.getKey().toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase())) {
                  translateList.add(entry.getKey().toLowerCase());
               }
            }
         }
      }

      return translateList;
   }
}
