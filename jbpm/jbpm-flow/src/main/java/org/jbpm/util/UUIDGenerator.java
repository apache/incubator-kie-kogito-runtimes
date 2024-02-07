package org.jbpm.util;

import java.util.Random;
import java.util.UUID;

public final class UUIDGenerator {
    private static final Random RANDOM = new Random(1);

    private UUIDGenerator() {
        // It is not allowed to create instances of a util class.
    }

    public static synchronized String getID() {
        byte[] array = new byte[16];
        RANDOM.nextBytes(array);
        return String.valueOf(UUID.nameUUIDFromBytes(array));
    }
}
