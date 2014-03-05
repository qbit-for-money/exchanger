var resultModule = angular.module("wizard.result");

resultModule.controller("ResultController", function($rootScope, $scope, $timeout, $interval, ordersResource) {
	// START Test
//	$rootScope.orderInfo.userPublicKey = "948bd81b-77b4-4b97-84d6-039c0b733637";
//	$rootScope.orderInfo.creationDate = "2014-02-15T18:00:32.00";
	// END Test

	function actualizeOrder() {
		if ($rootScope.orderInfo.userPublicKey && $rootScope.orderInfo.creationDate) {
			var orderInfo = ordersResource.getByUserAndTimestamp({
				userPublicKey: $rootScope.orderInfo.userPublicKey,
				creationDate: $rootScope.orderInfo.creationDate
			}, function() {
				$rootScope.orderInfo = orderInfo;
				$scope.actualizationTimeout = $timeout(actualizeOrder, 10 * 1000);
			});
		} else {
			$scope.actualizationTimeout = $timeout(actualizeOrder, 10 * 1000);
		}
	}
	$scope.actualizationTimeout = $timeout(actualizeOrder, 1000);

	function updateInTransferStatus() {
		if (!$rootScope.orderInfo.status) {
			return;
		}
		switch ($rootScope.orderInfo.status) {
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
	$rootScope.$watch("orderInfo.status", updateInTransferStatus);

	function updateOutTransferStatus() {
		if (!$rootScope.orderInfo.status) {
			return;
		}
		switch ($rootScope.orderInfo.status) {
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
	$rootScope.$watch("orderInfo.status", updateOutTransferStatus);

	function updateRate() {
		if (!$rootScope.orderInfo.status) {
			return;
		}
		$scope.rate = {
			numerator: $rootScope.orderInfo.inTransfer.amount,
			denominator: $rootScope.orderInfo.outTransfer.amount
		};
	}
	updateRate();
	$rootScope.$watch("orderInfo.status", updateRate);

	$scope.secondsInWaiting = 0;
	function updateSecondsInWaiting() {
		if ($rootScope.orderInfo.status && (["INITIAL", "PAYED"].indexOf($rootScope.orderInfo.status) >= 0)) {
			$scope.secondsInWaiting++;
		} else {
			$scope.secondsInWaiting = 0;
		}
	}
	$scope.secondsInWaitingTimeout = $interval(updateSecondsInWaiting, 1000);

	$scope.$on("$destroy", function() {
		if ($scope.actualizationTimeout) {
			$timeout.cancel($scope.actualizationTimeout);
		}
		if ($scope.secondsInWaitingTimeout) {
			$interval.cancel($scope.secondsInWaitingTimeout);
		}
	});
});
