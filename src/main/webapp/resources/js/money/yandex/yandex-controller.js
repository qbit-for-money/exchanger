var moneyModule = angular.module("money");

moneyModule.controller("YandexController", function($scope, $location, yandexResource, localStorage) {
	$scope.authorized = false;
	// *** TEST ***
	var propNewWindowAuth = false;
	// ***
	var address;
	if (propNewWindowAuth) {
		address = localStorage.getItem("wallet");
		localStorage.setItem("wallet", null);
	} else {
		address = $location.search()["wallet"];
	}
	if (address) {
		$scope.address = address;
		$scope.authorized = true;
	}
});