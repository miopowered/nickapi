package eu.miopowered.nickapi.identity;

import eu.miopowered.nickapi.utility.SkinConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.entity.Player;

@Accessors(fluent = true)
@AllArgsConstructor
@Data
public class Skin {

    public static final String PROPERTY_KEY = "textures";
    public static final Skin EMPTY = new Skin("", "");

    public static Skin of(Player player) {
        return SkinConverter.convert(player);
    }

    private String value, signature;

    public static void main(String[] args) {
        System.out.println(PacketPlayOutPlayerInfo.class.getConstructors()[1]);
    }
}
