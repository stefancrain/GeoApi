package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.addr.Address;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.ArrayList;

/**
 * Comment this...
 */
public interface AddressService
{
    public AddressService newInstance();

    public AddressResult validate(Address address);
    public ArrayList<AddressResult> validate(ArrayList<Address> addresses);
    public ArrayList<AddressResult> lookupCityState(ArrayList<Address> addresses);
    public AddressResult lookupCityState(Address address);
}
