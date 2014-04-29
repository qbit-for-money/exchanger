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
	/*angular.element(document).on('focus', '#pin', function(e) {
		angular.element("#pin").autoNumeric('init');

	});*/

	/*angular.element(document).on('keypress', '#pin', function(e) {
	 //alert('fgh')
	 return e.preventDefault();
	 });*/





	$scope.changePin = function() {
		if ($scope.model.pin && ($scope.model.pin.length === 4) && !isNaN($scope.model.pin) && $scope.model.pin.indexOf(".") === -1) {
			$scope.pinInvalid = false;
			$scope.updateImage();
			changeDialogPosition(false);
			$scope.model.encodedKey = "";
		} else {
			$scope.pinInvalid = true;
			changeDialogPosition(true);
		}
	};

	function changeDialogPosition(state) {
		if (state) {
			angular.element(".modal-dialog").css("padding-top", "13%");
		} else {
			angular.element(".modal-dialog").css("padding-top", "10%");
		}
	}

	$scope.changeEncodedKey = function() {
		if ($scope.model.encodedKey && ($scope.model.encodedKey !== "")) {
			var authRequest = {encodedKey: $scope.model.encodedKey, pin: $scope.model.pin, timestamp: timestamp};
			var authResponse = captchaAuthResource.auth({}, authRequest);
			authResponse.$promise.then(function() {
				$scope.encodedKeyInvalid = false;
				setTimeout(function() {
					location.reload();
				}, 1000);
			});
		} else {
			$scope.encodedKeyInvalid = true;
		}
	};

	$scope.updateImage = function() {
		$scope.src = window.context + "webapi/captcha-auth/image?pin=" + $scope.model.pin + "&timestamp=" + timestamp + "&rand=" + Math.round(Math.random() * 1000);
	};

	$scope.checked = function(state) {
		if (state) {
			$scope.pinInvalid = true;
			$scope.model.pin = "";
			changeDialogPosition(true);
		} 
	};
});