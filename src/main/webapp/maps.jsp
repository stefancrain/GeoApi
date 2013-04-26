<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="sage" tagdir="/WEB-INF/tags" %>

<sage:wrapper>
    <jsp:attribute name="title">SAGE Map Viewer</jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="js/blockui.js"></script>
        <script type="text/javascript" src="app.js"></script>
    </jsp:attribute>
    <jsp:body>
        <div ng-controller="EmbeddedMapController">
            <script>
                districtType = "${districtType}";
                districtCode = "${districtCode}";
            </script>
        </div>
        <div id="mapView" ng-controller="EmbeddedMapViewController">
            <div class="top-header">
                <div class="large-icon icon-map white"></div>
                <div class="text">Map | {{header}}</div>
            </div>

            <div class="info-container" ng-show="showPrompt" style="position:absolute;left:40px;z-index:10000;">
                <table style="width:100%">
                <tr>
                    <td>
                        <a ng-hide="showInfo" ng-click="showInfo=true;">View Senator Information</a>
                        <a ng-show="showInfo" ng-click="showInfo=false;">Hide Senator Information</a>
                    </td>
                    <td class="right-icon-placeholder">
                        <a ng-hide="showInfo" ng-click="showInfo=true;"><div class="icon-arrow-down"></div></a>
                        <a ng-show="showInfo" ng-click="showInfo=false;"><div class="icon-arrow-up"></div></a>
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
    </jsp:body>
</sage:wrapper>
