package io.github.eufranio.commanditems.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 23/01/2018.
 */
public class ConfigManager <T> {

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode config;
    private T node;
    private String file;
    private File configDir;
    private GuiceObjectMapperFactory mapper;
    private Task task;
    private Class<T> configClass;

    public static <T> ConfigManager<T> of(Class<T> clazz, File configDir, String file, GuiceObjectMapperFactory mapper, boolean save, Object plugin) {
        return new ConfigManager<>(clazz, configDir, file, mapper, save, plugin);
    }

    public ConfigManager(Class<T> clazz, File configDir, String file, GuiceObjectMapperFactory mapper, boolean autoSave, Object plugin) {
        this.file = file;
        this.mapper = mapper;
        this.configDir = configDir;
        this.configClass = clazz;
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        if (autoSave) {
            this.task = Task.builder()
                    .interval(60, TimeUnit.SECONDS)
                    .delayTicks(60)
                    .execute(this::reload)
                    .async()
                    .submit(plugin);
        }
        load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            File c = new File(configDir, file);
            if (!c.exists()) c.createNewFile();
            this.loader = HoconConfigurationLoader.builder().setFile(c).build();
            config = loader.load(ConfigurationOptions.defaults()
                    .setObjectMapperFactory(mapper)
                    .setShouldCopyDefaults(true));
            node = config.getValue(TypeToken.of(configClass), configClass.newInstance());
            loader.save(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T getConfig() {
        return this.node;
    }

    @SuppressWarnings("unchecked")
    public void reload() {
        try {
            config.setValue(TypeToken.of(this.configClass), node);
            loader.save(config);
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        this.task.cancel();
        this.reload();
    }

}