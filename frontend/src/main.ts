import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { App } from './app/app';
import { StoreList } from './app/store/store-list';

bootstrapApplication(App, {
  providers: [provideHttpClient(), provideRouter([
    { path: 'stores', component: StoreList },
    { path: '', pathMatch: 'full', redirectTo: 'stores' },
    { path: '**', redirectTo: 'stores' }
  ])]
}).catch(console.error);
