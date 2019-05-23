package org.kie.submarine.codegen.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageMetaDataTest {

    @Test
    public void getSetLabels() {
        final ImageMetaData imageMetaData = new ImageMetaData();
        final List<Map<String, String>> labels = new ArrayList<>();

        final Map<String, String> firstLabels = new HashMap<>();
        firstLabels.put("firstLabelsKey1", "firstLabelsValue1");

        final Map<String, String> secondLabels = new HashMap<>();
        secondLabels.put("secondLabelsKey1", "secondLabelsValue1");

        labels.add(firstLabels);
        labels.add(secondLabels);

        imageMetaData.setLabels(labels);
        assertThat(imageMetaData.getLabels()).isSameAs(labels);
        assertThat(imageMetaData.getLabels()).containsExactlyInAnyOrder(firstLabels, secondLabels);
    }

    @Test
    public void getSetLabelsNull() {
        final ImageMetaData imageMetaData = new ImageMetaData();
        imageMetaData.setLabels(null);
        assertThat(imageMetaData.getLabels()).isNull();
    }

    @Test
    public void add() {
        final ImageMetaData imageMetaData = new ImageMetaData();

        final Map<String, String> labels = new HashMap<>();
        labels.put("firstLabelsKey1", "firstLabelsValue1");
        imageMetaData.add(labels);

        final Map<String, String> secondLabels = new HashMap<>();
        secondLabels.put("secondLabelsKey1", "secondLabelsValue1");
        imageMetaData.add(secondLabels);

        assertThat(imageMetaData.getLabels()).isNotNull();
        assertThat(imageMetaData.getLabels()).hasSize(1);
        labels.putAll(secondLabels);
        assertThat(imageMetaData.getLabels().get(0)).hasSize(2);
        assertThat(imageMetaData.getLabels().get(0)).containsAllEntriesOf(labels);
    }
}