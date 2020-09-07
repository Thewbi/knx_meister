import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CommunicationobjectComponent } from './communicationobject.component';

describe('CommunicationobjectComponent', () => {
  let component: CommunicationobjectComponent;
  let fixture: ComponentFixture<CommunicationobjectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CommunicationobjectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CommunicationobjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
