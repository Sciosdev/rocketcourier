import { Component, Input, OnChanges } from '@angular/core';

@Component({
  selector: 'app-shopify-result',
  templateUrl: './shopify-result.component.html',
  styleUrls: ['./shopify-result.component.scss']
})
export class ShopifyResultComponent implements OnChanges {
  @Input() resultado: any;
  @Input() errores: any[];

  ngOnChanges(): void {}
}
