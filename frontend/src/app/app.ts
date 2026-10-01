import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterOutlet],
  template: `
    <a class="skip-link" href="#main">Skip to content</a>
    <header><a routerLink="/stores" class="brand">Aurora Bakery</a><span>Fictional bakery network</span></header>
    <main id="main"><router-outlet /></main>
    <footer>Portfolio project · Store directory · No purchases available</footer>
  `
})
export class App {}
