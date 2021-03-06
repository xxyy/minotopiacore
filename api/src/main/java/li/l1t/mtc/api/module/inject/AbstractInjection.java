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

/**
 * Abstract base class for injections.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-24
 */
abstract class AbstractInjection<T> implements Injection<T> {
    private final InjectionTarget<T> dependency;
    private final InjectionTarget<?> dependant;

    public AbstractInjection(InjectionTarget<?> dependant, InjectionTarget<T> dependency) {
        this.dependant = dependant;
        this.dependency = dependency;
    }

    @Override
    public InjectionTarget<T> getDependency() {
        return dependency;
    }

    @Override
    public InjectionTarget<?> getDependant() {
        return dependant;
    }

    @Override
    public String toString() {
        return "{AbstractInjection: dependency=" + dependency +
                "of dependant=" + dependant +
                '}';
    }
}
