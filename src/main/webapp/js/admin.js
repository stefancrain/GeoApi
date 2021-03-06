var sageAdmin = angular.module('sage-admin', ['sage-common']);
var baseAdminApi = contextPath + "/admin/api";

sageAdmin.filter("code", function(){
    return function(input) {
        return (input) ? input.replace(/\\n/g, '<br/>').replace(/\\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;') : '';
    }
});

sageAdmin.controller('DashboardController', function($scope, $http, menuService, dataBus) {
    $scope.id = 1;
    $scope.visible = true;
    $scope.now = new Date();
    $scope.lastWeek = new Date(new Date().setDate(new Date().getDate() - 7));

    $scope.from = $scope.lastWeek;
    $scope.to = $scope.now;

    $scope.fromMonth = $scope.lastWeek.getMonth() + 1;
    $scope.fromDate = $scope.lastWeek.getDate();
    $scope.fromYear = $scope.lastWeek.getFullYear();

    $scope.toMonth = $scope.now.getMonth() + 1;
    $scope.toDate = $scope.now.getDate();
    $scope.toYear = $scope.now.getFullYear();

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.update = function() {
        dataBus.setBroadcast("update", true);
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init = function() {
        $scope.from.setMonth($scope.fromMonth - 1);
        $scope.from.setDate($scope.fromDate);
        $scope.from.setFullYear($scope.fromYear);
        $scope.from.setHours(0);
        $scope.from.setMinutes(0);
        $scope.to.setMonth($scope.toMonth - 1);
        $scope.to.setDate($scope.toDate);
        $scope.to.setFullYear($scope.toYear);
        $scope.to.setHours(23);
        $scope.to.setMinutes(59);
    };
});

sageAdmin.controller('DeploymentStatsController', function($scope, $http, dataBus) {

    $scope.init = function() {
        this.getDeploymentStats();
    };

    $scope.getDeploymentStats = function() {
        $http.get(baseAdminApi + "/deployment")
            .success(function(data){
                $scope = angular.extend($scope, data);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('ApiUsageController', function($scope, $http, dataBus){

    $scope.init = function() {
        this.getUsageStats((+this.from), (+this.to));
    };

    $scope.getUsageStats = function(startTime, endTime) {
        $http.get(baseAdminApi + "/usage?interval=HOUR&from=" + startTime + "&to=" + endTime)
            .success(function(data){
                $scope = angular.extend($scope, data);
                getSeriesData($scope.intervalFrom, $scope.intervalTo, $scope.intervalSizeInMinutes, $scope.intervalUsageCounts);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    var getSeriesData = function(startDate, endDate, interval, data) {
        var seriesData = [];
        var intervalMilli = interval * 60000;
        var next = startDate;
        $.each(data, function(i, v) {
            while (next < v.time && next < endDate) {
                seriesData.push(0);
                next += intervalMilli;
            }
            seriesData.push(v.count);
            next += intervalMilli;
        });
        makeApiUsageChart(startDate, seriesData);
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('ExceptionViewController', function($scope, $http, dataBus){
    $scope.exceptions = [];

    $scope.init = function() {
        this.getExceptions();
    };

    $scope.getExceptions = function() {
        $http.get(baseAdminApi + "/exception")
            .success(function(data){
                $scope.exceptions = data;
            })
            .error(function(){});
    };

    $scope.hideException = function(id) {
        $http.post(baseAdminApi + "/hideException?id=" + id)
            .success(function(data){
                if (data) {
                    if (data.success) {
                        $scope.getExceptions();
                    }
                    else {
                        alert(data.message);
                    }
                }
            })
            .error(function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('ApiUserStatsController', function($scope, $http, dataBus) {
    $scope.apiUserStats = [];

    $scope.init = function() {
        this.getApiUserStats();
    };

    $scope.getApiUserStats = function() {
        $http.get(baseAdminApi + "/apiUserUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                $scope.apiUserStats = data;
            })
            .error(function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('JobStatusController', function($scope, $http, dataBus){
    $scope.jobStatuses = [];

    $scope.init = function() {
        this.getJobStatuses();
    };

    $scope.getJobStatuses = function() {
        $http.get(baseAdminApi + "/jobStatuses?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                if (data) {
                    $scope.jobStatuses = data;
                }
            })
            .error(function(){
                console.log("Failed to retrieve job statuses!")
            });
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller("GeocodeUsageController", function($scope, $http, dataBus){

    $scope.init = function() {
        this.getGeocodeStats();
    };

    $scope.getGeocodeStats = function() {
        $http.get(baseAdminApi + "/geocodeUsage?from=" + (+$scope.from) + "&to=" + (+$scope.to))
            .success(function(data){
                if (data) {
                    $scope = angular.extend($scope, data);
                }
            })
            .error(function(data){
                console.log("Failed to retrieve geocode usage response!");
            });
    };

    $scope.getBarStyle = function(hits, total) {
        return {width: (hits/total) * 100 + "%"}
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});

sageAdmin.controller('UserConsoleController', function($scope, $http, menuService, dataBus) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.currentApiUsers = null;
    $scope.currentJobUsers = null;

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.getCurrentApiUsers = function() {
        $http.get(baseAdminApi + "/currentApiUsers").success(function(data){
            $scope.currentApiUsers = data;
        }).error(function(data){
            console.log("Failed to retrieve list of current Api users!");
        });
    };

    $scope.getCurrentJobUsers = function() {
        $http.get(baseAdminApi + "/currentJobUsers").success(function(data){
            $scope.currentJobUsers = data;
        }).error(function(data){
            console.log("Failed to retrieve list of current Job users!")
        });
    };

    $scope.createApiUser = function() {
        if (this.apiUserName == null || this.apiUserName == '') {
            alert("A name is required!");
        }
        else {
            $http.post(baseAdminApi + "/createApiUser?name=" + this.apiUserName + "&desc=" + this.apiUserDesc)
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                    else {
                        alert("Failed to add Api User!")
                    }
                }).error(function(data){
                    console.log("Failed to add Api User, invalid response from Admin Api.");
                });
        }
    };

    $scope.deleteApiUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteApiUser?id=" + id)
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                }).error(function(data){
                    console.log("Failed to delete Api User, invalid response from Admin Api");
                });
        }
    };

    $scope.createJobUser = function() {
        if (this.jobEmail == null || this.jobEmail == '' || this.jobPassword == null || this.jobPassword == '') {
            alert("Email and password must be specified!");
        }
        else {
            $http.post(baseAdminApi + "/createJobUser?email=" + this.jobEmail + "&password=" + this.jobPassword
                                                  + "&firstname=" + this.jobFirstName + "&lastname=" + this.jobLastName + "&admin=" + (this.jobAdmin ? "true" : "false"))
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) {
                            $scope.getCurrentJobUsers();
                        }
                    }
                }).error(function(data){
                    console.log("Failed to create Job User, invalid response from Admin Api");
                });
        }
    };

    $scope.deleteJobUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteJobUser?id=" + id).success(function(data){
                if (data) {
                    alert(data.message);
                    if (data.success) $scope.getCurrentJobUsers();
                }
            }).error(function(data){
                console.log("Failed to delete Job User, invalid response from Admin Api");
            });
        }
    };

    (function init() {
        $scope.getCurrentApiUsers();
        $scope.getCurrentJobUsers();
    })();

});

$(document).ready(function() {
    initVerticalMenu();
});

function makeApiUsageChart(startDate, seriesData) {
    $('#api-usage-stats').highcharts({
        chart: {
            zoomType: 'x',
            spacingRight: 20,
            height: 300
        },
        credits: {
            enabled: false
        },
        title: {
            text: null
        },
        subtitle: {
            text: null
        },
        xAxis: {
            type: 'datetime',
            maxZoom: 3600000, // 1 hour
            title: {
                text: null
            },
            tickColor: 'teal',
            tickWidth: 3
        },
        yAxis: {
            title: {
                text: 'Requests per hour'
            },
            min: 0
        },
        tooltip: {
            shared: true
        },
        legend: {
            enabled: false
        },
        plotOptions: {
            areaspline : {
                fillColor: '#CC333F',
                lineWidth: 1,
                lineColor: '#CC333F',
                marker: {
                    enabled: false
                },
                shadow: false,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },
        series: [{
            type: 'areaspline',
            name: 'Requests',
            pointInterval: 3600 * 1000,
            pointStart: startDate - (4 * 3600000), /* EST Time zone correction ~.~ */
            data: seriesData
        }]
    });
}