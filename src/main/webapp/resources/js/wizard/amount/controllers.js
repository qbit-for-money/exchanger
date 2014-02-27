var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope, restoreOrderInfoFromSession, ordersResource) {
	// if there was a redirect, load stored order
	restoreOrderInfoFromSession();
	
	// TEST
	$scope.send = function() {
		if ($rootScope.orderInfo) {
			$rootScope.orderInfo.userPublicKey = $rootScope.user.publicKey;
			var orderInfo = ordersResource.create($rootScope.orderInfo);
			orderInfo.$promise.then(function() {
				$rootScope.orderInfo = orderInfo;
			});
		}
	}
});