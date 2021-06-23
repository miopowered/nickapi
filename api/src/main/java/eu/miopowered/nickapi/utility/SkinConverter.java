package eu.miopowered.nickapi.utility;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.miopowered.nickapi.identity.Skin;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.Collection;

@Accessors(fluent = true)
public class SkinConverter {

    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getClass("{obc}.entity.CraftPlayer");
    private static final Reflection.MethodInvoker CRAFT_PLAYER_GET_PROFILE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS, "getProfile");

    public static Skin convert(Player player) {
        GameProfile profile = (GameProfile) CRAFT_PLAYER_GET_PROFILE_METHOD.invoke(player);
        Collection<Property> textures = profile.getProperties().get(Skin.PROPERTY_KEY);
        if (textures == null) return Skin.EMPTY;
        Property property = textures.stream().filter(p -> p.getName().equals(Skin.PROPERTY_KEY)).findAny().orElse(null);
        if (property == null) return Skin.EMPTY;
        return new Skin(property.getValue(), property.getSignature());
    }

}
