package br.com.plutomc.lobby.bedwars;

import br.com.plutomc.core.bukkit.utils.character.handler.ActionHandler;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.lobby.bedwars.listener.ScoreboardListener;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LobbyMain extends LobbyHost {

    private static LobbyMain instance;

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();

        Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), this);

        this.createCharacter(
                "npc-solo",
                "CptSkinny",
                new ActionHandler() {
                    @Override
                    public boolean onInteract(Player player, boolean right) {
                        LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.BW_SOLO});
                        return false;
                    }
                },
                Arrays.asList(
                        ServerType.BW_SOLO
                ),
                new String[]{"§bSolo"}
        );
        this.createCharacter(
                "npc-duo",
                "bedwars_1",
                new ActionHandler() {
                    @Override
                    public boolean onInteract(Player player, boolean right) {
                        LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.BW_DUOS});
                        return false;
                    }
                },
                Arrays.asList(
                        ServerType.BW_DUOS
                ),
                new String[]{"§bDuplas"}
        );
    }

    public static LobbyMain getInstance() {
        return instance;
    }
}
