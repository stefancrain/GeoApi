package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;

import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Simple validation methods that can be utilized by provider implementations of DistrictService
 */
public abstract class DistrictServiceValidator
{
    /**
     * Perform basic null checks on the input parameters.
     * @return true if all required objects are set, false otherwise and sets status in districtResult
     */
    public static boolean validateInput(final GeocodedAddress geoAddress, final DistrictResult districtResult,
                                        boolean requireGeocode)
    {
        if (geoAddress == null) {
            districtResult.setStatusCode(MISSING_GEOCODED_ADDRESS);
        }
        else
        {
            if (geoAddress.getAddress() == null && geoAddress.getGeocode() == null) {
                districtResult.setStatusCode(MISSING_ADDRESS);
            }
            else if (geoAddress.getAddress() != null && geoAddress.getAddress().isEmpty()
                                                     && geoAddress.getGeocode() == null) {
                districtResult.setStatusCode(INSUFFICIENT_ADDRESS);
            }
            else if (requireGeocode && !geoAddress.isValidGeocode()) {
                districtResult.setStatusCode(MISSING_GEOCODE);
            }
            else if (geoAddress.getAddress() != null) {
                String state = geoAddress.getAddress().getState();
                if (state != null && !state.isEmpty() && !state.matches("(?i)(NY|NEW YORK)")) {
                    districtResult.setStatusCode(NON_NY_STATE);
                }
                else {
                    return true;
                }
            }
            else {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform validation on the resulting DistrictInfo object supplied by the provider. Partial district
     * assignment refers to when the required types are not in the subset of assigned types.
     */
    public static boolean validateDistrictInfo(final DistrictInfo districtInfo, final List<DistrictType> reqTypes,
                                               final DistrictResult districtResult)
    {
        /** An empty district info is an invalid response so we return false */
        if (districtInfo == null || districtInfo.getAssignedDistricts().size() == 0) {
            districtResult.setStatusCode(ResultStatus.NO_DISTRICT_RESULT);
            return false;
        }
        /** If the result is only partial it still has some value so we don't want to return false here */
        else if (!districtInfo.getAssignedDistricts().containsAll(reqTypes)) {
            districtResult.setStatusCode(PARTIAL_DISTRICT_RESULT);
        }
        return true;
    }
}
