package gov.nysenate.sage.factory;

import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.provider.*;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.address.CityZipServiceProvider;
import gov.nysenate.sage.service.address.ParallelAddressService;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.geo.*;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.service.street.StreetLookupServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;
import gov.nysenate.services.model.Senator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.*;

/**
 * ApplicationFactory is responsible for instantiating all single-instance objects that are utilized
 * across the application and providing a single access point for them. By utilizing the ApplicationFactory
 * all classes that would typically be implemented as singletons can be instantiated like regular classes
 * which allows for unit testing.
 *
 * The bootstrap method must be called once when the application is starting up. However if only
 * unit tests are to be run, the bootstrapTest method should be called instead. While these two
 * methods may setup similar dependencies, it will allow for using different configurations and
 * implementations for running unit tests.
 *
 * @author Ash
 */
public class ApplicationFactory
{
    private static final Logger logger = Logger.getLogger(ApplicationFactory.class);

    /** Static factory instance */
    private static final ApplicationFactory factoryInstance = new ApplicationFactory();
    private ApplicationFactory() {}

    /** Dependency instances */
    private Config config;
    private DB baseDB;
    private DB tigerDB;

    /** Service Providers */
    private AddressServiceProvider addressServiceProvider;
    private DistrictServiceProvider districtServiceProvider;
    private GeocodeServiceProvider geocodeServiceProvider;
    private RevGeocodeServiceProvider revGeocodeServiceProvider;
    private MapServiceProvider mapServiceProvider;
    private StreetLookupServiceProvider streetLookupServiceProvider;
    private CityZipServiceProvider cityZipServiceProvider;

    /** Meta Information */
    private Map<String, Class<? extends GeocodeService>> activeGeoProviders = new HashMap<>();

    /** Default values */
    private static String defaultPropertyFileName = "app.properties";
    private static String defaultTestPropertyFileName = "test.app.properties";

    /**
     * Sets up core application classes
     * @return boolean - If true then build succeeded
     */
    public static boolean bootstrap()
    {
        return factoryInstance.build(defaultPropertyFileName);
    }

    /**
     * Sets up core application classes for testing
     * @return boolean - If true then build succeeded
     */
    public static boolean bootstrapTest()
    {
        return factoryInstance.build(defaultTestPropertyFileName);
    }

    /**
     * Builds all the in-memory caches
     */
    public static void initializeCache()
    {
        factoryInstance.initCache();
    }

    /**
     * Closes all data connections
     * @return true if succeeded, false if exception was thrown
     */
    public static boolean close()
    {
        try {
            factoryInstance.baseDB.getDataSource().purge();
            factoryInstance.tigerDB.getDataSource().purge();
        }
        catch (Exception ex) {
            logger.error("Failed to purge data connections!", ex);
        }

        try {
            factoryInstance.baseDB.getDataSource().close(true);
            factoryInstance.tigerDB.getDataSource().close(true);

            ParallelDistrictService.shutdownThread();
            ParallelGeocodeService.shutdownThread();
            ParallelRevGeocodeService.shutdownThread();
            ParallelAddressService.shutdownThread();

            return true;
        }
        catch (Exception ex) {
            logger.error("Failed to close data connections/threads!", ex);
        }
        return false;
    }

