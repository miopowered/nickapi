package eu.miopowered.nickapi.listener;

import eu.miopowered.nickapi.NickImplementation;
import eu.miopowered.nickapi.identity.Identity;
import eu.miopowered.nickapi.user.NickUser;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.Collection;

@Accessors(fluent = true)
public class TabCompleteListener implements Listener {
    private NickImplementation nickImplementation;

    public TabCompleteListener(NickImplementation nickImplementation) {
        this.nickImplementation = nickImplementation;
        Bukkit.getPluginManager().registerEvents(this, this.nickImplementation.plugin());
    }

    @EventHandler
    public void handle(PluginDisableEvent event) {
        if (event.getPlugin().equals(this.nickImplementation.plugin())) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PlayerChatTabCompleteEvent event) {
        Collection<String> completions = event.getTabCompletions();
        this.nickImplementation
                .users()
                .stream()
                .map(NickUser::realIdentity)
                .map(Identity::username)
                .forEach(completions::remove);

        this.nickImplementation
                .users()
                .stream()
                .map(NickUser::fakeIdentity)
                .map(Identity::username)
                .filter(s -> s.startsWith(event.getLastToken().toLowerCase()))
                .forEach(completions::add);
    }
}
