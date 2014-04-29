var authModule = angular.module("captcha-auth");

authModule.directive('numberRequired', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			element.bind("keydown", function(e) {
				if ([46, 8, 9, 27, 13, 110, 190].indexOf(e.keyCode) !== -1 ||
					(e.keyCode == 65 && e.ctrlKey === true) ||
					(e.keyCode >= 35 && e.keyCode <= 39)) {
					return;
				}
				if (this.value.length > 3) {
					e.preventDefault();
				}
				if (!(e.which == 8 || e.which == 44 || e.which == 45 || e.which == 46 || (e.which > 47 && e.which < 58))) {
					return false;
				}
			});
			/*element.on('keypress', function(e) {
				//alert('gh')
				//return e.preventDefault();
				if ([46, 8, 9, 27, 13, 110, 190].indexOf(e.keyCode) !== -1 ||
					(e.keyCode == 65 && e.ctrlKey === true) ||
					(e.keyCode >= 35 && e.keyCode <= 39)) {
					return;
				}
				if (this.value.length > 3) {
					e.preventDefault();
				}
				if (!(e.which == 8 || e.which == 44 || e.which == 45 || e.which == 46 || (e.which > 47 && e.which < 58))) {
					return false;
				}
				/*if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57))
				 && (e.keyCode < 96 || e.keyCode > 105)) {
				 e.preventDefault();
				 }*/

				/*if (!(e.which==8 || e.which==44 ||e.which==45 ||e.which==46 ||(e.which>47 && e.which<58))) {
				 return false;
				 }*/
				/*if (this.value.length > 3) {
				 e.preventDefault();
				 }*/


			/*});*/
		}
	};
});

