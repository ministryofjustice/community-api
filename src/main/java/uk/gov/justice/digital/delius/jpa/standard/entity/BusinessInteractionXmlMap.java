package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BUSINESS_INT_XML_MAP")
@Where(clause = "XSD_NUMBER=1")
public class BusinessInteractionXmlMap {
    @Id
    @Column(name = "BUSINESS_INT_XML_MAP_ID")
    private Long businessIntXmlMapId;
    @Column(name = "BUSINESS_INTERACTION_ID")
    private Long businessInteractionId;
    @Column(name = "ROOT_XML_MESSAGE_ID")
    private Long rootXmlMessageId;
    @Column(name = "XML_MESSAGE_NAME")
    private String xmlMessageName;
    @Column(name = "DATA_UPDATE_MODE")
    private String dataUpdateMode;
    @Column(name = "INCLUDE_XML_MESSAGE_LIST")
    private String includeXmlMessageList;
    @Column(name = "EXCLUDE_XML_MESSAGE_LIST")
    private String excludeXmlMessageList;
    @Column(name = "XSD_NUMBER")
    private Long xsdNumber;
}
