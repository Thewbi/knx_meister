import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { DeviceDto } from './devicedto';

@Injectable({
  providedIn: 'root'
})
export class DeviceServiceService {

  private devices: DeviceDto[];

  constructor(private http: HttpClient) {

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

  getDevices() {
    //return this.devices;
    return this.http.get<DeviceDto[]>('http://localhost:8189/knxmeister/api/devices/all');
  }

}
