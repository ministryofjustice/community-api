package uk.gov.justice.digital.delius.data.filters;

public class DocumentFilter {
    private static final DocumentFilter NO_FILTER = new DocumentFilter();
    public static DocumentFilter noFilter() {
        return NO_FILTER;
    }
}
