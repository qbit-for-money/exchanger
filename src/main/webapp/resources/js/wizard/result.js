var resultModule = angular.module("result");

resultModule.controller("ResultController", function($rootScope, $scope, $timeout) {
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
		"status": "PAYED",
		"userPublicKey": "948bd81b-77b4-4b97-84d6-039c0b733637"
	};
	var statuses = ["INITIAL", "PAYED", "SUCCESS", "IN_FAILED", "OUT_FAILED"];
	function setRandomStatus() {
			$rootScope.order.status = statuses[Math.floor(Math.random() * statuses.length)];
			$timeout(setRandomStatus, 1000);
		};
	$timeout(setRandomStatus, 1000);
	// END Test
	
	function updateInTransferStatus() {
		if (!$rootScope.order) {
			return;
		}
		switch ($rootScope.order.status) {
			case "INITIAL":
				$scope.inTransferStatus = "IN_PROGRESS";
				break;
			case "PAYED":
			case "SUCCESS":
			case "OUT_FAILED":
				$scope.inTransferStatus = "OK";
				break;
			case "IN_FAILED":
				$scope.inTransferStatus = "ERROR";
				break;
		}
	}
	
	updateInTransferStatus();
	$rootScope.$watch("order.status", updateInTransferStatus);
	
	function updateOutTransferStatus() {
		if (!$rootScope.order) {
			return;
		}
		switch ($rootScope.order.status) {
			case "INITIAL":
			case "PAYED":
			case "IN_FAILED":
				$scope.outTransferStatus = "IN_PROGRESS";
				break;
			case "SUCCESS":
				$scope.outTransferStatus = "OK";
				break;
			case "OUT_FAILED":
				$scope.outTransferStatus = "ERROR";
				break;
		}
	}
	
	updateOutTransferStatus();
	$rootScope.$watch("order.status", updateOutTransferStatus);
});

resultModule.directive("resultTransfer", function() {
	return {
		restrict: "E",
		scope: { transfer: "=", status: "=" },
		templateUrl: "resources/html/wizard/result-transfer.html"
	};
});