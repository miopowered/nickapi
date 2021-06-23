package eu.miopowered.nickapi.plugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection.FieldAccessor;
import eu.miopowered.nickapi.utility.tinyprotocol.Reflection.MethodInvoker;
import eu.miopowered.nickapi.utility.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
public class NickPlugin extends JavaPlugin implements Listener {

    private Class<?> PLAYER_SPAWN_PACKET = Reflection.getClass("{nms}.PacketPlayOutNamedEntitySpawn");
    private FieldAccessor<UUID> PLAYER_SPAWN_UUID_FIELD = Reflection.getField(PLAYER_SPAWN_PACKET, "b", UUID.class);

    private Class<?> PLAYER_LIST_ITEM_PACKET = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo");
    private FieldAccessor<List> B_FIELD = Reflection.getField(PLAYER_LIST_ITEM_PACKET, "b", List.class);
    private Class<?> PLAYER_INFO_DATA = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo$PlayerInfoData");
    private FieldAccessor<GameProfile> D_FIELD = Reflection.getField(PLAYER_INFO_DATA, "d", GameProfile.class);
    private FieldAccessor<UUID> GAME_PROFILE_UUID_FIELD = Reflection.getField(GameProfile.class, "id", UUID.class);
    private Class<?> CHAT_COMPONENT = Reflection.getClass("{nms}.IChatBaseComponent");
    private FieldAccessor<?> PLAYER_INFO_DATA_CHAT_COMPONENT = Reflection.getField(PLAYER_INFO_DATA, "e", CHAT_COMPONENT);
    private Class<?> CHAT_SERIALIZER = Reflection.getClass("{nms}.IChatBaseComponent$ChatSerializer");
    private Class<?> CHAT_COMPONENT_TEXT = Reflection.getClass("{nms}.ChatComponentText");
    private MethodInvoker TO_COMPONENT = Reflection.getMethod(CHAT_SERIALIZER, "a", String.class);
    private Class<?> TAB_COMPLETE_PACKET = Reflection.getClass("{nms}.PacketPlayOutTabComplete");
    private FieldAccessor<String[]> TAB_COMPLETE_PACKET_A_FIELD = Reflection.getField(TAB_COMPLETE_PACKET, "a", String[].class);

    private TinyProtocol protocol;
    private String sentence = "§7Hey, wusstet ihr, dass es keine Beschränkung gibt, wie lang ein DisplayName von einem User sein kann? Ich wusste es nicht, aber sehr interessant.";
    private int tick = 0;

