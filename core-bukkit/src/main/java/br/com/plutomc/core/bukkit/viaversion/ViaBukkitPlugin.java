package br.com.plutomc.core.bukkit.viaversion;

import com.comphenix.protocol.ProtocolLib;
import com.viaversion.viaversion.ViaVersionPlugin;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.io.File;

public abstract class ViaBukkitPlugin extends ViaVersionPlugin implements ViaRewindPlatform {

    private ProtocolLib protocolLib;

    public ViaBukkitPlugin() {
        super();
        getConf().setCheckForUpdates(false);
        ((CraftServer) getServer()).getHandle().getServer().setAllowFlight(true);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        protocolLib = new ProtocolLib(this, getClassLoader(), getFile());
        protocolLib.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        ViaRewindConfigImpl conf = new ViaRewindConfigImpl(new File(getDataFolder(),
                "viarewind.yml"));
        conf.reloadConfig();
        this.init(conf);

        protocolLib.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        protocolLib.onDisable();
    }

    @Override
    public boolean isPluginEnabled() {
        return this.isEnabled();
    }
}