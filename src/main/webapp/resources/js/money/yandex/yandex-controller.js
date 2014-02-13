var moneyModule = angular.module("money");

moneyModule.controller("YandexController", function($scope, $location, $window, $rootScope, yandexResource) {
	$scope.authorized = false;
	var address;
	address = $location.search()["wallet"];
	if (address) {
		$scope.address = address;
		$scope.authorized = true;
	} else {
		// TODO redirect
	}

	$scope.amount = {
		coins: 0,
		cents: 0
	};

	$scope.redirect = function() {
		var urlResponse = yandexResource.getAuthorizeUrl();
		urlResponse.$promise.then(function() {
			console.log(urlResponse.url);
			$window.location.href = urlResponse.url;
		});
	};

	$scope.fillInTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		if (orderInfo) {
			orderInfo.inTransfer.address = $scope.address;
			orderInfo.inTransfer.amount = $scope.amount;
		}
	};
	$scope.fillOutTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		if (orderInfo) {
			orderInfo.outTransfer.address = $scope.address;
			orderInfo.outTransfer.amount = $scope.amount;
		}
	};

	$scope.$watch("amount", $scope.fillInTransfer);
	$scope.$watch("address", $scope.fillInTransfer);

	function convertToOrderAmount(amount) {
		var result = {
			coins: 0,
			cents: 0
		};
		if (amount) {
			result.coins = amount.coins ? amount.coins : 0;
			result.cents = amount.cents ? amount.cents : 0;
		}
		return result;
	}
});