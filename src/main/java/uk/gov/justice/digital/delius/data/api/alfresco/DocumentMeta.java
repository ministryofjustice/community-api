package uk.gov.justice.digital.delius.data.api.alfresco;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class DocumentMeta {
    @JsonProperty("id")
    private String _id;
    //Annoyingly, the /details alfresco result has field named ID rather than id
    @JsonProperty("ID")
    private String __ID;
    private String crn;
    private String name;
    private String entityId;
    private String entityType;
    private String docType;
    private String author;
    private String lockOwner;
    private String locked;
    private String reserved;
    private String reservationOwner;
    private OffsetDateTime lastModifiedDate;
    private OffsetDateTime creationDate;
    private String modifier;
    private String url;

    public String getId() {
        return _id != null ? _id : __ID;
    }
}
