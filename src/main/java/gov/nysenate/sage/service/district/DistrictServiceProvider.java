package gov.nysenate.sage.service.district;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

import static gov.nysenate.sage.model.result.ResultStatus.API_INPUT_FORMAT_UNSUPPORTED;
import static gov.nysenate.sage.model.result.ResultStatus.NO_GEOCODE_RESULT;

/**
 * Point of access for all district assignment requests. This class maintains a collection of available
 * district providers and contains logic for distributing requests and collecting responses from the providers.
 */
public class DistrictServiceProvider extends ServiceProviders<DistrictService>
{
    private final Logger logger = Logger.getLogger(DistrictServiceProvider.class);
    private Config config = ApplicationFactory.getConfig();
    private final static String DEFAULT_DISTRICT_PROVIDER = "shapefile";
    private final static LinkedList<String> DEFAULT_DISTRICT_FALLBACK = new LinkedList<>(Arrays.asList("streetfile", "geoserver"));

    /** Specifies the distance to a district boundary in which the accuracy of shapefiles is uncertain */
    private static Double PROXIMITY_THRESHOLD = 0.001;

    public DistrictServiceProvider()
    {
        PROXIMITY_THRESHOLD = Double.parseDouble(this.config.getValue("proximity.threshold", "0.001"));
    }

