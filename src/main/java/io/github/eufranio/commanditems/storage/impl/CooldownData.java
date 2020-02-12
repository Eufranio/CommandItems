package io.github.eufranio.commanditems.storage.impl;

import com.google.common.collect.Maps;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@DatabaseTable(tableName = "cooldowns")
public class CooldownData extends BaseDaoEnabled<CooldownData, UUID> {

    @DatabaseField(id = true)
    private UUID uuid;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<String, Instant> lastUsedItems = Maps.newHashMap();

}
