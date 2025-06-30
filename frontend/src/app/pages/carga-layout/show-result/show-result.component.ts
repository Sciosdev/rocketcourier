import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { Router } from '@angular/router';
import { NbStepperComponent } from '@nebular/theme';

@Component({
  selector: 'app-show-result',
  templateUrl: './show-result.component.html',
  styleUrls: ['./show-result.component.scss']
})
export class ShowResultComponent implements OnInit, OnChanges {

  @Input() resultado: any;
  @Input() errores: any[];

  constructor(private stepper: NbStepperComponent, private router: Router) { }

  ngOnInit(): void {
  }
  
  finaliza(){
    this.stepper.reset();
 //   this.router.navigate(['/intranet/carga-layout']);
  }
  
  ngOnChanges(): void {
    if (this.resultado != null  && this.errores != null) {
      return;
    }
  }
}
