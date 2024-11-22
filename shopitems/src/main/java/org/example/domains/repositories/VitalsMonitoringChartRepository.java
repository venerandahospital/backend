package org.example.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.domains.VitalsMonitoringChart;

@ApplicationScoped
public class VitalsMonitoringChartRepository implements PanacheRepository<VitalsMonitoringChart> {
}
