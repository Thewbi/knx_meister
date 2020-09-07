import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { DeviceDto } from './devicedto';
import { CommunicationObjectDto } from './communicationobjectdto';

@Injectable({
  providedIn: 'root'
})
export class DeviceServiceService {

  private devices: DeviceDto[];

  constructor(private http: HttpClient) { }

  getDevices() {
    return this.http.get<DeviceDto[]>('http://localhost:8189/knxmeister/api/devices/all');
  }

  getCommunicationObjectsByDevicePhysicalAddress(physicalAddress: string) {
    console.log('Service - physicalAddress:' + physicalAddress);
    // return of(physicalAddress);
    return this.http.get<CommunicationObjectDto[]>('http://localhost:8189/knxmeister/api/devices/' + physicalAddress + '/communicationobjects');
  }

}
