package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.CountyDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.COUNTY;

/**
 * Provides a data abstraction layer for performing Geoserver WFS requests.
 *
 * Geoserver WFS Documentation:
 * http://docs.geoserver.org/stable/en/user/services/wfs/reference.html
 */
public class GeoserverDao extends BaseDao
{
    private static final String DEFAULT_BASE_URL = "http://geoserver.nysenate.gov:8080/wfs";
    private static final String DEFAULT_WORKSPACE = "nysenate";
    private static final String FEATURE_QUERY = "?service=WFS&version=1.1.0&request=GetFeature";
    private static final String INTERSECT_FILTER = "INTERSECTS(the_geom, POINT ( %f %f ))";

    private Logger logger = Logger.getLogger(GeoserverDao.class);
    private Map<Integer, County> fipsCountyMap; /** Mapping between fips codes and associated county */
    private ObjectMapper mapper;

    private String baseUrl = "";    // Location of geoserver e.g http://localhost:8080/geoserver
    private String workspace = "";  // Geoserver workspace for the shapefiles e.g nysenate

    public GeoserverDao() {
        this.mapper = new ObjectMapper();
        this.fipsCountyMap = new CountyDao().getFipsCountyMap();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return (this.baseUrl != null && !this.baseUrl.isEmpty()) ? this.baseUrl : DEFAULT_BASE_URL;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getWorkspace() {
        return (this.workspace != null && !this.workspace.isEmpty()) ? this.workspace : DEFAULT_WORKSPACE;
    }

    /**
     * Submits a url request to GeoServer, parses response, and returns a DistrictInfo object
     *
     * @param point            The Point to find districts at
     * @param districtTypes    The types of districts to get features for
     * @return DistrictInfo if successful, null otherwise
     */
    public DistrictInfo getDistrictInfo(Point point, List<DistrictType> districtTypes)
    {
        ArrayList<String> featureTypesList = new ArrayList<>();
        for (DistrictType districtType : districtTypes) {
            featureTypesList.add(getWorkspace() + ":" + districtType.toString().toLowerCase());
        }
        String featureTypes = "typename="+ StringUtils.join(featureTypesList, ",");
        String filter = String.format(INTERSECT_FILTER, point.getLat(), point.getLon());

        try {
            String sourceUrl = String.format(getBaseUrl() + FEATURE_QUERY + "&%s&CQL_FILTER=%s&outputformat=JSON", featureTypes, URLEncoder.encode(filter, "UTF-8"));
            String json = UrlRequest.getResponseFromUrl(sourceUrl);
            JsonNode response = mapper.readTree(json);
            return getDistrictInfoFromResponse(response);
        }
        catch (IOException ex) {
            logger.error("Could not retrieve response from geocoder. Ensure that geoserver is running", ex);
            return null;
        }
    }

    /**
     * Parses JSON response and creates a DistrictInfo object.
     * @param response  Root level JsonNode
     * @return          DistrictInfo
     */
    private DistrictInfo getDistrictInfoFromResponse(JsonNode response)
    {
        DistrictInfo districtInfo = new DistrictInfo();
        JsonNode features = response.get("features");

        for (int i = 0; i < features.size(); i++) {
            JsonNode feature = features.get(i);
            JsonNode properties = feature.get("properties");

            String layer = feature.get("id").asText().split("\\.")[0];
            DistrictType districtType = DistrictType.resolveType(layer);
            if (districtType != null){
                /** Get the column names used in the shapefiles */
                String nameColumn = DistrictShapeCode.getNameColumn(districtType);
                String codeColumn = DistrictShapeCode.getCodeColumn(districtType);

                /** Set the name, code, and map data for the district layer */
                districtInfo.setDistName(districtType, properties.get(nameColumn).asText());
                districtInfo.setDistCode(districtType, properties.get(codeColumn).asText());
                districtInfo.setDistMap(districtType, getDistrictMapFromFeature(feature));

                /** Handle county fips -> senate code conversion */
                if (districtType == COUNTY){
                    Integer countyCode = Integer.valueOf(districtInfo.getDistCode(COUNTY));
                    districtInfo.setDistCode(COUNTY, Integer.toString(fipsCountyMap.get(countyCode).getId()));
                }
            }
            else {
                logger.warn("Unidentified feature id " + feature.get("id").asText() + " found in geoserver response");
            }
        }
        return districtInfo;
    }

    /**
     * Parses JSON response and creates a DistrictMap object containing the district geometry.
     * @param feature   Feature level JsonNode
     * @return          DistrictMap containing the geometry.
     */
    private DistrictMap getDistrictMapFromFeature(JsonNode feature)
    {
        DistrictMap districtMap = new DistrictMap();
        List<Point> points = new ArrayList<>();

        /** The geometry response comes in as a quadruply nested array */
        JsonNode coordinates = feature.get("geometry").get("coordinates").get(0).get(0);
        for (int i = 0; i < coordinates.size(); i++){
            points.add(new Point(coordinates.get(i).get(0).asDouble(), coordinates.get(i).get(1).asDouble()));
        }
        districtMap.addPolygon(new Polygon(points));
        return districtMap;
    }
}

