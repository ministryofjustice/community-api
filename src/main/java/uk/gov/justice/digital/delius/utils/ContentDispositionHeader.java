package uk.gov.justice.digital.delius.utils;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

import static java.net.URLEncoder.encode;


@Getter
public class ContentDispositionHeader {
    private static final String FILENAME_CONTENT_DISPOSITION = "attachment; filename=\"%s\"; filename*=UTF-8''%s";
    private final String filename;
    private final String value;
    private ContentDispositionHeader(String filename){
        this.filename = filename;
        this.value = String.format(FILENAME_CONTENT_DISPOSITION, filename, encode(filename, StandardCharsets.UTF_8)
            .replace("+","%20"));
    }
    public static ContentDispositionHeader of(String filename){
        return new ContentDispositionHeader(filename);
    }
}
