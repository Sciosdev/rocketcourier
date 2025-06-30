import { Component } from '@angular/core';
import { Subject } from 'rxjs';
import { NbIconLibraries } from '@nebular/theme';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})


export class AppComponent {
  title = 'rocket-front-end';

  private destroyed$ = new Subject();

  constructor(private iconLibraries: NbIconLibraries) {
    this.iconLibraries.registerFontPack('font-awesome', { packClass:'fa',iconClassPrefix: 'fa' });
  }

ngOnInit() {
}

ngOnDestroy() {
    this.destroyed$.next();
}

}
