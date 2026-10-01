import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Store, StoreApi } from './store-api';

type State = { kind: 'loading' } | { kind: 'error' } | { kind: 'ready'; stores: Store[] };

@Component({
  selector: 'app-store-list',
  template: `
    <p class="eyebrow">Our neighborhood bakeries</p>
    <h1>Find our stores</h1>
    <p>Explore the fictional Aurora network. Our store directory is the first step.</p>
    <section aria-label="Store directory" [attr.aria-busy]="state().kind === 'loading'">
      @if (state().kind === 'loading') {
        <p role="status">Loading stores…</p>
      }
      @if (state().kind === 'error') {
        <div role="alert"><p>We couldn't load the stores. Please try again.</p><button type="button" (click)="load()">Try again</button></div>
      }
      @if (ready(); as result) {
        @if (result.stores.length === 0) {
          <p role="status">No stores are available yet. Please check back later.</p>
        } @else {
          <ul class="stores">
            @for (store of result.stores; track store.id) {
              <li><article><h2>{{ store.name }}</h2><p>{{ store.slug }}</p></article></li>
            }
          </ul>
        }
      }
    </section>
  `
})
export class StoreList {
  private readonly api = inject(StoreApi);
  private readonly destroyRef = inject(DestroyRef);
  readonly state = signal<State>({ kind: 'loading' });
  constructor() { this.load(); }
  ready() { const current = this.state(); return current.kind === 'ready' ? current : null; }
  load() {
    this.state.set({ kind: 'loading' });
    this.api.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: stores => this.state.set({ kind: 'ready', stores }),
      error: () => this.state.set({ kind: 'error' })
    });
  }
}
