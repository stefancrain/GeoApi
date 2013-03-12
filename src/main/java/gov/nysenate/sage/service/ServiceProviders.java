package gov.nysenate.sage.service;

import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is used for registering and obtaining implementation instances for a
 * particular service identified by the template parameter.
 *
 * For example, using ExampleService as the service and exampleImpl as an instance:
 * <code>
 * ServiceProviders<ExampleService> exampleServiceProvider = new ServiceProviders<>();
 * exampleServiceProvider.registerDefaultProvider("impl", exampleImpl); // Register
 * ExampleService impl = exampleServiceProvider.newInstance();   // Get new instance
 * </code>
 *
 * So essentially it's a simple way to keep track of which classes can serve as an
 * implementation of a given service and instantiate them.
 * @param <T>   T is the Service to provide implementations for.
 */
public class ServiceProviders<T>
{
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String,T> providers = new HashMap<>();
    private String defaultProvider = "default";

    /**
     * Registers the default service as an instance of the given provider.
     * @param provider  The service implementation that should be default.
     */
    public void registerDefaultProvider(String providerName, T provider)
    {
        defaultProvider = providerName;
        providers.put(defaultProvider, provider);
    }

    /**
     * Registers an instance of a service implementation.
     * @param providerName  Key that will be used to reference this provider.
     * @param provider      An instance of the provider.
     */
    public void registerProvider(String providerName, T provider)
    {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Returns the set of mapped keys.
     * @return
     */
    public Set<String> getProviderNames()
    {
        return providers.keySet();
    }

    /**
     * Determines if given providerName is registered
     * @param providerName
     * @return
     */
    public boolean isRegistered(String providerName)
    {
        return (providerName != null && !providerName.isEmpty() && this.providers.containsKey(providerName.toLowerCase()));
    }

    /**
     * Returns a new instance of the default T implementation.
     * @return   T if default provider is set.
     *           null if default provider not set.
     */
    public T newInstance()
    {
        if (providers.containsKey(defaultProvider)){
            return newInstance(defaultProvider);
        }
        else {
            logger.debug("Default address provider not registered!");
            return null;
        }
    }

    /**
     * Returns a new instance of the provider that has been registered
     * with the given providerName.
     * @param providerName
     * @return  T instance specified by providerName.
     *          null if provider is not specified/registered.
     */
    public T newInstance(String providerName)
    {
        if (providerName != null && !providerName.isEmpty()) {
            if (providers.containsKey(providerName.toLowerCase())){
                try {
                    return (T) providers.get(providerName.toLowerCase()).getClass().newInstance();
                }
                catch (InstantiationException ie){
                    logger.error(ie.getMessage());
                }
                catch (IllegalAccessException iea){
                    logger.error(iea.getMessage());
                }
            }
            else {
                logger.debug(providerName + " is not a registered provider!");
            }
        }
        return null;
    }

    /**
     * Allows for fallback to a default service if the provider does not exist.
     * @param providerName
     * @param useFallback - Set true to use the default registered provider if
     *                      providerName does not exist.
     * @return  T if providerName found or useFallback:true
     *          null otherwise
     */
    public T newInstance(String providerName, boolean useFallback)
    {
        if (providerName != null){
            T a = newInstance(providerName);
            if (a != null){
                return a;
            }
        }
        else if (useFallback){
            return newInstance();
        }
        return null;
    }

    /**
     * Allows for a specified fallback service if the provider does not exist.
     * @param providerName
     * @param fallbackProviderName
     * @return T if providerName or fallbackProviderName valid
     *         null otherwise
     */
    public T newInstance(String providerName, String fallbackProviderName)
    {
        T a = newInstance(providerName);
        return (a != null) ? a : newInstance(fallbackProviderName);
    }
}
