/**
 * Created by yuxiaobin on 2016/7/28.
 *
 * 模拟增加模块页面
 */
angular.module('app', [ ])
    .service('wfService', ['$http', '$q', function ($http, $q) {
        this.newModule = function(refMkid){
            var delay = $q.defer();
            var req = {
                method: 'POST',
                url: basePath+'/wfadmin/module',
                data:{refMkid:refMkid}
            };
            $http(req)
                .success(function(data, status, headers, config){
                    delay.resolve(data);
                })
                .error(function(data, status, headers, config){
                  delay.reject(data);
                });
            return delay.promise;
        };
        this.getAllModules = function(){
            var delay = $q.defer();
            var req = {
                method: 'GET',
                url:basePath+ '/wfadmin/modules/list'
            };
            $http(req)
                .success(function(data, status, headers, config){
                    delay.resolve(data);
                })
                .error(function(data, status, headers, config){
                    delay.reject(data);
                });
            return delay.promise;
        };
    }])
    .controller('ctrl', ['$scope', 'wfService', function ($scope, wfService) {
        $scope.newModule = function () {
            if($scope.refMkid==undefined || $scope.refMkid==""){
                alert("请输入功能模块编号");
                return;
            }else{
                wfService.newModule($scope.refMkid).then(function(success){
                    if(success.statusCode=="dup"){
                        alert("该功能模块编号已存在");
                        return;
                    }
                    if($scope.moduleList==undefined){
                        $scope.moduleList = []
                    }
                    $scope.moduleList[$scope.moduleList.length] = {refMkid:success.refMkid};
                },function(fail){
                    console.error("newModule failed");
                    alert("创建失败");
                });
            }
        };
        wfService.getAllModules().then(function(success){
            $scope.moduleList = success.records;
        },function(fail){
            console.error("getAllModules failed");
        });

    }])

;