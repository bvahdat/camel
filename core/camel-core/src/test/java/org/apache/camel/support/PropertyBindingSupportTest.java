/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.PropertyBindingException;
import org.apache.camel.spi.Injector;
import org.junit.Test;

/**
 * Unit test for PropertyBindingSupport
 */
public class PropertyBindingSupportTest extends ContextTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        Company work = new Company();
        work.setId(456);
        work.setName("Acme");
        context.getRegistry().bind("myWork", work);

        Properties placeholders = new Properties();
        placeholders.put("companyName", "Acme");
        placeholders.put("committer", "rider");
        context.getPropertiesComponent().setInitialProperties(placeholders);

        return context;
    }

    @Test
    public void testProperties() throws Exception {
        Foo foo = new Foo();

        Map<String, Object> prop = new HashMap<>();
        prop.put("name", "James");
        prop.put("bar.age", "33");
        prop.put("bar.{{committer}}", "true");
        prop.put("bar.gold-customer", "true");
        prop.put("bar.work.id", "123");
        prop.put("bar.work.name", "{{companyName}}");

        PropertyBindingSupport.bindProperties(context, foo, prop);

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(123, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());

        assertTrue("Should bind all properties", prop.isEmpty());
    }

    @Test
    public void testWithFluentBuilder() throws Exception {
        Foo foo = new Foo();

        Map<String, Object> prop = new HashMap<>();
        prop.put("bar.age", "33");
        prop.put("bar.{{committer}}", "true");
        prop.put("bar.gold-customer", "true");
        prop.put("bar.work.name", "{{companyName}}");

        PropertyBindingSupport.build().withCamelContext(context).withTarget(foo).withProperty("name", "James").withProperty("bar.work.id", "123")
            // and add the rest
            .withProperties(prop).bind();

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(123, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());

        assertTrue("Should bind all properties", prop.isEmpty());
    }

    @Test
    public void testPropertiesIgnoreCase() throws Exception {
        Foo foo = new Foo();

        Map<String, Object> prop = new HashMap<>();
        prop.put("name", "James");
        prop.put("bar.AGE", "33");
        prop.put("BAR.{{committer}}", "true");
        prop.put("bar.gOLd-Customer", "true");
        prop.put("bAr.work.ID", "123");
        prop.put("bar.WORk.naME", "{{companyName}}");

        PropertyBindingSupport.build().withIgnoreCase(true).bind(context, foo, prop);

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(123, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());

        assertTrue("Should bind all properties", prop.isEmpty());
    }

    @Test
    public void testBindPropertiesWithOptionPrefix() throws Exception {
        Foo foo = new Foo();

        Map<String, Object> prop = new HashMap<>();
        prop.put("my.prefix.name", "James");
        prop.put("my.prefix.bar.age", "33");
        prop.put("my.prefix.bar.{{committer}}", "true");
        prop.put("my.prefix.bar.gold-customer", "true");
        prop.put("my.prefix.bar.work.id", "123");
        prop.put("my.prefix.bar.work.name", "{{companyName}}");
        prop.put("my.other.prefix.something", "test");

        PropertyBindingSupport.build().withOptionPrefix("my.prefix.").bind(context, foo, prop);

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(123, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
        assertTrue(prop.containsKey("my.other.prefix.something"));
        assertEquals(1, prop.size());
    }

    @Test
    public void testBindPropertiesWithOptionPrefixIgnoreCase() throws Exception {
        Foo foo = new Foo();

        Map<String, Object> prop = new HashMap<>();
        prop.put("my.prefix.name", "James");
        prop.put("my.PREFIX.bar.AGE", "33");
        prop.put("my.prefix.bar.{{committer}}", "true");
        prop.put("My.prefix.bar.Gold-custoMER", "true");
        prop.put("mY.prefix.bar.work.ID", "123");
        prop.put("my.prEFIx.bar.Work.Name", "{{companyName}}");
        prop.put("my.other.prefix.something", "test");

        PropertyBindingSupport.build().withOptionPrefix("my.prefix.").withIgnoreCase(true).bind(context, foo, prop);

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(123, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
        assertTrue(prop.containsKey("my.other.prefix.something"));
        assertEquals(1, prop.size());
    }

    @Test
    public void testNested() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        PropertyBindingSupport.build().bind(context, foo, "bar.age", "33");
        PropertyBindingSupport.build().bind(context, foo, "bar.{{committer}}", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.gold-customer", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.work.id", "123");
        PropertyBindingSupport.build().bind(context, foo, "bar.work.name", "{{companyName}}");

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(123, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
    }

    @Test
    public void testNestedReference() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        PropertyBindingSupport.build().bind(context, foo, "bar.age", "33");
        PropertyBindingSupport.build().bind(context, foo, "bar.gold-customer", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.rider", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.work", "#bean:myWork");

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(456, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
    }

    @Test
    public void testNestedReferenceId() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        PropertyBindingSupport.build().bind(context, foo, "bar.age", "33");
        PropertyBindingSupport.build().bind(context, foo, "bar.gold-customer", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.rider", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.work", "#bean:myWork");

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(456, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
    }

    @Test
    public void testNestedType() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        PropertyBindingSupport.build().bind(context, foo, "bar.age", "33");
        PropertyBindingSupport.build().bind(context, foo, "bar.{{committer}}", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.gold-customer", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.work", "#type:org.apache.camel.support.Company");

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(456, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
    }

    @Test
    public void testNestedClass() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        PropertyBindingSupport.build().bind(context, foo, "bar.age", "33");
        PropertyBindingSupport.build().bind(context, foo, "bar.{{committer}}", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.gold-customer", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.work", "#class:org.apache.camel.support.Company");

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        // a new class was created so its empty
        assertEquals(0, foo.getBar().getWork().getId());
        assertEquals(null, foo.getBar().getWork().getName());
    }

    @Test
    public void testAutowired() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        PropertyBindingSupport.build().bind(context, foo, "bar.age", "33");
        PropertyBindingSupport.build().bind(context, foo, "bar.{{committer}}", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.gold-customer", "true");
        PropertyBindingSupport.build().bind(context, foo, "bar.work", "#autowired");

        assertEquals("James", foo.getName());
        assertEquals(33, foo.getBar().getAge());
        assertTrue(foo.getBar().isRider());
        assertTrue(foo.getBar().isGoldCustomer());
        assertEquals(456, foo.getBar().getWork().getId());
        assertEquals("Acme", foo.getBar().getWork().getName());
    }

    @Test
    public void testMandatory() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().withMandatory(true).bind(context, foo, "name", "James");

        boolean bound = PropertyBindingSupport.build().bind(context, foo, "bar.myAge", "33");
        assertFalse(bound);

        try {
            PropertyBindingSupport.build().withMandatory(true).bind(context, foo, "bar.myAge", "33");
            fail("Should have thrown exception");
        } catch (PropertyBindingException e) {
            assertEquals("bar.myAge", e.getPropertyName());
            assertSame(foo, e.getTarget());
        }
    }

    @Test
    public void testDoesNotExistClass() throws Exception {
        Foo foo = new Foo();

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        try {
            PropertyBindingSupport.build().bind(context, foo, "bar.work", "#class:org.apache.camel.support.DoesNotExist");
            fail("Should throw exception");
        } catch (PropertyBindingException e) {
            assertIsInstanceOf(ClassNotFoundException.class, e.getCause());
        }
    }

    @Test
    public void testNullInjectorClass() throws Exception {
        Foo foo = new Foo();

        context.setInjector(new Injector() {
            @Override
            public <T> T newInstance(Class<T> type) {
                return null;
            }

            @Override
            public <T> T newInstance(Class<T> type, String factoryMethod) {
                return null;
            }

            @Override
            public <T> T newInstance(Class<T> type, boolean postProcessBean) {
                return null;
            }

            @Override
            public boolean supportsAutoWiring() {
                return false;
            }
        });

        PropertyBindingSupport.build().bind(context, foo, "name", "James");
        try {
            PropertyBindingSupport.build().bind(context, foo, "bar.work", "#class:org.apache.camel.support.Company");
            fail("Should throw exception");
        } catch (PropertyBindingException e) {
            assertIsInstanceOf(IllegalStateException.class, e.getCause());
        }
    }

    public static class Foo {
        private String name;
        private Bar bar = new Bar();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Bar getBar() {
            return bar;
        }

        public void setBar(Bar bar) {
            this.bar = bar;
        }
    }

    public static class Bar {
        private int age;
        private boolean rider;
        private Company work; // has no default value but Camel can automatic
                              // create one if there is a setter
        private boolean goldCustomer;

        public int getAge() {
            return age;
        }

        public boolean isRider() {
            return rider;
        }

        public Company getWork() {
            return work;
        }

        public boolean isGoldCustomer() {
            return goldCustomer;
        }

        // this has no setter but only builders
        // and mix the builders with both styles (with as prefix and no prefix
        // at all)

        public Bar withAge(int age) {
            this.age = age;
            return this;
        }

        public Bar withRider(boolean rider) {
            this.rider = rider;
            return this;
        }

        public Bar work(Company work) {
            this.work = work;
            return this;
        }

        public Bar goldCustomer(boolean goldCustomer) {
            this.goldCustomer = goldCustomer;
            return this;
        }
    }

}
