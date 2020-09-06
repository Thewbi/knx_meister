import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DeviceComponent } from './devices/device/device.component';
import { AppComponent } from './app.component';

import { ModuleWithProviders } from '@angular/core';

// { path: '/', component: AppComponent },
//{ path: '', pathMatch: 'full', component: AppComponent },
//  { path: 'devices', component: DeviceComponent },
const routes: Routes = [
  { path: '', pathMatch: 'full', component: AppComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
