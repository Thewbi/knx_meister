import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DeviceComponent } from './device/device.component';
import { DevicesComponent } from './devices/devices.component';
import { CommunicationobjectComponent } from './communicationobject/communicationobject.component';
import { DatasenderModule } from '../datasender/datasender.module';
import { DatasenderformComponent } from '../datasender/datasenderform/datasenderform.component';

// You """""import modules""""" and """""declare components""""" !!!!!
@NgModule({
  declarations: [DeviceComponent, DevicesComponent, CommunicationobjectComponent, DeviceComponent],
  imports: [
    CommonModule,
    DatasenderModule
  ],
  exports: [
    DeviceComponent
  ]
})
export class DevicesModule { }
