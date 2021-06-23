package eu.miopowered.nickapi.identity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.UUID;

@Accessors(fluent = true)
@AllArgsConstructor
@Data
public class Identity {
    public static Identity of(Player player) {
        return new Identity(
                player.getName(),
                player.getUniqueId(),
                Skin.of(player)
        );
    }

    private String username;
    private UUID uniqueId;
    private Skin skin;

    private GameProfile gameProfile;

    public Identity(String username, UUID uniqueId, Skin skin) {
        this.username = username;
        this.uniqueId = uniqueId;
        this.skin = skin;

        this.gameProfile = new GameProfile(this.uniqueId, this.username);
        if (Skin.EMPTY.equals(this.skin)) return;
        this.gameProfile.getProperties().put(Skin.PROPERTY_KEY, new Property(Skin.PROPERTY_KEY, this.skin.value(), this.skin.signature()));
    }
}
