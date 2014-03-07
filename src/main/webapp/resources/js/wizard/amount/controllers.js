var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function(restoreOrderInfoFromSession) {
// if there was a redirect, load stored order
	restoreOrderInfoFromSession();
});