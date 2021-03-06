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

import com.google.common.base.Preconditions;
import li.l1t.mtc.api.module.MTCModule;
import li.l1t.mtc.api.module.inject.exception.InjectionException;
import li.l1t.mtc.api.module.inject.exception.SilentFailException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a target of {@link InjectMe} injections, which may have dependencies and dependants
 * itself.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-06-11
 */
public class SimpleInjectionTarget<T> implements InjectionTarget<T> {
    private final Class<T> clazz;
    private final Map<InjectionTarget<?>, Injection<T>> dependants = new HashMap<>();
    private final Map<InjectionTarget<?>, Injection<?>> dependencies = new HashMap<>();
    private final boolean isModule;
    private Constructor<T> constructor;
    private T instance;

    /**
     * Creates a new injection target.
     *
     * @param clazz the target class, e.g. what will be injected
     */
    public SimpleInjectionTarget(Class<T> clazz) {
        this.clazz = clazz;
        isModule = MTCModule.class.isAssignableFrom(clazz);
    }

    @Override
    public <V> Injection<T> registerDependant(InjectionTarget<V> dependant, Field field) {
        InjectMe annotation = field.getAnnotation(InjectMe.class);
        Preconditions.checkNotNull(annotation, "field must have @InjectMe annotation: %s", field);
        Injection<T> injection = new FieldInjection<>(field, annotation, this, dependant);
        dependants.put(dependant, injection);
        dependant.getDependencies().put(this, injection);
        return injection;
    }

    @Override
    public <V> Injection<T> registerDependant(InjectionTarget<V> dependant, Constructor<V> constructor) {
        Injection<T> injection = new ConstructorInjection<>(constructor, this, dependant);
        dependants.put(dependant, injection);
        dependant.getDependencies().put(this, injection);
        return injection;
    }

    @Override
    public boolean hasDependencyOn(InjectionTarget<?> dependency) {
        return dependencies.containsKey(dependency);
    }

    @Override
    public boolean hasRequiredDependencyOn(InjectionTarget<?> dependency) {
        return hasDependencyOn(dependency) && dependencies.get(dependency).isRequired();
    }

    @Override
    public Stream<Injection<?>> getRequiredDependencies() {
        return dependencies.entrySet().stream()
                .filter(e -> e.getValue().isRequired())
                .map(Map.Entry::getValue);
    }

    @Override
    public T createInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Preconditions.checkState(instance == null, "Instance already present: %s", instance);
        return this.instance = instantiate();
    }

    private T instantiate() throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<T> constructor = getInjectableConstructor();
        constructor.setAccessible(true);
        return constructInstance(constructor);
    }

    @Override
    public Constructor<T> getInjectableConstructor() {
        if (this.constructor == null) {
            this.constructor = findInjectMeConstructor()
                    .orElseGet(this::findDefaultConstructorOrFail);
        }
        return this.constructor;
    }

    @SuppressWarnings("unchecked")
    private Optional<Constructor<T>> findInjectMeConstructor() {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .map(constructor -> (Constructor<T>) constructor) // https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#getConstructors--
                .filter(constructor -> constructor.getAnnotation(InjectMe.class) != null)
                .reduce((a, b) -> {
                    throw new InjectionException("can only have one @InjectMe constructor on " + clazz);
                });
    }

    private Constructor<T> findDefaultConstructorOrFail() {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new InjectionException(
                    "unable to instantiate " + clazz + ": Neither an @InjectMe annotated " +
                            "constructor nor a default constructor was found");
        }
    }

    private T constructInstance(Constructor<T> constructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<?> parameterList = Arrays.stream(constructor.getParameterTypes())
                .map(this::findConstructorDependency)
                .map(objectInjection -> objectInjection.getDependency().getInstance())
                .collect(Collectors.toList());
        Object[] parameters = parameterList.toArray(new Object[parameterList.size()]);
        return constructor.newInstance(parameters);
    }

    private Injection<?> findConstructorDependency(Class<?> dependency) {
        return getDependencies().entrySet().stream()
                .filter(entry -> dependency.isAssignableFrom(entry.getKey().getClazz()))
                .map(Map.Entry::getValue)
                .reduce((a, b) -> {
                    throw new InjectionException(String.format("Ambiguous constructor dependency: " +
                                    "%s matched at least [%s, %s] in %s",
                            dependency, a, b, constructor
                    ));
                })
                .orElseThrow(() -> new InjectionException(String.format(
                        "Unknown constructor dependency: %s in %s, required by %s",
                        dependency, getDependencies(), constructor
                )));
    }

    @Override
    public void handleMissingClass(NoClassDefFoundError error) throws SilentFailException {
        boolean isExpectedlyMissingDependency = findMissingDependencyFiltersIfAny()
                .anyMatch(s -> error.getMessage().contains(s));
        if (isExpectedlyMissingDependency) {

            throw new SilentFailException(String.format(
                    "An external dependency is missing for %s: %s",
                    clazz, error.getMessage()), error
            );
        } //else don't prevent the error from being propagated
    }

    private Stream<String> findMissingDependencyFiltersIfAny() {
        ExternalDependencies annotation = clazz.getAnnotation(ExternalDependencies.class);
        if (annotation != null) {
            return Arrays.stream(annotation.value()).map(s -> s.replace('.', '/'));
        } else {
            return Stream.of();
        }
    }

    @Override
    public boolean isModule() {
        return isModule;
    }

    @Override
    public Class<T> getClazz() {
        return clazz;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public boolean hasInstance() {
        return getInstance() != null;
    }

    @Override
    public void setInstance(T instance) {
        if (instance == null) {
            this.instance = null;
        } else {
            Preconditions.checkArgument(getClazz().isAssignableFrom(instance.getClass()));
            this.instance = instance;
        }
    }

    @Override
    public Map<InjectionTarget<?>, Injection<T>> getDependants() {
        return dependants;
    }

    @Override
    public Map<InjectionTarget<?>, Injection<?>> getDependencies() {
        return dependencies;
    }
}
