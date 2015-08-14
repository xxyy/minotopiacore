/*
 * Copyright (c) 2013-2015.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package io.github.xxyy.mtc.module;

import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.xxyy.mtc.MTC;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the module loading facilities.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 18/06/15
 */
public class ModuleLoaderTest {
    private static ModuleManager moduleManager;
    private static ModuleLoader loader;
    private static MTC mtc;

    @BeforeClass
    public static void initClass() {
        mtc = mock(MTC.class);
        moduleManager = new ModuleManager(mtc);
        loader = new ModuleLoader(moduleManager);
    }

    @Test
    public void testLoadAll() throws Exception {
        loader.loadAll(Arrays.asList(Module3.class, Module1.class, Module2.class, Module1.class, Module4.class),
                (meta, thrown) -> {
                    thrown.printStackTrace();
                    Assert.fail("Exception while loading " + meta + "!");
                });

        List<MTCModule> loaded2 = loader.setEnabled(loader.getLoadedModule(Module2.class), true);
        assertTrue(loaded2.stream().anyMatch(m -> m instanceof Module1)); //check that it actually returns enabled modules
        assertTrue(loaded2.stream().anyMatch(m -> m instanceof Module2));

        assertEnabledStates(true, true, false, false);
        assertThat(moduleManager.getModule(Module2.class).module1, is(moduleManager.getModule(Module1.class)));
        assertThat(moduleManager.getModule(Module2.class).module3, is(nullValue()));

        loader.setEnabled(loader.getLoadedModule(Module3.class), true);
        assertEnabledStates(true, true, true, false);
        assertThat(moduleManager.getModule(Module3.class).module1, is(moduleManager.getModule(Module1.class)));
        assertThat(moduleManager.getModule(Module3.class).module2, is(moduleManager.getModule(Module2.class)));
        assertThat(moduleManager.getModule(Module3.class).module4, is(nullValue()));
        assertThat(moduleManager.getModule(Module2.class).module3, is(moduleManager.getModule(Module3.class)));

        loader.setEnabled(loader.getLoadedModule(Module1.class), false);
        assertEnabledStates(false, false, true, false);
        assertThat(moduleManager.getModule(Module3.class).module1, is(nullValue()));
        assertThat(moduleManager.getModule(Module3.class).module2, is(nullValue()));
        assertThat(moduleManager.getModule(Module1.class), is(nullValue()));
        assertThat(moduleManager.getModule(Module2.class), is(nullValue()));
        assertThat(moduleManager.getModule(Module3.class).module4, is(moduleManager.getModule(Module4.class)));

        loader.setEnabled(loader.getLoadedModule(Module1.class), true);
        assertEnabledStates(true, false, true, false);
        assertThat(moduleManager.getModule(Module3.class).module1, is(moduleManager.getModule(Module1.class)));
    }

    @Test
    public void testExceptionSandbox() throws Exception {
        Logger logger = mock(PluginLogger.class); // Mocking the method doesn't work, nobody knows why
        Field field = JavaPlugin.class.getDeclaredField("logger"); //  Possibly related to it being final, but PowerMockito
        field.setAccessible(true); //  should actually be able to deal with that
        field.set(mtc, logger); // we need this for the logger call in registerEnabled(...)

        loader.loadAll(Arrays.asList(LoadExceptionModule.class, EnableExceptionModule.class),
                (meta, thrown) -> assertThat("wrong module failed to load",
                        meta.getClazz(), sameInstance(LoadExceptionModule.class))
        );

        assertThat(loader.getLoadedModule(LoadExceptionModule.class), is(nullValue()));

        loader.setEnabled(loader.getLoadedModule(EnableExceptionModule.class), true); //If this throws something, we fail automatically
    }

    private void assertEnabledStates(boolean m1, boolean m2, boolean m3, boolean m4) {
        assertModuleState(m1, Module1.class);
        assertModuleState(m2, Module2.class);
        assertModuleState(m3, Module3.class);
        assertModuleState(m4, Module4.class);
    }

    private void assertModuleState(boolean expectedState, Class<? extends MTCModule> clazz) {
        assertThat(clazz.getSimpleName() + " has wrong enabled state",
                moduleManager.isEnabled(clazz), is(expectedState));
    }


    private static class MockMTCModule extends MTCModuleAdapter {
        protected MockMTCModule(String name) {
            super(name);
        }

        @Override
        public boolean canBeEnabled(MTC plugin) {
            return true;
        }
    }

    private static class Module1 extends MockMTCModule {

        protected Module1() {
            super("Module1");
        }
    }

    private static class Module2 extends MockMTCModule {
        @InjectModule(required = true)
        Module1 module1;

        @InjectModule
        Module3 module3;

        Module1 ordinaryField;

        protected Module2() {
            super("Module2");
        }
    }

    private static class Module3 extends MockMTCModule {
        @InjectModule
        Module1 module1;

        @InjectModule
        Module2 module2;

        @InjectModule
        Module4 module4;

        protected Module3() {
            super("Module3");
        }
    }

    private static class Module4 extends MockMTCModule {

        protected Module4() {
            super("Module1");
        }
    }

    private static class LoadExceptionModule extends MockMTCModule {
        protected LoadExceptionModule() {
            super("LoadExceptionModule");
            throw new NoClassDefFoundError("testing errors in load");
        }
    }

    private static class EnableExceptionModule extends MockMTCModule {

        protected EnableExceptionModule() {
            super("EnableExceptionModule");
        }

        @Override
        public void enable(MTC plugin) throws Exception {
            throw new Exception("random exception in enable");
        }

        @Override
        public void disable(MTC plugin) {
            throw new NullPointerException("random exception in disable");
        }
    }
}