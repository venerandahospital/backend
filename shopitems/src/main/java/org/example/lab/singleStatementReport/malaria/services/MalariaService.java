package org.example.lab.singleStatementReport.malaria.services;

import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.configuration.handler.ResponseMessage;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;
import org.example.lab.singleStatementReport.malaria.domains.repositories.MalariaRepository;
import org.example.lab.singleStatementReport.malaria.services.Payloads.requests.MalariaUpdateRequest;
import org.example.lab.singleStatementReport.malaria.services.Payloads.responses.MalariaDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDateTime;

@ApplicationScoped
public class MalariaService {

    @Inject
    MalariaRepository malariaRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Inject
    MySQLPool client;

    @Transactional
    public void createMrdtReport(ProcedureRequested procedureRequested){

        PatientVisit patientVisit = procedureRequested.visit; // ✅ correct

        Patient patient = patientVisit.patient;

        Malaria malaria = new Malaria();
        malaria.patientName = patient.patientFirstName +" "+ patient.patientSecondName;
        malaria.gender = patient.patientGender;
        malaria.patientAge = patient.patientAge;
        malaria.visit = procedureRequested.visit;
        malaria.procedureRequested = procedureRequested;
        malaria.doneBy = "";
        malaria.recommendation = "";
        malaria.labReportTitle = "";
        malaria.test = procedureRequested.procedureRequestedType;
        malaria.bs = "";
        malaria.mrdt = "";

        malaria.reportCreationDateAndTime = LocalDateTime.now();
        malaria.sampleCollectionDateAndTime = LocalDateTime.now();

        malaria.labRequestDate = procedureRequested.dateOfProcedure;

        malariaRepository.persist(malaria);

        procedureRequestedRepository.persist(procedureRequested);

    }

    @Transactional
    public Response updateMalariaReportById(Long id, MalariaUpdateRequest request) {
        Malaria malaria = Malaria.findById(id); // ✅ correct
        if (malaria == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("malaria Report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(malaria.procedureRequested.id);

        malaria.procedureRequested = procedureRequested;
        malaria.doneBy = request.doneBy;
        malaria.recommendation = request.recommendation;
        malaria.labReportTitle = request.bs;
        malaria.test = procedureRequested.procedureRequestedName;
        malaria.bs = request.bs;
        malaria.notes = request.notes;
        malaria.mrdt = request.mrdt;
        malaria.procedureDoneDateAndTime = LocalDateTime.now();
        malaria.reportUpDatedDateAndTime = LocalDateTime.now();
        malaria.reportCreationDateAndTime = LocalDateTime.now();
        malaria.sampleCollectionDateAndTime = LocalDateTime.now();

        malariaRepository.persist(malaria);

        procedureRequested.report = malaria.bs;

        if (malaria.bs == null || malaria.bs.isEmpty() ||
                malaria.mrdt == null || malaria.mrdt.isEmpty()
)
        {
            procedureRequested.status = "Pending";
        }else{
            procedureRequested.status = "Done";
            procedureRequested.bgColor = "rgba(206, 7, 17, 1)";
        }

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("malaria report updated successfully", new MalariaDTO(malaria)))
                .build();


    }


    @Transactional
    public Response getLabReportByRequestId(Long procedureRequestedId){


        Malaria mrdt = Malaria.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (mrdt == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Mrdt lab Report not found for procedureRequest ID: " + procedureRequestedId))
                    .build();
        }else {
           /*return Response.status(Response.Status.FOUND)
                   .entity(new ResponseMessage("scan report fetched successfully: ", new GeneralUsDTO(generalUs)))
                   .build();*/
            return Response.ok(new ResponseMessage("lab report fetched successfully", new MalariaDTO(mrdt))).build();

        }


    }









}
