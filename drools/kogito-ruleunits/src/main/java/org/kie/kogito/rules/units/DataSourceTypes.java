package org.kie.kogito.rules.units;

import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.DataStream;
import org.kie.kogito.rules.SingletonStore;

public final class DataSourceTypes {

    public final Class<DataSource> DataSource;
    public final Class<DataStore> DataStore;
    public final Class<DataStream> DataStream;
    public final Class<SingletonStore> SingletonStore;

    public static DataSourceTypes getInstance(ClassLoader cl) {
        try {
            return new DataSourceTypes(cl);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private DataSourceTypes(ClassLoader classLoader) throws ClassNotFoundException {
        this.DataSource = (Class<DataSource>) classLoader.loadClass(DataSource.class.getCanonicalName());
        this.DataStore = (Class<DataStore>) classLoader.loadClass(DataStore.class.getCanonicalName());
        this.DataStream = (Class<DataStream>) classLoader.loadClass(DataStream.class.getCanonicalName());
        this.SingletonStore = (Class<SingletonStore>) classLoader.loadClass(SingletonStore.class.getCanonicalName());
    }
}
