import { Component, OnInit } from '@angular/core';
import { DeviceServiceService } from '../device-service.service';

import { DeviceDto } from '../devicedto';

@Component({
  selector: 'app-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.css']
})
export class DeviceComponent implements OnInit {

  devices: DeviceDto[];

  constructor(private deviceService: DeviceServiceService) { }

  ngOnInit() {
    this.deviceService.getDevices().subscribe((devices: DeviceDto[]) => {
      this.devices = devices;
    });
  }

}
