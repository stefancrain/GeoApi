package gov.nysenate.sage.model.result;

public enum ResultStatus
{
    SUCCESS(0, "Success."),

    SERVICE_NOT_SUPPORTED(1, "The requested service is unsupported."),
    PROVIDER_NOT_SUPPORTED(2, "The requested provider is unsupported."),
    FEATURE_NOT_SUPPORTED(3, "The requested features is unsupported."),

    API_KEY_INVALID(10, "The supplied API key could not be authenticated."),
    API_KEY_MISSING(11, "An API key is required."),

    API_REQUEST_INVALID(20, "The request is not in a valid format. Check the documentation for proper usage."),
    API_INPUT_FORMAT_UNSUPPORTED(21, "The requested input format is currently not supported."),
    API_OUTPUT_FORMAT_UNSUPPORTED(22, "The requested output format is currently not supported."),

    RESPONSE_MISSING_ERROR(90, "No response from service provider."),
    RESPONSE_PARSE_ERROR(91, "Error parsing response from service provider."),

    MISSING_INPUT_PARAMS(100, "One or more parameters are missing."),
    MISSING_ADDRESS(110, "An address is required."),
    MISSING_GEOCODE(120, "A valid geocoded coordinate pair is required."),
    MISSING_ZIPCODE(130, "A zipcode is required."),
    MISSING_STATE(140, "A state is required."),
    MISSING_POINT(150, "A coordinate pair is required."),

    INVALID_INPUT_PARAMS(200, "One or more parameters are invalid."),
    INVALID_ADDRESS(210, "The supplied address is invalid."),
    INVALID_GEOCODE(220, "The supplied geocoded coordinate pair is invalid."),
    INVALID_ZIPCODE(230, "The supplied zipcode is invalid."),
    INVALID_STATE(240, "The supplied state is invalid or is not supported."),

    INSUFFICIENT_INPUT_PARAMS(300, "One or more parameters are insufficient."),
    INSUFFICIENT_ADDRESS(310, "The supplied address is missing one or more parameters."),
    INSUFFICIENT_GEOCODE(310, "The supplied geocoded is missing one or more parameters."),

    NO_DISTRICT_RESULT(400, "District assignment returned no results."),
    MULTIPLE_DISTRICT_RESULT(401, "Multiple matches were found for certain districts."),
    NO_GEOCODE_RESULT(410, "Geocode service returned no results."),
    NO_REVERSE_GEOCODE_RESULT(411, "Reverse Geocode service returned no results."),
    NO_ADDRESS_VALIDATE_RESULT(420, "The address could not be validated."),

    PARTIAL_DISTRICT_RESULT(430, "District assignment yielded some districts."),

    /** Unexpected errors */
    INTERNAL_ERROR(500, "Internal Server Error."),
    DATABASE_ERROR(501, "Database Error."),
    RESPONSE_ERROR(502, "Application failed to provide a response."),
    RESPONSE_SERIALIZATION_ERROR(503, "Failed to serialize response.");


    private int code;
    private String desc;
    ResultStatus(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public String getDesc()
    {
        return this.desc;
    }

}
