var yandexModule = angular.module("money.yandex");

yandexModule.controller("YandexController", function($scope, $location, $window, $rootScope,
		yandexResource, storeOrderInfoInSession) {
	$scope.authorized = false;
	var address = $location.search()["wallet"];
	if (address) {
		$scope.address = address;
		$scope.authorized = true;
	}

	$scope.redirect = function() {
		var urlResponse = yandexResource.getAuthorizeUrl();
		urlResponse.$promise.then(function() {
			storeOrderInfoInSession();
			$window.location.href = urlResponse.url;
		});
	};

	$scope.fillInTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		orderInfo.inTransfer.address = $scope.address;
	};
	$scope.fillOutTransfer = function() {
		var orderInfo = $rootScope.orderInfo;
		orderInfo.outTransfer.address = $scope.address;
	};

	if ($rootScope.orderInfo.inTransfer.currency === "YANDEX_RUB") {
		$scope.$watch("address", $scope.fillInTransfer);
	} else {
		$scope.$watch("address", $scope.fillOutTransfer);
	}
});