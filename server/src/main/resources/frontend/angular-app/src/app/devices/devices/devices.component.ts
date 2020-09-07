import { Component, OnInit } from '@angular/core';
import { DeviceServiceService } from '../device-service.service';
import { Router, NavigationExtras } from '@angular/router';

import { DeviceDto } from '../devicedto';

@Component({
  selector: 'app-devices',
  templateUrl: './devices.component.html',
  styleUrls: ['./devices.component.css']
})
export class DevicesComponent implements OnInit {

  devices: DeviceDto[];

  constructor(private router: Router, private deviceService: DeviceServiceService) { }

  ngOnInit() {
    this.deviceService.getDevices().subscribe((devices: DeviceDto[]) => {
      this.devices = devices;
    });
  }

  navigate(pyhsicalAddressValue: string) {
    const navigationExtras: NavigationExtras = {
      queryParams: {
        physicalAddress: pyhsicalAddressValue
      }
    };
    this.router.navigate(['/device'], navigationExtras);
  }

}
