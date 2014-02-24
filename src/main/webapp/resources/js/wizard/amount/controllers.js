var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope, sessionStorage) {

	// if there was a redirect, load stored order
	var storedOrder = sessionStorage.getItem("orderInfo");
	if (storedOrder) {
		$rootScope.orderInfo = JSON.parse(storedOrder);
	}

// for test
	$scope.getInUrl = function() {
		var prefix = 'resources/html/money/';
		return prefix + $rootScope.orderInfo.inTransfer.currency + '/in.html';
	};
});