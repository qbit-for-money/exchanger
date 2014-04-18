var authModule = angular.module("captcha-auth");

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

authModule.controller("AuthDialogController", function($scope, captchaAuthResource) {
	var timestamp = new Date().getTime();
	$scope.auth = "True";
	$scope.pinInvalid = true;
	$scope.encodedKeyInvalid = true;
	$scope.model = {};
	$scope.model.pin = "";
	$scope.model.encodedKey = "";
	$scope.src = "";

	$scope.changePin = function() {
		if ($scope.model.pin && ($scope.model.pin.length === 4) && !isNaN($scope.model.pin)) {
			$scope.pinInvalid = false;
			$scope.updateImage();
			angular.element(".modal-dialog").css( "padding-top", "10%" );
		} else {
			$scope.pinInvalid = true;
			angular.element(".modal-dialog").css( "padding-top", "13%" );
		}
	};
	
	$scope.changeEncodedKey = function() {
		if ($scope.model.encodedKey && ($scope.model.encodedKey !== "")) {
			var authRequest = {encodedKey: $scope.model.encodedKey, pin: $scope.model.pin, timestamp: timestamp};
			var authResponse = captchaAuthResource.auth({}, authRequest);
			authResponse.$promise.then(function() {
				$scope.encodedKeyInvalid = false;
				setTimeout( function() { 
					location.reload(); 
				} , 1000);
			});
		} else {
			$scope.encodedKeyInvalid = true;
		}
	}

	$scope.updateImage = function() {
		$scope.src = window.context + "webapi/captcha-auth/image?pin=" + $scope.model.pin + "&timestamp=" + timestamp + "&rand=" + Math.round(Math.random() * 1000);
	};
});