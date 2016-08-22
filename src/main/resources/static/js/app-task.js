/**
 * Created by yuxiaobin on 2016/8/5.
 *
 * Popup page for Edit Task
 */
var basePath = "";
angular.module('taskApp', [ ])
    .service('userGroupService',['$http', '$q', function ($http, $q) {
        this.getAllUsers = function(){
            var delay = $q.defer();
            var req = {
                method: 'GET',
                url: basePath+'/usergroup/users'
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
        this.getAllGroups = function(){
            var delay = $q.defer();
            var req = {
                method: 'GET',
                url: basePath+'/usergroup/groups'
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
    .controller('taskCtrl', ['$scope','$timeout', 'userGroupService', function ($scope,$timeout, userGroupService) {
        $scope.defSelModTxt_yes = "默认选中";
        $scope.defSelModTxt_no = "默认不选中";
        $scope.assignTypeDesc_user = "用户";
        $scope.assignTypeDesc_group = "用户组";
        $scope.assignTypeDesc_other = "其他";
        userGroupService.getAllUsers().then(function(success){
            $scope.userList = success.records;
            userGroupService.getAllGroups().then(function(success){
                $scope.groupList = success.records;
                if(taskData.assigners!=""){
                    $timeout(function(){
                        $scope.assignerList = taskData.assigners;
                        angular.forEach($scope.assignerList,function(item,index){
                            if(item.assignTypeCode=="U"){
                                item.assignTypeDesc = $scope.assignTypeDesc_user;
                            }else if(item.assignTypeCode=="G"){
                                item.assignTypeDesc =  $scope.assignTypeDesc_group;
                            } else{
                                item.assignTypeDesc = $scope.assignTypeDesc_other;
                            }
                            if(item.defSelMod=="Y"){
                                item.defSelModTxt = $scope.defSelModTxt_yes;

                            }else{
                                item.defSelModTxt = $scope.defSelModTxt_no;
                            }
                        });

                    },500);
                }
            })
        });
        $scope.task = taskData;
        if(taskData.taskType==RS_TYPE_START || taskData.taskType==RS_TYPE_END){
            $scope.showAssignerEdit = false;
            $scope.isStartEndNode = true;
            if(taskData.taskType==RS_TYPE_START){
                $scope.isStartNode = true;
                $scope.isEndNode = false;
                $scope.task.txType = "B";
                $scope.task.buzStatus = "I";
                if(angular.isUndefined($scope.TX_CHOICES)){
                    $scope.TX_CHOICES = {};
                }
                $scope.TX_CHOICES.AllowEdit = true;
                $scope.TX_CHOICES.AllowDelete = true;
            }else{
                $scope.isStartNode = false;
                $scope.isEndNode = true;
                $scope.task.txType = "E";
                $scope.task.buzStatus = "C";
                if(angular.isUndefined($scope.TX_CHOICES)){
                    $scope.TX_CHOICES = {};
                }
                $scope.TX_CHOICES.AllowEdit = false;
                $scope.TX_CHOICES.AllowDelete = false;
            }
            $scope.task.timeLimitTp = "H";
            $scope.task.alarmTimeTp = "H";
            $scope.txTypeOptions = [{value:"B",descp:"开始"},{value:"E",descp:"结束"}]
        }else{
            $scope.showAssignerEdit = true;
            $scope.isStartEndNode = false;
            $scope.txTypeOptions = [{value:"S",descp:"一般事务"},{value:"M",descp:"会签事务"}]
        }

        $scope.TX_CHOICES = taskData.TX_CHOICES;//to avoid data convert issue when save
        if($scope.task.TX_PR_CHOICES.NoticeNextAfterGo || $scope.task.TX_PR_CHOICES.NoticeFirstAfterGo || $scope.task.TX_PR_CHOICES.NoticePreviousAfterGo || $scope.task.TX_PR_CHOICES.NoticeElseAfterGo){
            $scope.needNotifyFlag = true;
        }

        $scope.updateTaskProperties = function(){
            taskData.taskDescp = $("#taskDescp").val();
            taskData.taskDescpDisp = $("#taskDescpDisp").val();
            var assigns = [];
            angular.forEach($scope.assignerList,function(item, index){
                var assigner = {};
                assigner.assignTypeDesc = item.assignTypeDesc;
                assigner.assignTypeCode = item.assignTypeCode;
                assigner.name = item.name;
                assigner.id = item.id;
                assigner.defSelModTxt = item.defSelModTxt;
                assigner.checkFlag = item.checkFlag;
               assigns.push(assigner);
            });
            taskData.assigners = assigns;
            taskData.txCode = $scope.task.txCode;
            taskData.txType = $scope.task.txType;
            taskData.buzStatus = $scope.task.buzStatus;
            taskData.timeLimit =  $scope.task.timeLimit;
            taskData.timeLimitTp =  $scope.task.timeLimitTp;
            taskData.alarmTime = $scope.task.alarmTime;
            taskData.alarmTimeTp =  $scope.task.alarmTimeTp;
            taskData.moduleId = $scope.task.moduleId;
            taskData.runParam = $scope.task.runParam;
            taskData.TX_CHOICES = $scope.TX_CHOICES;
            if(!taskData.TX_CHOICES.AllowGoBack){
                taskData.TX_CHOICES.SignWhenGoBack = false;
            }
            if(!taskData.TX_CHOICES.AllowReCall){
                taskData.TX_CHOICES.SignWhenReCall = false;
            }
            if(!taskData.TX_CHOICES.AllowVeto){
                taskData.TX_CHOICES.SignWhenVeto = false;
            }
            taskData.TX_PR_CHOICES = $scope.task.TX_PR_CHOICES;
            taskData.TX_BK_CHOICES = $scope.task.TX_BK_CHOICES;
            taskData.SIGN_CHOICES = $scope.task.SIGN_CHOICES;
            taskData.opt = "U";
            window.parent.postMessage(JSON.stringify(taskData), '*');
            $("#successMsg").css("display","");
        }

        $scope.deleteTask = function(){
            taskData.opt = "D";
            window.parent.postMessage(JSON.stringify(taskData), '*');
            $("#deleteTaskAlert").css("display","none");
            $("#successMsg").css("display","");
        }

        $scope.selectUser = function(user_id){
            var elm_ = $("#selectAllUsersId").siblings("[rs-data-asid='"+user_id+"']").eq(0);
            if(elm_.hasClass("active")){
                angular.forEach($scope.userList, function (item, index) {
                    if(item.id==user_id){
                        item.extClass = "";
                    }
                });
            }else{
                angular.forEach($scope.userList, function (item, index) {
                    if(item.id==user_id) {
                        item.extClass = "active";
                    }
                });
            }
        }
        $scope.selectGroup = function(group_id){
            var elm_ = $("#selectAllGroupId").siblings("[rs-data-asid='"+group_id+"']");
            if(elm_.hasClass("active")){
                angular.forEach($scope.groupList, function (item, index) {
                    if(item.groupId==group_id){
                        item.extClass = "";
                    }
                });
            }else{
                angular.forEach($scope.groupList, function (item, index) {
                    if(item.groupId==group_id) {
                        item.extClass = "active";
                    }
                });
            }
        }
        $scope.selectAllUserChange = function(){
            if($("#allUsersCheckbox").is(':checked')){
                angular.forEach($scope.userList, function (item, index) {
                    item.extClass = "active";
                });
            }else{
                angular.forEach($scope.userList, function (item, index) {
                    item.extClass = "";
                });
            }
        }
        $scope.selectAllGroupChange = function(){
            if($("#allGroupsCheckbox").is(':checked')){
                angular.forEach($scope.groupList, function (item, index) {
                    item.extClass = "active";
                });
            }else{
                angular.forEach($scope.groupList, function (item, index) {
                    item.extClass = "";
                });
            }
        }

        $scope.saveUserGroup = function(){
            if(angular.isUndefined($scope.assignerList)){
                $scope.assignerList = [];
            }
            var assignListId = "";
            if($scope.ugflag=="U"){
                assignListId = "selectAllUsersId";
            }else if($scope.ugflag=="G"){
                assignListId = "selectAllGroupId";
            }else{
                assignListId = "selectAllGroupId";//TODO:
            }
            $("#"+assignListId).siblings(".active").each(function(){
                var assigner = {};
                if($scope.ugflag=="U"){
                    assigner.assignTypeDesc = "用户";
                }else if($scope.ugflag=="G"){
                    assigner.assignTypeDesc = "用户组";
                }else if($scope.ugflag=="R"){
                    assigner.assignTypeDesc = "岗位";
                }else{
                    assigner.assignTypeDesc = "自定义";
                }
                assigner.assignTypeCode = $scope.ugflag;
                assigner.name = $(this).attr("rs-data-asname");
                assigner.id = $(this).attr("rs-data-asid");
                assigner.defSelModTxt = $("#selModeTxt").text();
                assigner.defSelMod = $("#selModeTxt").attr("def-sel-mod");
                if($("#selAllFlag").is(':checked')){
                    assigner.checkFlag = true;
                }else{
                    assigner.checkFlag = false;
                }
                assigner.exeConn = $scope.exeConn;
                if(isAssignerSelected($scope.assignerList,assigner)){
                    console.log("same user/group already existed");
                }else{
                    $scope.assignerList[$scope.assignerList.length] = assigner;
                }
                $(this).removeClass("active");

            });
            $("#selectAllGroupId").siblings(".active").each(function(){
//                assignedGroupsStr+=$(this).attr("rs-data-gpname")+",";
            });
        };
        $scope.selectAddUserGroups = function(ugflag,evt){
            $scope.ugflag = ugflag;
            $("#addUserGroupId").css("display","");
            if("U"==ugflag){
                $("#showUsers").css("display","");
                $("#showGroups").css("display","none");
            }
            else if("G"==ugflag){
                $("#showGroups").css("display","");
                $("#showUsers").css("display","none");
            }
            $("#addDropdownTxt").text($(evt.target).text());
        }
        $scope.selectAssignerDefSelMode = function(evt){
            var sel_target = $(evt.target);
            $("#selModeTxt").text(sel_target.text()).attr("def-sel-mod",sel_target.attr("def-sel-mod"));
            sel_target.parent().addClass("active");
            sel_target.parent().siblings().removeClass("active");
        }

        $scope.csFlag = false;
        $scope.$watch('task.txType', function(newVal, oldVal) {
            if(newVal=="M"){
                $scope.csFlag = true;
            }else{
                $scope.csFlag = false;
                $scope.task.SIGN_CHOICES.AllHandledThenGo = false;
                $scope.task.SIGN_CHOICES.PartHandledThenGo = false;
                $scope.task.SIGN_CHOICES.AtLeastHandled = "";
                $scope.task.TX_PR_CHOICES.NoticeElseAfterGo = false;
            }
        });

        $scope.selectNotify = function(event){
            if($("#txpNotifyNextOnPro").is(':checked') || $("#txpNotifyCreOnPro").is(':checked') || $("#txpNotifyPreOnPro").is(':checked')){
                $scope.needNotifyFlag = true;
            }else{
                $scope.needNotifyFlag = false;
                $scope.task.TX_PR_CHOICES.MsgAlert = false;
                $scope.task.TX_PR_CHOICES.SmsAlert = false;
            }
        }
    }]);

function confirmDelete(){
    $("#deleteTaskAlert").css("display","");
}
function notDeleteTask(){
    $("#deleteTaskAlert").css("display","none");
}

function changeTab(obj,formId_){
    var li_ = $(obj).parent();
    li_.addClass("active");
    li_.siblings("li").removeClass("active");
    $("#"+formId_).show();
    $("#"+formId_).siblings("form").hide();
}

function hideModal(){
    var data_ = {};
    data_.opt="C";
    window.parent.postMessage(JSON.stringify(data_), '*');
}

function isAssignerSelected(assignerList, assigner){
    var asnId = assigner.id;
    var asnType = assigner.assignTypeCode;
    var existed = $.grep(assignerList,function(value) {
        return value.id == asnId && value.assignTypeCode==asnType;
    });
    if(existed==undefined || existed==""){
        return false;
    }
    return true;
}
