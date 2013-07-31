package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.dao.provider.TigerGeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.map.MapService;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.*;

import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

public class DistrictShapefile implements DistrictService, MapService
{
    private static Logger logger = Logger.getLogger(DistrictShapefile.class);
    private DistrictShapefileDao districtShapefileDao;

    /** The street file and cityzip daos are needed to determine overlap */
    private StreetFileDao streetFileDao;
    private CityZipDB cityZipDBDao;
    private TigerGeocoderDao tigerGeocoderDao;

    public DistrictShapefile()
    {
        this.districtShapefileDao = new DistrictShapefileDao();
        this.streetFileDao = new StreetFileDao();
        this.cityZipDBDao = new CityZipDB();
        this.tigerGeocoderDao = new TigerGeocoderDao();
    }

    @Override
    public boolean requiresGeocode() { return true; }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, DistrictType.getStateBasedTypes());
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());

        /** Validate input */
        if (!validateInput(geocodedAddress, districtResult, true, false)) {
            return districtResult;
        }
        try {
            Geocode geocode = geocodedAddress.getGeocode();
            DistrictInfo districtInfo = this.districtShapefileDao.getDistrictInfo(geocode.getLatLon(), reqTypes, true);

            /** Validate response */
            if (!validateDistrictInfo(districtInfo, reqTypes, districtResult)) {
                return districtResult;
            }
            /** Set the result */
            districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.HOUSE));
            districtResult.setResultTime(new Timestamp(new Date().getTime()));
        }
        catch (Exception ex) {
            districtResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            logger.error(ex);
        }

        return districtResult;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return assignDistricts(geocodedAddresses, DistrictType.getStandardTypes());
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> reqTypes)
    {
        return ParallelDistrictService.assignDistricts(this, geocodedAddresses, reqTypes);
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType)
    {
        return nearbyDistricts(geocodedAddress, districtType, 2);
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count)
    {
        if (geocodedAddress != null && geocodedAddress.isValidGeocode()) {
            Point point = geocodedAddress.getGeocode().getLatLon();
            return this.districtShapefileDao.getNearbyDistricts(districtType, point, true, count);
        }
        return null;
    }


    @Override
    public MapResult getDistrictMap(DistrictType districtType, String code)
    {
        MapResult mapResult = new MapResult(this.getClass());
        if (code != null && !code.isEmpty()) {
            code = FormatUtil.trimLeadingZeroes(code);
            if (districtShapefileDao.getDistrictMapLookup().get(districtType) != null) {
                DistrictMap map = districtShapefileDao.getDistrictMapLookup().get(districtType).get(code);
                if (map != null) {
                    mapResult.setDistrictMap(map);
                    mapResult.setStatusCode(ResultStatus.SUCCESS);
                }
                else {
                    mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
                }
            }
            else {
                mapResult.setStatusCode(ResultStatus.UNSUPPORTED_DISTRICT_MAP);
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.MISSING_DISTRICT_CODE);
        }
        return mapResult;
    }

    @Override
    public MapResult getDistrictMaps(DistrictType districtType)
    {
        MapResult mapResult = new MapResult(this.getClass());
        List<DistrictMap> mapCollection = districtShapefileDao.getDistrictMaps().get(districtType);
        if (mapCollection != null) {
            mapResult.setDistrictMaps(mapCollection);
            mapResult.setStatusCode(ResultStatus.SUCCESS);
        }
        else {
            mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
        }
        return mapResult;
    }

    /**
     *
     * @param geocodedAddress
     * @return
     */
    public DistrictResult getMultiMatchResult(GeocodedAddress geocodedAddress, Boolean zipProvided)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());
        DistrictedAddress districtedAddress = new DistrictedAddress(geocodedAddress, null, DistrictMatchLevel.NOMATCH);
        DistrictInfo districtInfo = new DistrictInfo();
        DistrictMatchLevel matchLevel = DistrictMatchLevel.NOMATCH;
        ResultStatus resultStatus = ResultStatus.INSUFFICIENT_ADDRESS;

        /** Validate the geocoded address before proceeding */
        if (!validateInput(geocodedAddress, districtResult, true, true)) {
            return districtResult;
        }

        Address address = geocodedAddress.getAddress();
        GeocodeQuality geocodeQuality = geocodedAddress.getGeocode().getQuality();
        Map<DistrictType, Set<String>> matches = new HashMap<>();
        List<String> zip5List = new ArrayList<>();
        List<String> streetList = new ArrayList<>();
        String streetLineJson = null;

        logger.debug("Zip Provided: " + zipProvided);

        if (geocodeQuality.compareTo(GeocodeQuality.CITY) >= 0) {
            if (geocodeQuality.compareTo(GeocodeQuality.ZIP) >= 0 &&!address.getZip5().isEmpty()) {
                if (geocodeQuality.compareTo(GeocodeQuality.STREET) >= 0) {
                    logger.debug("Determining street level district overlap");
                    matchLevel = DistrictMatchLevel.STREET;
                    streetList.add(address.getAddr1());
                    zip5List = (zipProvided) ? Arrays.asList(address.getZip5()) : cityZipDBDao.getZipsByCity(address.getCity());
                    districtInfo.setStreetLineReference(tigerGeocoderDao.getStreetLineGeometry(address.getAddr1(), zip5List));
                    districtInfo.setStreetRanges(streetFileDao.getDistrictStreetRanges(address.getAddr1(), zip5List));
                }
                else {
                    logger.debug("Determining zip level district overlap");
                    matchLevel = DistrictMatchLevel.ZIP5;
                    zip5List = Arrays.asList(address.getZip5());
                }
            }
            else if (!address.getCity().isEmpty()) {
                logger.debug("Determining city level district overlap");
                matchLevel = DistrictMatchLevel.CITY;
                zip5List = cityZipDBDao.getZipsByCity(address.getCity());
            }

            if (!zip5List.isEmpty()) {
                matches = streetFileDao.getAllStandardDistrictMatches(streetList, zip5List);
                if (matches != null && !matches.isEmpty()) {
                    /** Retrieve source map for city and zip match levels */
                    if (matchLevel.compareTo(DistrictMatchLevel.STREET) < 0) {
                        DistrictMap sourceMap = districtShapefileDao.getOverlapReferenceBoundary(DistrictType.ZIP, new HashSet<String>(zip5List));
                        districtInfo.setReferenceMap(sourceMap);
                    }

                    for (DistrictType matchType : matches.keySet()) {
                        if (matches.get(matchType) != null && !matches.get(matchType).isEmpty() && !matchType.equals(DistrictType.ZIP)) {
                            Set<String> distCodeSet = matches.get(matchType);
                            DistrictOverlap overlap = null;
                            logger.trace("Matches for " + matchType + " " + distCodeSet);

                            /** Senate districts should always get overlap assigned */
                            if (matchType.equals(DistrictType.SENATE) || distCodeSet.size() > 1) {
                                overlap = districtShapefileDao.getDistrictOverlap(matchType, matches.get(matchType),
                                                                                  DistrictType.ZIP, new HashSet<String>(zip5List));
                                districtInfo.addDistrictOverlap(matchType, overlap);
                            }
                            /** If the district set from the street files is size 1, set it as the district */
                            if (distCodeSet.size() == 1) {
                                districtInfo.setDistCode(matchType, distCodeSet.iterator().next());
                            }
                            /** Otherwise if the overlap count is size 1 for senate, set it as the district */
                            else if (matchType.equals(DistrictType.SENATE) && overlap != null && overlap.getTargetOverlap().size() == 1) {
                                districtInfo.setDistCode(matchType, overlap.getOverlapDistrictCodes().get(0));
                            }
                        }
                    }
                    resultStatus = ResultStatus.SUCCESS;
                    districtedAddress.setDistrictInfo(districtInfo);
                    districtedAddress.setDistrictMatchLevel(matchLevel);
                    logger.info("Resulting match level: " + matchLevel);
                }
            }
        }
        districtResult.setStatusCode(resultStatus);
        districtResult.setDistrictedAddress(districtedAddress);
        districtResult.setResultTime(new Timestamp(new Date().getTime()));
        return districtResult;
    }
}