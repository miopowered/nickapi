package eu.miopowered.nickapi.user.update;

import com.mojang.authlib.GameProfile;
import eu.miopowered.nickapi.NickImplementation;
import eu.miopowered.nickapi.identity.Identity;
import eu.miopowered.nickapi.user.NickUser;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
public class NickUpdater {

    private static final Class<?> PLAYER_LIST_ITEM_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo");
    private static final Reflection.ConstructorInvoker PLAYER_LIST_ITEM_CONSTRUCTOR = Reflection.getConstructor(PLAYER_LIST_ITEM_PACKET_CLASS);

    private static final Class<?> PLAYER_INFO_ACTION_ENUM = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
    private static final Reflection.FieldAccessor<?> PLAYER_LIST_ITEM_ACTION_FIELD = Reflection.getField(PLAYER_LIST_ITEM_PACKET_CLASS, "a", PLAYER_INFO_ACTION_ENUM);

    private static final Class<?> GAME_MODE_ENUM = Reflection.getClass("{nms}.WorldSettings$EnumGamemode");
    private static final Class<?> I_CHAT_BASE_COMPONENT_CLASS = Reflection.getClass("{nms}.IChatBaseComponent");

    private static final Class<?> PLAYER_INFO_DATA_CLASS = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo$PlayerInfoData");
    private static final Reflection.ConstructorInvoker PLAYER_INFO_DATA_CONSTRUCTOR = Reflection.getConstructor(PLAYER_INFO_DATA_CLASS, PLAYER_LIST_ITEM_PACKET_CLASS, GameProfile.class, int.class, GAME_MODE_ENUM, I_CHAT_BASE_COMPONENT_CLASS);
    private static final Reflection.FieldAccessor<List> PLAYER_LIST_ITEM_INFOS_FIELD = Reflection.getField(PLAYER_LIST_ITEM_PACKET_CLASS, "b", List.class);

    private static final Class<?> ENTITY_DESTROY_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayOutEntityDestroy");
    private static final Reflection.ConstructorInvoker ENTITY_DESTROY_CONSTRUCTOR = Reflection.getConstructor(ENTITY_DESTROY_PACKET_CLASS, int[].class);

