import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DevicesModule } from './devices/devices.module';
import { DeviceComponent } from './devices/device/device.component';
import { DevicesComponent } from './devices/devices/devices.component';
import { CommunicationobjectComponent } from './devices/communicationobject/communicationobject.component';
import { AppComponent } from './app.component';

import { ModuleWithProviders } from '@angular/core';
import { from } from 'rxjs';


const routes: Routes = [
  { path: '', pathMatch: 'full', component: DevicesComponent },
  { path: 'devices', pathMatch: 'full', component: DevicesComponent },
  { path: 'device', pathMatch: 'full', component: DeviceComponent },
  { path: 'comobject', pathMatch: 'full', component: CommunicationobjectComponent },
];

@NgModule({
  imports: [DevicesModule, RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
