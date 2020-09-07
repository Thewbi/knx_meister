import { Component, OnInit } from '@angular/core';
import { DeviceServiceService } from '../device-service.service';
import { ActivatedRoute } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Router, NavigationExtras } from '@angular/router';

import { DeviceDto } from '../devicedto';
import { CommunicationObjectDto } from '../communicationobjectdto';

@Component({
  selector: 'app-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.css']
})
export class DeviceComponent implements OnInit {

  physicalAddress: string;
  communicationObjectDto: CommunicationObjectDto[];

  constructor(private router: Router, private route: ActivatedRoute, private deviceService: DeviceServiceService) {

    this.route.queryParams.subscribe(params => {
      this.physicalAddress = params.physicalAddress;

      this.deviceService.getCommunicationObjectsByDevicePhysicalAddress(params.physicalAddress)
        .subscribe((communicationObjectDto: CommunicationObjectDto[]) => {
          this.communicationObjectDto = communicationObjectDto;
        });
    });

  }

  ngOnInit() { }

  navigate(pyhsicalAddressValue: string, comObjectIdValue: string, groupAddressValue: string) {
    const navigationExtras: NavigationExtras = {
      queryParams: {
        physicalAddress: pyhsicalAddressValue,
        comObjectId: comObjectIdValue,
        groupAddress: groupAddressValue,
      }
    };
    this.router.navigate(['/comobject'], navigationExtras);
  }

}
