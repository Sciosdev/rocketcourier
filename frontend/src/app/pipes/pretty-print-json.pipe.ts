import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'prettyPrintJson'
})
export class PrettyPrintJsonPipe implements PipeTransform {
  
  transform(val) {
    return JSON.stringify(val, undefined, 4)
      .replace(/ /g, '&nbsp;')
      .replace(/\n/g, '<br/>');
  }
}
