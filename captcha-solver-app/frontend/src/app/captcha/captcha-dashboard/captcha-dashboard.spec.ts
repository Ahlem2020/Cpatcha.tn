import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaptchaDashboard } from './captcha-dashboard';

describe('CaptchaDashboard', () => {
  let component: CaptchaDashboard;
  let fixture: ComponentFixture<CaptchaDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaptchaDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaptchaDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
