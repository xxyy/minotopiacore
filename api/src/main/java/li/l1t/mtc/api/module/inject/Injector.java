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

package li.l1t.mtc.api.module.inject;


import li.l1t.mtc.api.module.inject.exception.InjectionException;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Performs injections.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.6.16
 */
public interface Injector {
    /**
     * Perform all injections given target depends on, if they have an instance available.
     *
     * @param target the target to inject into
     * @throws InjectionException if given target has a required dependency that does not have an instance
     */
    void injectAvailableDependencies(InjectionTarget<?> target);

    /**
     * Perform all injections dependant on a target.
     *
     * @param injectee the target to inject
     * @param value    the value to inject into dependants
     * @throws IllegalStateException if the injection fails
     */
    void injectAll(InjectionTarget<?> injectee, Object value);

    /**
     * Performs an injection.
     *
     * @param receiver the injection to be performed
     * @param value    the object to inject
     * @throws IllegalStateException if the injection fails
     */
    void injectInto(Injection<?> receiver, Object value);

    /**
     * Registers an instance with the injector. If there is already a target for given instance's
     * class, the instance is set on that, overriding any existing instance. If there is not yet a
     * target, a new one is created to hold the instance.
     *
     * @param instance the instance to register
     * @param clazz    the class to register as
     * @param <T>      the type of the object to register
     */
    <T> void registerInstance(T instance, Class<? super T> clazz);

    /**
     * Gets or creates a creation target for given class.
     *
     * @param clazz the class to create a target for
     * @param <T>   the type of the class
     * @return the created or cached target
     */
    @Nonnull
    <T> InjectionTarget<T> getTarget(@Nonnull Class<T> clazz);

    /**
     * @return all injection targets managed by this injector
     */
    Set<InjectionTarget<?>> getTargets();
}