    /**
     * The build() method will construct all the objects and their necessary dependencies that are
     * needed in the application scope..
     *
     * @return boolean  If true then build succeeded
     */
    private boolean build(String propertyFileName)
    {
        try
        {
            logger.info("------------------------------");
            logger.info("       INITIALIZING SAGE      ");
            logger.info("------------------------------");

            /** Setup application config */
            SageConfigurationListener configurationListener = new SageConfigurationListener();
            this.config = new Config(propertyFileName, configurationListener);
            this.baseDB = new DB(this.config, "db");
            this.tigerDB = new DB(this.config, "tiger.db");

            /** Setup address service providers. */
            String defaultUspsProvider = this.config.getValue("usps.default", "usps");
            Map<String, Class<? extends AddressService>> addressProviders = new HashMap<>();
            addressProviders.put("usps", USPSAMS.class);
            addressProviders.put("uspsais", USPSAIS.class);
            addressServiceProvider = new AddressServiceProvider();
            for (String key : addressProviders.keySet()) {
                addressServiceProvider.registerProvider(key, addressProviders.get(key));
            }
            addressServiceProvider.setDefaultProvider(defaultUspsProvider);

            /** Setup geocode service providers. */
            Map<String, Class<? extends GeocodeService>> geoProviders = new HashMap<>();
            geoProviders.put("yahoo", Yahoo.class);
            geoProviders.put("google", GoogleGeocoder.class);
            geoProviders.put("tiger", TigerGeocoder.class);
            geoProviders.put("mapquest", MapQuest.class);
            geoProviders.put("yahooboss", YahooBoss.class);
            geoProviders.put("osm", OSM.class);
            geoProviders.put("ruby", RubyGeocoder.class);

            /** Register the providers mapped above. */
            geocodeServiceProvider = new GeocodeServiceProvider();
            for (String key : geoProviders.keySet()) {
                logger.debug("Adding geocoder: " + key);
                geocodeServiceProvider.registerProvider(key, geoProviders.get(key));
            }

            List<String> activeList = this.config.getList("geocoder.active", Arrays.asList("yahoo", "tiger"));
            for (String provider : activeList) {
                GeocodeServiceValidator.setGeocoderAsActive(geoProviders.get(provider));
                activeGeoProviders.put(provider, geoProviders.get(provider));
            }

            LinkedList<String> geocoderRankList = new LinkedList<>(this.config.getList("geocoder.rank", Arrays.asList("yahoo", "tiger")));
            if (!geocoderRankList.isEmpty()) {
                /** Set the first geocoder as the default. */
                geocodeServiceProvider.setDefaultProvider(geocoderRankList.removeFirst());
                /** Set the fallback chain in the order of the ranking (excluding first). */
                geocodeServiceProvider.setProviderFallbackChain(geocoderRankList);
            }

            /** Designate which geocoders are allowed to cache. */
            List<String> cacheableProviderList = this.config.getList("geocoder.cacheable", Arrays.asList("yahoo", "mapquest", "yahooboss"));
            for (String provider : cacheableProviderList) {
                geocodeServiceProvider.registerProviderAsCacheable(provider);
            }

            /** Setup reverse geocode service providers. */
            revGeocodeServiceProvider = new RevGeocodeServiceProvider();
            revGeocodeServiceProvider.registerDefaultProvider("google", GoogleGeocoder.class);
            revGeocodeServiceProvider.registerProvider("mapquest", MapQuest.class);
            revGeocodeServiceProvider.registerProvider("tiger", TigerGeocoder.class);
            revGeocodeServiceProvider.setProviderFallbackChain(Arrays.asList("tiger"));

            /** Setup district lookup service providers. */
            districtServiceProvider = new DistrictServiceProvider();
            districtServiceProvider.registerDefaultProvider("shapefile", DistrictShapefile.class);
            districtServiceProvider.registerProvider("streetfile", StreetFile.class);
            districtServiceProvider.registerProvider("geoserver", Geoserver.class);
            districtServiceProvider.setProviderFallbackChain(Arrays.asList("streetfile"));

            /** Setup map data service providers. */
            mapServiceProvider = new MapServiceProvider();
            mapServiceProvider.registerDefaultProvider("shapefile", DistrictShapefile.class);

            /** Setup street data service providers. */
            streetLookupServiceProvider = new StreetLookupServiceProvider();
            streetLookupServiceProvider.registerDefaultProvider("streetfile", StreetFile.class);

            /** Setup city/zip data service providers. */
            cityZipServiceProvider = new CityZipServiceProvider();
            cityZipServiceProvider.registerDefaultProvider("cityZipDB", CityZipDB.class);

            logger.info("------------------------------");
            logger.info("            READY             ");
            logger.info("------------------------------");

            return true;
        }
        catch (ConfigurationException ce)
        {
            logger.fatal("Failed to load configuration file "+propertyFileName);
            logger.fatal(ce.getMessage());
        }
        catch (Exception ex)
        {
            logger.fatal("An exception occurred while building dependencies", ex);
        }
        return false;
    }

    private boolean initCache()
    {
        logger.info("Loading Map and Senator Caches...");

        /** Initialize district map cache */
        DistrictShapefileDao dso = new DistrictShapefileDao();
        if (!dso.cacheDistrictMaps()) {
            logger.fatal("Failed to cache district maps!");
            return false;
        };

        /** Initialize senator cache */
        SenateDao sd = new SenateDao();
        Collection<Senator> senators = sd.getSenators();
        if (senators == null || senators.isEmpty()) {
            logger.fatal("Failed to cache senators!");
            return false;
        }

        return true;
    }

    public static Config getConfig() {
        return factoryInstance.config;
    }

    public static DataSource getDataSource() {
        return factoryInstance.baseDB.getDataSource();
    }

    public static DataSource getTigerDataSource() {
        return factoryInstance.tigerDB.getDataSource();
    }

    public static AddressServiceProvider getAddressServiceProvider() {
        return factoryInstance.addressServiceProvider;
    }

    public static DistrictServiceProvider getDistrictServiceProvider() {
        return factoryInstance.districtServiceProvider;
    }

    public static GeocodeServiceProvider getGeocodeServiceProvider()  {
        return factoryInstance.geocodeServiceProvider;
    }

    public static RevGeocodeServiceProvider getRevGeocodeServiceProvider() {
        return factoryInstance.revGeocodeServiceProvider;
    }

    public static MapServiceProvider getMapServiceProvider() {
        return factoryInstance.mapServiceProvider;
    }

    public static StreetLookupServiceProvider getStreetLookupServiceProvider() {
        return factoryInstance.streetLookupServiceProvider;
    }

    public static CityZipServiceProvider getCityZipServiceProvider() {
        return factoryInstance.cityZipServiceProvider;
    }

    public static Map<String, Class<? extends GeocodeService>> getActiveGeoProviders() {
        return factoryInstance.activeGeoProviders;
    }
}
