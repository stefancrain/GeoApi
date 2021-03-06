package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static gov.nysenate.sage.DistrictTestBase.assertMultipleAddressDistrictAssign;
import static gov.nysenate.sage.DistrictTestBase.assertSingleAddressDistrictAssign;
import static gov.nysenate.sage.DistrictTestBase.expected;
import static junit.framework.Assert.*;

public class StreetFileTest extends TestBase
{
    private Logger logger = Logger.getLogger(StreetFileTest.class);
    private StreetFile streetFile;

    @Before
    public void setUp()
    {
        this.streetFile = new StreetFile();
    }

    @Test
    public void singleAddressDistrictAssign_ReturnsDistrictResult()
    {
        assertSingleAddressDistrictAssign(streetFile);
    }

    @Test
    public void multipleAddressDistrictAssign_ReturnsDistrictResult()
    {
        /** TODO: Fails because NYC addresses aren't parsed correcty atm.. */
        assertMultipleAddressDistrictAssign(streetFile);
    }

    @Test
    public void singleAddressDistrictAssignWithMissingTypes_ReturnsPartialResult()
    {
        DistrictedAddress distAddrExpected = expected.get(0);
        DistrictResult districtResult = this.streetFile.assignDistricts(distAddrExpected.getGeocodedAddress(), DistrictType.getAllTypes());
    }

    @Test
    public void test()
    {
        FormatUtil.printObject(this.streetFile.assignDistricts(new GeocodedAddress(new Address("161 ATTORNEY ST APT 3A" ,"", "NY", "10002"))));
    }

}
