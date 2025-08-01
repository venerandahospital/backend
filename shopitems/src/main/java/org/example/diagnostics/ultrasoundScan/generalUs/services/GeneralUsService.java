package org.example.diagnostics.ultrasoundScan.generalUs.services;

import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.repositories.GeneralUsRepository;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsUpdateRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses.GeneralUsDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsRequest;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.visit.domains.PatientVisit;
import org.example.visit.domains.repositories.PatientVisitRepository;


import java.time.LocalDate;
import java.time.LocalTime;

@ApplicationScoped
public class GeneralUsService {

    @Inject
    GeneralUsRepository generalUsRepository;

    @Inject
    MySQLPool client;


    public static final String NOT_FOUND = "Not found!";

        @Transactional
        public Response createGeneralUsReport(GeneralUsRequest request){

            PatientVisit patientVisit = PatientVisit.findById(request.visitId); // ✅ correct
            if (patientVisit == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("Patient visit not found for ID: " + request.visitId))
                        .build();
            }

            Patient patient = patientVisit.patient;

            ProcedureRequested procedureRequested = ProcedureRequested.findById(request.procedureRequestedId); // ✅ correct
            if (procedureRequested == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("procedureRequested not found for ID: " + request.procedureRequestedId))
                        .build();
            }

            GeneralUs generalUs = new GeneralUs();
            generalUs.patientName = patient.patientFirstName +" "+ patient.patientSecondName;
            generalUs.gender = patient.patientGender;
            generalUs.patientAge = patient.patientAge;
            generalUs.visit = patientVisit;
            generalUs.procedureRequested = procedureRequested;
            generalUs.indication = request.indication;
            generalUs.doneBy = request.doneBy;
            generalUs.recommendation = request.recommendation;

            generalUs.exam = request.exam;
            generalUs.findings = request.findings;
            generalUs.impression = request.impression;

            generalUs.scanPerformingDate = LocalDate.now();
            generalUs.upDatedDate = LocalDate.now();
            generalUs.timeOfProcedure = LocalTime.now();
            generalUs.scanRequestDate = procedureRequested.dateOfProcedure;


            generalUsRepository.persist(generalUs);


            return Response.status(Response.Status.CREATED)
                    .entity(new ResponseMessage("Scan report created successfully", new GeneralUsDTO(generalUs)))
                    .build();


        }

    @Transactional
    public Response updateScanReportById(Long id, GeneralUsUpdateRequest request) {
        GeneralUs generalUs = GeneralUs.findById(id); // ✅ correct
        if (generalUs == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("Scan Report not found for ID: " + id))
                    .build();
        }

        generalUs.indication = request.indication;
        generalUs.doneBy = request.doneBy;
        generalUs.recommendation = request.recommendation;

        generalUs.exam = request.exam;
        generalUs.findings = request.findings;
        generalUs.impression = request.impression;

        generalUs.upDatedDate = LocalDate.now();

        generalUsRepository.persist(generalUs);


        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Scan report updated successfully", new GeneralUsDTO(generalUs)))
                .build();


    }



























}
