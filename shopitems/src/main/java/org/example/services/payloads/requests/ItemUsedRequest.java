package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ItemUsedRequest {

    @Schema(example = "1")
    public int quantity;

    @Schema(example = "1")
    public Long labTestId;

    @Schema(example = "1")
    public Long itemId;

}
