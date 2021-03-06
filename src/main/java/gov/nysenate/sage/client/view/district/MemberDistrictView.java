package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;

public class MemberDistrictView extends DistrictView
{
    protected MemberView member;

    public MemberDistrictView(DistrictType districtType, DistrictInfo districtInfo, DistrictMember districtMember)
    {
        super(districtType, districtInfo);
        this.member = new MemberView(districtMember);
    }

    public MemberView getMember() {
        return (member != null && member.name != null) ? member : null;
    }
}
