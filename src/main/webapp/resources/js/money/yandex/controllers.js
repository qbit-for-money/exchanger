var yandexModule = angular.module("money.yandex");

yandexModule.controller("YandexController", function($rootScope, $scope, $location, $window,
		yandexResource, restoreOrderInfoFromSession, storeOrderInfoInSession) {
	$scope.authorized = false;
	var address = $location.search()["wallet"];
	if (address) {
		restoreOrderInfoFromSession();
		$rootScope.orderInfo.inTransfer.address = address;
		$scope.authorized = true;
	}

	$scope.redirect = function() {
		var urlResponse = yandexResource.getAuthorizeUrl();
		urlResponse.$promise.then(function() {
			storeOrderInfoInSession();
			$window.location.href = urlResponse.url;
		});
	};
});