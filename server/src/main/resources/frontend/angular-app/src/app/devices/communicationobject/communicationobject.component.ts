import { Component, OnInit } from '@angular/core';
import { Router, NavigationExtras } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { DeviceServiceService } from '../device-service.service';

import { DatasenderService } from 'src/app/datasender/datasender.service';
import { DataSenderDto } from 'src/app/datasender/datasenderdto';

@Component({
  selector: 'app-communicationobject',
  templateUrl: './communicationobject.component.html',
  styleUrls: ['./communicationobject.component.css']
})
export class CommunicationobjectComponent implements OnInit {

  physicalAddress: string;
  comObjectId: string;
  groupAddress: string;

  constructor(private router: Router, private route: ActivatedRoute,
    private deviceService: DeviceServiceService, private dataSenderService: DatasenderService) {

    this.route.queryParams.subscribe(params => {
      this.physicalAddress = params.physicalAddress;
      this.comObjectId = params.comObjectId;
      this.groupAddress = params.groupAddress;
    });

  }

  ngOnInit() { }

  addDataSender() {
    console.log('addDataSender ' + this.physicalAddress + ' ' + this.groupAddress);

    const dataSenderDto: DataSenderDto = {
      id: 0,
      dataGeneratorType: 'CONSTANT',
      upperBound: 100,
      lowerBound: 0,
      constant: 1,
    }

    this.dataSenderService.createDataSender(this.physicalAddress, this.groupAddress, dataSenderDto);
  }

}
