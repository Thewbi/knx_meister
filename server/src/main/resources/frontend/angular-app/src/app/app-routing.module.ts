import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DevicesModule } from './devices/devices.module';
import { DeviceComponent } from './devices/device/device.component';
import { AppComponent } from './app.component';

import { ModuleWithProviders } from '@angular/core';

// { path: '/', component: AppComponent },
//{ path: '', pathMatch: 'full', component: AppComponent },
//  { path: 'devices', component: DeviceComponent },
const routes: Routes = [
  { path: '', pathMatch: 'full', component: DeviceComponent },
  { path: 'devices', pathMatch: 'full', component: DeviceComponent },
];

@NgModule({
  imports: [DevicesModule, RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