    private static final Class<?> NAMED_ENTITY_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayOutNamedEntitySpawn");
    private static final Class<?> ENTITY_HUMAN_CLASS = Reflection.getClass("{nms}.EntityHuman");
    private static final Reflection.ConstructorInvoker NAMED_ENTITY_PACKET_CONSTRUCTOR = Reflection.getConstructor(NAMED_ENTITY_PACKET_CLASS, ENTITY_HUMAN_CLASS);

    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getClass("{obc}.entity.CraftPlayer");
    private static final Reflection.MethodInvoker CRAFT_PLAYER_GET_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS, "getHandle");

    private static final Class<?> ENTITY_PLAYER_CLASS = Reflection.getClass("{nms}.EntityPlayer");
    private static final Class<?> ENTITY_PLAYER_ARRAY = Array.newInstance(ENTITY_PLAYER_CLASS, 0).getClass();
    private static final Reflection.ConstructorInvoker PLAYER_LIST_ITEM_CONSTRUCTOR_2 = Reflection.getConstructor(PLAYER_LIST_ITEM_PACKET_CLASS, PLAYER_INFO_ACTION_ENUM, ENTITY_PLAYER_ARRAY);

    private static final Class<?> HEAD_ROTATION_PACKET = Reflection.getClass("{nms}.PacketPlayOutEntityHeadRotation");
    private static final Reflection.ConstructorInvoker HEAD_ROTATION_CONSTRUCTOR = Reflection.getConstructor(HEAD_ROTATION_PACKET);
    private static final Reflection.FieldAccessor<Integer> HEAD_ROTATION_ENTITY_ID_FIELD = Reflection.getField(HEAD_ROTATION_PACKET, "a", int.class);
    private static final Reflection.FieldAccessor<Byte> HEAD_ROTATION_YAW_FIELD = Reflection.getField(HEAD_ROTATION_PACKET, "b", byte.class);
    private static final Class<?> ENUM_GAME_MODE_CLASS = Reflection.getClass("{nms}.WorldSettings$EnumGamemode");

    private NickImplementation implementation;

    public NickUpdater(NickImplementation implementation) {
        this.implementation = implementation;
    }

    public void update(NickUser nickUser, Identity identity) {
        GameProfile currentGameProfile = identity.equals(nickUser.realIdentity()) ? nickUser.fakeIdentity().gameProfile() : nickUser.realIdentity().gameProfile();

        this.destroyEntity(nickUser.viewers(), nickUser.realIdentity().uniqueId());
        this.removeFromTab(nickUser.viewers(), currentGameProfile);
        this.addToTab(nickUser.viewers(), nickUser.realIdentity().uniqueId());
        this.namedEntitySpawn(nickUser.viewers(), nickUser.realIdentity().uniqueId());
        this.fixHeadRotation(nickUser.viewers(), nickUser.realIdentity().uniqueId());
    }

    public void fixHeadRotation(List<Player> viewers, UUID uniqueId) {
        Object packet = HEAD_ROTATION_CONSTRUCTOR.invoke();
        Player target = Bukkit.getPlayer(uniqueId);
        HEAD_ROTATION_ENTITY_ID_FIELD.set(packet, target.getEntityId());
        HEAD_ROTATION_YAW_FIELD.set(packet, (byte) ((target.getLocation().getYaw() * 256.0F) / 360.0F));

        viewers.forEach(player -> this.implementation.packetListener().sendPacket(player, packet));
    }

    public void removeFromTab(List<Player> viewers, GameProfile gameProfile) {
        Object packet = PLAYER_LIST_ITEM_CONSTRUCTOR.invoke();
        PLAYER_LIST_ITEM_ACTION_FIELD.set(packet, Enum.valueOf((Class<Enum>) PLAYER_INFO_ACTION_ENUM, "REMOVE_PLAYER"));
        List list = PLAYER_LIST_ITEM_INFOS_FIELD.get(packet);
        // SURVIVAL
        list.add(PLAYER_INFO_DATA_CONSTRUCTOR.invoke(
                packet,
                gameProfile,
                1337,
                Enum.valueOf((Class<Enum>) ENUM_GAME_MODE_CLASS, "SURVIVAL"),
                null
        ));
        PLAYER_LIST_ITEM_INFOS_FIELD.set(packet, list);

        viewers.forEach(player -> this.implementation.packetListener().sendPacket(player, packet));
    }

    public void addToTab(List<Player> viewers, UUID uniqueId) {
        Object players = Array.newInstance(ENTITY_PLAYER_CLASS, 1);
        Array.set(players, 0, CRAFT_PLAYER_GET_HANDLE_METHOD.invoke(Bukkit.getPlayer(uniqueId)));

        Object packet = PLAYER_LIST_ITEM_CONSTRUCTOR_2.invoke(Enum.valueOf((Class<Enum>) PLAYER_INFO_ACTION_ENUM, "ADD_PLAYER"), players);
        viewers.forEach(player -> this.implementation.packetListener().sendPacket(player, packet));
    }

    public void destroyEntity(List<Player> viewers, UUID uniqueId) {
        Object packet = ENTITY_DESTROY_CONSTRUCTOR.invoke(new int[]{ Bukkit.getPlayer(uniqueId).getEntityId() });
        viewers.forEach(player -> this.implementation.packetListener().sendPacket(player, packet));
    }

    public void namedEntitySpawn(List<Player> viewers, UUID uniqueId) {
        Object packet = NAMED_ENTITY_PACKET_CONSTRUCTOR.invoke(CRAFT_PLAYER_GET_HANDLE_METHOD.invoke(Bukkit.getPlayer(uniqueId)));
        viewers.stream().filter(player -> !player.getUniqueId().equals(uniqueId)).forEach(player -> this.implementation.packetListener().sendPacket(player, packet));
    }

}
