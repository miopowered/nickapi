package eu.miopowered.nickapi.user;

import eu.miopowered.nickapi.NickFilter;
import eu.miopowered.nickapi.identity.Identity;
import org.bukkit.entity.Player;

import java.util.List;

public interface NickUser {
    static NickUserBuilder builder(Player player) {
        return new NickUserBuilder(player);
    }

    Identity realIdentity();

    Identity fakeIdentity();

    List<NickFilter> filters();

    List<Player> viewers();
}
