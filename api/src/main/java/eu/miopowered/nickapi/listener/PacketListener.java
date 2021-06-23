package eu.miopowered.nickapi.listener;

import com.mojang.authlib.GameProfile;
import eu.miopowered.nickapi.NickAPI;
import eu.miopowered.nickapi.NickImplementation;
import eu.miopowered.nickapi.identity.Identity;
import eu.miopowered.nickapi.user.NickUser;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection;
import eu.miopowered.nickapi.utility.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.Collection;
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

    // Scoreboard team packet
    private static final Class<?> SCOREBOARD_TEAM_PACKET = Reflection.getClass("{nms}.PacketPlayOutScoreboardTeam");
    private static final Reflection.FieldAccessor<Collection> SCOREBOARD_TEAM_PLAYERS_FIELD = Reflection.getField(SCOREBOARD_TEAM_PACKET, "g", Collection.class);
    private static final Reflection.FieldAccessor<String> SCOREBOARD_TEAM_PREFIX_FIELD = Reflection.getField(SCOREBOARD_TEAM_PACKET, "c", String.class);
    private static final Reflection.FieldAccessor<String> SCOREBOARD_TEAM_SUFFIX_FIELD = Reflection.getField(SCOREBOARD_TEAM_PACKET, "d", String.class);

    // Chat packet
    private static final Class<?> CHAT_PACKET = Reflection.getClass("{nms}.PacketPlayOutChat");
    private static final Class<?> I_CHAT_BASE_COMPONENT_CLASS = Reflection.getClass("{nms}.IChatBaseComponent");
    private static final Reflection.MethodInvoker I_CHAT_BASE_COMPONENT_SIBLINGS_METHOD = Reflection.getMethod(I_CHAT_BASE_COMPONENT_CLASS, "a");

    private static final Reflection.FieldAccessor<?> CHAT_PACKET_COMPONENT_FIELD = Reflection.getField(CHAT_PACKET, "a", I_CHAT_BASE_COMPONENT_CLASS);
    private static final Class<?> TEXT_COMPONENT_CLASS = Reflection.getClass("{nms}.ChatComponentText");
    private static final Reflection.FieldAccessor<String> TEXT_COMPONENT_TEXT_FIELD = Reflection.getFinalField(TEXT_COMPONENT_CLASS, "b", String.class);


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
        if (SCOREBOARD_TEAM_PACKET.isInstance(packet)) {
            this.handleScoreboardTeams(receiver, packet);
        }
        if (CHAT_PACKET.isInstance(packet)) {
            this.handleChat(receiver, packet);
        }
        return packet;
    }

    private void handleChat(Player receiver, Object packet) {
        Object o = CHAT_PACKET_COMPONENT_FIELD.get(packet);
        if (I_CHAT_BASE_COMPONENT_CLASS.isInstance(o)) {
            modify(receiver, o);
        }
    }

    private void modify(Player player, Object base) {
        if (TEXT_COMPONENT_CLASS.isInstance(base)) {
            final String[] message = { TEXT_COMPONENT_TEXT_FIELD.get(base) };
            Optional<NickUser> optional = this.implementation.users()
                    .stream()
                    .filter(nickUser -> message[0].contains(nickUser.realIdentity().username()))
                    .filter(nickUser -> nickUser.filters().stream().noneMatch(filter -> filter.filter(player)))
                    .findAny();

            if (optional.isPresent()) {
                NickUser nickUser = optional.get();
                if (!message[0].contains(NickAPI.CHAT_PLACEHOLDER)) {
                    message[0] = message[0].replace(nickUser.realIdentity().username(), nickUser.fakeIdentity().username());
                } else {
                    String[] parts = message[0].split("\\{nick}");
                    parts[0] = parts[0].replace(nickUser.realIdentity().username(), nickUser.fakeIdentity().username());
                    message[0] = String.join("", parts);
                }
            }

            message[0] = message[0].replace(NickAPI.CHAT_PLACEHOLDER, "");


            TEXT_COMPONENT_TEXT_FIELD.set(base, message[0]);
        }
        Collection<Object> objects = (Collection<Object>) I_CHAT_BASE_COMPONENT_SIBLINGS_METHOD.invoke(base);

        objects.forEach(o -> modify(player, o));
    }

    private void handleScoreboardTeams(Player receiver, Object packet) {
        Collection<String> collection = (Collection<String>) SCOREBOARD_TEAM_PLAYERS_FIELD.get(packet);
        collection.forEach(s -> this.implementation
                .users()
                .stream()
                .filter(nickUser -> nickUser.realIdentity().username().equalsIgnoreCase(s))
                .filter(nickUser -> nickUser.filters().stream().noneMatch(filter -> filter.filter(receiver)))
                .map(NickUser::fakeIdentity)
                .map(Identity::username)
                .findAny()
                .ifPresent(nickUser -> {
                    collection.remove(s);
                    collection.add(nickUser);
                }));
        this.implementation
                .users()
                .stream()
                .filter(nickUser -> nickUser.filters().stream().noneMatch(filter -> filter.filter(receiver)))
                .forEach(nickUser -> {
                    SCOREBOARD_TEAM_PREFIX_FIELD.set(packet, SCOREBOARD_TEAM_PREFIX_FIELD.get(packet)
                            .replace(nickUser.realIdentity().username(), nickUser.fakeIdentity().username()));
                    SCOREBOARD_TEAM_SUFFIX_FIELD.set(packet, SCOREBOARD_TEAM_SUFFIX_FIELD.get(packet)
                            .replace(nickUser.realIdentity().username(), nickUser.fakeIdentity().username()));
                });
        SCOREBOARD_TEAM_PLAYERS_FIELD.set(packet, collection);
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
