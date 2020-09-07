import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatasenderformComponent } from '../datasender/datasenderform/datasenderform.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [DatasenderformComponent],
  imports: [
    CommonModule,
    FormsModule
  ],
  exports: [
    DatasenderformComponent
  ]
})
export class DatasenderModule { }