    /**
     * Assign standard districts using default method.
     * @param geocodedAddress
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, null, DistrictType.getStandardTypes(), false, false);
    }

    /**
     * Assign standard districts using specified provider
     * @param geocodedAddress
     * @param distProvider
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider)
    {
        return assignDistricts(geocodedAddress, distProvider, DistrictType.getStandardTypes(), false, false);
    }

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     *
     * @param geocodedAddress
     * @param distProvider
     * @param getMembers
     * @param getMaps
     * @return
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider,
                                          final List<DistrictType> districtTypes, final boolean getMembers, final boolean getMaps)
    {
        logger.info("Assigning districts " + ((geocodedAddress != null) ? geocodedAddress.getAddress() : ""));
        DistrictResult districtResult = null;
        ExecutorService districtExecutor = null;

        if (this.isRegistered(distProvider)) {
            DistrictService districtService = this.newInstance(distProvider);
            districtService.fetchMaps(getMaps);
            districtResult = districtService.assignDistricts(geocodedAddress, districtTypes);
        }
        else {
            try {
                districtExecutor = Executors.newFixedThreadPool(2);

                DistrictService shapeFileService = this.newInstance("shapefile");
                DistrictService streetFileService = this.newInstance("streetfile");

                Callable<DistrictResult> shapeFileCall = getDistrictsCallable(geocodedAddress, shapeFileService, districtTypes, getMaps);
                Callable<DistrictResult> streetFileCall = getDistrictsCallable(geocodedAddress, streetFileService, districtTypes, false);

                Future<DistrictResult> shapeFileFuture = districtExecutor.submit(shapeFileCall);
                Future<DistrictResult> streetFileFuture = districtExecutor.submit(streetFileCall);

                DistrictResult shapeFileResult = shapeFileFuture.get();
                DistrictResult streetFileResult = streetFileFuture.get();

                districtResult = consolidateDistrictResults(shapeFileService, shapeFileResult, streetFileResult);

                if (getMembers) {
                    DistrictServiceMetadata.assignDistrictMembers(districtResult);
                }
            }
            catch (InterruptedException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            catch (ExecutionException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            finally {
                if (districtExecutor != null) {
                    districtExecutor.shutdownNow();
                }
            }
        }

        if (getMembers) {
            DistrictServiceMetadata.assignDistrictMembers(districtResult);
        }
        return districtResult;
    }

    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses,
                                                final List<DistrictType> districtTypes) {
        return assignDistricts(geocodedAddresses, null, districtTypes, false, false);
    }

    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses, final String distProvider,
                                                final List<DistrictType> districtTypes, final boolean getMembers, final boolean getMaps)
    {
        ExecutorService districtExecutor;
        List<DistrictResult> districtResults = new ArrayList<>();

        if (this.isRegistered(distProvider)) {
            DistrictService districtService = this.newInstance(distProvider);
            districtService.fetchMaps(getMaps);
            districtResults = districtService.assignDistricts(geocodedAddresses, districtTypes);
        }
        else {
            try {
                districtExecutor = Executors.newFixedThreadPool(2);

                DistrictService streetFileService = this.newInstance("streetfile");
                DistrictService shapeFileService = this.newInstance("shapefile");

                Callable<List<DistrictResult>> streetFileCall = getDistrictsCallable(geocodedAddresses, streetFileService, districtTypes, false);
                Callable<List<DistrictResult>> shapeFileCall = getDistrictsCallable(geocodedAddresses, shapeFileService, districtTypes, getMaps);

                Future<List<DistrictResult>> shapeFileFuture = districtExecutor.submit(shapeFileCall);
                Future<List<DistrictResult>> streetFileFuture = districtExecutor.submit(streetFileCall);

                List<DistrictResult> shapeFileResults = shapeFileFuture.get();
                List<DistrictResult> streetFileResults = streetFileFuture.get();

                for (int i = 0; i < shapeFileResults.size(); i++) {
                    districtResults.add(consolidateDistrictResults(shapeFileService, shapeFileResults.get(i),
                                                                   streetFileResults.get(i)));
                }
            }
            catch (InterruptedException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
            catch (ExecutionException ex) {
                logger.error("Failed to get district results from future!", ex);
            }
        }

        if (getMembers) {
            for (DistrictResult districtResult : districtResults) {
                DistrictServiceMetadata.assignDistrictMembers(districtResult);
            }
        }

        return districtResults;
    }

    private Callable<DistrictResult> getDistrictsCallable(final GeocodedAddress geocodedAddress,
                                                          final DistrictService districtService,
                                                          final List<DistrictType> districtTypes, final boolean getMaps) {
        return new Callable<DistrictResult>() {
            @Override
            public DistrictResult call() throws Exception {
                districtService.fetchMaps(getMaps);
                return districtService.assignDistricts(geocodedAddress, districtTypes);
            }
        };
    }

    private Callable<List<DistrictResult>> getDistrictsCallable(final List<GeocodedAddress> geocodedAddresses,
                                                                final DistrictService districtService,
                                                                final List<DistrictType> districtTypes, final boolean getMaps) {
        return new Callable<List<DistrictResult>>() {
            @Override
            public List<DistrictResult> call() throws Exception {
                districtService.fetchMaps(getMaps);
                return districtService.assignDistricts(geocodedAddresses, districtTypes);
            }
        };
    }

    /**
     * Shapefile lookups return a proximity metric that indicates how close the geocode is to a district boundary.
     * If it is too close to the boundary it is possible that the neighboring district is in fact the valid one.
     * If the streetfile lookup returned a match for that district type then we can check to see if that district
     * is in the subset of neighboring districts returned by the shapefiles. If it is then we declare the neighbor
     * district to be the valid one. If there are districts in the street result that do not exist in the shapefile
     * result, apply them to the consolidated result. In the event of a district mismatch between the two and the
     * shapefile is not near the proximity threshold, log the case and give preference to the shapefile.
     *
     * @param shapeResult   Result obtained from shapefile district assign
     * @param streetResult  Result obtained from streetfile district assign
     * @return  Consolidated district result
     */
    private DistrictResult consolidateDistrictResults(DistrictService shapeService, DistrictResult shapeResult,
                                                      DistrictResult streetResult)
    {
        if (shapeResult.isSuccess() || shapeResult.isPartialSuccess()) {

            DistrictInfo shapeInfo = shapeResult.getDistrictInfo();
            GeocodedAddress geocodedAddress = shapeResult.getGeocodedAddress();
            String address = (shapeResult.getAddress() != null) ? shapeResult.getAddress().toString() : "Missing Address!";

            /** Can only consolidate if a second set of results exists */
            if (streetResult.isSuccess() || streetResult.isPartialSuccess()) {
                DistrictInfo streetInfo = streetResult.getDistrictInfo();
                if (!shapeResult.isSuccess() && !shapeResult.isPartialSuccess()) {
                    shapeResult.setDistrictInfo(new DistrictInfo());
                }
                Set<DistrictType> fallbackSet = new HashSet<>(streetResult.getAssignedDistricts());
                for (DistrictType distType : shapeResult.getAssignedDistricts()) {
                    if (shapeInfo.getDistProximity(distType) < PROXIMITY_THRESHOLD) {
                        String shapeCode = shapeInfo.getDistCode(distType);
                        String streetCode = streetInfo.getDistCode(distType);
                        if (fallbackSet.contains(distType) && ! shapeCode.equalsIgnoreCase(streetCode)) {
                            Map<String, DistrictMap> nearby = shapeService.nearbyDistricts(geocodedAddress, distType);
                            if (nearby.containsKey(streetCode)) {
                                logger.debug("Consolidating " + distType + " district from " + shapeCode + " to " + streetCode + " for " + address);
                                shapeInfo.setDistCode(distType, streetCode);
                                shapeInfo.setDistMap(distType, nearby.get(streetCode));
                            }
                            else {
                                logger.warn("Mismatch on " + distType + "| Shape: " + shapeCode + " Street: " + streetCode + " for " + address);
                            }
                        }
                        else {
                            logger.trace(distType + " district could not be verified for " + address);
                            shapeInfo.addUncertainDistrict(distType);
                        }
                    }
                }
                fallbackSet.removeAll(shapeInfo.getAssignedDistricts());
                for (DistrictType districtType : fallbackSet) {
                    shapeInfo.setDistCode(districtType, streetInfo.getDistCode(districtType));
                }
            }
            else {
                logger.info("No street file result for " + address);
                for (DistrictType distType : shapeResult.getAssignedDistricts()) {
                    if (shapeInfo.getDistProximity(distType) < PROXIMITY_THRESHOLD) {
                        shapeInfo.addUncertainDistrict(distType);
                    }
                }
            }
        }
        return shapeResult;
    }
}