import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DeviceComponent } from './device/device.component';

@NgModule({
  declarations: [DeviceComponent],
  imports: [
    CommonModule
  ],
  exports: [
    DeviceComponent
  ]
})
export class DevicesModule { }
