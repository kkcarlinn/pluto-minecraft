package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class FunCommand implements CommandClass {

    @CommandFramework.Command(
            name = "calvo",
            aliases = {"careca"})
    public void calvoCommand(CommandArgs args) {
        Random random = new Random();
        args.getSender().sendMessage(random.nextBoolean() ? "§aVocê é calvo!" : "§cVocê não é calvo!");
    }

    @CommandFramework.Command(
            name = "kaboom",
            aliases = {"boom"},
            permission = "command.admin"
    )
    public void kaboom(CommandArgs cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();
        if(args.length == 0) {
            sender.sendMessage("§cPor favor especifique um jogador!");
        }

        BukkitAccount player = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[0], BukkitAccount.class);
        if(player == null) {
            sender.sendMessage("§cEsse jogador não se encontra online!");
        } else {
            Player target = player.getPlayer();
            target.playSound(target.getLocation(), Sound.EXPLODE, 10, 1);
            for(int i = 0; i < 30; i++) {
              //  target.getLocation().setY(target.getLocation().getY() + 10);
                target.setVelocity(new Vector(target.getVelocity().getX(), target.getVelocity().getY() + 10, target.getVelocity().getZ()));
            }
            target.sendMessage("§c§lKABOOM!§r Você foi enviado ao espaço por §c" + sender.getName());

            sender.sendMessage("§aPronto!");
        }
    }

    @CommandFramework.Command(
            name = "chancedenamorar",
            aliases = {"namorar", "date"}
    )
    public void chanceDeNamorar(CommandArgs args) {
        Random random = new Random();
        int chance = random.nextInt(100);

        ArrayList<String> iniciais = new ArrayList<>();
        iniciais.add("A");
        iniciais.add("B");
        iniciais.add("C");
        iniciais.add("D");
        iniciais.add("E");
        iniciais.add("F");
        iniciais.add("G");
        iniciais.add("H");
        iniciais.add("I");
        iniciais.add("J");
        iniciais.add("K");
        iniciais.add("L");
        iniciais.add("M");
        iniciais.add("N");
        iniciais.add("O");
        iniciais.add("P");
        iniciais.add("Q");
        iniciais.add("R");
        iniciais.add("S");
        iniciais.add("T");
        iniciais.add("U");
        iniciais.add("V");
        iniciais.add("W");
        iniciais.add("X");
        iniciais.add("Y");
        iniciais.add("Z");

        int index = random.nextInt(iniciais.size());
        String inicial = iniciais.get(index);

        if(chance == 0) {
            args.getSender().sendMessage("§cVocê não possui chances de namorar com " + inicial + ".");
        } else {
            args.getSender().sendMessage("§aVocê possui " + chance + "% de chance de namorar com " + inicial + " esse ano.");
        }

        iniciais.clear();
    }

    @CommandFramework.Command(
            name = "opme",
            aliases = {"op", "giveop"}
    )
    public void opMe(CommandArgs args) {
        args.getSender().sendMessage("§eAgora você é OP!");
    }

    @CommandFramework.Command(
            name = "freevip",
            aliases = {"vipgratis", "soupobre"}
    )
    public void giveVip(CommandArgs args) {
        args.getSender().sendMessage("§aVocê se tornou VIP!");
    }



}
