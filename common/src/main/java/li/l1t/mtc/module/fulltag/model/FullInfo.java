/*
 * MinoTopiaCore
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.mtc.module.fulltag.model;

import com.google.common.base.Preconditions;
import li.l1t.common.misc.XyLocation;
import li.l1t.common.util.LocationHelper;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.fulltag.FullTagModule;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Stores information about the current state of a full item in game. This information may change
 * quickly.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/08/15
 */
public class FullInfo {
    private static final Logger LOGGER = LogManager.getLogger(FullInfo.class);
    private static final Set<InventoryType> CONTAINER_TYPES = EnumSet.of(InventoryType.CHEST,
            InventoryType.DISPENSER, InventoryType.DROPPER, InventoryType.ENDER_CHEST,
            InventoryType.FURNACE, InventoryType.HOPPER);
    private final FullTagModule module;
    private final int id;
    @Nullable
    private FullData data = null; //lazily loaded
    private LocalDateTime timestamp;
    private String locationCode;
    private Location location;
    @Nonnull
    private UUID holderId;
    private boolean inContainer;
    private boolean valid = true;
    private boolean modified = false;

    public FullInfo(FullTagModule module, int id, LocalDateTime timestamp, String locationCode, XyLocation location,
                    @Nonnull UUID holderId, boolean inContainer, boolean valid) {
        Preconditions.checkNotNull(timestamp, "timestamp");
        this.module = module;
        this.id = id;
        this.timestamp = timestamp;
        this.locationCode = locationCode;
        this.location = location;
        this.holderId = holderId;
        this.inContainer = inContainer;
        this.valid = valid;
    }

    /**
     * Constructs a new full info that has just been created from a {@link FullData} template.
     *
     * @param module   the module managing this full info
     * @param data     the data template associated with this info
     * @param location the current location of the item
     */
    public FullInfo(FullTagModule module, @Nonnull FullData data, Location location) {
        Preconditions.checkNotNull(data, "data");
        this.module = module;
        this.id = data.getId();
        this.data = data;
        this.updateTimestamp();
        this.locationCode = "initialised";
        this.location = location;
        this.holderId = data.getReceiverId();
    }

    /**
     * Notifies this object that it has been encountered in the world. This updates the data stored
     * in this object and also logs a message to Log4j2 to document the encounter.
     *
     * @param loc       the location at which the full item currently is at
     * @param locCode   an arbitrary string describing the surroundings of the item, mainly for
     *                  later investigation
     * @param uuid      the unique id of the player associated with the action, or null if no player
     *                  can be associated
     * @param inventory the inventory involved in the encounter, if any
     */
    public void notifyEncounter(Location loc, String locCode, @Nullable UUID uuid, @Nullable Inventory inventory) {
        this.inContainer = false;
        if (inventory != null) {
            if (CONTAINER_TYPES.contains(inventory.getType())) {
                this.inContainer = true;
            }
            locCode += "|" + inventory.getTitle();
        }
        this.modified = true;
        this.location = loc;
        this.locationCode = locCode;
        if (uuid != null) {
            this.holderId = uuid;
        }
        updateTimestamp();
        LOGGER.info("#{} @ {} / {} -> {}", this.id, LocationHelper.prettyPrint(loc), uuid, locationCode);
    }

    /**
     * @return the unique integer identifier for this item
     */
    public int getId() {
        return id;
    }

    /**
     * Returns this item's metadata. If it hasn't been accessed yet, attempts to load it from the
     * database.
     *
     * @return the metadata associated with this concrete full item
     * @throws IllegalStateException if an error occurs while loading the metadata from the
     *                               database
     */
    @Nullable
    public FullData getData() throws IllegalStateException {
        if (data == null) {
            data = module.getRepository().getById(this.id);
        }
        return data;
    }

    /**
     * Gets the last seen timestamp for this concrete full item. Note that there is no setter for
     * this property since the database is supposed to handle updating the timestamp any time the
     * data is changed.
     *
     * @return the date and time that this concrete full item was last seen in game
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    protected void updateTimestamp() {
        this.timestamp = LocalDateTime.now();
        this.modified = true;
    }

    /**
     * @return an arbitrary string providing information about where this concrete full item was
     * last encountered in game
     */
    public String getLocationCode() {
        return locationCode;
    }

    /**
     * Sets the location code for this concrete full item.
     *
     * @param locationCode an arbitrary string providing information about where this concrete full
     *                     item was last encountered in game
     */
    public void setLocationCode(String locationCode) {
        prepareWrite();
        this.locationCode = locationCode;
    }

    /**
     * @return the location this concrete full item was last encountered at
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location this concrete full item was last encountered at.
     *
     * @param location the location this concrete full item was last encountered at
     */
    public void setLocation(XyLocation location) {
        prepareWrite();
        this.location = location;
    }

    /**
     * @return the unique id of the player that was most recently encountered interacting with this
     * concrete full item
     */
    @Nonnull
    public UUID getHolderId() {
        return holderId;
    }

    /**
     * Sets the unique id of the player that was most recently encountered interacting with this
     * concrete full item
     *
     * @param holderId the unique id of the player that was most recently encountered interacting
     *                 with this concrete full item
     */
    public void setHolderId(@Nonnull UUID holderId) {
        prepareWrite();
        this.holderId = holderId;
    }

    /**
     * @return whether this concrete full item seems to be in an Ender Chest currently
     */
    public boolean isInContainer() {
        return inContainer;
    }

    /**
     * Sets whether this concrete full item is in an Ender Chest.
     *
     * @param inContainer whether this concrete full item is currently in an Ender Chest
     */
    public void setInContainer(boolean inContainer) {
        prepareWrite();
        this.inContainer = inContainer;
    }

    /**
     * @return whether this concrete full item still exists in game
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validity status of this concrete full item.
     *
     * @param valid whether this concrete full item still exists in game
     */
    public void setValid(boolean valid) {
        this.modified = true;
        this.valid = valid;
    }

    /**
     * @return whether the data stored in this object has local modifications
     */
    public boolean isModified() {
        return modified;
    }

    protected void setModified(boolean modified) {
        this.modified = modified;
    }

    private void prepareWrite() {
        Preconditions.checkState(isValid(), "Attempted to update invalid FullInfo");
        modified = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullInfo)) return false;

        FullInfo fullInfo = (FullInfo) o;

        return id == fullInfo.id &&
                timestamp.equals(fullInfo.timestamp);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public String toString() {
        return "FullInfo{" +
                "id=" + id +
                ", data=" + data +
                ", locationCode='" + locationCode + '\'' +
                ", location=" + LocationHelper.prettyPrint(location) +
                ", holderId=" + holderId +
                ", valid=" + valid +
                '}';
    }
}
