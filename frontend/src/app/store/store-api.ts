import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Store { id: string; slug: string; name: string; }

@Injectable({ providedIn: 'root' })
export class StoreApi {
  private readonly http = inject(HttpClient);
  list() { return this.http.get<Store[]>('/api/v1/stores'); }
}
