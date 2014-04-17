var authModule = angular.module("auth");

authModule.controller("ModalDialogOpeningController", function($modal) {
	$modal.open({
		templateUrl: "resources/html/auth/dialog.html",
		backdrop: "static",
		keyboard: false,
		backdropClick: false,
		dialogFade: false,
		placement: "bottom",
		controller: "ModalDialogController",
		windowClass: "child"
	});
});

authModule.controller("ModalDialogController", function($scope, $rootScope, modalResource, usersResource, userService) {
	var timestamp = new Date().getTime();
	$scope.auth = "True";
	$scope.pinInvalid = true;
	$scope.model = {};
	$scope.model.pin = "";
	$scope.model.encodedKey = "";
	var destroyPinWatch = $scope.$watch('model.pin', function(newValue, oldValue) {
		if ((newValue.length === 4) && !isNaN(newValue)) {
			$scope.pinInvalid = false;
			var img = new Image();
			img.src = window.context + "webapi/captcha/image?pin=" + newValue + "&timestamp=" + timestamp;
			img.onload = function() {
				angular.element("#captcha").attr("src", img.src);
			};
		}
	}, true);
	$scope.change = function() {
		$scope.pinInvalid = true;
	};
	var destroyPubKeyWatch = $scope.$watch("model.encodedKey", function(newValue, oldValue) {
		if (newValue !== "") {
			var authTransfer = {encodedKey: newValue, pin: $scope.model.pin, timestamp: timestamp};
			var authResponse = modalResource.auth({}, authTransfer);
			authResponse.$promise.then(function() {
				//$rootScope.user = usersResource.current({});
				/*var currentUser = usersResource.current({});
				currentUser.$promise.then(function() {
					if (currentUser.publicKey) {
						$rootScope.user = currentUser;
					}
				});*/

				location.reload();
			});
		}
	}, true);
	$scope.$on("$destroy", function() {
		destroyPinWatch();
		destroyPubKeyWatch();
	});
});