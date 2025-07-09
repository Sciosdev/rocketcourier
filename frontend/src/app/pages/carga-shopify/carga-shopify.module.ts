import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CargaShopifyComponent } from './carga-shopify.component';
import { ThemeModule } from '../../theme/theme.module';
import { MaterialModule } from '../../material/material.module';
import { ShopifyResultComponent } from './shopify-result/shopify-result.component';

@NgModule({
  declarations: [CargaShopifyComponent, ShopifyResultComponent],
  imports: [CommonModule, ThemeModule, MaterialModule]
})
export class CargaShopifyModule {}
