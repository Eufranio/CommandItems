package io.github.eufranio.commanditems.storage;

import com.google.common.collect.Lists;
import io.github.eufranio.commanditems.CommandItems;
import io.github.eufranio.commanditems.api.Storage;
import io.github.eufranio.commanditems.config.Config;
import io.github.eufranio.commanditems.storage.impl.FileStorageConfig;

import java.time.Instant;
import java.util.UUID;

public class FileStorage implements Storage {

    private Config<FileStorageConfig> config;

    @Override
    public void init(CommandItems plugin) {
        this.config = new Config<>(FileStorageConfig.class, "Cooldowns.conf", plugin.configDir);
    }

    @Override
    public Instant getLastUse(UUID player, String itemName) {
        FileStorageConfig.ItemEntry entry = this.getEntry(player, itemName);
        return entry == null ? null : Instant.parse(entry.lastUsed);
    }

    @Override
    public void updateLastUsed(UUID player, String itemName) {
        FileStorageConfig.ItemEntry entry = this.getEntry(player, itemName);
        if (entry == null) {
            entry = new FileStorageConfig.ItemEntry(itemName, Instant.now().toString());
            this.config.get().items.get(player).add(entry);
        } else
            entry.lastUsed = Instant.now().toString();

        this.config.save();
    }

    @Override
    public void removeCooldown(UUID player, String itemName) {
        FileStorageConfig.ItemEntry entry = this.getEntry(player, itemName);
        if (entry != null)
            this.config.get().items.get(player).remove(entry);

        this.config.save();
    }

    private FileStorageConfig.ItemEntry getEntry(UUID player, String itemUuid) {
        return this.config.get().items.computeIfAbsent(player, uuid -> Lists.newArrayList())
                .stream()
                .filter(e -> e.item.equals(itemUuid))
                .findFirst().orElse(null);
    }

}
