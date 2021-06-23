package eu.miopowered.nickapi;

import org.bukkit.entity.Player;

public interface NickFilter {

    NickFilter EVERYONE = player -> true;

    boolean filter(Player player);
}
