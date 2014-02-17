var resultModule = angular.module("result");

resultModule.controller("ResultController", function($rootScope, $scope) {
	// START Test
	$rootScope.order = {
		"creationDate": "2014-02-15T14:47:39.42",
		"inTransfer": {
			"address": "1234567890",
			"amount": {"cents": 1, "centsInCoin": 100000000, "coins": 0},
			"currency": "BITCOIN",
			"type": "IN"
		},
		"outTransfer": {
			"address": "0987654321",
			"amount": {"cents": 0, "centsInCoin": 100, "coins": 1},
			"currency": "YANDEX_RUB",
			"type": "OUT"
		},
		"status": "ACTIVE",
		"userPublicKey": "948bd81b-77b4-4b97-84d6-039c0b733637"
	};
	// END Test
});

resultModule.directive("resultTransfer", function() {
	return {
		restrict: "E",
		scope: { transfer: "=" },
		templateUrl: "resources/html/wizard/result-transfer.html"
	};
});