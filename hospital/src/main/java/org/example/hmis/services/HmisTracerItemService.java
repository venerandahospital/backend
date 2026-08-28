package org.example.hmis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import org.example.hmis.domains.HmisTracerItem;
import org.example.hmis.services.payloads.HmisTracerItemDTO;
import org.example.hmis.services.payloads.HmisTracerItemUpdateRequest;

@ApplicationScoped
public class HmisTracerItemService {

    public List<HmisTracerItemDTO> listActive() {
        return HmisTracerItem.find("active = true order by sortOrder, id").list().stream()
                .map(row -> HmisTracerItemDTO.from((HmisTracerItem) row))
                .toList();
    }

    @Transactional
    public HmisTracerItemDTO update(Long id, HmisTracerItemUpdateRequest request) {
        HmisTracerItem row = HmisTracerItem.findById(id);
        if (row == null) {
            throw new NotFoundException("Tracer item not found");
        }
        if (request != null) {
            if (request.stockItemId != null) {
                row.stockItemId = request.stockItemId;
            }
            if (request.shopItemId != null) {
                row.shopItemId = request.shopItemId;
            }
            if (request.active != null) {
                row.active = request.active;
            }
        }
        row.persist();
        return HmisTracerItemDTO.from(row);
    }
}