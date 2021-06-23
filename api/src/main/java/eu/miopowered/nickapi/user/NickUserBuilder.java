package eu.miopowered.nickapi.user;

import eu.miopowered.nickapi.NickFilter;
import eu.miopowered.nickapi.identity.Identity;
import eu.miopowered.nickapi.identity.Skin;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
public class NickUserBuilder {

    private Player player;
    private List<NickFilter> filters;
    private Identity realIdentity;
    @Setter
    private String username;
    @Setter
    private UUID uniqueId;
    @Setter
    private Skin skin;

    NickUserBuilder(Player player) {
        this.player = player;
        this.filters = new ArrayList<>();
        this.realIdentity = Identity.of(player);
    }

    public NickUserBuilder filter(NickFilter filter) {
        this.filters.add(filter);
        return this;
    }

    public NickUser build() {
        return new ClientSideUser(
                this.realIdentity,
                new Identity(this.username, this.uniqueId, this.skin),
                this.filters
        );
    }
}
