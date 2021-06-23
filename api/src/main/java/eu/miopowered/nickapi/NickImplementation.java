package eu.miopowered.nickapi;

import eu.miopowered.nickapi.listener.PacketListener;
import eu.miopowered.nickapi.listener.TabCompleteListener;
import eu.miopowered.nickapi.user.NickUser;
import eu.miopowered.nickapi.user.update.NickUpdater;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Accessors(fluent = true)
@Getter
public class NickImplementation implements NickAPI {
    private Plugin plugin;
    private List<NickUser> users;
    private PacketListener packetListener;
    private TabCompleteListener tabCompleteListener;
    private NickUpdater updater;

    NickImplementation(Plugin plugin) {
        this.plugin = plugin;
        this.users = new CopyOnWriteArrayList<>();

        this.packetListener = new PacketListener(this);
        this.tabCompleteListener = new TabCompleteListener(this);
        this.updater = new NickUpdater(this);
    }

    @Override
    public void register(NickUser NickUser) {
        this.users.add(NickUser);
    }

    @Override
    public void unregister(Player player) {
        this.users
                .stream()
                .filter(nickUser -> nickUser.realIdentity().uniqueId().equals(player.getUniqueId()))
                .findAny()
                .ifPresent(nickUser -> {
                    this.users.remove(nickUser);
                });
    }

    @Override
    public Optional<NickUser> get(UUID uniqueId) {
        return this.users.stream().filter(nickUser -> nickUser.realIdentity().uniqueId().equals(uniqueId)).findAny();
    }

}
