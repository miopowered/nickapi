package eu.miopowered.nickapi;

import eu.miopowered.nickapi.user.NickUser;
import eu.miopowered.nickapi.user.NickUserBuilder;
import eu.miopowered.nickapi.user.update.NickUpdater;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public interface NickAPI {
    static NickAPI create(Plugin plugin) {
        return new NickImplementation(plugin);
    }

    List<NickUser> users();

    void register(NickUser builder);

    void unregister(Player player);

    NickUpdater updater();
}
