package io.github.eufranio.commanditems.data;

import io.github.eufranio.commanditems.CommandItems;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class CommandItemData extends AbstractSingleData<String, CommandItemData, CommandItemData.Immutable>
        implements DataManipulator<CommandItemData, CommandItemData.Immutable> {

    public CommandItemData(String item) {
        super(CommandItems.KEY, item);
    }

    @Override
    public Optional<CommandItemData> fill(DataHolder dataHolder, MergeFunction overlap) {
        dataHolder.get(CommandItemData.class)
                .ifPresent(data -> this.setValue(overlap.merge(this, data).getValue()));
        return Optional.of(this);
    }

    @Override
    public Optional<CommandItemData> from(DataContainer container) {
        container.getString(CommandItems.KEY.getQuery()).ifPresent(this::setValue);
        return Optional.of(this);
    }

    @Override
    public CommandItemData copy() {
        return new CommandItemData(this.getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 2;
    }

    @Override
    protected Value<String> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(CommandItems.KEY, getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(CommandItems.KEY.getQuery(), this.getValue());
    }

    public static class Immutable
            extends AbstractImmutableSingleData<String, Immutable, CommandItemData>
            implements ImmutableDataManipulator<Immutable, CommandItemData> {

        public Immutable(String item) {
            super(CommandItems.KEY, item);
        }

        @Override
        public CommandItemData asMutable() {
            return new CommandItemData(this.getValue());
        }

        @Override
        public int getContentVersion() {
            return 2;
        }

        @Override
        protected ImmutableValue<String> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(
                    CommandItems.KEY, getValue()).asImmutable();
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer().set(CommandItems.KEY.getQuery(), this.getValue());
        }

    }

    public static class Builder extends AbstractDataBuilder<CommandItemData>
            implements DataManipulatorBuilder<CommandItemData, Immutable> {

        public Builder() {
            super(CommandItemData.class, 2);
        }

        @Override
        public CommandItemData create() {
            return new CommandItemData("");
        }

        @Override
        public Optional<CommandItemData> createFrom(DataHolder dataHolder) {
            return Optional.of(dataHolder.get(CommandItemData.class).orElse(create()));
        }

        @Override
        public Optional<CommandItemData> buildContent(DataView dataView) {
            return dataView.getString(CommandItems.KEY.getQuery()).map(CommandItemData::new);
        }

    }

}
