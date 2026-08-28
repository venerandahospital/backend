package org.example.hmis.services.payloads;

public class HmisTracerItemDTO {
    public Long id;
    public String hmisTracerCode;
    public String tracerName;
    public Long stockItemId;
    public Long shopItemId;
    public Boolean active;
    public Integer sortOrder;

    public static HmisTracerItemDTO from(org.example.hmis.domains.HmisTracerItem row) {
        HmisTracerItemDTO dto = new HmisTracerItemDTO();
        dto.id = row.id;
        dto.hmisTracerCode = row.hmisTracerCode;
        dto.tracerName = row.tracerName;
        dto.stockItemId = row.stockItemId;
        dto.shopItemId = row.shopItemId;
        dto.active = row.active;
        dto.sortOrder = row.sortOrder;
        return dto;
    }
}