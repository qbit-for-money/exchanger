var moneyModule = angular.module("money");

moneyModule.controller("YandexController", function($scope, $location, $window, yandexResource) {
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
		// TODO
	};

	$scope.fillOutTransfer = function() {
		// TODO
	};
	
	$scope.$watch("amount", function() {console.log($scope.amount);});
});