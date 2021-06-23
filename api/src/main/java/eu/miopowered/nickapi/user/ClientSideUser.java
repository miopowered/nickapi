package eu.miopowered.nickapi.user;

import eu.miopowered.nickapi.NickFilter;
import eu.miopowered.nickapi.identity.Identity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@Accessors(fluent = true)
@Data
@AllArgsConstructor
public class ClientSideUser implements NickUser {
    private Identity realIdentity, fakeIdentity;
    private List<NickFilter> filters;

    @Override
    public List<Player> viewers() {
        return Bukkit
                .getOnlinePlayers()
                .stream()
                .filter(player -> this.filters.stream().noneMatch(filter -> filter.filter(player)))
                .collect(Collectors.toList());
    }
}
