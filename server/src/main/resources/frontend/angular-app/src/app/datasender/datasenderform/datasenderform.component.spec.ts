import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatasenderformComponent } from './datasenderform.component';

describe('DatasenderformComponent', () => {
  let component: DatasenderformComponent;
  let fixture: ComponentFixture<DatasenderformComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatasenderformComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatasenderformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
