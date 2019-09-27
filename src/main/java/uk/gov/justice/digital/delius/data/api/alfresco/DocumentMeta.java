package uk.gov.justice.digital.delius.data.api.alfresco;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMeta {
    @Getter(AccessLevel.NONE)
    @JsonProperty("id")
    private String _id;
    //Annoyingly, the /details alfresco result has field named ID rather than id
    @Getter(AccessLevel.NONE)
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
    private String userData;

    public String getId() {
        return _id != null ? _id : __ID;
    }
}

/*
  "ID": "bc4796ce-09d6-4f04-b715-a3bc8173ac35",
    "crn": "X087946",
    "name": "shortFormatPreSentenceReport-40.pdf",
    "entityId": 2500032066,
    "entityType": "COURTREPORT",
    "docType": "DOCUMENT",
    "author": "andy.marke,andy.marke",
    "lockOwner": "",
    "locked": false,
    "reserved": false,
    "reservationOwner": "",
    "lastModifiedDate": "2018-04-06T14:24:13Z",
    "modifier": "N00",
    "creationDate": "2017-12-08T14:19:59Z",
    "url": "/noms-spg/fetch/bc4796ce-09d6-4f04-b715-a3bc8173ac35",
    "userData": "{\"templateName\":\"shortFormatPreSentenceReport\",\"values\":{\"issueBehaviourDetails\":\"\",\"oasysAssessmentsInformationSource\":false,\"issueFinanceDetails\":\"\",\"pageNumber\":2,\"issueSubstanceMisuse\":false,\"office\":\"\",\"riskOfSeriousHarm\":\"\",\"counterSignature\":\"\",\"feedback\":\"Really great service. Saves me so much time\",\"jumpNumber\":2,\"issueOther\":false,\"reportAuthor\":\"\",\"reportDate\":\"08/12/2017\",\"additionalPreviousSupervision\":\"\",\"likelihoodOfReOffending\":\"\",\"otherOffences\":\"\",\"issueEmploymentDetails\":\"\",\"cpsSummaryInformationSource\":false,\"proposal\":\"\",\"watermark\":\"DRAFT\",\"pnc\":\"\",\"childrenServicesInformationSource\":false,\"court\":\"Mansfield  Magistrates Court\",\"issueSubstanceMisuseDetails\":\"\",\"mainOffence\":\"\",\"offenceSummary\":\"\",\"issueAccommodationDetails\":\"\",\"name\":\"Johnny PDF\",\"issueBehaviour\":false,\"interviewInformationSource\":false,\"startDate\":\"08/12/2017\",\"issueAccommodation\":false,\"issueRelationshipsDetails\":\"\",\"onBehalfOfUser\":\"andy.marke,andy.marke\",\"courtOfficePhoneNumber\":\"\",\"pncSupplied\":false,\"sentencingGuidelinesInformationSource\":false,\"otherInformationDetails\":\"\",\"issueFinance\":false,\"policeInformationSource\":false,\"otherInformationSource\":false,\"previousSupervisionResponse\":\"\",\"dateOfHearing\":\"27/07/2017\",\"previousConvictionsInformationSource\":false,\"crn\":\"X087946\",\"issueRelationships\":false,\"address\":\"123\\nFake St\\n\",\"issueEmployment\":false,\"localJusticeArea\":\"\",\"dateOfBirth\":\"26/07/1977\",\"entityId\":2500032066,\"serviceRecordsInformationSource\":false,\"visitedPages\":\"[1,2,10]\",\"addressSupplied\":true,\"patternOfOffending\":\"\",\"issueHealth\":false,\"offenceAnalysis\":\"\",\"issueHealthDetails\":\"\",\"issueOtherDetails\":\"\",\"documentId\":\"bc4796ce-09d6-4f04-b715-a3bc8173ac35\",\"age\":40,\"victimStatementInformationSource\":false}}"
 */
