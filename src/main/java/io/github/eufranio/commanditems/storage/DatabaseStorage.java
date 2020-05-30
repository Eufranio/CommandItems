package io.github.eufranio.commanditems.storage;

import io.github.eufranio.commanditems.CommandItems;
import io.github.eufranio.commanditems.api.Storage;
import io.github.eufranio.commanditems.storage.impl.CooldownData;
import io.github.eufranio.storage.Persistable;

import java.time.Instant;
import java.util.UUID;

public class DatabaseStorage implements Storage {

    private Persistable<CooldownData, UUID> cooldowns;

    @Override
    public void init(CommandItems plugin) {
        this.cooldowns = Persistable.create(CooldownData.class, plugin.config.get().databaseUrl);
        this.cooldowns.idFieldName = "uuid";
    }

    @Override
    public Instant getLastUse(UUID player, String itemName) {
        CooldownData data = this.cooldowns.get(player);
        if (data == null) return null;
        return data.lastUsedItems.get(itemName);
    }

    @Override
    public void updateLastUsed(UUID player, String itemName) {
        CooldownData data = this.cooldowns.getOrCreate(player);
        data.lastUsedItems.put(itemName, Instant.now());
        this.cooldowns.save(data);
    }

    @Override
    public void removeCooldown(UUID player, String itemName) {
        CooldownData data = this.cooldowns.get(player);
        if (data == null) return;
        data.lastUsedItems.remove(itemName);
        if (data.lastUsedItems.isEmpty())
            this.cooldowns.delete(data);
        else
            this.cooldowns.save(data);
    }
}
