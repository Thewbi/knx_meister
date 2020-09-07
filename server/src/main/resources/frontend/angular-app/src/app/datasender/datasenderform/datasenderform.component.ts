import { Component, OnInit, Input } from '@angular/core';
import { DataSenderDto } from '../datasenderdto';
import { DatasenderService } from 'src/app/datasender/datasender.service';

@Component({
  selector: 'app-datasenderform',
  templateUrl: './datasenderform.component.html',
  styleUrls: ['./datasenderform.component.css']
})
export class DatasenderformComponent implements OnInit {

  @Input() physicalAddress: string;
  @Input() groupAddress: string;

  submitted: boolean;

  types = ['SAW', 'CONSTANT',
    'RANDOM'];

  type: string;

  model = new DataSenderDto(18, 'SAW', 100, 0, 17);

  constructor(private dataSenderService: DatasenderService) { }

  ngOnInit() {
  }

  get diagnostic() { return JSON.stringify(this.model); }

  onSubmit() {

    this.submitted = false;

    console.log('onSubmit physicalAddress: ' + this.physicalAddress + ' groupAddress: ' + this.groupAddress + ' ' + JSON.stringify(this.model));

    this.dataSenderService.createDataSender(this.physicalAddress, this.groupAddress, this.model).subscribe(
      res => { console.log('HTTP response', res); this.submitted = true; },
      err => { console.log('HTTP Error', err); this.submitted = false; },
      () => { console.log('complete'); this.submitted = true; }
    );

    console.log('submitted: ' + this.submitted);
  }

}
