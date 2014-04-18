var authModule = angular.module("auth");

authModule.controller("AuthDialogOpeningController", function($modal) {
	$modal.open({
		templateUrl: "resources/html/auth/dialog.html",
		backdrop: "static",
		keyboard: false,
		backdropClick: false,
		dialogFade: false,
		controller: "AuthDialogController"
	});
});

authModule.controller("AuthDialogController", function($scope, $rootScope, modalResource) {
	var timestamp = new Date().getTime();
	$scope.auth = "True";
	$scope.pinInvalid = true;
	$scope.model = {};
	$scope.model.pin = "";
	$scope.model.encodedKey = "";
	$scope.src = "";

	$scope.changePin = function() {
		if (($scope.model.pin.length === 4) && !isNaN($scope.model.pin)) {
			$scope.pinInvalid = false;
			$scope.updateImage();
		} else {
			$scope.pinInvalid = true;
		}
	};
	$scope.$watch("model.encodedKey", auth, true);

	function auth(encodedKey) {
		if (encodedKey !== "") {
			var authRequest = {encodedKey: encodedKey, pin: $scope.model.pin, timestamp: timestamp};
			var authResponse = modalResource.auth({}, authRequest);
			authResponse.$promise.then(function() {
				location.reload();
			});
		}
	}

	$scope.updateImage = function() {
		$scope.src = window.context + "webapi/captcha/image?pin=" + $scope.model.pin + "&timestamp=" + timestamp + "&rand=" + Math.round(Math.random() * 1000);
	};
});