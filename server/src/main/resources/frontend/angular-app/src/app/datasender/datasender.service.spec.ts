import { TestBed } from '@angular/core/testing';

import { DatasenderService } from './datasender.service';

describe('DatasenderService', () => {

  beforeEach(() => TestBed.configureTestingModule({}));
  
  let service: DatasenderService;
  beforeEach(() => { service = new DatasenderService(); });



  it('should be created', () => {
    const service: DatasenderService = TestBed.get(DatasenderService);
    expect(service).toBeTruthy();
  });
});
