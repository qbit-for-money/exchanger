angular.module("common", ["ngResource"]);
angular.module("main-menu", ["ngRoute", "angular-carousel"]);
angular.module("order", ["ngResource"]);

angular.module("main", ["common", "ngRoute", "ui.bootstrap", "main-menu", "order"]);
