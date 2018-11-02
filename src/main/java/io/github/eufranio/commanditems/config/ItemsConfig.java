package io.github.eufranio.commanditems.config;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Frani on 23/01/2018.
 */
@ConfigSerializable
public class ItemsConfig {

    public ItemsConfig() {
        items.put(UUID.randomUUID().toString(), new CommandItem());
    }

    @Setting
    public Map<String, CommandItem> items = Maps.newHashMap(); // UUID <-> CommandItem

    public CommandItem getItemFor(String uuid) {
        return this.items.get(uuid);
    }

    public boolean hasItem(String uuid) {
        return this.items.containsKey(uuid);
    }

    public String getUUID(CommandItem item) {
        return this.items.entrySet().stream().filter(e -> e.getValue().equals(item)).findFirst().get().getKey();
    }

    public void createItem(String name) {
        this.items.put(name == null ? UUID.randomUUID().toString() : name, new CommandItem());
    }

}