    @Override
    public void onEnable() {
        this.protocol = new TinyProtocol(this) {
            @Override
            public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
                return super.onPacketInAsync(sender, channel, packet);
            }

            @Override
            public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
                if (receiver == null) return packet;

                if (PLAYER_SPAWN_PACKET.isInstance(packet)) {
                    Bukkit.broadcastMessage(String.format("%s receiving player spawn", receiver.getName()));
                    if (PLAYER_SPAWN_UUID_FIELD.get(packet).toString().equals("776a08c7-01d7-4637-89b7-0026a04331ab")) {
                        PLAYER_SPAWN_UUID_FIELD.set(packet, UUID.fromString("34e57efa-5783-46c7-a9fc-890296aaba1f"));
                    }
                }
//                if (TAB_COMPLETE_PACKET.isInstance(packet)) {
//                    String[] completions = TAB_COMPLETE_PACKET_A_FIELD.get(packet);
//                    for (int i = 0; i < completions.length; i++) {
//                        if (completions[i].equalsIgnoreCase("rexlManu")) {
//                            completions[i] = "PluginStubeNET";
//                        }
//                    }
//                    TAB_COMPLETE_PACKET_A_FIELD.set(packet, completions);
//                }
                if (PLAYER_LIST_ITEM_PACKET.isInstance(packet)) {
                    List list = B_FIELD.get(packet);
                    for (Object o : list) {
//                        GameProfile gameProfile = D_FIELD.get(o);
                        GameProfile profile = D_FIELD.get(o);
                        UUID uuid = GAME_PROFILE_UUID_FIELD.get(profile);
//                        System.out.println("Target: " + uuid.toString());
                        if (uuid.toString().equalsIgnoreCase("776a08c7-01d7-4637-89b7-0026a04331ab")) {
//                            System.out.println("before:");
//                            System.out.println(profile.getName());
//                            System.out.println(profile.getId().toString());
                            GameProfile gameProfile = new GameProfile(UUID.fromString("34e57efa-5783-46c7-a9fc-890296aaba1f"), "PluginStubeNET");
//                            GAME_PROFILE_UUID_FIELD.set(gameProfile, );
//
//                            try {
//                                PLAYER_INFO_DATA_CHAT_COMPONENT.set(o, CHAT_COMPONENT_TEXT.getConstructor(String.class).newInstance("§7Hey, wusstet ihr, dass es keine Beschränkung gibt, wie lang ein DisplayName von einem User sein kann? Ich wusste es nicht, aber sehr interessant."));
//                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                                e.printStackTrace();
//                            }

//                            System.out.println("spoofing uuid & name");

                            // Remove old textures
//                            gameProfile.getProperties().asMap().forEach((s, properties) -> {
//                                System.out.println(s);
//                                for (Property property : properties) {
//                                    System.out.println(property.getName());
//                                    System.out.println(property.getValue());
//                                }
//                            });
                            gameProfile.getProperties().removeAll("textures");
                            gameProfile.getProperties().put("textures", new Property(
                                    "textures",
                                    "ewogICJ0aW1lc3RhbXAiIDogMTYyNDQwOTYxNDczMSwKICAicHJvZmlsZUlkIiA6ICI0M2ZiZjA1MWZkZTk0YjMwYjkyZWZlNDgwNGI4YWIwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3ZWViIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFkODZjMWY3NGI0YjFiYjgwZTI3YzUxOTdlMWE4ZDlhYjRlNDY4M2FiMDMzZmU0MGFkYmU0ZmZiMWY0NWQxNDgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                                    "ZgB4RDHiU8cDzs3NTZAvMh1/06Py9BEEmlNy/pNjp02QiBy8QsYziQcPRAELqkQWpBM5ArAjToUFZfaMQy1ej5/P8N9S89xM0lD9Mho2cF23nfMVQY53+S4tAFuKKQ1IU7+EyWYxuCLq2vS7w1ENiOxcKXW3hW97snKlQVPP3DyZKyt07HjvlHOJlQb7KYg+cRK7f++K2qFTjcbUtubrVQRRpWdFisOwKjLqGLiQA1JTG18QXjQH5kIwAVIg5hCkrq5jgRzd3DvA6O1UvU3+pePh+QLqUwVBZV27Jpe6NVzwT9iHdcgpCKkku9Aehs/ff8UI3BqBdFI+4sQtMcAtBxqiLITPH+zeeb5EQnnS7sZE8caRT2U6GHXra2ZX81oNULspQBIe/5CrawMRpnzGZ17NS0ux7vR2pRBzfuqGdPhKbrajEFqq9WRXAzEki8DJNGt/DvFHrIn2ltdffm2ECMqQ6Bq9Kk0XE3bTK4sopKNST9R49KdBBDOLoY7YqXy4u4I9Wm5JjDTnVz8hZwUUseuXKquqHBqoG8bczxck3fzehUnEC647sbegcmAcZl35ND7OxfBispOCXFNuFrQcRDhyANIZp+RxnuBsWo6gMKqijbFfVoQLq14p/fRzaUQauTAe6wFveHuYRBatMr6Ej75cA9n9566XqCoAhvVbor8="));

//                            System.out.println("spoofing skin");

                            try {

                                Field d = o.getClass().getDeclaredField("d");
                                d.setAccessible(true);
                                Field modifiersField = Field.class.getDeclaredField("modifiers");
                                modifiersField.setAccessible(true);
                                modifiersField.setInt(d, d.getModifiers() & ~Modifier.FINAL);
                                d.set(o, gameProfile);

//                                D_FIELD.set(o, gameProfile);
                            } catch (ReflectiveOperationException e) {
                                e.printStackTrace();
                            }

                            GameProfile profile2 = D_FIELD.get(o);
//                            System.out.println("after:");
//                            System.out.println(profile2.getName());
//                            System.out.println(profile2.getId().toString());
                        }
                    }

                }
                return super.onPacketOutAsync(receiver, channel, packet);
            }
        };
        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.protocol.injectPlayer(onlinePlayer);
        }
    }

    @EventHandler
    public void handle(PlayerChatTabCompleteEvent event) {
        Collection<String> tabCompletions = event.getTabCompletions();
        tabCompletions.remove("rexlManu");
        if ("pluginstubenet".startsWith(event.getLastToken().toLowerCase())) {
            tabCompletions.add("PluginStubeNET");
        }
    }

    @EventHandler
    public void handle(PlayerCommandPreprocessEvent event) {

    }

    @Override
    public void onDisable() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.protocol.uninjectPlayer(onlinePlayer);
        }
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {

        this.protocol.uninjectPlayer(event.getPlayer());


    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        this.protocol.injectPlayer(event.getPlayer());
    }
}
