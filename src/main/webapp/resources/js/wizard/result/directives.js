var resultModule = angular.module("wizard.result");

resultModule.directive("resultTransfer", function() {
	return {
		restrict: "E",
		scope: {transfer: "=", status: "="},
		templateUrl: "resources/html/wizard/result-transfer.html"
	};
});
