var resultModule = angular.module("wizard.result");

resultModule.controller("ResultController", function($rootScope, $scope, $interval, orderService) {
	function actualizeOrder() {
		orderService.actualize();
	}
	$scope.actualizationTimerId = $interval(actualizeOrder, 10000);
	actualizeOrder();
	angular.element(document).ready(function() {
		setTimeout(function() {
			angular.element("#inAddress").select();
		}, 10);
	});

	function handleOrderLoaded() {
		var orderInfo = orderService.get();
		updateInTransferStatus(orderInfo);
		updateOutTransferStatus(orderInfo);
		updateRate(orderInfo);
	}
	function updateInTransferStatus(orderInfo) {
		if (!orderInfo || !orderInfo.status) {
			return;
		}
		switch (orderInfo.status) {
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
	function updateOutTransferStatus(orderInfo) {
		if (!orderInfo || !orderInfo.status) {
			return;
		}
		switch (orderInfo.status) {
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
	function updateRate(orderInfo) {
		if (!orderInfo || !orderInfo.status) {
			return;
		}
		$scope.rate = {
			numerator: orderInfo.inTransfer.amount,
			denominator: orderInfo.outTransfer.amount
		};
	}
	$scope.$on("order-loaded", handleOrderLoaded);

	$scope.secondsInWaiting = 0;
	function updateSecondsInWaiting() {
		var orderInfo = orderService.get();
		if (orderInfo && orderInfo.status) {
			if (["INITIAL", "PAYED"].indexOf(orderInfo.status) >= 0) {
				$scope.secondsInWaiting++;
			} else {
				// do nothing
			}
		} else {
			$scope.secondsInWaiting = 0;
		}
	}
	$scope.secondsInWaitingTimerId = $interval(updateSecondsInWaiting, 1000);

	$scope.$on("$destroy", function() {
		if ($scope.actualizationTimerId) {
			$interval.cancel($scope.actualizationTimerId);
		}
		if ($scope.secondsInWaitingTimerId) {
			$interval.cancel($scope.secondsInWaitingTimerId);
		}
	});
});
