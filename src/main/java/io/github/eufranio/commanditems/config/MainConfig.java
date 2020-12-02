package io.github.eufranio.commanditems.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class MainConfig {

    @Setting(comment = "if enabled, a database will be used to store cooldowns rather than flatfile, so it's " +
            "faster. if this is enabled, you might want to change databaseUrl as well.")
    public boolean enableDatabaseStorage = true;

    @Setting(comment = "if enableDatabaseStorage is true, this is the URL of the database that the plugin should use")
    public String databaseUrl = "jdbc:sqlite:CommandItemsCooldowns.db";

    @Setting
    public Map<String, CommandItem> items = new HashMap<String, CommandItem>() {{
        put("default", new CommandItem());
    }};

    @Setting
    public Messages messages = new Messages();

    @ConfigSerializable
    public static class Messages {

        @Setting
        public String NO_PERMISSION_TO_USE_ITEM = "You don't have permission to use this item!";

        @Setting
        public String COOLDOWN = "You must wait %seconds% seconds before using this item again!";

        @Setting
        public String ITEM_USED = "&aYou successfully used this %item%&a!";

        @Setting
        public String YOU_GOT_A_ITEM = "&aYou were given a %item%&a! Enjoy!";

    }

}
