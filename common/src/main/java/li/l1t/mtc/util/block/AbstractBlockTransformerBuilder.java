/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.util.block;

import com.google.common.base.Preconditions;
import li.l1t.common.misc.XyLocation;
import org.bukkit.block.Block;

import java.util.function.Consumer;

/**
 * Provides a fluent builder interface for block transformers.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-21
 */
abstract class AbstractBlockTransformerBuilder
        <T extends BlockTransformer, B extends AbstractBlockTransformerBuilder<T, B>> {
    protected XyLocation firstBoundary;
    protected XyLocation secondBoundary;
    protected int blocksPerTick = Integer.MAX_VALUE;
    protected Consumer<Block> transformer;

    public AbstractBlockTransformerBuilder() {
    }

    public B withLocations(XyLocation from, XyLocation to) {
        this.firstBoundary = from;
        this.secondBoundary = to;
        return self();
    }

    public B withBlocksPerTick(int blocksPerTick) {
        this.blocksPerTick = blocksPerTick;
        return self();
    }

    public B withTransformer(Consumer<Block> transformer) {
        this.transformer = transformer;
        return self();
    }

    public T build() {
        Preconditions.checkState(firstBoundary != null && secondBoundary != null, "need boundary locations");
        Preconditions.checkState(transformer != null, "need transformer");
        T instance = newInstance();
        instance.setBlocksPerTick(blocksPerTick);
        instance.setTransformerFunction(transformer);
        return instance;
    }

    protected abstract T newInstance();

    protected abstract B self();
}
