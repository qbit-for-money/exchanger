var resultModule = angular.module("amount");

resultModule.controller("AmountController", function($rootScope, $scope, sessionStorage) {
	var storedOrder = sessionStorage.getItem("orderInfo");
	if (storedOrder) {
		$rootScope.orderInfo = JSON.parse(storedOrder);
	}

	$scope.getInUrl = function() {
		var prefix = 'resources/html/money/';
		return prefix + $rootScope.orderInfo.inTransfer.currency + '/in.html';
	};
});