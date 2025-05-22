package org.example.vitals;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VitalsMonitoringChartRepository implements PanacheRepository<VitalsMonitoringChart> {
}
