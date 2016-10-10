/**
 * Created by yuxiaobin on 2016/10/8.
 */
angular.module('funcVarApp', [ ])
    .service('wfService', ['$http', '$q', function ($http, $q) {
        this.getFuncVars = function(refMkid,version){
            var delay = $q.defer();
            var data = {refMkid:refMkid};
            if(typeof(version)!='undefined'){
                data.version = version;
            }
            var req = {
                method: 'POST',
                data:data,
                url: basePath+'/wfadmin/custvars'
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
    .controller('ctrl', ['$scope','$window','$timeout', 'wfService', function ($scope,$window,$timeout, wfService) {
        $scope.getFuncVars = function(refMkid){
            wfService.getFuncVars(refMkid).then(function(success){
                $scope.funcVarList = success;
                custFuncVarArray = $scope.funcVarList;
            },function(fail){
                console.error("getTaskInbox failed"+fail);
            });
        };
        $scope.getFuncVars(refMkid);
        $scope.saveCustVar = function(){
            if(angular.isUndefined($scope.custVar.varCode)||$scope.custVar.varCode==""
                ||angular.isUndefined($scope.custVar.varDescp)|| $scope.custVar.varDescp==""){
                $scope.funcVarError = "名称和编码必须指定";
                $('#messageModal').modal({backdrop:true});
                $timeout(function(){
                    $("#messageModal").modal("hide");
                },2000);
                return;
            }
            var edit_index = -1;
            $("#funcVarTable tr").each(function(index){
                if($(this).hasClass("active")){
                    edit_index = index-1;//first tr is <thead>
                }
            });
            if(edit_index==-1){//add
                var isContained = false;
                for(var i=0;i<$scope.funcVarList.length;++i){
                    if($scope.funcVarList[i].varCode==$scope.custVar.varCode){
                        $scope.funcVarList[i] = $scope.custVar;
                        isContained = true;
                        break;
                    }
                }
                if(!isContained){
                    $scope.funcVarList[$scope.funcVarList.length] = $scope.custVar;
                }
            }else{//edit
                for(var i=0;i<$scope.funcVarList.length;++i){
                    if(i!=edit_index && $scope.funcVarList[i].varCode==$scope.custVar.varCode){
                        $scope.funcVarError = "相同的编码已存在";
                        $('#messageModal').modal({backdrop:true});
                        $timeout(function(){
                            $("#messageModal").modal("hide");
                        },2000);
                        return;
                    }
                }
                $scope.funcVarList[edit_index] = $scope.custVar;
                $("#funcVarTable tr").eq(edit_index+1).removeClass("active");
            }
            custFuncVarArray = $scope.funcVarList;
            $scope.addCustVar();
        };
        $scope.addCustVar = function(){
            $scope.custVar = {varType:"U"};
            $("#funcVarTable tr").removeClass("active");
            $("#selectVarType").selectpicker('hide').selectpicker("destroy");
            setTimeout(function(){
                $("#selectVarType").selectpicker('show').selectpicker('val', "U");
            },100);
            $("#funcVarEdit").show();
        }
        $scope.editFuncVar = function(evt,funcVar){
            var selectedTr = $(evt.target).parent();
            selectedTr.addClass("active").siblings().removeClass("active");
            angular.copy(funcVar,$scope.custVar);
        }
        $scope.deleteCustVar = function(){
            if(angular.isUndefined($scope.funcVarList)){
                return;
            }
            var del_index = -1;
            $("#funcVarTable tr").each(function(index){
                if($(this).hasClass("active")){
                    del_index = index-1;//first tr is <thead>
                }
            });
            if(del_index==-1){
                return;
            }
            $scope.funcVarList = $.grep($scope.funcVarList, function(value, index){
                if(index==del_index){
                    return false;
                }
                return true;
            })
            $scope.addCustVar();
        }

    }])
   ;

function showWfDef(obj){
    $("#wfDefDiv").show();
    $("#custVarDiv").hide();
    $(obj).parent().addClass("active").siblings().removeClass("active");
}
function showCustVar(obj){
    $("#custVarDiv").show();
    $("#wfDefDiv").hide();
    $(obj).parent().addClass("active").siblings().removeClass("active");
}