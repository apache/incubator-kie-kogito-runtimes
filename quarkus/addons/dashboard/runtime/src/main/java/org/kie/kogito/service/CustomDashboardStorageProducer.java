package org.kie.kogito.service;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.ConfigProvider;
import org.kie.kogito.dashboard.CustomDashboardStorage;
import org.kie.kogito.dashboard.impl.CustomDashboardStorageImpl;

@ApplicationScoped
public class CustomDashboardStorageProducer {
    public static final String PROJECT_CUSTOM_DASHBOARD_STORAGE_PROP = "quarkus.kogito-runtime-tools.custom.dashboard.folder";
    private static final String CUSTOM_DASHBOARD_STORAGE_PATH = "/dashboards/";
    CustomDashboardStorageProducer(){

    }

//    @Produces
//    @Default
//    @ApplicationScoped
//    public CustomDashboardStorageImpl customDashboardStorage(){
//        Optional<String> storageUrl = ConfigProvider.getConfig()
//                .getOptionalValue(PROJECT_CUSTOM_DASHBOARD_STORAGE_PROP, String.class);
//
//        CustomDashboardStorage storage = new CustomDashboardStorageImpl();
//        storage.s
//    }

}
