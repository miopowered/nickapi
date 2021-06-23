package eu.miopowered.nickapi.listener;

import com.mojang.authlib.GameProfile;
import eu.miopowered.nickapi.NickImplementation;
import eu.miopowered.nickapi.user.NickUser;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection;
import eu.miopowered.nickapi.utility.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Accessors(fluent = true)
public class PacketListener extends TinyProtocol {

    // Player spawn packet
    private static final Class<?> PLAYER_SPAWN_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayOutNamedEntitySpawn");
    private static final Reflection.FieldAccessor<UUID> PLAYER_SPAWN_PACKET_UUID_FIELD = Reflection.getField(PLAYER_SPAWN_PACKET_CLASS, "b", UUID.class);

    // Player list item packet
    private static final Class<?> PLAYER_LIST_ITEM_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo");
    private static final Reflection.FieldAccessor<List> PLAYER_LIST_ITEM_INFOS_FIELD = Reflection.getField(PLAYER_LIST_ITEM_PACKET_CLASS, "b", List.class);
    private static final Class<?> PLAYER_INFO_DATA_CLASS = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo$PlayerInfoData");
    private static final Reflection.FieldAccessor<GameProfile> PLAYER_INFO_DATA_GAME_PROFILE_FIELD = Reflection.getFinalField(PLAYER_INFO_DATA_CLASS, "d", GameProfile.class);

    private static final Class<?> PLAYER_INFO_ACTION_ENUM = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
    private static final Reflection.FieldAccessor<?> PLAYER_LIST_ITEM_ACTION_FIELD = Reflection.getField(PLAYER_LIST_ITEM_PACKET_CLASS, "a", PLAYER_INFO_ACTION_ENUM);
    private static final Reflection.MethodInvoker PLAYER_INFO_ACTION_NAME_METHOD = Reflection.getMethod(PLAYER_INFO_ACTION_ENUM, "name");

    private NickImplementation implementation;

    public PacketListener(NickImplementation implementation) {
        super(implementation.plugin());
        this.implementation = implementation;
    }

    @Override
    public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
        if (PLAYER_SPAWN_PACKET_CLASS.isInstance(packet)) {
            this.handlePlayerSpawn(receiver, packet);
        }
        if (PLAYER_LIST_ITEM_PACKET_CLASS.isInstance(packet)) {
            this.handlePlayerListItem(receiver, packet);
        }
        return packet;
    }

    private void handlePlayerListItem(Player receiver, Object packet) {
        Object enumName = PLAYER_INFO_ACTION_NAME_METHOD.invoke(PLAYER_LIST_ITEM_ACTION_FIELD.get(packet));
        if (!"ADD_PLAYER".equals(enumName.toString())) return;
        for (Object playerInfoData : PLAYER_LIST_ITEM_INFOS_FIELD.get(packet)) {
            GameProfile gameProfile = PLAYER_INFO_DATA_GAME_PROFILE_FIELD.get(playerInfoData);

            Optional<NickUser> optional = this.implementation
                    .users()
                    .stream()
                    .filter(nickUser -> nickUser.realIdentity().uniqueId().equals(gameProfile.getId()))
                    .findAny();

            if (optional.isEmpty()) continue;
            NickUser nickUser = optional.get();
            if (nickUser.filters().stream().anyMatch(filter -> filter.filter(receiver))) continue;
            PLAYER_INFO_DATA_GAME_PROFILE_FIELD.set(playerInfoData, nickUser.fakeIdentity().gameProfile());
        }
    }

    private void handlePlayerSpawn(Player receiver, Object packet) {
        UUID uuid = PLAYER_SPAWN_PACKET_UUID_FIELD.get(packet);
        this.implementation.users()
                .stream()
                .filter(nickUser -> nickUser.realIdentity().uniqueId().equals(uuid))
                .filter(nickUser -> nickUser.filters().stream().noneMatch(filter -> filter.filter(receiver)))
                .map(nickUser -> nickUser.fakeIdentity().uniqueId())
                .findAny()
                .ifPresent(uniqueId -> PLAYER_SPAWN_PACKET_UUID_FIELD.set(packet, uniqueId));
    }
}
