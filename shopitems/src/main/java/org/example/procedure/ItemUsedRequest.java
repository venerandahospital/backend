package org.example.procedure;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ItemUsedRequest {

    @Schema(example = "1")
    public int quantityUsed;

    @Schema(example = "1")
    public Long procedureId;

    @Schema(example = "1")
    public Long itemId;

}
