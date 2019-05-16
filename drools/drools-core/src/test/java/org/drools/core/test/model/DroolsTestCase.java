/*
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.test.model;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class DroolsTestCase {

    public void assertLength(final int len,
                             final Object[] array) {
        assertEquals(len, array.length, Arrays.asList(array ) + " does not have length of " + len);
    }

    public void assertLength(final int len,
                             final Collection collection) {
        assertEquals(len, collection.size(), collection + " does not have length of " + len);
    }

    public void assertNotContains (final Object obj,
                                   final Object[] array) {
        try {
            assertContains( obj, array);
            fail( Arrays.asList( array ) + " contains " + obj );
        } catch(Throwable t) {
            // do nothing as this is assertion is ok
        }
    }

    public void assertContains(final Object obj,
                               final Object[] array) {
        for ( int i = 0; i < array.length; ++i ) {
            if ( array[i] == obj ) {
                return;
            }
        }

        fail( Arrays.asList( array ) + " does not contain " + obj );
    }

    public void assertNotContains (final Object obj,
                                   final Collection collection) {
        try {
            assertContains( obj, collection);
            fail( collection + " does not contain " + obj );
        } catch(Throwable t) {
            // do nothing as this is assertion is ok
        }
    }
    
    public void assertContains(final Object obj,
                               final Collection collection) {
        assertTrue( collection.contains( obj ), collection + " does not contain " + obj );
    }

}
