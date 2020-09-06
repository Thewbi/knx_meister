"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
exports.__esModule = true;
exports.DeviceServiceService = void 0;
var core_1 = require("@angular/core");
var DeviceServiceService = /** @class */ (function () {
    function DeviceServiceService(http) {
        this.http = http;
        // this.http.get<DeviceDto[]>('http://localhost:8189/knxmeister/api/devices/all')
        //   .subscribe(
        //     res => {
        //       console.log('HTTP response', res);
        //       this.devices = res;
        //     },
        //     err => console.log('HTTP Error', err),
        //     () => console.log('complete')
        //   );
    }
    DeviceServiceService.prototype.getDevices = function () {
        //return this.devices;
        return this.http.get('http://localhost:8189/knxmeister/api/devices/all');
    };
    DeviceServiceService = __decorate([
        core_1.Injectable({
            providedIn: 'root'
        })
    ], DeviceServiceService);
    return DeviceServiceService;
}());
exports.DeviceServiceService = DeviceServiceService;
