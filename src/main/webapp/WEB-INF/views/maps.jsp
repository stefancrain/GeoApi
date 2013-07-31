<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="ngApp">sage</jsp:attribute>
    <jsp:attribute name="title">SAGE Map Viewer</jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC-vIdRb4DI5jzKI92UNTnjHiwU7P0GqxI&sensor=false&libraries=places"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/vendor/blockui.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/app.js"></script>
    </jsp:attribute>
    <jsp:body>
        <div id="contentwrapper">
            <div id="mapcontentcolumn">
                <div ng-controller="EmbeddedMapController">
                    <script>
                        districtType = "${districtType}";
                        districtCode = "${districtCode}";
                    </script>
                </div>
                <div id="mapView" ng-controller="EmbeddedMapViewController">
                    <div class="info-container" ng-show="showPrompt" style="width:280px;padding:3px 10px;position:absolute;left:40px;z-index:10000;">
                        <table style="width:100%">
                            <tr>
                                <td>
                                    <a ng-hide="showInfo" ng-click="showInfo=true;">Senator Information</a>
                                    <a ng-show="showInfo" ng-click="showInfo=false;">Senator Information</a>
                                </td>
                                <td class="right-icon-placeholder">
                                    <a ng-hide="showInfo" ng-click="showInfo=true;"><div class="icon-arrow-down4"></div></a>
                                    <a ng-show="showInfo" ng-click="showInfo=false;"><div class="icon-arrow-up4"></div></a>
                                </td>
                            </tr>
                        </table>
                        <div ng-show="showInfo" id="senator-view" style="padding-top:10px;border-top:1px solid #ddd">
                            <div class="mini-senator-pic-holder">
                                <a ng-href="{{senator.url}}" target="_top"><img ng-src="{{senator.imageUrl | senatorPic}}" class="senator-pic" /></a>
                            </div>
                            <div>
                                <p class="senator member-name">
                                    <a target="_blank" ng-href="{{senator.url}}">{{senator.name}}</a>
                                </p>
                                <p class="senate district">Senate District {{district}}</p>
                            </div>
                        </div>
                    </div>
                    <div id="map_canvas"></div>
                </div>
            </div>
        </div>

        <!-- Map Tooltip -->
        <div id="mapTooltip"></div>
    </jsp:body>
</sage:wrapper>