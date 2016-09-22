/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.putindance;

import li.l1t.common.misc.XyLocation;
import li.l1t.mtc.yaml.ManagedConfiguration;
import org.bukkit.DyeColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the configuration file of PutinDance.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-20
 */
class PutinDanceConfig {
    private static final String FIRST_BOUNDARY_PATH = "boundaries.first";
    private static final String SECOND_BOUNDARY_PATH = "boundaries.second";
    private static final String SPAWN_LOCATION_PATH = "spawn";
    private static final String REMOVE_DELAY_PATH = "tick-wool-remove-delay-ticks";
    private static final String VALID_COLORS_PATH = "valid-wool-colors-for-generation";
    private static final String MIN_AIR_PERCENT_PATH = "min-air-percent-per-layer";
    private static final String MAX_AIR_PERCENT_PATH = "max-air-percent-per-layer";
    private XyLocation firstBoardBoundary;
    private XyLocation secondBoardBoundary;
    private XyLocation spawnLocation;
    private long tickRemoveDelayTicks;
    private List<DyeColor> validColors;
    private int minAirPercent;
    private int maxAirPercent;

    public PutinDanceConfig() {
        ConfigurationSerialization.registerClass(XyLocation.class);
    }

    public void loadFrom(ManagedConfiguration config) {
        firstBoardBoundary = (XyLocation) config.get(FIRST_BOUNDARY_PATH);
        secondBoardBoundary = (XyLocation) config.get(SECOND_BOUNDARY_PATH);
        spawnLocation = (XyLocation) config.get(SPAWN_LOCATION_PATH);
        tickRemoveDelayTicks = config.getLong(REMOVE_DELAY_PATH, 2L * 20L);
        validColors = config.getStringList(VALID_COLORS_PATH).stream()
                .map(this::parseDyeColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        maxAirPercent = config.getInt(MAX_AIR_PERCENT_PATH, 60);
        minAirPercent = config.getInt(MIN_AIR_PERCENT_PATH, 30);
    }

    private DyeColor parseDyeColor(String str) {
        try {
            return DyeColor.valueOf(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void saveTo(ManagedConfiguration config) {
        config.set(FIRST_BOUNDARY_PATH, firstBoardBoundary);
        config.set(SECOND_BOUNDARY_PATH, secondBoardBoundary);
        config.set(SPAWN_LOCATION_PATH, spawnLocation);
        config.set(REMOVE_DELAY_PATH, tickRemoveDelayTicks);
        config.set(VALID_COLORS_PATH, validColors.stream().map(DyeColor::name).collect(Collectors.toList()));
        config.set(MIN_AIR_PERCENT_PATH, minAirPercent);
        config.set(MAX_AIR_PERCENT_PATH, maxAirPercent);
    }

    public XyLocation getFirstBoardBoundary() {
        return firstBoardBoundary;
    }

    public void setFirstBoardBoundary(XyLocation firstBoardBoundary) {
        this.firstBoardBoundary = firstBoardBoundary;
    }

    public XyLocation getSecondBoardBoundary() {
        return secondBoardBoundary;
    }

    public void setSecondBoardBoundary(XyLocation secondBoardBoundary) {
        this.secondBoardBoundary = secondBoardBoundary;
    }

    public XyLocation getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(XyLocation spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public long getTickRemoveDelayTicks() {
        return tickRemoveDelayTicks;
    }

    public void setTickRemoveDelayTicks(long tickRemoveDelayTicks) {
        this.tickRemoveDelayTicks = tickRemoveDelayTicks;
    }

    public List<DyeColor> getValidColors() {
        return validColors;
    }

    public int getMinAirPercent() {
        return minAirPercent;
    }

    public void setMinAirPercent(int minAirPercent) {
        this.minAirPercent = minAirPercent;
    }

    public int getMaxAirPercent() {
        return maxAirPercent;
    }

    public void setMaxAirPercent(int maxAirPercent) {
        this.maxAirPercent = maxAirPercent;
    }
}