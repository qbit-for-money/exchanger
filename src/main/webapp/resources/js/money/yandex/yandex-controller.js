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

	$scope.amount = 0;

	$scope.redirect = function() {
		var urlWrapper = yandexResource.getAuthorizeUrl();
		urlWrapper.$promise.then(function() {
			console.log(urlWrapper.url);
			$window.location.href = urlWrapper.url;
		});
	};

	$scope.fillInTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		if (orderInfo) {
			if (!orderInfo.inTransfer) {
				orderInfo.inTransfer = {};
			}
			orderInfo.inTransfer.address = $scope.address;
			orderInfo.inTransfer.amount = convertToOrderAmount($scope.amount);
		}
	};
	$scope.fillOutTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		if (orderInfo) {
			if (!orderInfo.outTransfer) {
				orderInfo.outTransfer = {};
			}
			orderInfo.outTransfer.address = $scope.address;
			orderInfo.outTransfer.amount = convertToOrderAmount($scope.amount);
		}
	};

	$scope.$watch("amount", $scope.fillInTransfer);
	$scope.$watch("address", $scope.fillInTransfer);

	function convertToOrderAmount(amount) {
		if (!amount) {
			return null;
		}
		var CENTS_IN_COIN = 100;
		var coins = Math.floor(amount);
		var cents = Math.floor(amount * CENTS_IN_COIN - coins * CENTS_IN_COIN);
		return {
			coins: coins,
			cents: cents
		};
	}
});