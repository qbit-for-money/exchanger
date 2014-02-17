var moneyModule = angular.module("amount");

moneyModule.controller("YandexController", function($scope, $location, $window, $rootScope, yandexResource, sessionStorage) {
	$scope.authorized = false;
	var address;
	address = $location.search()["wallet"];
	if (address) {
		$scope.address = address;
		$scope.authorized = true;
	}

	$scope.amount = {
		coins: 0,
		cents: 0,
		centsInCoin: 100
	};

	$scope.redirect = function() {
		var urlResponse = yandexResource.getAuthorizeUrl();
		urlResponse.$promise.then(function() {
			sessionStorage.setItem("orderInfo", JSON.stringify($rootScope.orderInfo));
			$window.location.href = urlResponse.url;
		});
	};

	$scope.fillInTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		orderInfo.inTransfer.address = $scope.address;
		orderInfo.inTransfer.amount = $scope.amount;
	};
	$scope.fillOutTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		orderInfo.outTransfer.address = $scope.address;
		orderInfo.outTransfer.amount = $scope.amount;
	};

	if ($rootScope.orderInfo.inTransfer.currency === "YANDEX_RUB") {
		$scope.$watch("amount", $scope.fillInTransfer);
		$scope.$watch("address", $scope.fillInTransfer);
	} else {
		$scope.$watch("amount", $scope.fillOutTransfer);
		$scope.$watch("address", $scope.fillOutTransfer);
	}
});