package org.example.visit.services;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

@ApplicationScoped
public class VisitGroupInitializer {

    private static final Logger LOG = Logger.getLogger(VisitGroupInitializer.class);

    @Inject
    PatientVisitService patientVisitService;

    void onStart(@Observes StartupEvent ev) {
        try {
            patientVisitService.updateAllVisitGroupsAndFinancialsFromPatients();
            patientVisitService.fixProcedureRequestedNames();

            LOG.info("Visit groups initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize visit groups", e);
        }
    }
}