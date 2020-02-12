package io.github.eufranio.commanditems.storage.impl;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Frani on 23/01/2018.
 */
@ConfigSerializable
public class FileStorageConfig {

    @Setting
    public Map<UUID, List<ItemEntry>> items = Maps.newHashMap();

    @ConfigSerializable
    public static class ItemEntry {

        public ItemEntry(){}

        public ItemEntry(String item, String lastUsed) {
            this.item = item;
            this.lastUsed = lastUsed;
        }

        @Setting
        public String item;

        @Setting
        public String lastUsed;

    }
}
