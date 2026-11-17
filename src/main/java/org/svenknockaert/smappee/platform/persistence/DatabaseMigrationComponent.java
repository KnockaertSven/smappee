package org.svenknockaert.smappee.platform.persistence;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class DatabaseMigrationComponent {

    @Activate
    public void activate() {
        DatabaseMigrator.migrateFromEnv();
    }
}
