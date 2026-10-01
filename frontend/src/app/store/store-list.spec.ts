import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StoreList } from './store-list';

describe('Store directory', () => {
  let fixture: ComponentFixture<StoreList>;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [StoreList], providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(StoreList);
    fixture.detectChanges();
  });
  afterEach(() => http.verify());
  it('shows loading until the request completes, then displays stores', () => {
    expect(fixture.nativeElement.textContent).toContain('Loading stores');
    const request = http.expectOne('/api/v1/stores');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: '1', slug: 'aurora-centro', name: 'Aurora Centro' }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('h2').textContent).toBe('Aurora Centro');
    expect(fixture.nativeElement.textContent).not.toContain('Loading stores');
  });
  it('shows a useful empty state', () => {
    http.expectOne('/api/v1/stores').flush([]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No stores are available');
    expect(fixture.nativeElement.querySelector('article')).toBeNull();
  });
  it('shows an error and retries through loading to success', () => {
    http.expectOne('/api/v1/stores').flush({}, { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain("couldn't load");
    fixture.nativeElement.querySelector('button').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Loading stores');
    http.expectOne('/api/v1/stores').flush([{ id: '2', slug: 'jardins', name: 'Aurora Jardins' }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aurora Jardins');
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });
});
