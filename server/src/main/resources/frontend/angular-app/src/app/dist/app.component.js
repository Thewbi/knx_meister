"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
exports.__esModule = true;
exports.AppComponent = void 0;
var core_1 = require("@angular/core");
var AppComponent = /** @class */ (function () {
    //devices: DeviceDto[];
    function AppComponent(http) {
        this.http = http;
        this.title = 'KNX Simulator';
    }
    AppComponent.prototype.ngOnInit = function () {
    };
    AppComponent.prototype.onClickMe = function () {
        console.log('click');
        //this.devices = this.deviceService.getDevices();
        // GET plain text https://github.com/angular/angular/issues/18672
        this.http.get('http://127.0.0.1:8189/knxmeister/api/system/version', { responseType: 'text' })
            .subscribe(function (res) { return console.log('HTTP response', res); }, function (err) { return console.log('HTTP Error', err); }, function () { return console.log('complete'); });
        //// get with URL params
        //let params = new HttpParams().set('logNamespace', 'logNamespace');
        //
        //this.http.get<string>('http://127.0.0.1:8182/bacnetz/sysinfo/version', {params: params})
        //.subscribe(
        //    res => console.log('HTTP response', res),
        //    err => console.log('HTTP Error', err),
        //    () => console.log('complete')
        //);
        //let urlSearchParams = new URLSearchParams();
        //urlSearchParams.append('uid', '101');
        //const httpOptions = {
        //    params: { uid: 101}
        //};
        // post with body
        // this.http.post('http://127.0.0.1:8182/bacnetz/device/toggle/', JSON.stringify({
        //   username: 'username',
        //   password: 'password',
        // })).subscribe(
        //     res => console.log('HTTP response', res),
        //     err => console.log('HTTP Error', err),
        //     () => console.log('complete')
        // );
        // post with body and URL params
        // this.http.post('http://127.0.0.1:8182/bacnetz/device/toggle', JSON.stringify({
        //   username: 'username',
        //   password: 'password',
        // }), {params: params}).subscribe(
        //     res => console.log('HTTP response', res),
        //     err => console.log('HTTP Error', err),
        //     () => console.log('complete')
        // );
        //// post with path parameter
        //const url = 'http://127.0.0.1:8182/bacnetz/device/toggle/' + 101;
        //this.http.post(url, {}).subscribe(
        //    res => console.log('HTTP response', res),
        //    err => console.log('HTTP Error', err),
        //    () => console.log('complete')
        //);
        // // post
        // const url = 'http://127.0.0.1:8182/bacnetz/device/toggle';
        // this.http.post(url, {}).subscribe(
        //     res => console.log('HTTP response', res),
        //     err => console.log('HTTP Error', err),
        //     () => console.log('complete')
        // );
    };
    AppComponent = __decorate([
        core_1.Component({
            selector: 'app-root',
            templateUrl: './app.component.html',
            styleUrls: ['./app.component.css']
        })
    ], AppComponent);
    return AppComponent;
}());
exports.AppComponent = AppComponent;
