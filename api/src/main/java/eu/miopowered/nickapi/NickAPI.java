package eu.miopowered.nickapi;

import eu.miopowered.nickapi.user.NickUser;
import eu.miopowered.nickapi.user.update.NickUpdater;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NickAPI {
    static NickAPI create(Plugin plugin) {
        return new NickImplementation(plugin);
    }

    List<NickUser> users();

    Optional<NickUser> get(UUID uniqueId);

    void register(NickUser builder);

    void unregister(Player player);

    NickUpdater updater();
}
