var amountModule = angular.module("wizard.amount");

amountModule.directive("amountTransfer", function() {
	return {
		restrict: "E",
		scope: {transfer: "=", direction: "="},
		templateUrl: "resources/html/wizard/amount-transfer.html"
	};
});
