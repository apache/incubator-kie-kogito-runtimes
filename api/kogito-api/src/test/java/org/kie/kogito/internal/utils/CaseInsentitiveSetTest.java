package org.kie.kogito.internal.utils;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseInsentitiveSetTest {

    private Set<String> set;

    @BeforeEach
    void setup() {
        set = new CaseInsensitiveSet("Content-Length");
    }

    @Test
    void testRegular() {
        final String lowerCase = "content-length";
        assertThat(set.add(null)).isTrue();
        assertThat(set.add(lowerCase)).isFalse();
        assertThat(set.add(null)).isFalse();
        assertThat(set).hasSize(2);
        assertThat(set.contains(lowerCase)).isTrue();
        assertThat(set.contains(null)).isTrue();
        assertThat(set.remove(lowerCase)).isTrue();
        assertThat(set).hasSize(1);
        assertThat(set.remove(null)).isTrue();
        assertThat(set).isEmpty();
    }
}
